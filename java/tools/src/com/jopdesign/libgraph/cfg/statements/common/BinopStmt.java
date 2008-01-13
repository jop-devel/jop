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

import com.jopdesign.libgraph.struct.type.TypeInfo;

/**
 * Common interface for all binary operations (including, but not only comparisons).
 *
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public abstract class BinopStmt extends AbstractStatement {

    public static final int OP_ADD =           10;
    public static final int OP_SUB =           11;
    public static final int OP_MUL =           12;
    public static final int OP_DIV =           13;
    public static final int OP_REMINDER =      14;
    public static final int OP_SHIFT_LEFT =    15;
    public static final int OP_SHIFT_RIGHT =   16;
    public static final int OP_LOGIC_SHIFT_RIGHT = 17;
    public static final int OP_XOR =           18;
    public static final int OP_OR =            19;
    public static final int OP_AND =           20;
    public static final int OP_CMP =           21;
    public static final int OP_CMPL =           22;
    public static final int OP_CMPG =           23;

    private TypeInfo type;
    private int operand;

    protected BinopStmt(TypeInfo type, int operand) {
        this.type = type;
        this.operand = operand;
    }

    public TypeInfo getType() {
        return type;
    }

    public int getOperand() {
        return operand;
    }

    public boolean canThrowException() {
        return false;
    }

    public String getOperandName() {
        String name;

        switch (operand) {
            case OP_ADD:    name = "add"; break;
            case OP_SUB:    name = "sub"; break;
            case OP_MUL:    name = "mul"; break;
            case OP_DIV:    name = "div"; break;
            case OP_REMINDER: name = "rem"; break;
            case OP_SHIFT_LEFT: name = "shl"; break;
            case OP_SHIFT_RIGHT: name = "shr"; break;
            case OP_LOGIC_SHIFT_RIGHT: name = "ushr"; break;
            case OP_XOR:    name = "xor"; break;
            case OP_OR:     name = "or"; break;
            case OP_AND:    name = "and"; break;
            case OP_CMP:    name = "cmp"; break;
            case OP_CMPL:   name = "cmpl"; break;
            case OP_CMPG:   name = "cmpg"; break;
            default: name = "unknown";
        }

        name += "." + type.getTypeName();

        return name;
    }

    public String getOperator() {
        String name;

        switch (operand) {
            case OP_ADD:    name = "+"; break;
            case OP_SUB:    name = "-"; break;
            case OP_MUL:    name = "*"; break;
            case OP_DIV:    name = "/"; break;
            case OP_REMINDER: name = "%"; break;
            case OP_SHIFT_LEFT: name = "<<"; break;
            case OP_SHIFT_RIGHT: name = ">>"; break;
            case OP_LOGIC_SHIFT_RIGHT: name = ">>>"; break;
            case OP_XOR:    name = "^"; break;
            case OP_OR:     name = "|"; break;
            case OP_AND:    name = "&"; break;
            case OP_CMP:    name = "cmp"; break;
            case OP_CMPL:   name = "cmpl"; break;
            case OP_CMPG:   name = "cmpg"; break;
            default: name = "unknown";
        }

        return name;
    }
}
