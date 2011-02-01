/*
 * This file is part of JOP, the Java Optimized Processor
 *   see <http://www.jopdesign.com/>
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

package com.jopdesign.common.code;

import com.jopdesign.common.AppInfo;
import com.jopdesign.common.MethodCode;
import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.misc.JavaClassFormatError;
import com.jopdesign.common.type.MethodRef;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InvokeInstruction;

/**
 * A class which represents an invocation.
 *
 * <p>Two invoke-sites are considered equal if the invoker methodInfo are {@link MethodInfo#equals(Object) equal},
 * and if they point to the same InstructionHandle.</p>
 *
 * @see MethodCode#getInvokeSite(InstructionHandle)
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class InvokeSite {

    private final InstructionHandle instruction;
    private final MethodInfo invoker;

    /**
     * Create a new invoke site.
     * <p>
     * You should not use this constructor yourself, instead use {@link MethodCode#getInvokeSite(InstructionHandle)}.
     * </p>
     *
     * @param instruction the instruction handle containing the invoke instruction
     * @param invoker the method containing the invoke instruction
     */
    public InvokeSite(InstructionHandle instruction, MethodInfo invoker) {
        this.instruction = instruction;
        this.invoker = invoker;
    }

    public InstructionHandle getInstruction() {
        return instruction;
    }

    public MethodInfo getMethod() {
        return invoker;
    }

    /**
     * @return a method reference to the invokee method.
     */
    public MethodRef getInvokeeRef() {
        Instruction instr = instruction.getInstruction();
        AppInfo appInfo = AppInfo.getSingleton();
        if (instr instanceof InvokeInstruction) {
            return appInfo.getReferencedMethod(invoker, (InvokeInstruction) instr);
        }
        if (appInfo.getProcessorModel().isImplementedInJava(instr)) {
            return appInfo.getProcessorModel().getJavaImplementation(appInfo, invoker, instr).getMethodRef();
        }
        throw new JavaClassFormatError("InvokeSite handle does not refer to an invoke instruction: "+toString());
    }

    /**
     * Create a string representation of this InvokeSite.
     * Note that the result is neither unique nor constant (since the position in the code can change).
     *
     * @return a readable representation of this callsite.
     */
    @Override
    public String toString() {
        return invoker.getFQMethodName() + ":" + instruction.getPosition();
    }

    /**
     * Two invoke-sites are considered equal if the invoker methodInfo are {@link MethodInfo#equals(Object) equal},
     * and if they point to the same InstructionHandle.
     * @param obj the object to compare.
     * @return true if they refer to the same invocation.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof InvokeSite))    return false;
        InvokeSite is = (InvokeSite) obj;
        if (!invoker.equals(is.getMethod())) return false;
        return instruction.equals(is.getInstruction());
    }

    @Override
    public int hashCode() {
        int result = invoker.hashCode();
        // Beware! we cannot use .getPosition() here since its value can and will change
        result = 31 * result + instruction.hashCode();
        return result;
    }
}
