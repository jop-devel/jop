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
import com.jopdesign.common.type.StackHelper;
import com.jopdesign.common.type.TypeHelper;
import com.jopdesign.common.type.ValueInfo;
import com.jopdesign.jcopter.JCopter;
import com.jopdesign.jcopter.analysis.ValueMapAnalysis;
import com.jopdesign.jcopter.optimizer.AbstractOptimizer;
import org.apache.bcel.generic.ARRAYLENGTH;
import org.apache.bcel.generic.ArithmeticInstruction;
import org.apache.bcel.generic.CHECKCAST;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.ConversionInstruction;
import org.apache.bcel.generic.DUP;
import org.apache.bcel.generic.DUP2;
import org.apache.bcel.generic.FieldInstruction;
import org.apache.bcel.generic.GETFIELD;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionConstants;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.LDC;
import org.apache.bcel.generic.LDC2_W;
import org.apache.bcel.generic.NOP;
import org.apache.bcel.generic.POP;
import org.apache.bcel.generic.POP2;
import org.apache.bcel.generic.PUTFIELD;
import org.apache.bcel.generic.PushInstruction;
import org.apache.bcel.generic.RETURN;
import org.apache.bcel.generic.ReturnInstruction;
import org.apache.bcel.generic.StackInstruction;
import org.apache.bcel.generic.Type;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class SimpleInliner extends AbstractOptimizer {

    private static class InlineData {
        private List<ValueInfo> params;
        private InstructionList prologue;
        private InstructionList epilogue;
        private int inlineStart;
        private int oldPrologueLength;
        private InvokeSite invokeSite;

        private InlineData() {
            params = new ArrayList<ValueInfo>(4);
            prologue = new InstructionList();
            epilogue = new InstructionList();
        }

        public void setInlineStart(int inlineStart) {
            this.inlineStart = inlineStart;
        }

        /**
         * @param oldPrologueLength number of instructions before the invokesite to replace with the prologue
         */
        public void setOldPrologueLength(int oldPrologueLength) {
            this.oldPrologueLength = oldPrologueLength;
        }

        public void addPrologue(Instruction instruction) {
            prologue.append(instruction);
        }

        public void addEpilogue(Instruction instruction) {
            epilogue.append(instruction);
        }

        public void addParam(ValueInfo param) {
            this.params.add(param);
        }

        public void setInvokeSite(InvokeSite invokeSite) {
            this.invokeSite = invokeSite;
        }

        public List<ValueInfo> getParams() {
            return params;
        }

        public InvokeSite getInvokeSite() {
            return invokeSite;
        }

        public InstructionList getPrologue() {
            return prologue;
        }

        public InstructionList getEpilogue() {
            return epilogue;
        }

        public int getOldPrologueLength() {
            return oldPrologueLength;
        }

        public int getInlineStart() {
            return inlineStart;
        }

        public void reset() {
            params.clear();
            inlineStart = 0;
            prologue.dispose();
            epilogue.dispose();
            oldPrologueLength = 0;
            invokeSite = null;
        }
    }

    private static final Logger logger = Logger.getLogger(JCopter.LOG_INLINE+".SimpleInliner");

    private final InlineHelper helper;

    private int inlineCounter;
    private int candidates;
    private int unhandledInstructions;
    private int requiresNPCheck;
    private int signatureMismatch;
    private int codesizeTooLarge;
    private int countInvokeSites;
    private int countDevirtualized;

    public SimpleInliner(JCopter jcopter, InlineConfig inlineConfig) {
        super(jcopter, true);
        helper = new InlineHelper(jcopter, inlineConfig);
    }

    @Override
    public void initialize() {
        inlineCounter = 0;
        candidates = 0;
        unhandledInstructions = 0;
        requiresNPCheck = 0;
        signatureMismatch = 0;
        codesizeTooLarge = 0;
        countInvokeSites = 0;
        countDevirtualized = 0;
    }

    @Override
    public void optimizeMethod(MethodInfo method) {
        InlineData inlineData = new InlineData();

        List<InvokeSite> invokes = new ArrayList<InvokeSite>( method.getCode().getInvokeSites() );
        // we iterate over the invoke sites in a sorted order for the single reason that the DFA cache hack works
        // (else the order of the entries in the constantpool can differ)
        Collections.sort(invokes, new Comparator<InvokeSite>() {
            @Override
            public int compare(InvokeSite o1, InvokeSite o2) {
                return o1.getInstructionHandle().getPosition() - o2.getInstructionHandle().getPosition();
            }
        });
        for (InvokeSite invoke : invokes) {

            // The callstring contains 'original' invokesites from the unmodified callgraph,
            // 'invoke' refers to the new invokesite in the modified code
            CallString cs = new CallString(invoke);

            while (invoke != null) {
                countInvokeSites++;

                MethodInfo invokee = helper.devirtualize(cs);
                if (invokee == null) break;

                countDevirtualized++;

                // Preliminary checks
                if (checkInvoke(invoke, cs, invokee, inlineData)) {

                    invoke = performSimpleInline(invoke, invokee, inlineData);

                    inlineCounter++;

                    if (inlineData.getInvokeSite() != null) {
                        cs.push(inlineData.getInvokeSite());
                    } else {
                        break;
                    }

                } else {
                    break;
                }
            }
        }

        // update callgraph (?) If we update the callgraph, the callstrings become invalid!
        // -> update callgraph only after we finished inlining of a toplevel invokesite;
        //    collect all invokesites to collapse into toplevel invokesite;
        //    replace old invokesite with invokesites from inlined code, add edges to not inlined methods

        // We could collect all nodes to merge and call callgraph.merge(), but this is not yet implemented.
        // Instead we simply rebuild the whole callgraph and all results which use callstrings.

    }

    @Override
    public void printStatistics() {
        logger.info("Inlined "+inlineCounter+" invoke sites.");
        logger.info("Found invoke sites: "+countInvokeSites+
                    ", not devirtualized: "+(countInvokeSites-countDevirtualized));
        logger.info("Candidates: "+candidates+"; need NP check: "+ requiresNPCheck +", uncorrectable signature mismatch: "+
                signatureMismatch +", unhandled instruction: "+unhandledInstructions+", codesize: "+codesizeTooLarge);
    }

    private boolean checkInvoke(InvokeSite invokeSite, CallString cs, MethodInfo invokee, InlineData inlineData) {

        // could be a native method, or it has not been devirtualized
        if (invokee == null || !invokee.hasCode()) {
            return false;
        }

        if (invokee.getCode().getExceptionHandlers().length > 0) {
            // We do not support inlining code with exception handles (as this code would be too large anyway..)
            return false;
        }

        // ignore methods which are most certainly too large (allow for param loading, invoke and return,
        // and some slack to allow for unused params)
        int estimate = invokee.getArgumentTypes().length * 2 + 10;
        if (invokee.getCode().getNumberOfBytes(false) > estimate) {
            return false;
        }

        // initial checks
        if (!helper.canInline(cs, invokeSite, invokee)) {
            return false;
        }

        // TODO we should check if the stack is empty and if so inline anyway?
        if (helper.needsEmptyStack(invokeSite, invokee)) {
            return false;
        }

        // If we pass all tests so far, we count this as a candidate..
        candidates++;

        // check the invokee, the invoke site and the new code size and store the results into inlineData
        inlineData.reset();

        // We do not check stacksize and maxLocals here, because we do not inline store instructions and we simply
        // assume that for simple invokees stacksize is never a problem ..

        if (!analyzeInvokee(cs, invokee, inlineData)) {
            return false;
        }

        if (!analyzeInvokeSite(invokeSite, invokee, inlineData)) {
            signatureMismatch++;
            if (logger.isTraceEnabled()) {
                logger.trace("Not inlining "+invokee+" at "+invokeSite+" because of signature mismatch.");
            }
            return false;
        }

        if (!analyzeCodeSize(invokeSite, invokee, inlineData)) {
            codesizeTooLarge++;
            if (logger.isTraceEnabled()) {
                logger.trace("Not inlining "+invokee+" at "+invokeSite+" because of codesize.");
            }
            return false;
        }

        return true;
    }

    /**
     * @param cs the callstring from the invoker to the invoke to inline (if recursive). Used to check DFA results.
     * @param invokee the invoked method to analyze
     * @param inlineData the map to populate with the parameters and the instructions to inline.
     * @return true if inlining is possible
     */
    private boolean analyzeInvokee(CallString cs, MethodInfo invokee, InlineData inlineData) {

        // we allow loading of parameters, loading of constants, some instruction, and a return
        ValueMapAnalysis values = new ValueMapAnalysis(invokee);
        values.loadParameters();

        InstructionList il = invokee.getCode().getInstructionList(true, false);
        InstructionHandle ih = il.getStart();

        // we should at least have a return instruction, so even for empty methods we should fall through

        // generate the parameter mapping
        int count = 0;
        while (true) {
            Instruction instruction = ih.getInstruction();

            if (instruction instanceof PushInstruction || instruction instanceof NOP) {
                values.transfer(instruction);
                ih = ih.getNext();
                count++;
            } else {
                break;
            }
        }

        // store the mapping
        for (ValueInfo value : values.getValueTable().getStack()) {
            inlineData.addParam(value);
        }

        inlineData.setInlineStart(count);

        // if we do not need an NP check, we can also inline code which does not throw an exception in the same way
        boolean needsNPCheck = helper.needsNullpointerCheck(cs, invokee, false);
        boolean hasNPCheck = false;

        // we allow up to 5 instructions and one return before assuming that the resulting code will be too large
        for (int i = 0; i < 6; i++) {
            // now lets see what we have here as non-push instructions
            Instruction instruction = ih.getInstruction();

            if (instruction instanceof InvokeInstruction) {
                if (inlineData.getInvokeSite() != null) {
                    // only inline at most one invoke
                    return false;
                }
                InvokeSite is = invokee.getCode().getInvokeSite(ih);
                inlineData.setInvokeSite(is);

                hasNPCheck |= !is.isInvokeStatic();
            }
            else if (instruction instanceof FieldInstruction) {
                if (instruction instanceof GETFIELD) {
                    hasNPCheck |= values.getValueTable().top().isThisReference();
                }
                if (instruction instanceof PUTFIELD) {
                    int down = values.getValueTable().top().isContinued() ? 2 : 1;
                    hasNPCheck |= values.getValueTable().top(down).isThisReference();
                }
            }
            else if (instruction instanceof ArithmeticInstruction ||
                     instruction instanceof ConversionInstruction ||
                     instruction instanceof StackInstruction ||
                     instruction instanceof LDC || instruction instanceof LDC2_W ||
                     instruction instanceof ARRAYLENGTH ||
                     instruction instanceof CHECKCAST ||
                     instruction instanceof NOP)
            {
                // nothing to do, just copy them
            }
            else if (instruction instanceof ReturnInstruction) {
                if (needsNPCheck && !hasNPCheck) {
                    // We were nearly finished.. but NP check test failed
                    this.requiresNPCheck++;
                    if (logger.isTraceEnabled()) {
                        logger.trace("Not inlining "+invokee+" because it requires a NP check.");
                    }
                    return false;
                }

                // we must have a return instruction now.. Check if we return the only value on the stack,
                // else we need to add pop instructions
                if (instruction instanceof RETURN) {

                    // we do not return anything, so we must empty the stack
                    while (values.getValueTable().getStackSize() > 0) {
                        Instruction pop;
                        if (values.getValueTable().top().isContinued()) {
                            pop = new POP2();
                        } else {
                            pop = new POP();
                        }
                        inlineData.addEpilogue(pop);
                        values.transfer(pop);
                    }

                    return true;
                } else {
                    Type type = ((ReturnInstruction) instruction).getType();

                    // If we return a value, we only inline if the stack contains only the return value,
                    // else we would need to move the return value down to the first stack slot and pop the rest
                    // which would most likely produce too much code (and such a code is not generated by
                    // javac anyway)
                    return values.getValueTable().getStackSize() == type.getSize();
                }

            }
            else {
                // if we encounter an instruction which we do not handle, we do not inline
                unhandledInstructions++;
                if (logger.isTraceEnabled()) {
                    logger.trace("Not inlining "+invokee+" because of unhandled instruction "+
                            instruction.toString(invokee.getClassInfo().getConstantPoolGen().getConstantPool()));
                }
                return false;
            }

            // update the stack map since we need it to handle RETURN
            values.transfer(instruction);

            ih = ih.getNext();
        }

        // too many instructions, do not inline
        return false;
    }

    /**
     * Check if the invokesite can be modified in a way so that the parameters are passed in the correct order
     * @param invokeSite the invokesite to inline.
     * @param invokee the invoked method.
     * @param inlineData the map to store the analyzer results
     * @return true if the prologue can be changed to match the expected behaviour
     */
    private boolean analyzeInvokeSite(InvokeSite invokeSite, MethodInfo invokee, InlineData inlineData) {
        MethodInfo invoker = invokeSite.getInvoker();

        ConstantPoolGen invokerCpg = invoker.getConstantPoolGen();

        InstructionHandle invoke = invokeSite.getInstructionHandle();

        // Check epilogue
        Type[] ret = StackHelper.produceStack(invokerCpg, invoke.getInstruction());
        // works if the invoked method returns the same (single) type as the replaced instruction..
        boolean match = (ret.length == 1 && TypeHelper.canAssign(invokee.getType(), ret[0]));
        // .. or if the invoked method returns void.. we accept that case and assume that if the invokee should
        //    return something but doesn't then it is a JVM call and throws an exception.
        if (!match && !invokee.getType().equals(Type.VOID)) {
            return false;
        }

        // Check and build prologue
        Type[] args = StackHelper.consumeStack(invokerCpg, invoke.getInstruction());

        List<Instruction> oldPrologue = new LinkedList<Instruction>();
        int cnt = 0;

        InstructionHandle current = invoke;
        while (cnt < args.length) {
            if (current.hasTargeters()) {
                // stay within the basic block
                break;
            }

            current = current.getPrev();

            Instruction instr = current.getInstruction();
            // we only rearrange push-instructions
            if (!(instr instanceof PushInstruction) || (instr instanceof DUP) || (instr instanceof DUP2)) {
                break;
            }

            // we add this instruction to the old prologue to replace
            cnt++;
            oldPrologue.add(0, instr);
        }

        inlineData.setOldPrologueLength(cnt);

        List<ValueInfo> params = inlineData.getParams();

        // other parameters must be used in the order they are pushed on the stack, we do not rearrange them
        int offset = args.length - cnt;

        for (int i = 0; i < offset; i++) {
            if (i >= params.size()) {
                Type t = args[i];
                // unused argument, we cannot remove the push instruction so we pop it
                inlineData.addPrologue(t.getSize() == 2 ? new POP2() : new POP() );
            } else {
                ValueInfo value = params.get(i);

                int argNum = value.getParamNr();
                if (!invokee.isStatic()) {
                    argNum++;
                }
                if (argNum != i) {
                    return false;
                }
            }
        }

        // Now, we create a new prologue using the expected argument values and the old push instructions
        for (int i = offset; i < params.size(); i++) {
            ValueInfo value = params.get(i);

            if (value.isThisReference() || value.isParamReference()) {
                int argNum = value.getParamNr();
                if (!invokee.isStatic()) {
                    argNum++;
                }

                if (argNum < offset) {
                    // loading a param a second time which we do not duplicate, cannot inline this
                    return false;
                }

                // To be on the safe side, copy the instruction in case a param is used more than once
                Instruction instr = oldPrologue.get(argNum - offset).copy();

                inlineData.addPrologue(instr);

            } else if (value.isNullReference()) {

                inlineData.addPrologue(InstructionConstants.ACONST_NULL);

            } else if (value.isConstantValue() || value.isStaticFieldReference()) {

                // We need to push a constant on the stack
                Instruction instr = value.getConstantValue().createPushInstruction(invoker.getConstantPoolGen());

                inlineData.addPrologue(instr);

            } else if (!value.isContinued()) {
                throw new AssertionError("Unhandled value type");
            }
        }

        return true;
    }

    /**
     * Check if the resulting code will not be larger than the older code.
     * @param invokeSite the invokesite to inline.
     * @param invokee the invoked method.
     * @param inlineData the map to store the analyzer results
     * @return true if the new code will not violate any size constrains
     */
    private boolean analyzeCodeSize(InvokeSite invokeSite, MethodInfo invokee, InlineData inlineData) {

        ProcessorModel pm = AppInfo.getSingleton().getProcessorModel();
        MethodInfo invoker = invokeSite.getInvoker();

        // delta = new prologue + inlined code + epilogue - old prologue - invokesite
        int delta = 0;

        InstructionHandle[] il = invokee.getCode().getInstructionList().getInstructionHandles();
        InstructionHandle ih = il[inlineData.getInlineStart()];
        while (ih != null) {
            Instruction instr = ih.getInstruction();
            if (instr instanceof ReturnInstruction) {
                break;
            }
            delta += pm.getNumberOfBytes(invokee, instr);

            ih = ih.getNext();
        }

        for (InstructionHandle instr : inlineData.getPrologue().getInstructionHandles()) {
            delta += pm.getNumberOfBytes(invoker, instr.getInstruction());
        }
        for (InstructionHandle instr : inlineData.getEpilogue().getInstructionHandles()) {
            delta += pm.getNumberOfBytes(invoker, instr.getInstruction());
        }

        ih = invokeSite.getInstructionHandle();

        for (int i = 0; i <= inlineData.getOldPrologueLength(); i++) {
            Instruction instr = ih.getInstruction();
            delta -= pm.getNumberOfBytes(invoker, instr);
            ih = ih.getPrev();
        }

        // TODO we could allow for some slack, especially if we decreased the codesize before..
        return delta <= 0;
    }

    /**
     * Try to inline a simple getter, wrapper or stub method.
     * <p>
     * If the inlined code is again an invoke, the InvokeSite does not change because
     * the InstructionHandle of the invoker's invoke is kept.</p>
     *
     * @param invokeSite the invoke to replace.
     * @param invokee the method to inline.
     * @param inlineData the parameters of the invokee and the code to inline.
     * @return true if inlining has been performed.
     */
    private InvokeSite performSimpleInline(InvokeSite invokeSite, MethodInfo invokee, InlineData inlineData) {

        MethodInfo invoker = invokeSite.getInvoker();
        MethodCode invokerCode = invoker.getCode();

        if (logger.isDebugEnabled()) {
            logger.debug("Inlining at "+invokeSite+" using " + invokee);
        }

        // Prepare code for the actual inlining
        helper.prepareInlining(invoker, invokee);

        InstructionHandle invoke = invokeSite.getInstructionHandle();

        // Perform inlining: update the prologue
        if (inlineData.getOldPrologueLength() > 0) {
            InstructionHandle start = invoke;

            for (int i=0; i < inlineData.getOldPrologueLength(); i++) {
                start = start.getPrev();
            }

            // invokeSite is not a target in this case, no need to worry about targets here
            invokerCode.replace(start, inlineData.getOldPrologueLength(), inlineData.getPrologue(), false);

        } else if (inlineData.getPrologue().getLength() > 0) {

            // old-prologue is empty, invokeSite may be a target.. Need to update the targets to the new prologue
            if (invoke.hasTargeters()) {
                invokerCode.retarget(invoke, inlineData.getPrologue().getStart());
            }

            InstructionList il = invokerCode.getInstructionList();
            il.insert(invoke, inlineData.getPrologue());
        }

        // Replace the invoke
        MethodCode invokeeCode = invokee.getCode();
        InstructionList il = invokeeCode.getInstructionList();
        InstructionHandle start = invokeeCode.getInstructionHandle(inlineData.getInlineStart());

        int cnt = il.getLength() - inlineData.getInlineStart();
        if (il.getEnd().getInstruction() instanceof ReturnInstruction) {
            // do not inline the return
            cnt--;
        }

        InstructionHandle end = invokerCode.replace(invoke, 1, invokee, start, cnt, true);

        // copy source line number for first inlined instruction separately, since we skipped some instructions in
        // the invokee
        if (cnt > 0) {
            InstructionHandle ih = end;
            for (int i = 0; i < cnt; i++) {
                ih = ih.getPrev();
            }
            invokerCode.setLineNumber(ih, invokeeCode.getSourceClassInfo(start), invokeeCode.getLineNumber(start));
        }

        // insert epilogue
        invokerCode.getInstructionList().insert(end, inlineData.getEpilogue());

        // If we inlined another invokesite, find the new invokesite and return it
        if (inlineData.getInvokeSite() != null) {
            end = end.getPrev();
            // search backwards from last inlined instruction
            while (end != null) {
                if (invokerCode.isInvokeSite(end)) {
                    return invokerCode.getInvokeSite(end);
                }
                end = end.getPrev();
            }
        }

        return null;
    }

}

