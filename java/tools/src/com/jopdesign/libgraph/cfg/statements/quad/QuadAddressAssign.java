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
import com.jopdesign.libgraph.cfg.statements.common.AddressAssignStmt;
import com.jopdesign.libgraph.cfg.statements.stack.StackAddressAssign;
import com.jopdesign.libgraph.cfg.statements.stack.StackStatement;
import com.jopdesign.libgraph.cfg.variable.Variable;
import com.jopdesign.libgraph.cfg.variable.VariableTable;
import com.jopdesign.libgraph.struct.ConstantValue;
import com.jopdesign.libgraph.struct.type.TypeInfo;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class QuadAddressAssign extends AddressAssignStmt implements QuadStatement, AssignStmt {

    private Variable variable;

    public QuadAddressAssign(Variable variable) {
        this.variable = variable;
    }

    public String getCodeLine() {
        return variable.getName() + " = @returnAddress";
    }

    public StackStatement[] getStackCode(VariableTable varTable) {
        return new StackStatement[] {
                new StackAddressAssign(),
                QuadHelper.createStore(this)
        };
    }

    public Variable getAssignedVar() {
        return variable;
    }

    public TypeInfo getAssignedType() {
        return TypeInfo.CONST_ADDRESSREF;
    }

    public void setAssignedVar(Variable newVar) {
        variable = newVar;
    }

    public boolean isConstant() {
        return false;
    }

    public ConstantValue evaluateConstantStmt() {
        return null;
    }

}
