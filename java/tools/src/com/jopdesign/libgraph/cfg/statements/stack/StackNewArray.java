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

import com.jopdesign.libgraph.cfg.statements.common.NewArrayStmt;
import com.jopdesign.libgraph.cfg.statements.quad.QuadNewArray;
import com.jopdesign.libgraph.cfg.statements.quad.QuadStatement;
import com.jopdesign.libgraph.cfg.variable.Variable;
import com.jopdesign.libgraph.cfg.variable.VariableTable;
import com.jopdesign.libgraph.struct.ConstantValue;
import com.jopdesign.libgraph.struct.TypeException;
import com.jopdesign.libgraph.struct.type.ArrayRefType;
import com.jopdesign.libgraph.struct.type.TypeInfo;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class StackNewArray extends NewArrayStmt implements StackStatement, StackAssign {
    
    public StackNewArray(TypeInfo arrayType) {
        super(arrayType);
    }

    public TypeInfo[] getPopTypes() {
        return new TypeInfo[] { TypeInfo.CONST_INT };
    }

    public TypeInfo[] getPushTypes() {
        return new TypeInfo[] { new ArrayRefType(1, getArrayType()) };
    }

    public QuadStatement[] getQuadCode(TypeInfo[] stack, VariableTable varTable) throws TypeException {
        Variable s0 = varTable.getDefaultStackVariable(stack.length - 1);
        return new QuadStatement[] { new QuadNewArray(getArrayType(), s0, s0) };
    }

    public int getOpcode() {
        return getArrayType().getMachineType() == TypeInfo.TYPE_REFERENCE ? 0xbd : 0xbc;
    }

    public int getBytecodeSize() {
        return getArrayType().getMachineType() == TypeInfo.TYPE_REFERENCE ? 3 : 2;
    }

    public String getCodeLine() {
        return "newarray " + getArrayType().getTypeName();
    }

    public ConstantValue[] getConstantValues(ConstantValue[] input) {
        return new ConstantValue[0];
    }
}
