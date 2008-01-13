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
import com.jopdesign.libgraph.cfg.statements.common.NewMultiArrayStmt;
import com.jopdesign.libgraph.cfg.statements.stack.StackNewMultiArray;
import com.jopdesign.libgraph.cfg.statements.stack.StackStatement;
import com.jopdesign.libgraph.cfg.variable.Variable;
import com.jopdesign.libgraph.cfg.variable.VariableTable;
import com.jopdesign.libgraph.struct.ConstantClass;
import com.jopdesign.libgraph.struct.ConstantValue;
import com.jopdesign.libgraph.struct.type.ArrayRefType;
import com.jopdesign.libgraph.struct.type.TypeInfo;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class QuadNewMultiArray extends NewMultiArrayStmt implements QuadStatement, AssignStmt, VariableStmt {

    private Variable newArray;
    private Variable[] params;

    public QuadNewMultiArray(ConstantClass arrayClass, short dimensions, Variable newArray, Variable[] params) {
        super(arrayClass, dimensions);
        this.newArray = newArray;
        this.params = params;
    }

    public TypeInfo getResultType() {
        return new ArrayRefType(getDimensions(), getArrayType());
    }

    public String getCodeLine() {
        StringBuffer code = new StringBuffer(newArray.getName());

        code.append(" = new ");
        code.append(getArrayClass().getClassName());
        for ( int i = 0; i < getDimensions(); i++ ) {
            code.append("[");
            code.append(params[i].getName());
            code.append("]");
        }

        return code.toString();
    }

    public StackStatement[] getStackCode(VariableTable varTable) {
        StackStatement[] stmt = new StackStatement[getDimensions() + 2];
        for (int i = 0; i < getDimensions(); i++ ) {
            stmt[i] = QuadHelper.createLoad(this, i);
        }
        stmt[getDimensions()] = new StackNewMultiArray(getArrayClass(), getDimensions());
        stmt[getDimensions()+1] = QuadHelper.createStore(this);
        return stmt;
    }

    public Variable getAssignedVar() {
        return newArray;
    }

    public TypeInfo getAssignedType() {
        return getResultType();
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

    public Variable[] getUsedVars() {
        return params;
    }

    public TypeInfo[] getUsedTypes() {
        TypeInfo[] types = new TypeInfo[getDimensions()];
        for (int i = 0; i < types.length; i++) {
            types[i] = TypeInfo.CONST_INT;
        }
        return types;
    }

    public void setUsedVar(int i, Variable var) {
        if ( i < params.length ) {
            params[i] = var;
        }
    }
}
