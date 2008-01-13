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
import com.jopdesign.libgraph.cfg.statements.stack.StackIfZero;
import com.jopdesign.libgraph.cfg.statements.stack.StackStatement;
import com.jopdesign.libgraph.cfg.variable.Variable;
import com.jopdesign.libgraph.cfg.variable.VariableTable;
import com.jopdesign.libgraph.struct.type.TypeInfo;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class QuadIfZero extends IfStmt implements QuadStatement, VariableStmt {

    private Variable value;

    public QuadIfZero(TypeInfo type, int operand, Variable value) {
        super(type, operand, true);
        this.value = value;
    }

    public String getCodeLine() {
        return "if " + value.getName() + " " + getOperator() + ": goto #0";
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
                new StackIfZero(getType(), getOperand())
        };
    }

    public Variable[] getUsedVars() {
        return new Variable[] { value };
    }

    public TypeInfo[] getUsedTypes() {
        return new TypeInfo[] { getType() };
    }

    public void setUsedVar(int i, Variable var) {
        if ( i == 0 ) {
            value = var;
        }
    }
}
