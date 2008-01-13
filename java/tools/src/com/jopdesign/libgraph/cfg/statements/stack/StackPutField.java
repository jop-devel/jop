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
package com.jopdesign.libgraph.cfg.statements.stack;

import com.jopdesign.libgraph.cfg.statements.common.PutfieldStmt;
import com.jopdesign.libgraph.cfg.statements.quad.QuadPutfield;
import com.jopdesign.libgraph.cfg.statements.quad.QuadStatement;
import com.jopdesign.libgraph.cfg.variable.Variable;
import com.jopdesign.libgraph.cfg.variable.VariableTable;
import com.jopdesign.libgraph.struct.ClassInfo;
import com.jopdesign.libgraph.struct.ConstantField;
import com.jopdesign.libgraph.struct.FieldInfo;
import com.jopdesign.libgraph.struct.TypeException;
import com.jopdesign.libgraph.struct.type.TypeInfo;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class StackPutField extends PutfieldStmt implements StackStatement {
    
    public StackPutField(ClassInfo classInfo, FieldInfo fieldInfo) {
        super(classInfo, fieldInfo);
    }

    public StackPutField(ConstantField field) {
        super(field.getClassInfo(), field.getFieldInfo());
    }

    public TypeInfo[] getPopTypes() {
        return isStatic() ? new TypeInfo[] { getFieldInfo().getType() }
                : new TypeInfo[] { TypeInfo.CONST_OBJECTREF, getFieldInfo().getType() };
    }

    public TypeInfo[] getPushTypes() {
        return new TypeInfo[0];
    }

    public int getClockCycles() {
        return 0;
    }

    public QuadStatement[] getQuadCode(TypeInfo[] stack, VariableTable varTable) throws TypeException {
        if ( isStatic() ) {
            Variable s0 = varTable.getDefaultStackVariable(stack.length - 1);
            return new QuadStatement[] { new QuadPutfield(getClassInfo(), getFieldInfo(), s0) };
        } else {
            Variable s0 = varTable.getDefaultStackVariable(stack.length - 2);
            Variable s1 = varTable.getDefaultStackVariable(stack.length - 1);
            return new QuadStatement[] { new QuadPutfield(getClassInfo(), getFieldInfo(), s0, s1) };
        }
    }

    public String getCodeLine() {
        return "putfield " + getClassInfo().getClassName() + "." + getFieldInfo().getName();
    }
}
