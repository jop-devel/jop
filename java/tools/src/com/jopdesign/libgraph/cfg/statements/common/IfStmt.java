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

import com.jopdesign.libgraph.cfg.statements.BranchStmt;
import com.jopdesign.libgraph.cfg.statements.CmpStmt;
import com.jopdesign.libgraph.struct.type.TypeInfo;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public abstract class IfStmt extends AbstractStatement implements CmpStmt, BranchStmt {

    private TypeInfo type;
    private int operand;
    private boolean cmpZero;

    public IfStmt(TypeInfo type, int operand, boolean cmpZero) {
        this.type = type;
        this.operand = operand;
        this.cmpZero = cmpZero;
    }

    public TypeInfo getType() {
        return type;
    }

    public int getOperand() {
        return operand;
    }

    public boolean doCmpZero() {
        return cmpZero;
    }

    public boolean canThrowException() {
        return false;
    }

    public boolean isAlwaysTaken() {
        return false;
    }

    public int getTargetCount() {
        return 1;
    }

    public int getConstantTarget() {
        return getConstantResult() ? 0 : -1;
    }

    public String getOperandName() {
        String op;
        boolean isRef = cmpZero && type.getMachineType() == TypeInfo.TYPE_REFERENCE;

        switch (operand) {
            case OP_EQUAL: op = isRef ? "null" : "eq"; break;
            case OP_NOTEQUAL: op = isRef ? "nonnull" : "ne"; break;
            case OP_LESS: op = "lt"; break;
            case OP_LESS_OR_EQUAL: op = "le"; break;
            case OP_GREATER: op = "gt"; break;
            case OP_GREATER_OR_EQUAL: op = "ge"; break;
            default: op = "unknown";
        }
        return op;
    }

    public String getOperator() {
        String op;

        if ( cmpZero ) {
            boolean isRef = type.getMachineType() == TypeInfo.TYPE_REFERENCE;
            switch (operand) {
                case OP_EQUAL: op = isRef ? "== null" : " == 0"; break;
                case OP_NOTEQUAL: op = isRef ? "!= null" : "!= 0"; break;
                case OP_LESS: op = "< 0"; break;
                case OP_LESS_OR_EQUAL: op = "<= 0"; break;
                case OP_GREATER: op = "> 0"; break;
                case OP_GREATER_OR_EQUAL: op = ">= 0"; break;
                default: op = "unknown";
            }
        } else {
            switch (operand) {
                case OP_EQUAL: op = "=="; break;
                case OP_NOTEQUAL: op = "!="; break;
                case OP_LESS: op = "<"; break;
                case OP_LESS_OR_EQUAL: op = "<="; break;
                case OP_GREATER: op = ">"; break;
                case OP_GREATER_OR_EQUAL: op = ">="; break;
                default: op = "unknown";
            }
        }

        return op;
    }
}
