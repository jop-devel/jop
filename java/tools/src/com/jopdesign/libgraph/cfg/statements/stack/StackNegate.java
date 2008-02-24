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

import com.jopdesign.libgraph.cfg.statements.common.NegateStmt;
import com.jopdesign.libgraph.cfg.statements.quad.QuadNegate;
import com.jopdesign.libgraph.cfg.statements.quad.QuadStatement;
import com.jopdesign.libgraph.cfg.variable.Variable;
import com.jopdesign.libgraph.cfg.variable.VariableTable;
import com.jopdesign.libgraph.struct.ConstantValue;
import com.jopdesign.libgraph.struct.TypeException;
import com.jopdesign.libgraph.struct.type.TypeInfo;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class StackNegate extends NegateStmt implements StackStatement, StackAssign {
    
    public StackNegate(TypeInfo type) {
        super(type);
    }

    public boolean canThrowException() {
        return false;
    }

    public String getCodeLine() {
        return "neg." + getType().getTypeName();
    }

    public TypeInfo[] getPopTypes() {
        return new TypeInfo[] { getType() };
    }

    public TypeInfo[] getPushTypes() {
        return new TypeInfo[] { getType() };
    }

    public QuadStatement[] getQuadCode(TypeInfo[] stack, VariableTable varTable) throws TypeException {
        Variable s0 = varTable.getDefaultStackVariable(stack.length - 1);
        return new QuadStatement[] { new QuadNegate(getType(), s0, s0) };
    }

    public int getOpcode() {
        switch ( getType().getMachineType() ) {
            case TypeInfo.TYPE_INT: return 0x74;
            case TypeInfo.TYPE_LONG: return 0x75;
            case TypeInfo.TYPE_FLOAT: return 0x76;
            case TypeInfo.TYPE_DOUBLE: return 0x77;
        }
        return -1;
    }

    public int getBytecodeSize() {
        return 1;
    }

    public boolean isConstant() {
        return false;
    }

    public ConstantValue[] getConstantValues(ConstantValue[] input) {
        return new ConstantValue[0];
    }
}
