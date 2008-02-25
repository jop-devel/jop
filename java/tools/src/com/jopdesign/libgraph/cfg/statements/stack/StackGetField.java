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

import com.jopdesign.libgraph.cfg.statements.common.GetfieldStmt;
import com.jopdesign.libgraph.cfg.statements.quad.QuadGetfield;
import com.jopdesign.libgraph.cfg.statements.quad.QuadStatement;
import com.jopdesign.libgraph.cfg.variable.Variable;
import com.jopdesign.libgraph.cfg.variable.VariableTable;
import com.jopdesign.libgraph.struct.ConstantField;
import com.jopdesign.libgraph.struct.ConstantValue;
import com.jopdesign.libgraph.struct.type.TypeInfo;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class StackGetField extends GetfieldStmt implements StackStatement, StackAssign {
    
    public StackGetField(ConstantField field) {
        super(field);
    }

    public TypeInfo[] getPopTypes() {
        return isStatic() ? new TypeInfo[0] : new TypeInfo[] { TypeInfo.CONST_OBJECTREF };
    }

    public TypeInfo[] getPushTypes() {
        return new TypeInfo[] { getConstantField().getType() };
    }

    public QuadStatement[] getQuadCode(TypeInfo[] stack, VariableTable varTable) {
        if ( isStatic() ) {
            Variable s0 = varTable.getDefaultStackVariable(stack.length);
            return new QuadStatement[] { new QuadGetfield(getConstantField(), s0) };
        } else {
            Variable s0 = varTable.getDefaultStackVariable(stack.length - 1);
            return new QuadStatement[] { new QuadGetfield(getConstantField(), s0, s0) };
        }
    }

    public int getOpcode() {
        return isStatic() ? 0xb2: 0xb4;
    }

    public int getBytecodeSize() {
        return 3;
    }

    public String getCodeLine() {
        return "getfield " + getConstantField().getFQName();
    }

    public ConstantValue[] getConstantValues(ConstantValue[] input) {
        return new ConstantValue[0];
    }
}
