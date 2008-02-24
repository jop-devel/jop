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

import com.jopdesign.libgraph.cfg.statements.common.IfStmt;
import com.jopdesign.libgraph.cfg.statements.quad.QuadIfZero;
import com.jopdesign.libgraph.cfg.statements.quad.QuadStatement;
import com.jopdesign.libgraph.cfg.variable.Variable;
import com.jopdesign.libgraph.cfg.variable.VariableTable;
import com.jopdesign.libgraph.struct.type.TypeInfo;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class StackIfZero extends IfStmt implements StackStatement {
    
    public static final int BYTE_SIZE = 3;

    public StackIfZero(TypeInfo type, int operand) {
        super(type, operand, true);
    }

    public TypeInfo[] getPopTypes() {
        return new TypeInfo[] { getType() };
    }

    public TypeInfo[] getPushTypes() {
        return new TypeInfo[0];
    }

    public QuadStatement[] getQuadCode(TypeInfo[] stack, VariableTable varTable) {
        Variable s0 = varTable.getDefaultStackVariable(stack.length - 1);
        return new QuadStatement[] { new QuadIfZero(getType(), getOperand(), s0) };
    }

    public int getOpcode() {
        switch (getType().getMachineType()) {
            case TypeInfo.TYPE_INT:
                switch (getOperand()) {
                    case OP_EQUAL: return 0x99;
                    case OP_NOTEQUAL: return 0x9a;
                    case OP_LESS: return 0x9b;
                    case OP_GREATER_OR_EQUAL: return 0x9c;
                    case OP_GREATER: return 0x9d;
                    case OP_LESS_OR_EQUAL: return 0x9e;
                }
                break;
            case TypeInfo.TYPE_REFERENCE:
                switch (getOperand()) {
                    case OP_EQUAL: return 0xc6;
                    case OP_NOTEQUAL: return 0xc7;
                }
                break;
        }
        return -1;
    }

    public int getBytecodeSize() {
        return BYTE_SIZE;
    }

    public String getCodeLine() {
        return "if" + getOperandName() + ": goto #0";
    }

    public boolean isConstant() {
        return false;
    }

    public boolean getConstantResult() {
        return false;
    }
}
