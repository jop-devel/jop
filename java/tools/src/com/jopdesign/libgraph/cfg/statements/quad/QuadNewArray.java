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
import com.jopdesign.libgraph.cfg.statements.common.NewArrayStmt;
import com.jopdesign.libgraph.cfg.statements.stack.StackNewArray;
import com.jopdesign.libgraph.cfg.statements.stack.StackStatement;
import com.jopdesign.libgraph.cfg.variable.Variable;
import com.jopdesign.libgraph.cfg.variable.VariableTable;
import com.jopdesign.libgraph.struct.ConstantValue;
import com.jopdesign.libgraph.struct.type.ArrayRefType;
import com.jopdesign.libgraph.struct.type.TypeInfo;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class QuadNewArray extends NewArrayStmt implements QuadStatement, VariableStmt, AssignStmt {

    private Variable newArray;
    private Variable count;

    public QuadNewArray(TypeInfo arrayType, Variable newArray, Variable count) {
        super(arrayType);
        this.newArray = newArray;
        this.count = count;
    }

    public String getCodeLine() {
        return newArray.getName() + " = new " + getArrayType().getTypeName() + "[" + count.getName() + "]";
    }

    public StackStatement[] getStackCode(VariableTable varTable) {
        return new StackStatement[] {
                QuadHelper.createLoad(this, 0),
                new StackNewArray(getArrayType()),
                QuadHelper.createStore(this)
        };
    }

    public Variable[] getUsedVars() {
        return new Variable[] { count };
    }

    public TypeInfo[] getUsedTypes() {
        return new TypeInfo[] { TypeInfo.CONST_INT };
    }

    public void setUsedVar(int i, Variable var) {
        if ( i == 0 ) {
            count = var;
        }
    }

    public Variable getAssignedVar() {
        return newArray;
    }

    public TypeInfo getAssignedType() {
        return new ArrayRefType(1, getArrayType());
    }

    public void setAssignedVar(Variable newVar) {
        newArray = newVar;
    }

    public boolean isConstant() {
        return false;
    }

    public ConstantValue evaluateConstantStmt() {
        return null;
    }
}
