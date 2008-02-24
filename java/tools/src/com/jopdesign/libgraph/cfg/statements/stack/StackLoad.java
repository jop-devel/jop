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

import com.jopdesign.libgraph.cfg.statements.VariableStmt;
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
public class StackLoad extends AbstractStatement implements StackStatement, StackAssign, VariableStmt {

    private TypeInfo type;
    private Variable variable;

    public StackLoad(TypeInfo type, Variable variable) {
        this.type = type;
        this.variable = variable;
    }

    public TypeInfo[] getPopTypes() {
        return new TypeInfo[0];
    }

    public TypeInfo[] getPushTypes() {
        return new TypeInfo[] {type};
    }

    public QuadStatement[] getQuadCode(TypeInfo[] stack, VariableTable varTable) throws TypeException {
        Variable s0 = varTable.getDefaultStackVariable(stack.length);
        return new QuadStatement[] { new QuadCopy(type, s0, variable) };
    }

    public int getOpcode() {
        int index = variable.getIndex();
        switch ( getType().getMachineType() ) {
            case TypeInfo.TYPE_INT: return index > 3 ? 0x15 : 0x1a + index;
            case TypeInfo.TYPE_LONG: return index > 3 ? 0x16 : 0x1e + index;
            case TypeInfo.TYPE_FLOAT: return index > 3 ? 0x17 : 0x22 + index;
            case TypeInfo.TYPE_DOUBLE: return index > 3 ? 0x18 : 0x26 + index;
            case TypeInfo.TYPE_REFERENCE: return index > 3 ? 0x19 : 0x2a + index;
        }

        return -1;
    }

    public int getBytecodeSize() {
        return variable.getIndex() <= 3 ? 1 : 2;
    }

    public TypeInfo getType() {
        return type;
    }

    public Variable getVariable() {
        return variable;
    }

    public boolean canThrowException() {
        return false;
    }

    public String getCodeLine() {
        return "load." + type.getTypeName() + " " + variable.getName();
    }

    public ConstantValue[] getConstantValues(ConstantValue[] input) {
        return new ConstantValue[0];
    }

    public Variable[] getUsedVars() {
        return new Variable[] { variable };
    }

    public TypeInfo[] getUsedTypes() {
        return new TypeInfo[] { type };
    }

    public void setUsedVar(int i, Variable var) {
        if ( i == 0 ) {
            variable = var;
        }
    }
}
