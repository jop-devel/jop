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
import com.jopdesign.libgraph.cfg.statements.common.ReturnStmt;
import com.jopdesign.libgraph.cfg.statements.stack.StackReturn;
import com.jopdesign.libgraph.cfg.statements.stack.StackStatement;
import com.jopdesign.libgraph.cfg.variable.Variable;
import com.jopdesign.libgraph.cfg.variable.VariableTable;
import com.jopdesign.libgraph.struct.type.TypeInfo;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class QuadReturn extends ReturnStmt implements QuadStatement, VariableStmt {

    private Variable retValue;

    public QuadReturn() {
    }

    public QuadReturn(TypeInfo type, Variable retValue) {
        super(type);
        this.retValue = retValue;
    }

    public String getCodeLine() {
        return "return" + (getType() != null ? " " + retValue.getName() : "");
    }

    public StackStatement[] getStackCode(VariableTable varTable) {
        if ( getType() != null ) {
            return new StackStatement[] {
                    QuadHelper.createLoad(this, 0),
                    new StackReturn(getType())
            };
        } else {
            return new StackStatement[] {
                    new StackReturn()
            };
        }
    }

    public Variable[] getUsedVars() {
        if ( getType() != null ) {
            return new Variable[] { retValue };
        } else {
            return new Variable[0];
        }
    }

    public TypeInfo[] getUsedTypes() {
        TypeInfo type = getType();
        if ( type != null ) {
            return new TypeInfo[] { type };
        } else {
            return new TypeInfo[0];
        }
    }

    public void setUsedVar(int i, Variable var) {
        if ( i == 0 ) {
            retValue = var;
        }
    }

    public Variable getReturnVar() {
        return retValue;
    }
}
