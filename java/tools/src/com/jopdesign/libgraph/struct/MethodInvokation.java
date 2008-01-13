/*
 * Copyright (c) 2007,2008, Stefan Hepp
 *
 * This file is part of JOPtimizer.
 *
 * JOPtimizer is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * JOPtimizer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jopdesign.libgraph.struct;

/**
 * Contains information about a method call.
 * TODO store additional infos like lineNr,.. ?
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class MethodInvokation {
    
    private MethodInfo invoker;
    private ClassInfo invokedClass;
    private MethodInfo invokedMethod;
    private int instructionIdx;

    public MethodInvokation(MethodInfo invoker, ClassInfo invokedClass, MethodInfo invokedMethod) {
        this.invoker = invoker;
        this.invokedClass = invokedClass;
        this.invokedMethod = invokedMethod;
        instructionIdx = -1;
    }

    public MethodInfo getInvoker() {
        return invoker;
    }

    /**
     * get the class of which the method is invoked.
     * This may not be the same as getInvokedMethod().getClassInfo(), as the invoked
     * method may be inherited.
     * @return the class which is invoked.
     */
    public ClassInfo getInvokedClass() {
        return invokedClass;
    }

    public MethodInfo getInvokedMethod() {
        return invokedMethod;
    }

    /**
     * return index of invoke instruction, or -1 if not known.
     * @return index of instruction, or -1 if not set.
     */
    public int getInstructionIndex() {
        return instructionIdx;
    }


    /**
     * set the index of the invoke instruction in the method instruction list.
     * @param instructionIdx indes of the invoke instruction.
     */
    public void setInstructionIndex(int instructionIdx) {
        this.instructionIdx = instructionIdx;
    }

    public String toString() {
        StringBuffer out = new StringBuffer();
        out.append("Invokation: ");
        out.append(invoker.getFQMethodName());
        out.append(" [");
        out.append(instructionIdx);
        out.append("] -> ");
        out.append(invokedMethod.getFQMethodName());
        return out.toString();
    }
}
