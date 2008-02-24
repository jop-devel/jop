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

import com.jopdesign.libgraph.cfg.statements.common.NewMultiArrayStmt;
import com.jopdesign.libgraph.cfg.statements.quad.QuadNewMultiArray;
import com.jopdesign.libgraph.cfg.statements.quad.QuadStatement;
import com.jopdesign.libgraph.cfg.variable.Variable;
import com.jopdesign.libgraph.cfg.variable.VariableTable;
import com.jopdesign.libgraph.struct.ConstantClass;
import com.jopdesign.libgraph.struct.ConstantValue;
import com.jopdesign.libgraph.struct.TypeException;
import com.jopdesign.libgraph.struct.type.ArrayRefType;
import com.jopdesign.libgraph.struct.type.TypeInfo;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class StackNewMultiArray extends NewMultiArrayStmt implements StackStatement, StackAssign {
    
    public StackNewMultiArray(ConstantClass constantClass, short dimensions) {
        super(constantClass, dimensions);
    }

    public TypeInfo getResultType() {
        return new ArrayRefType(getDimensions(), getArrayType());
    }

    public TypeInfo[] getPopTypes() {
        TypeInfo[] types = new TypeInfo[getDimensions()];
        for (int i = 0; i < types.length; i++) {
            types[i] = TypeInfo.CONST_INT;
        }
        return types;
    }

    public TypeInfo[] getPushTypes() {
        return new TypeInfo[] { getResultType() };
    }

    public QuadStatement[] getQuadCode(TypeInfo[] stack, VariableTable varTable) throws TypeException {
        Variable s0 = varTable.getDefaultStackVariable(stack.length - getDimensions());
        Variable[] params = new Variable[getDimensions()];

        for (int i = 0; i < params.length; i++) {
            params[i] = varTable.getDefaultStackVariable(stack.length - getDimensions() + i);
        }

        return new QuadStatement[] { new QuadNewMultiArray(getArrayClass(), getDimensions(), s0, params) };
    }

    public int getOpcode() {
        return 0xc5;
    }

    public int getBytecodeSize() {
        return 4;
    }

    public String getCodeLine() {
        StringBuffer code = new StringBuffer("multinewarray ");

        code.append(getArrayClass().getClassName());
        for ( int i = 0; i < getDimensions(); i++ ) {
            code.append("[]");
        }
        
        return code.toString();
    }

    public ConstantValue[] getConstantValues(ConstantValue[] input) {
        return new ConstantValue[0];
    }
}
