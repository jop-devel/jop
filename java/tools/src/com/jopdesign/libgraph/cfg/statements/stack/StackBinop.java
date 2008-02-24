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

    public QuadStatement[] getQuadCode(TypeInfo[] stack, VariableTable varTable) {
        Variable s0 = varTable.getDefaultStackVariable(stack.length - 2);
        Variable s1 = varTable.getDefaultStackVariable(stack.length - 1);
        return new QuadStatement[] { new QuadBinop(getType(), getOperand(), s0, s0, s1) };
    }

    public int getOpcode() {

        switch ( getOperand() ) {
            case OP_ADD:
                switch (getType().getMachineType()) {
                    case TypeInfo.TYPE_INT: return 0x60;
                    case TypeInfo.TYPE_LONG: return 0x61;
                    case TypeInfo.TYPE_FLOAT: return 0x62;
                    case TypeInfo.TYPE_DOUBLE: return 0x63;
                }
                break;
            case OP_SUB:
                switch (getType().getMachineType()) {
                    case TypeInfo.TYPE_INT: return 0x64;
                    case TypeInfo.TYPE_LONG: return 0x65;
                    case TypeInfo.TYPE_FLOAT: return 0x66;
                    case TypeInfo.TYPE_DOUBLE: return 0x67;
                }
                break;
            case OP_MUL:
                switch (getType().getMachineType()) {
                    case TypeInfo.TYPE_INT: return 0x68;
                    case TypeInfo.TYPE_LONG: return 0x69;
                    case TypeInfo.TYPE_FLOAT: return 0x6a;
                    case TypeInfo.TYPE_DOUBLE: return 0x6b;
                }
                break;
            case OP_DIV:
                switch (getType().getMachineType()) {
                    case TypeInfo.TYPE_INT: return 0x6c;
                    case TypeInfo.TYPE_LONG: return 0x6d;
                    case TypeInfo.TYPE_FLOAT: return 0x6e;
                    case TypeInfo.TYPE_DOUBLE: return 0x6f;
                }
                break;
            case OP_REMINDER:
                switch (getType().getMachineType()) {
                    case TypeInfo.TYPE_INT: return 0x70;
                    case TypeInfo.TYPE_LONG: return 0x71;
                    case TypeInfo.TYPE_FLOAT: return 0x72;
                    case TypeInfo.TYPE_DOUBLE: return 0x73;
                }
                break;
            case OP_SHIFT_LEFT:
                switch (getType().getMachineType()) {
                    case TypeInfo.TYPE_INT: return 0x78;
                    case TypeInfo.TYPE_LONG: return 0x79;
                }
                break;
            case OP_SHIFT_RIGHT:
                switch (getType().getMachineType()) {
                    case TypeInfo.TYPE_INT: return 0x7a;
                    case TypeInfo.TYPE_LONG: return 0x7b;
                }
                break;
            case OP_LOGIC_SHIFT_RIGHT:
                switch (getType().getMachineType()) {
                    case TypeInfo.TYPE_INT: return 0x7c;
                    case TypeInfo.TYPE_LONG: return 0x7d;
                }
                break;
            case OP_AND:
                switch (getType().getMachineType()) {
                    case TypeInfo.TYPE_INT: return 0x7e;
                    case TypeInfo.TYPE_LONG: return 0x7f;
                }
                break;
            case OP_OR:
                switch (getType().getMachineType()) {
                    case TypeInfo.TYPE_INT: return 0x80;
                    case TypeInfo.TYPE_LONG: return 0x81;
                }
                break;
            case OP_XOR:
                switch (getType().getMachineType()) {
                    case TypeInfo.TYPE_INT: return 0x82;
                    case TypeInfo.TYPE_LONG: return 0x83;
                }
                break;
            case OP_CMP:
                switch (getType().getMachineType()) {
                    case TypeInfo.TYPE_LONG: return 0x94;
                }
                break;
            case OP_CMPL:
                switch (getType().getMachineType()) {
                    case TypeInfo.TYPE_FLOAT: return 0x95;
                    case TypeInfo.TYPE_DOUBLE: return 0x97;
                }
                break;
            case OP_CMPG:
                switch (getType().getMachineType()) {
                    case TypeInfo.TYPE_FLOAT: return 0x96;
                    case TypeInfo.TYPE_DOUBLE: return 0x98;
                }
                break;
        }
        return -1;
    }

    public int getBytecodeSize() {
        return 1;
    }

    public String getCodeLine() {
        return getOperandName();
    }

    public ConstantValue[] getConstantValues(ConstantValue[] input) {
        return new ConstantValue[0];
    }
}
