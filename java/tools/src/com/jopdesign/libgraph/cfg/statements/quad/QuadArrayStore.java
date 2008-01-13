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
import com.jopdesign.libgraph.cfg.statements.common.ArrayStoreStmt;
import com.jopdesign.libgraph.cfg.statements.stack.StackArrayStore;
import com.jopdesign.libgraph.cfg.statements.stack.StackStatement;
import com.jopdesign.libgraph.cfg.variable.Variable;
import com.jopdesign.libgraph.cfg.variable.VariableTable;
import com.jopdesign.libgraph.struct.type.TypeInfo;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class QuadArrayStore extends ArrayStoreStmt implements QuadStatement, VariableStmt {
    
    private Variable arrayVar;
    private Variable index;
    private Variable value;

    public QuadArrayStore(TypeInfo arrayType, Variable arrayVar, Variable index, Variable value) {
        super(arrayType);
        this.arrayVar = arrayVar;
        this.index = index;
        this.value = value;
    }

    public String getCodeLine() {
        return arrayVar.getName() + "[" + index.getName() + "] = " + value.getName();
    }

    public StackStatement[] getStackCode(VariableTable varTable) {
        return new StackStatement[] {
                QuadHelper.createLoad(this, 0),
                QuadHelper.createLoad(this, 1),
                QuadHelper.createLoad(this, 2),
                new StackArrayStore(getArrayType())
        };
    }

    public Variable[] getUsedVars() {
        return new Variable[] { arrayVar, index, value };
    }

    public TypeInfo[] getUsedTypes() {
        return new TypeInfo[] { TypeInfo.CONST_OBJECTREF, TypeInfo.CONST_INT, getArrayType() };
    }

    public void setUsedVar(int i, Variable var) {
        if ( i == 0 ) {
            arrayVar = var;
        }
        if ( i == 1 ) {
            index = var;
        }
        if ( i == 2 ) {
            value = var;
        }
    }
}
