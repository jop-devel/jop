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

import com.jopdesign.libgraph.cfg.statements.common.BinopStmt;
import com.jopdesign.libgraph.cfg.statements.quad.QuadBinop;
import com.jopdesign.libgraph.cfg.statements.quad.QuadStatement;
import com.jopdesign.libgraph.cfg.variable.Variable;
import com.jopdesign.libgraph.cfg.variable.VariableTable;
import com.jopdesign.libgraph.struct.ConstantValue;
import com.jopdesign.libgraph.struct.type.TypeInfo;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class StackBinop extends BinopStmt implements StackStatement, StackAssign {

    public StackBinop(TypeInfo type, int operand) {
        super(type, operand);
    }

    public TypeInfo[] getPopTypes() {
        int op = getOperand();
        if ( op == OP_SHIFT_LEFT || op == OP_LOGIC_SHIFT_RIGHT || op == OP_SHIFT_RIGHT ) {
            return new TypeInfo[] { getType(), TypeInfo.CONST_INT };
        }
        return new TypeInfo[] { getType(), getType() };
    }

    public TypeInfo[] getPushTypes() {
        int op = getOperand();
        if ( op == OP_CMP || op == OP_CMPG || op == OP_CMPL ) {
            return new TypeInfo[] { TypeInfo.CONST_INT };
        }
        return new TypeInfo[] { getType() };
    }

    public int getClockCycles() {
        return 0;
    }

    public QuadStatement[] getQuadCode(TypeInfo[] stack, VariableTable varTable) {
        Variable s0 = varTable.getDefaultStackVariable(stack.length - 2);
        Variable s1 = varTable.getDefaultStackVariable(stack.length - 1);
        return new QuadStatement[] { new QuadBinop(getType(), getOperand(), s0, s0, s1) };
    }

    public String getCodeLine() {
        return getOperandName();
    }

    public ConstantValue[] getConstantValues(ConstantValue[] input) {
        return new ConstantValue[0];
    }
}
