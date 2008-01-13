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

import com.jopdesign.libgraph.cfg.statements.AssignStmt;
import com.jopdesign.libgraph.cfg.statements.IdentityStmt;
import com.jopdesign.libgraph.cfg.variable.Variable;
import com.jopdesign.libgraph.struct.ConstantClass;
import com.jopdesign.libgraph.struct.ConstantValue;
import com.jopdesign.libgraph.struct.type.ObjectRefType;
import com.jopdesign.libgraph.struct.type.TypeInfo;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public abstract class ThisAssignStmt extends AbstractStatement implements IdentityStmt, AssignStmt {

    private ConstantClass thisClass;
    private Variable thisVar;
    private ObjectRefType type;

    public ThisAssignStmt(ConstantClass thisClass, Variable thisVar) {
        this.thisClass = thisClass;
        this.thisVar = thisVar;
        type = new ObjectRefType(thisClass);
    }

    public TypeInfo getType() {
        return type;
    }

    public ConstantClass getThisClass() {
        return thisClass;
    }

    public Variable getVariable() {
        return thisVar;
    }
    
    public boolean canThrowException() {
        return false;
    }

    public Variable getAssignedVar() {
        return thisVar;
    }

    public TypeInfo getAssignedType() {
        return getType();
    }

    public void setAssignedVar(Variable newVar) {
        thisVar = newVar;
    }

    public boolean isConstant() {
        return false;
    }

    public ConstantValue evaluateConstantStmt() {
        return null;
    }

    public String getCodeLine() {
        return thisVar.getName() + " = @this";
    }
}
