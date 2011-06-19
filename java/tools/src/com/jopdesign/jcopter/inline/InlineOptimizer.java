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
import com.jopdesign.common.code.CallString;
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
import org.apache.bcel.generic.DUP;
import org.apache.bcel.generic.IFNONNULL;
import org.apache.bcel.generic.IINC;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.LocalVariableInstruction;
import org.apache.bcel.generic.ReturnInstruction;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class InlineOptimizer implements CodeOptimizer {

    public static final Logger logger = Logger.getLogger(JCopter.LOG_INLINE+".InlineOptimizer");

    private final InlineConfig config;
    private final AppInfo appInfo;
    private final ProcessorModel processorModel;
    private final InlineHelper helper;

    private final Map<InstructionHandle,CallString> callstrings;

    protected class InlineCandidate extends Candidate {

        private final MethodInfo invokee;
        private final boolean needsNPCheck;
        private final boolean needsEmptyStack;

        private int maxLocals;

        private int deltaCodesize;
        private int deltaLocals;
        private int localGain;
        private boolean isLastInvoke;

        protected InlineCandidate(InvokeSite invokeSite, MethodInfo invokee,
                                  boolean needsNPCheck, boolean needsEmptyStack, int maxLocals)
        {
            super(invokeSite.getInvoker(), invokeSite.getInstructionHandle(), invokeSite.getInstructionHandle());
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
            InstructionHandle invoke = start;
            InstructionHandle after = end.getNext();

            // insert the prologue
            InstructionHandle last = insertPrologue(il, invoke);

            // insert the

            return true;
        }

        private InstructionHandle insertPrologue(InstructionList il, InstructionHandle after)
        {
            int paramOffset = invokee.isStatic() ? maxLocals : maxLocals + 1;
            InstructionHandle last = after;

            // store all parameters in the slots used for the inlined code, except the this-reference


            if (invokee.isStatic()) {
                return last;
            }

            // store the this reference
            InstructionHandle store = il.append(last, new ASTORE(maxLocals));

            // insert nullpointer check
            if (needsNPCheck) {
                // we popped all arguments, so there must be the this-ref left on the TOS before the store
                il.insert(store, new DUP());
                il.insert(store, new IFNONNULL(store));
                il.append(store, new ATHROW());
            }

            // TODO saving the stack is currently not implemented..
            assert(!needsEmptyStack);

            return store;
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
            localGain = calcLocalGain(analyses);

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
        public int getLocalGain() {
            return localGain;
        }

        @Override
        public Collection<CallString> getRequiredContext() {
            // TODO we could support inlining only for certain contexts..
            return null;
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

            return false;
        }

        private int calcLocalGain(AnalysisManager analyses) {

            return 0;
        }
    }


    public InlineOptimizer(JCopter jcopter, InlineConfig config) {
        this.config = config;
        this.appInfo = AppInfo.getSingleton();
        this.processorModel = appInfo.getProcessorModel();

        this.helper = new InlineHelper(jcopter, config);
        this.callstrings = new HashMap<InstructionHandle, CallString>();
    }

    @Override
    public void initialize(Collection<MethodInfo> roots) {

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
