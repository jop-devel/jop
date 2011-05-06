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

import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.code.CallString;
import com.jopdesign.common.code.InvokeSite;
import com.jopdesign.common.type.ValueInfo;
import com.jopdesign.jcopter.JCopter;
import com.jopdesign.jcopter.analysis.ValueAnalysis;
import com.jopdesign.jcopter.optimizer.AbstractOptimizer;
import org.apache.bcel.generic.ArithmeticInstruction;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.ConversionInstruction;
import org.apache.bcel.generic.FieldInstruction;
import org.apache.bcel.generic.GETFIELD;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InvokeInstruction;
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
import java.util.List;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class SimpleInliner extends AbstractOptimizer {

    private static class InvokeMap {
        private List<Instruction> instructions;
        private List<ValueInfo> params;
        private InvokeSite invokeSite;

        private InvokeMap() {
            instructions = new ArrayList<Instruction>(3);
            params = new ArrayList<ValueInfo>(4);
        }

        public void addInstruction(Instruction instruction) {
            this.instructions.add(instruction);
        }

        public void addParam(ValueInfo param) {
            this.params.add(param);
        }

        public void setInvokeSite(InvokeSite invokeSite) {
            this.invokeSite = invokeSite;
        }

        public List<Instruction> getInstructions() {
            return instructions;
        }

        public List<ValueInfo> getParams() {
            return params;
        }

        public InvokeSite getInvokeSite() {
            return invokeSite;
        }
    }

    private static final Logger logger = Logger.getLogger(JCopter.LOG_INLINE+".SimpleInliner");

    private final InlineHelper helper;

    private int inlineCounter;

    public SimpleInliner(JCopter jcopter, InlineConfig inlineConfig) {
        super(jcopter);
        helper = new InlineHelper(jcopter, inlineConfig);
    }

    @Override
    public void initialize() {
        inlineCounter = 0;
    }

    @Override
    public void optimizeMethod(MethodInfo method) {
        ConstantPoolGen cpg = method.getConstantPoolGen();
        InstructionList il = method.getCode().getInstructionList();

        for (InvokeSite invoke : method.getCode().getInvokeSites()) {

            CallString cs = CallString.EMPTY;
            InvokeSite is = invoke;

            while (is != null) {
                cs = cs.push(is);

                // Preliminary checks
                MethodInfo invokee = helper.devirtualize(cs);

                if (checkInvoke(cs, invokee)) {
                    InvokeMap invokeMap = new InvokeMap();

                    if (!analyzeInvokee(cs, invokee, invokeMap)) {
                        break;
                    }

                    if (!performSimpleInline(cs.first(), invokee, invokeMap)) {
                        break;
                    }

                    inlineCounter++;
                    
                    is = invokeMap.getInvokeSite();
                } else {
                    break;
                }
            }

            // TODO update callgraph (?) If we update the callgraph, the callstrings become invalid!
            // -> update callgraph only after we finished inlining of a toplevel invokesite;
            //    collect all invokesites to collapse into toplevel invokesite;
            //    replace old invokesite with invokesites from inlined code, add edges to not inlined methods



            
        }
    }

    @Override
    public void printStatistics() {
        logger.info("Inlined "+inlineCounter+" invoke sites.");
    }

    private boolean checkInvoke(CallString cs, MethodInfo invokee) {

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

        if (!helper.canInline(cs, invokee)) {
            return false;
        }

        // TODO we should check if the stack is empty and if so inline anyway?
        if (helper.needsEmptyStack(cs.first(), invokee)) {
            return false;
        }

        // Other checks are done on the fly when trying to inline

        return true;
    }

    /**
     * @param cs the callstring from the invoker to the invoke to inline (if recursive). Used to check DFA results.
     * @param invokee the invoked method to analyze
     * @param invokeMap the map to populate with the parameters and the instructions to inline.
     * @return true if inlining is possible
     */
    private boolean analyzeInvokee(CallString cs, MethodInfo invokee, InvokeMap invokeMap) {

        // we allow loading of parameters, loading of constants, some instruction, and a return
        ValueAnalysis values = new ValueAnalysis(invokee);
        values.loadParameters();

        InstructionList il = invokee.getCode().getInstructionList(true, false);
        InstructionHandle ih = il.getStart();

        // we should at least have a return instruction, so even for empty methods we should fall through

        // generate the parameter mapping
        while (true) {
            Instruction instruction = ih.getInstruction();

            if (instruction instanceof PushInstruction || instruction instanceof NOP) {
                values.transfer(instruction);
                ih = ih.getNext();
            } else {
                break;
            }
        }

        // store the mapping
        for (ValueInfo value : values.getValueTable().getStack()) {
            invokeMap.addParam(value);
        }

        // if we do not need an NP check, we can also inline code which does not throw an exception in the same way
        boolean needsNPcheck = helper.needsNullpointerCheck(cs, invokee, false);
        boolean hasNPcheck = false;

        // we allow up to 5 instructions and one return before assuming that the resulting code will be too large
        for (int i = 0; i < 6; i++) {
            // now lets see what we have here as non-push instructions
            Instruction instruction = ih.getInstruction();

            if (instruction instanceof InvokeInstruction) {
                if (invokeMap.getInvokeSite() != null) {
                    // only inline at most one invoke
                    return false;
                }
                InvokeSite is = invokee.getCode().getInvokeSite(ih);
                invokeMap.setInvokeSite(is);

                hasNPcheck |= !is.isInvokeStatic();
            }
            else if (instruction instanceof FieldInstruction) {
                hasNPcheck |= (instruction instanceof GETFIELD || instruction instanceof PUTFIELD);
            }
            else if (instruction instanceof ArithmeticInstruction ||
                     instruction instanceof ConversionInstruction ||
                     instruction instanceof StackInstruction ||
                     instruction instanceof NOP)
            {
                // nothing to do, just copy them
            }
            else if (instruction instanceof ReturnInstruction) {
                if (needsNPcheck && !hasNPcheck) {
                    return false;
                }

                // we must have a return instruction now.. Check if we return the only value on the stack,
                // else we need to add pop instructions
                if (instruction instanceof RETURN) {

                    // we do not return anything, so we must empty the stack
                    while (values.getValueTable().getStackSize() > 0) {
                        Instruction pop;
                        if (values.getValueTable().getStackSize() > 1) {
                            pop = new POP2();
                        } else {
                            pop = new POP();
                        }
                        invokeMap.addInstruction(pop);
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
                return false;
            }

            // add instructions to inline, update the stack map since we need it to handle RETURN
            if (!(instruction instanceof NOP)) {
                invokeMap.addInstruction(instruction);
                values.transfer(instruction);
            }

            ih = ih.getNext();
        }

        // too many instructions, do not inline
        return false;
    }

    /**
     * Try to inline a simple getter, wrapper or stub method.
     * <p>
     * If the inlined code is again an invoke, the InvokeSite does not change because
     * the InstructionHandle of the invoker's invoke is kept.</p>
     *
     * @param invokeSite the invoke to replace.
     * @param invokee the method to inline.
     * @param invokeMap the parameters of the invokee and the code to inline.
     * @return true if inlining has been performed.
     */
    private boolean performSimpleInline(InvokeSite invokeSite, MethodInfo invokee, InvokeMap invokeMap) {

        MethodInfo invoker = invokeSite.getInvoker();

        // Do the actual inlining
        helper.prepareInlining(invoker, invokee);

        // First check if we can modify the callsite in a way that it matches the expected parameters
        InstructionHandle invoke = invokeSite.getInstructionHandle();
        

        // Check the resulting codesize: The size of the old preamble and the invoke must not be smaller than
        // the size of the new preamble and the code to inline


        // Perform inlining: update the preamble

        // replace the invoke instruction with the new code. If the code contains an invoke, reuse the old
        // instruction handle so that we keep the invokesite.


        return true;
    }

}

