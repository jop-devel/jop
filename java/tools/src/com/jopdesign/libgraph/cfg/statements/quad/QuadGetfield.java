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
import com.jopdesign.libgraph.cfg.statements.common.GetfieldStmt;
import com.jopdesign.libgraph.cfg.statements.stack.StackGetField;
import com.jopdesign.libgraph.cfg.statements.stack.StackStatement;
import com.jopdesign.libgraph.cfg.variable.Variable;
import com.jopdesign.libgraph.cfg.variable.VariableTable;
import com.jopdesign.libgraph.struct.ConstantField;
import com.jopdesign.libgraph.struct.ConstantValue;
import com.jopdesign.libgraph.struct.type.TypeInfo;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class QuadGetfield extends GetfieldStmt implements QuadStatement, AssignStmt, VariableStmt {

    private Variable output;
    private Variable instance;

    public QuadGetfield(ConstantField field, Variable output) {
        super(field);
        this.output = output;
        instance = null;
    }

    public QuadGetfield(ConstantField field, Variable output, Variable instance) {
        super(field);
        this.output = output;
        this.instance = instance;
    }

    public String getCodeLine() {
        return output.getName() + " = " + (isStatic() ? getClassInfo().getClassName() : instance.getName() ) +
                "." + getFieldInfo().getName();
    }

    public StackStatement[] getStackCode(VariableTable varTable) {
        if ( isStatic() ) {
            return new StackStatement[] {
                new StackGetField(getConstantField()),
                QuadHelper.createStore(this)
            };
        } else {
            return new StackStatement[] {
                QuadHelper.createLoad(this, 0),
                new StackGetField(getConstantField()),
                QuadHelper.createStore(this)
            };
        }
    }

    public boolean isConstant() {
        return false;
    }

    public ConstantValue evaluateConstantStmt() {
        return null;
    }

    public Variable getAssignedVar() {
        return output;
    }

    public TypeInfo getAssignedType() {
        return getFieldInfo().getType();
    }

    public void setAssignedVar(Variable newVar) {
        output = newVar;
    }

    public Variable[] getUsedVars() {
        return isStatic() ? new Variable[0] : new Variable[] { instance };
    }

    public TypeInfo[] getUsedTypes() {
        return isStatic() ? new TypeInfo[0] : new TypeInfo[] { TypeInfo.CONST_OBJECTREF };
    }

    public void setUsedVar(int i, Variable var) {
        if ( !isStatic() && i == 0 ) {
            instance = var;
        }
    }
}
