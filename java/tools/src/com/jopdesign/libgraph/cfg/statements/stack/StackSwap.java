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

import com.jopdesign.libgraph.cfg.statements.common.CopyStmt;
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
public class StackSwap extends CopyStmt implements StackStatement, StackAssign {

    private TypeInfo type1;
    private TypeInfo type2;

    public StackSwap(TypeInfo type1, TypeInfo type2) {
        this.type1 = type1;
        this.type2 = type2;
    }

    public TypeInfo[] getPopTypes() {
        return new TypeInfo[] { type1, type2 };
    }

    public TypeInfo[] getPushTypes() {
        return new TypeInfo[] { type2, type1 };
    }

    public QuadStatement[] getQuadCode(TypeInfo[] stack, VariableTable varTable) throws TypeException {
        Variable s0 = varTable.getDefaultStackVariable(stack.length - 2);
        Variable s1 = varTable.getDefaultStackVariable(stack.length - 1);
        return new QuadStatement[] { new QuadCopy(type1, s1, s0), new QuadCopy(type2, s0, s1) };
    }

    public int getOpcode() {
        return type1.getLength() == 1 ? 0x5f : -1;
    }

    public int getBytecodeSize() {
        return 1;
    }

    public int[] getCopyMap() {
        return new int[] {1, 0};
    }

    public String getCodeLine() {
        return "swap";
    }

    public ConstantValue[] getConstantValues(ConstantValue[] input) {
        return new ConstantValue[0];
    }
}
