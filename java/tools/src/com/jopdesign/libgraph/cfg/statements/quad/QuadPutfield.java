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
import com.jopdesign.libgraph.cfg.statements.common.PutfieldStmt;
import com.jopdesign.libgraph.cfg.statements.stack.StackPutField;
import com.jopdesign.libgraph.cfg.statements.stack.StackStatement;
import com.jopdesign.libgraph.cfg.variable.Variable;
import com.jopdesign.libgraph.cfg.variable.VariableTable;
import com.jopdesign.libgraph.struct.ConstantField;
import com.jopdesign.libgraph.struct.type.TypeInfo;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class QuadPutfield extends PutfieldStmt implements QuadStatement, VariableStmt {

    private Variable instance;
    private Variable value;

    public QuadPutfield(ConstantField fieldInfo, Variable instance, Variable value) {
        super(fieldInfo);
        this.instance = instance;
        this.value = value;
    }

    public QuadPutfield(ConstantField fieldInfo, Variable value) {
        super(fieldInfo);
        this.value = value;
        instance = null;
    }

    public String getCodeLine() {
        return (isStatic() ? getClassInfo().getClassName() : instance.getName() ) +
                "." + getFieldInfo().getName() + " = " + value.getName();
    }

    public StackStatement[] getStackCode(VariableTable varTable) {
        if ( isStatic() ) {
            return new StackStatement[] {
                    QuadHelper.createLoad(this, 0),
                    new StackPutField(getConstantField())
            };
        } else {
            return new StackStatement[] {
                    QuadHelper.createLoad(this, 0),
                    QuadHelper.createLoad(this, 1),
                    new StackPutField(getConstantField())
            };
        }
    }

    public Variable[] getUsedVars() {
        return isStatic() ? new Variable[] { value }
                : new Variable[] { instance, value };
    }

    public TypeInfo[] getUsedTypes() {
        return isStatic() ? new TypeInfo[] { getFieldInfo().getType() }
                : new TypeInfo[] { TypeInfo.CONST_OBJECTREF, getFieldInfo().getType() };
    }

    public void setUsedVar(int i, Variable var) {
        if ( isStatic() ) {
            if ( i == 0 ) {
                instance = var;
            }
            if ( i == 1 ) {
                value = var;
            }
        } else {
            if ( i == 0 ) {
                value = var;
            }
        }
    }
}
