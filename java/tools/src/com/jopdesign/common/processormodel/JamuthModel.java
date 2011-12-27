/*
 * This file is part of JOP, the Java Optimized Processor
 * see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2010, Stefan Hepp (stefan@stefant.org).
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

package com.jopdesign.common.processormodel;

import com.jopdesign.common.AppInfo;
import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.config.Config;
import com.jopdesign.tools.JopInstr;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.VariableLengthInstruction;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class JamuthModel implements ProcessorModel {

    public JamuthModel(Config config) {
    }

    public String getName() {
        return "jamuth";
    }

    // FIXME: native jamuth classes not yet supported

    public List<String> getJVMClasses() {
        return new ArrayList<String>(0);
    }

    @Override
    public List<String> getNativeClasses() {
        // TODO define native class here or read from config
        return new ArrayList<String>(0);
    }

    @Override
    public List<String> getJVMRoots() {
        // TODO define root methods
        return new ArrayList<String>(0);
    }

    @Override
    public boolean keepJVMClasses() {
        // TODO should we keep jvm classes?
        return false;
    }

    @Override
    public int getMaxMethodSize() {
        return 65535;
    }

    @Override
    public int getMaxStackSize() {
        return 65535;
    }

    @Override
    public int getMaxLocals() {
        // TODO do we have some limitations here?
        return 65535;
    }

    // FIXME: Java implemented bytecodes ?

    public MethodInfo getJavaImplementation(AppInfo ai,
                                            MethodInfo ctx,
                                            Instruction instr) {
        throw new AssertionError("jamuth model does not (yet) support java implemented methods");
    }

    public int getNativeOpCode(MethodInfo ctx, Instruction instr) {
        // FIXME: jamuth specific instructions ?
        return instr.getOpcode();
    }

    public int getNumberOfBytes(MethodInfo context, Instruction instruction) {
        int opCode = getNativeOpCode(context, instruction);
        if (opCode >= 0) {
            // To correctly support TableSwitch,..
            // TODO move this into JopInstr.len()?
            if (instruction instanceof VariableLengthInstruction) {
                return instruction.getLength();
            }
            // FIXME jamuth specific instructions ?
            return JopInstr.len(opCode);
        }
        else throw new AssertionError("Invalid opcode: " + context + " : " + instruction);
    }

    public boolean isImplementedInJava(MethodInfo ctx, Instruction i) {
        return false;
    }

    public boolean isSpecialInvoke(MethodInfo ctx, Instruction i) {
        return false;
    }
}
