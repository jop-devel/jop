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

import com.jopdesign.libgraph.cfg.statements.AssignStmt;
import com.jopdesign.libgraph.cfg.statements.VariableStmt;
import com.jopdesign.libgraph.cfg.statements.common.CopyStmt;
import com.jopdesign.libgraph.cfg.statements.stack.StackStatement;
import com.jopdesign.libgraph.cfg.variable.Variable;
import com.jopdesign.libgraph.cfg.variable.VariableTable;
import com.jopdesign.libgraph.struct.ConstantValue;
import com.jopdesign.libgraph.struct.type.TypeInfo;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class QuadCopy extends CopyStmt implements QuadStatement, AssignStmt, VariableStmt {

    private TypeInfo type;
    private Variable out;
    private Variable in;

    public QuadCopy(TypeInfo type, Variable out, Variable in) {
        this.type = type;
        this.out = out;
        this.in = in;
    }

    public int[] getCopyMap() {
        return new int[] { 0 };
    }

    public String getCodeLine() {
        return out.getName() + " = " + in.getName();
    }

    public StackStatement[] getStackCode(VariableTable varTable) {
        return new StackStatement[] {
                QuadHelper.createLoad(this, 0),
                QuadHelper.createStore(this)
        };
    }

    public boolean isConstant() {
        return false;
    }

    public ConstantValue evaluateConstantStmt() {
        return null;
    }

    public Variable getAssignedVar() {
        return out;
    }

    public Variable[] getUsedVars() {
        return new Variable[] { in };
    }

    public TypeInfo getAssignedType() {
        return type;
    }

    public void setAssignedVar(Variable newVar) {
        out = newVar;
    }

    public TypeInfo[] getUsedTypes() {
        return new TypeInfo[] {type};
    }

    public void setUsedVar(int i, Variable var) {
        if ( i == 0 ) {
            in = var;
        }
    }
}
