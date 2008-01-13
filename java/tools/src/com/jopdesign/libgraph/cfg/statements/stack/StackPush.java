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

import com.jopdesign.libgraph.cfg.statements.common.AbstractStatement;
import com.jopdesign.libgraph.cfg.statements.quad.QuadCopy;
import com.jopdesign.libgraph.cfg.statements.quad.QuadStatement;
import com.jopdesign.libgraph.cfg.variable.Variable;
import com.jopdesign.libgraph.cfg.variable.VariableTable;
import com.jopdesign.libgraph.struct.ConstantValue;
import com.jopdesign.libgraph.struct.TypeException;
import com.jopdesign.libgraph.struct.type.TypeInfo;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class StackPush extends AbstractStatement implements StackStatement, StackAssign {
    
    private TypeInfo type;
    private ConstantValue value;

    public StackPush(ConstantValue value) {
        this.type = value.getType();
        this.value = value;
    }

    public TypeInfo getType() {
        return type;
    }

    public ConstantValue getValue() {
        return value;
    }

    public boolean canThrowException() {
        return false;
    }

    public String getCodeLine() {
        return "push." + type.getTypeName() + " " + value.toString();
    }

    public TypeInfo[] getPopTypes() {
        return new TypeInfo[0];
    }

    public TypeInfo[] getPushTypes() {
        return new TypeInfo[] { type };
    }

    public int getClockCycles() {
        return 0;
    }

    public QuadStatement[] getQuadCode(TypeInfo[] stack, VariableTable varTable) throws TypeException {
        Variable s0 = varTable.getDefaultStackVariable(stack.length);
        Variable cval = varTable.getDefaultConstant(value);
        return new QuadStatement[] { new QuadCopy(type, s0, cval) };
    }

    public ConstantValue[] getConstantValues(ConstantValue[] input) {
        return new ConstantValue[] { value };
    }
}
