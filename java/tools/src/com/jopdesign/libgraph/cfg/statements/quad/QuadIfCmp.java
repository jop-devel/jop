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
package com.jopdesign.libgraph.cfg.statements.quad;

import com.jopdesign.libgraph.cfg.statements.VariableStmt;
import com.jopdesign.libgraph.cfg.statements.common.IfStmt;
import com.jopdesign.libgraph.cfg.statements.stack.StackIfCmp;
import com.jopdesign.libgraph.cfg.statements.stack.StackStatement;
import com.jopdesign.libgraph.cfg.variable.Variable;
import com.jopdesign.libgraph.cfg.variable.VariableTable;
import com.jopdesign.libgraph.struct.type.TypeInfo;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class QuadIfCmp extends IfStmt implements QuadStatement, VariableStmt {

    private Variable value1;
    private Variable value2;

    public QuadIfCmp(TypeInfo type, int operand, Variable value1, Variable value2) {
        super(type, operand, false);
        this.value1 = value1;
        this.value2 = value2;
    }

    public String getCodeLine() {
        return "if " + value1.getName() + " " + getOperator() + " " + value2.getName() + ": goto #0";
    }

    public boolean isConstant() {
        return false;
    }

    public boolean getConstantResult() {
        return false;
    }

    public StackStatement[] getStackCode(VariableTable varTable) {
        return new StackStatement[] {
                QuadHelper.createLoad(this, 0),
                QuadHelper.createLoad(this, 1),
                new StackIfCmp(getType(), getOperand())
        };
    }

    public Variable[] getUsedVars() {
        return new Variable[] { value1, value2 };
    }

    public TypeInfo[] getUsedTypes() {
        return new TypeInfo[] { getType(), getType() };
    }

    public void setUsedVar(int i, Variable var) {
        if ( i == 0 ) {
            value1 = var;
        }
        if ( i == 1 ) {
            value2 = var;
        }
    }
}
