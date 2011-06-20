/*
 * This file is part of JOP, the Java Optimized Processor
 *   see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2011, Stefan Hepp (stefan@stefant.org).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.jopdesign.jcopter.inline;

import com.jopdesign.common.AppInfo;
import com.jopdesign.common.MethodCode;
import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.code.CallGraph;
import com.jopdesign.common.code.CallString;
import com.jopdesign.common.code.ExecutionContext;
import com.jopdesign.common.code.InvokeSite;
import com.jopdesign.common.processormodel.ProcessorModel;
import com.jopdesign.common.type.TypeHelper;
import com.jopdesign.jcopter.JCopter;
import com.jopdesign.jcopter.analysis.AnalysisManager;
import com.jopdesign.jcopter.analysis.StacksizeAnalysis;
import com.jopdesign.jcopter.greedy.Candidate;
import com.jopdesign.jcopter.greedy.CodeOptimizer;
import org.apache.bcel.generic.ASTORE;
import org.apache.bcel.generic.ATHROW;
import org.apache.bcel.generic.BranchInstruction;
import org.apache.bcel.generic.DUP;
import org.apache.bcel.generic.GOTO;
import org.apache.bcel.generic.IFNONNULL;
import org.apache.bcel.generic.IINC;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.LocalVariableInstruction;
import org.apache.bcel.generic.ReturnInstruction;
import org.apache.bcel.generic.Select;
import org.apache.bcel.generic.TargetLostException;
import org.apache.bcel.generic.Type;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class InlineOptimizer implements CodeOptimizer {

    public static final Logger logger = Logger.getLogger(JCopter.LOG_INLINE+".InlineOptimizer");

    private final JCopter jcopter;
    private final InlineConfig config;
    private final AppInfo appInfo;
    private final ProcessorModel processorModel;
    private final InlineHelper helper;

    private final Map<InstructionHandle,CallString> callstrings;

    private int storeCycles;
    private int checkNPCycles;

    protected class InlineCandidate extends Candidate {

        // TODO having a context with a callstring != EMPTY is not yet fully supported (callgraph/analysis-updating!)
        private final ExecutionContext context;
        private final InvokeSite invokeSite;
        private final MethodInfo invokee;
        private final boolean needsNPCheck;
        private final boolean needsEmptyStack;

        private int maxLocals;

        private int deltaCodesize;
        private int deltaLocals;
        private long totalGain;
        private boolean isLastInvoke;

        protected InlineCandidate(InvokeSite invokeSite, MethodInfo invokee,
                                  boolean needsNPCheck, boolean needsEmptyStack, int maxLocals)
        {
            super(invokeSite.getInvoker(), invokeSite.getInstructionHandle(), invokeSite.getInstructionHandle());
            this.context = new ExecutionContext(invokeSite.getInvoker());
            this.invokeSite = invokeSite;
            this.invokee = invokee;
            this.needsNPCheck = needsNPCheck;
            this.needsEmptyStack = needsEmptyStack;
            this.maxLocals = maxLocals;
        }

        public InstructionHandle getInvokeInstruction() {
            return start;
        }

        @Override
        public boolean optimize(AnalysisManager analyses, StacksizeAnalysis stacksize) {

            // should we check again for stack and locals size? nah ..

            MethodCode code = getMethod().getCode();
            InstructionList il = code.getInstructionList();

            // To avoid problems with loosing targets we insert our code after the invoke and remove
            // the invoke afterwards, retargeting to the next instruction
            assert(start == end);
            InstructionHandle invoke = start;
            InstructionHandle next = end.getNext();

            // insert the prologue
            insertPrologue(il, next);

            // insert the invokee code
            Map<InvokeSite,InvokeSite> invokeMap;
            invokeMap = insertInvokee(analyses, il, next);

            // remove the invoke, retarget to next instruction, update start and end handlers
            start = invoke.getNext();
            end = next.getPrev();
            if (end == null) {
                // if we inline an empty static method with no arguments at the beginning of the code, end is null
                start = null;
            }

            try {
                il.delete(invoke);
            } catch (TargetLostException e) {
                code.retarget(e, start);
            }

            // finally, we need to update the analyses
            updateCallgraph(appInfo.getCallGraph(), invokeMap);
            updateAnalyses(analyses, invokeMap);

            return true;
        }

        private void insertPrologue(InstructionList il, InstructionHandle next)
        {
            int paramOffset = invokee.isStatic() ? maxLocals : maxLocals + 1;

            // store all parameters in the slots used for the inlined code, except the this-reference
            Type[] types = invokee.getArgumentTypes();
            paramOffset += TypeHelper.getNumSlots(types);

            for (int i = types.length - 1; i >= 0; i--) {
                paramOffset -= types[i].getSize();

                Instruction store = TypeHelper.createStoreInstruction(types[i], paramOffset);
                il.insert(next, store);
            }

            if (invokee.isStatic()) {
                return;
            }

            // store the this reference
            InstructionHandle store = il.insert(next, new ASTORE(maxLocals));

            // insert nullpointer check
            if (needsNPCheck) {
                // we popped all arguments, so there must be the this-ref left on the TOS before the store
                il.insert(store, new DUP());
                il.insert(store, new IFNONNULL(store));
                // throwing a null reference throws a nullpointer-exception, just what we want.
                // TODO this could insert a new invokesite (changing the cache,..), but we just ignore this for now!
                il.insert(store, new ATHROW());
            }

            // TODO saving the stack is currently not implemented..
            assert(!needsEmptyStack);
        }

        private Map<InvokeSite,InvokeSite> insertInvokee(AnalysisManager analyses, InstructionList il, InstructionHandle next) {

            MethodCode code = getMethod().getCode();
            String sourcefile = getMethod().getClassInfo().getSourceFileName();

            MethodCode invokeeCode = invokee.getCode();
            InstructionList iList = invokeeCode.getInstructionList(true, false);

            Map<InstructionHandle,InstructionHandle> instrMap = new HashMap<InstructionHandle, InstructionHandle>();
            Map<InvokeSite,InvokeSite> invokeMap = new HashMap<InvokeSite, InvokeSite>();

            // first copy all instruction handles
            for (InstructionHandle src = iList.getStart(); src != null; src = src.getNext()) {

                InstructionHandle ih = copyInstruction(invokeeCode, il, src, next);

                code.setSourceFileName(ih, sourcefile);

                if (code.isInvokeSite(ih)) {
                    InvokeSite newInvoke = code.getInvokeSite(ih);
                    InvokeSite oldInvoke = invokeeCode.getInvokeSite(src);

                    invokeMap.put(oldInvoke, newInvoke);

                    // TODO update inline-callstring for this instruction

                }

                instrMap.put(src, ih);
            }

            remapTargets(instrMap, next.getPrev(), iList.getEnd());

            return invokeMap;
        }

        private InstructionHandle copyInstruction(MethodCode invokeeCode, InstructionList il,
                                                  InstructionHandle src, InstructionHandle next)
        {
            InstructionHandle ih;
            Instruction instr = src.getInstruction();
            Instruction c = instr.copy();

            if (instr instanceof LocalVariableInstruction) {
                // remap local variables
                int slot = maxLocals + ((LocalVariableInstruction)instr).getIndex();
                ((LocalVariableInstruction)c).setIndex(slot);
            } else if (instr instanceof ReturnInstruction) {
                // replace return with goto
                c = new GOTO(next);
            }

            if (c instanceof BranchInstruction) {
                ih = il.insert(next, (BranchInstruction) c);
            } else {
                ih = il.insert(next, c);
            }

            invokeeCode.copyCustomValues(ih, src);

            return ih;
        }

        private void remapTargets(Map<InstructionHandle,InstructionHandle> instrMap,
                                  InstructionHandle last, InstructionHandle oldLast)
        {
            InstructionHandle src = oldLast;
            InstructionHandle ih = last;
            while (src != null) {
                Instruction i = src.getInstruction();
                Instruction c = ih.getInstruction();

                // Note that this skips over the goto instructions we inserted instead of returns, this is intentional
                if (i instanceof BranchInstruction) {
                    BranchInstruction bi = (BranchInstruction) i;
                    BranchInstruction bc = (BranchInstruction) c;
                    InstructionHandle target = bi.getTarget(); // old target

                    // New target is in hash map
                    if (bi instanceof Select) {
                        // Either LOOKUPSWITCH or TABLESWITCH
                        InstructionHandle[] targets = ((Select) bi).getTargets();
                        for (int j = 0; j < targets.length; j++) {
                            ((Select)bc).setTarget(j, instrMap.get(targets[j]));
                        }
                    }
                    bc.setTarget(instrMap.get(target));
                }

                src = src.getPrev();
                ih = ih.getPrev();
            }
        }

        private void updateCallgraph(CallGraph cg, Map<InvokeSite,InvokeSite> invokeMap) {

            // first we need to copy the execution contexts with new callstrings
            for (ExecutionContext invoker : cg.getNodes(getMethod())) {

                for (ExecutionContext ecInvokee : cg.getInvokedNodes(invoker, invokee)) {
                    CallString cs = updateCallString(invokeMap, ecInvokee.getCallString());

                    ExecutionContext newInvokee = cg.copyNodeRecursive(ecInvokee, cs, appInfo.getCallstringLength());

                    // Note that this may introduce a decreasing callstring length, since we removed an invokesite
                    cg.addEdge(invoker, newInvokee);
                }
            }

            // Now we remove the inlined edge(s) and contexts from the graph
            if (!cg.removeNodes(invokeSite, invokee, true)) {
                // we did not remove an exec context, this happens when callstring length is 0
                // only way to handle this is to check all invokesites of this method, see if the invokee is still in
                // the set of invokees.
                if (!searchInvokeSites()) {
                    cg.removeEdges(getMethod(), invokee, true);
                }
            }

            // Note that we do not need to check if any other method has been removed, because we added edges
            // to all targets of all invokesites of the removed method.
            isLastInvoke = cg.hasMethod(invokee);
        }

        private CallString updateCallString(Map<InvokeSite, InvokeSite> invokeMap, CallString callString) {
            List<InvokeSite> cs = new ArrayList<InvokeSite>(callString.length());

            for (InvokeSite is : callString) {
                if (is.equals(invokeSite)) continue;
                InvokeSite mapped = invokeMap.get(is);
                cs.add( mapped == null ? is : mapped );
            }

            return new CallString(cs);
        }

        private void updateAnalyses(AnalysisManager analyses, Map<InvokeSite,InvokeSite> invokeMap) {

            // TODO update execution frequencies of invokee

            // TODO update cache analysis

        }

        @Override
        public boolean recalculate(AnalysisManager analyses, StacksizeAnalysis stacksize) {

            // maxLocals: may have changed if other optimizations introduced new locals outside their range
            // TODO if other optimizations add locals which are live in our region, we need to increase maxLocals
            //      this is currently not supported by the GreedyOptimizer (either need to keep track of locals in
            //      GreedyOptimizer or extend StacksizeAnalysis to keep track of live locals per IH too)

            // deltaLocals: maxLocals may change in invokee, so we need to update
            deltaLocals = invokee.getCode().getMaxLocals();

            // check if max locals and stacksize still checks out (codesize is checked by the CandidateSelector)
            if (!checkStackAndLocals(stacksize)) {
                return false;
            }

            // deltaCodesize: codesize of invokee may have changed
            deltaCodesize = calcDeltaCodesize();

            // isLastInvoke: may have changed due to previous inlining
            isLastInvoke = checkIsLastInvoke();

            // localGain: could have changed due to codesize changes, or cache-miss-count changes
            totalGain = calcTotalGain(analyses);

            return true;
        }

        @Override
        public int getDeltaLocalCodesize() {
            return deltaCodesize;
        }

        @Override
        public Collection<MethodInfo> getUnreachableMethods() {
            return isLastInvoke ? Collections.singleton(invokee) : null;
        }

        @Override
        public int getMaxLocalsInRegion() {
            return maxLocals + deltaLocals;
        }

        @Override
        public long getTotalGain() {
            return totalGain;
        }

        @Override
        public Collection<CallString> getRequiredContext() {
            return context.getCallString().length() > 0 ? Collections.singleton(context.getCallString()) : null;
        }

        private boolean checkStackAndLocals(StacksizeAnalysis stacksize) {

            if (processorModel.getMaxLocals() < maxLocals + deltaLocals) {
                return false;
            }

            int stack = stacksize.getStacksizeBefore(getInvokeInstruction());
            stack -= TypeHelper.getNumInvokeSlots(invokee);
            stack += invokee.getCode().getMaxStack();

            if (processorModel.getMaxStackSize() < stack) {
                return false;
            }

            return true;
        }

        private int calcDeltaCodesize() {
            int delta = 0;

            // we remove the invokesite
            delta -= processorModel.getNumberOfBytes(getMethod(), getInvokeInstruction().getInstruction());

            // .. add a prologue ..
            if (needsNPCheck) {
                // DUP IFNONNULL ATHROW
                delta += 5;
            }
            if (!invokee.isStatic()) {
                // ASTORE this
                delta += maxLocals > 255 ? 4 : (maxLocals > 3 ? 2 : 1);

            }
            // xSTORE parameters: over-approximate by assuming 2/4 bytes per store
            if (invokee.getArgumentTypes().length + maxLocals >= 255) {
                delta += invokee.getArgumentTypes().length * 4;
            } else {
                delta += invokee.getArgumentTypes().length * 2;
            }

            // TODO if we need to save the stack, we need to account for this as well

            // .. and finally we inline the code, but with some modifications
            InstructionHandle ih = invokee.getCode().getInstructionList(true, false).getStart();
            while (ih != null) {
                Instruction instr = ih.getInstruction();

                if (instr instanceof ReturnInstruction) {
                    // we replace this with goto, and since method-size is limited to 16bit, we do not need the wide version
                    delta += 3;
                } else if (instr instanceof LocalVariableInstruction) {
                    // we map the local vars to higher indices, might increase code size
                    int idx = ((LocalVariableInstruction)instr).getIndex() + maxLocals;

                    if (instr instanceof IINC) {
                        delta += idx > 255 ? 6 : 3;
                    } else {
                        delta += maxLocals > 255 ? 4 : (maxLocals > 3 ? 2 : 1);
                    }
                } else {
                    delta += processorModel.getNumberOfBytes(invokee, instr);
                }

                ih = ih.getNext();
            }

            return delta;
        }

        private boolean checkIsLastInvoke() {
            CallGraph cg = appInfo.getCallGraph();

            for (ExecutionContext node :  cg.getNodes(invokee)) {
                if (node.getCallString().isEmpty()) {
                    // This is a problem, we can only find out if we check all invokes in this method.
                    if (searchInvokeSites()) {
                        return false;
                    }
                } else {
                    if (!node.getCallString().top().equals(invokeSite)) {
                        return false;
                    }
                }
            }

            return true;
        }

        private boolean searchInvokeSites() {
            for (InvokeSite site : getMethod().getCode().getInvokeSites()) {
                if (invokeSite.equals(site)) continue;
                // this checks the same callgraph we want to prune, but there is actually no difference to
                // checking the type hierarchy, since this returns only methods which override the invoked method.
                Set<MethodInfo> methods = appInfo.findImplementations(site);
                if (methods.isEmpty() || methods.contains(invokee)) {
                    return true;
                }
            }
            return false;
        }

        private long calcTotalGain(AnalysisManager analyses) {

            // gain without cache costs for single invoke..
            long gain = jcopter.getWCETProcessorModel().getExecutionTime(context, invokeSite.getInstructionHandle());

            // we loose some gain due to the prologue
            gain -= invokee.getArgumentTypes().length * storeCycles;
            if ( !invokee.isStatic() ) gain -= storeCycles;
            if (needsNPCheck) gain -= checkNPCycles;

            // TODO we may also loose/gain some speed because we replaced returns with gotos and changed local-var slots

            // .. gain for all executions
            gain *= analyses.getExecCountAnalysis().getExecCount(invokeSite);

            // We also gain something due to removed cache-misses (we do not consider gain-losses due to increased
            // invoker-codesize here)
            gain += analyses.getMethodCacheAnalysis().getTotalInvokeReturnMissCosts(invokeSite);

            return gain;
        }
    }


    public InlineOptimizer(JCopter jcopter, InlineConfig config) {
        this.jcopter = jcopter;
        this.config = config;
        this.appInfo = AppInfo.getSingleton();
        this.processorModel = appInfo.getProcessorModel();

        this.helper = new InlineHelper(jcopter, config);
        this.callstrings = new HashMap<InstructionHandle, CallString>();
    }

    @Override
    public void initialize(AnalysisManager analyses, Collection<MethodInfo> roots) {

        storeCycles = 0;
        checkNPCycles = 0;

    }

    @Override
    public Collection<Candidate> findCandidates(MethodInfo method, AnalysisManager analyses,
                                                StacksizeAnalysis stacksize, int maxLocals)
    {
        InstructionList il = method.getCode().getInstructionList(true, false);
        return findCandidates(method, analyses, stacksize, maxLocals, il.getStart(), il.getEnd());
    }

    @Override
    public Collection<Candidate> findCandidates(MethodInfo method, AnalysisManager analyses, StacksizeAnalysis stacksize,
                                                int maxLocals, InstructionHandle start, InstructionHandle end)
    {
        List<Candidate> candidates = new LinkedList<Candidate>();

        MethodCode code = method.getCode();

        InstructionHandle ih = start;
        while (ih != null) {

            if (code.isInvokeSite(ih)) {
                InvokeSite site = code.getInvokeSite(ih);
                // since we update the appInfo callgraph, the callstring only contains the invokesite and no
                // inlined methods
                CallString cs = new CallString(site);

                MethodInfo invokee = helper.devirtualize(cs);

                Candidate candidate = checkInvoke(code, cs, invokee);
                if (candidate != null) {
                    candidates.add(candidate);
                }
            }

            if (ih == end) break;
            ih = ih.getNext();
        }

        return candidates;
    }

    @Override
    public void printStatistics() {
    }

    private Candidate checkInvoke(MethodCode code, CallString cs, MethodInfo invokee) {


        return null;
    }

    /**
     * Get the callstring starting at the method to optimize to the invokesite to inline, containing all
     * original invokesites in the unoptimized code (if the invokesite to inline has been inlined before).
     * This is required to lookup results in analyses for which callstrings are not updated by the inliner (e.g. DFA).
     * Note that the AppInfo callgraph is updated, so this callstring must NOT be used for lookups there.
     *
     * @param code the code of the method to optimize.
     * @param ih the invokesite to inline
     * @return the callstring of all methods which have been inlined into the method leading to the invokesite.
     */
    private CallString getInlineCallString(MethodCode code, InstructionHandle ih) {
        CallString cs = callstrings.get(ih);
        return cs == null ? CallString.EMPTY : cs;
    }

    private CallString setInlineCallString(MethodCode code, InstructionHandle ih, CallString cs) {
        // TODO we might want to use InstructionHandle CustomKeys, we want those callstrings to be copied if code is copied.
        //      and if handles are reused, we need to make sure that the values are removed from the map.
        CallString old = callstrings.put(ih, cs);
        return old == null ? CallString.EMPTY : old;
    }

}
