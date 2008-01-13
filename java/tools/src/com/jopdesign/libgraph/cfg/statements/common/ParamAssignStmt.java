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
import com.jopdesign.libgraph.struct.ConstantValue;
import com.jopdesign.libgraph.struct.type.TypeInfo;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public abstract class ParamAssignStmt extends AbstractStatement implements IdentityStmt, AssignStmt {

    private TypeInfo type;
    private Variable paramVar;
    private int paramNr;

    public ParamAssignStmt(TypeInfo type, Variable param, int paramNr) {
        this.type = type;
        this.paramVar = param;
        this.paramNr = paramNr;
    }

    public Variable getParamVar() {
        return paramVar;
    }

    public int getParamNr() {
        return paramNr;
    }

    public TypeInfo getType() {
        return type;
    }

    public boolean canThrowException() {
        return false;
    }

    public String getCodeLine() {
        return paramVar.getName() + " = @param" + paramNr;
    }

    public Variable getAssignedVar() {
        return paramVar;
    }

    public TypeInfo getAssignedType() {
        return type;
    }

    public void setAssignedVar(Variable newVar) {
        paramVar = newVar;
    }

    public boolean isConstant() {
        return false;
    }

    public ConstantValue evaluateConstantStmt() {
        return null;
    }
}
