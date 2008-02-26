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
package com.jopdesign.libgraph.cfg.statements.common;

import com.jopdesign.libgraph.struct.ClassInfo;
import com.jopdesign.libgraph.struct.ConstantMethod;
import com.jopdesign.libgraph.struct.MethodInfo;
import com.jopdesign.libgraph.struct.type.TypeInfo;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public abstract class InvokeStmt extends AbstractStatement {

    public static final int TYPE_VIRTUAL = 1;
    public static final int TYPE_INTERFACE = 2;
    public static final int TYPE_SPECIAL = 3;
    public static final int TYPE_STATIC = 4;

    private ConstantMethod method;
    private int invokeType;

    public InvokeStmt(ConstantMethod methodInfo, int invokeType) {
        this.method = methodInfo;
        this.invokeType = invokeType;
    }

    public ClassInfo getClassInfo() {
        return method.getClassInfo();
    }

    public MethodInfo getMethodInfo() {
        return method.getMethodInfo();
    }

    public ConstantMethod getMethodConstant() {
        return method;
    }

    public int getInvokeType() {
        return invokeType;
    }

    public void setInvokeType(int type) {
        this.invokeType = type;
    }

    public boolean canThrowException() {
        return true;
    }

    public TypeInfo[] getParameterTypes() {
        return method.getParameterTypes();
    }

    public TypeInfo getResultType() {
        return method.getResultType();
    }

    public boolean isStatic() {
        return invokeType == TYPE_STATIC;
    }

    /**
     * Get a map of variable slots for each parameter of this invocation, depending on the
     * length of the parameter types. This map also includes the 'this' reference as first
     * entry for non-static invokes.
     *
     * @return a map with the slot number for each parameter, starting at 0.
     */
    public int[] getParamSlots() {
        TypeInfo[] params = getParameterTypes();

        int[] slots;
        int slot = 0;
        if ( isStatic() ) {
            slots = new int[params.length];
            for (int i = 0; i < params.length; i++) {
                slots[i] = slot;
                slot += params[i].getLength();
            }
        } else {
            slots = new int[params.length+1];
            slots[0] = 0;
            slot = 1;
            for (int i = 0; i < params.length; i++) {
                slots[i+1] = slot;
                slot += params[i].getLength();
            }
        }

        return slots;
    }

    /**
     * Get the number of stack-entries or local variables the parameters of this invocation
     * need. The object-reference is included, too.
     *
     * @return number of slots for all parameters of the incokation.
     */
    public int getParamSlotCount() {
        TypeInfo[] params = getParameterTypes();
        int slots = isStatic() ? 0 : 1;

        for (int i = 0; i < params.length; i++) {
            slots += params[i].getLength();
        }
        return slots;
    }

    public String getInvokeName() {
        String name = "";
        switch (invokeType) {
            case TYPE_VIRTUAL: name = "invokevirtual"; break;
            case TYPE_INTERFACE: name = "invokeinterface"; break;
            case TYPE_SPECIAL: name = "invokespecial"; break;
            case TYPE_STATIC: name = "invokestatic"; break;
        }
        return name;
    }

}
