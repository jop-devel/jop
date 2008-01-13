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
import com.jopdesign.libgraph.cfg.statements.common.ArrayLoadStmt;
import com.jopdesign.libgraph.cfg.statements.stack.StackArrayLoad;
import com.jopdesign.libgraph.cfg.statements.stack.StackStatement;
import com.jopdesign.libgraph.cfg.variable.Variable;
import com.jopdesign.libgraph.cfg.variable.VariableTable;
import com.jopdesign.libgraph.struct.ConstantValue;
import com.jopdesign.libgraph.struct.type.TypeInfo;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class QuadArrayLoad extends ArrayLoadStmt implements QuadStatement, AssignStmt, VariableStmt {

    private Variable result;
    private Variable arrayVar;
    private Variable index;

    public QuadArrayLoad(TypeInfo arrayType, Variable result, Variable arrayVar, Variable index) {
        super(arrayType);
        this.result = result;
        this.arrayVar = arrayVar;
        this.index = index;
    }

    public String getCodeLine() {
        return result.getName() + " = " + arrayVar.getName() + "[" + index.getName() + "]";
    }

    public StackStatement[] getStackCode(VariableTable varTable) {
        return new StackStatement[] {
                QuadHelper.createLoad(this, 0),
                QuadHelper.createLoad(this, 1),
                new StackArrayLoad(getArrayType()),
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
        return result;
    }

    public Variable[] getUsedVars() {
        return new Variable[] { arrayVar, index };
    }

    public TypeInfo getAssignedType() {
        return getArrayType();
    }

    public void setAssignedVar(Variable newVar) {
        result = newVar;
    }

    public TypeInfo[] getUsedTypes() {
        return new TypeInfo[] { TypeInfo.CONST_OBJECTREF, TypeInfo.CONST_INT };
    }

    public void setUsedVar(int i, Variable var) {
        if ( i == 0 ) {
            arrayVar = var;
        }
        if ( i == 1 ) {
            index = var;
        }
    }
}
