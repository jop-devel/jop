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
package com.jopdesign.libgraph.cfg.statements.stack;

import com.jopdesign.libgraph.cfg.statements.common.ArrayLoadStmt;
import com.jopdesign.libgraph.cfg.statements.quad.QuadArrayLoad;
import com.jopdesign.libgraph.cfg.statements.quad.QuadStatement;
import com.jopdesign.libgraph.cfg.variable.Variable;
import com.jopdesign.libgraph.cfg.variable.VariableTable;
import com.jopdesign.libgraph.struct.ConstantValue;
import com.jopdesign.libgraph.struct.type.ArrayRefType;
import com.jopdesign.libgraph.struct.type.TypeInfo;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class StackArrayLoad extends ArrayLoadStmt implements StackStatement, StackAssign {

    public StackArrayLoad(TypeInfo arrayType) {
        super(arrayType);
    }

    public TypeInfo[] getPopTypes() {
        return new TypeInfo[] { new ArrayRefType(1, getArrayType() ), TypeInfo.CONST_INT };
    }

    public TypeInfo[] getPushTypes() {
        return new TypeInfo[] { getArrayType() };
    }

    public QuadStatement[] getQuadCode(TypeInfo[] stack, VariableTable varTable) {
        Variable s0 = varTable.getDefaultStackVariable(stack.length - 2);
        Variable s1 = varTable.getDefaultStackVariable(stack.length - 1);
        return new QuadStatement[] { new QuadArrayLoad(getArrayType(), s0, s0, s1) };
    }

    public int getOpcode() {
        switch (getArrayType().getType()) {
            case TypeInfo.TYPE_INT: return 0x2e;
            case TypeInfo.TYPE_LONG: return 0x2f;
            case TypeInfo.TYPE_FLOAT: return 0x30;
            case TypeInfo.TYPE_DOUBLE: return 0x31;
            case TypeInfo.TYPE_BYTE: return 0x33;
            case TypeInfo.TYPE_CHAR: return 0x34;
            case TypeInfo.TYPE_SHORT: return 0x35;
            default: return 0x32;
        }
    }

    public int getBytecodeSize() {
        return 1;
    }

    public String getCodeLine() {
        return "arrayload."+getArrayType().getTypeName();
    }

    public ConstantValue[] getConstantValues(ConstantValue[] input) {
        return null;
    }
}
