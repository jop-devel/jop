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

package com.jopdesign.jcopter.analysis;

import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.code.InstructionAnalysis;
import com.jopdesign.common.code.InstructionInterpreter;
import com.jopdesign.common.code.InstructionInterpreter.Edge;
import org.apache.bcel.generic.CodeExceptionGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;

/**
 * TODO maybe also analyze the types on the stack and optionally the value-mapping if needed.
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class StacksizeAnalysis {

    private class StackAnalysis implements InstructionAnalysis<Integer> {
        @Override
        public Integer bottom() {
            return null;
        }

        @Override
        public Integer initial(InstructionHandle entry) {
            return 0;
        }

        @Override
        public Integer initial(CodeExceptionGen exceptionHandler) {
            return 1;
        }

        @Override
        public Integer transfer(Integer tailValue, Edge edge) {
            ConstantPoolGen cpg = method.getConstantPoolGen();
            Instruction tail = edge.getTail().getInstruction();
            return tailValue - tail.consumeStack(cpg) + tail.produceStack(cpg);
        }

        @Override
        public boolean compare(Integer transferred, Integer oldValue) {
            // stack size must be static, if we already have a value it must be the same value.
            assert (oldValue == null || transferred.equals(oldValue));
            return oldValue != null;
        }

        @Override
        public Integer join(Integer transferred, Integer oldValue) {
            // stack size must be static, if we already have a value it must be the same value.
            assert (oldValue == null || transferred.equals(oldValue));
            return transferred;
        }
    }

    private final MethodInfo method;
    private final InstructionInterpreter<Integer> interpreter;


    public StacksizeAnalysis(MethodInfo method) {
        this.method = method;
        interpreter = new InstructionInterpreter<Integer>(method, new StackAnalysis());
        interpreter.setStartAtExceptionHandlers(true);
    }

    public MethodInfo getMethod() {
        return method;
    }

    public int getStacksizeBefore(InstructionHandle ih) {
        return interpreter.getResult(ih);
    }

    public void analyze() {
        interpreter.interpret(true);
    }

    public void analyze(InstructionHandle start, InstructionHandle end) {
        interpreter.reset(start, end);
        interpreter.interpret(start, false);
    }

}
