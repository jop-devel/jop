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
import com.jopdesign.libgraph.cfg.statements.common.NewObjectStmt;
import com.jopdesign.libgraph.cfg.statements.stack.StackNew;
import com.jopdesign.libgraph.cfg.statements.stack.StackStatement;
import com.jopdesign.libgraph.cfg.variable.Variable;
import com.jopdesign.libgraph.cfg.variable.VariableTable;
import com.jopdesign.libgraph.struct.ConstantClass;
import com.jopdesign.libgraph.struct.ConstantValue;
import com.jopdesign.libgraph.struct.type.ObjectRefType;
import com.jopdesign.libgraph.struct.type.TypeInfo;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class QuadNew extends NewObjectStmt implements QuadStatement, AssignStmt {

    private Variable newVar;

    public QuadNew(ConstantClass clazz, Variable newVar) {
        super(clazz);
        this.newVar = newVar;
    }

    public String getCodeLine() {
        return newVar.getName() + " = new " + getObjectClass().getClassName();
    }

    public StackStatement[] getStackCode(VariableTable varTable) {
        return new StackStatement[] {
                new StackNew(getObjectClass()),
                QuadHelper.createStore(this)
        };
    }

    public Variable getAssignedVar() {
        return newVar;
    }

    public TypeInfo getAssignedType() {
        return new ObjectRefType(getObjectClass());
    }

    public void setAssignedVar(Variable newVar) {
        this.newVar = newVar;
    }

    public boolean isConstant() {
        return false;
    }

    public ConstantValue evaluateConstantStmt() {
        return null;
    }
}
