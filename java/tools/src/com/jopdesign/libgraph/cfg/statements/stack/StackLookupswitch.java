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

import com.jopdesign.libgraph.cfg.statements.common.LookupswitchStmt;
import com.jopdesign.libgraph.cfg.statements.quad.QuadLookupswitch;
import com.jopdesign.libgraph.cfg.statements.quad.QuadStatement;
import com.jopdesign.libgraph.cfg.variable.Variable;
import com.jopdesign.libgraph.cfg.variable.VariableTable;
import com.jopdesign.libgraph.struct.TypeException;
import com.jopdesign.libgraph.struct.type.TypeInfo;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class StackLookupswitch extends LookupswitchStmt implements StackStatement {

    private int[] matchs;

    public StackLookupswitch(int[] matchs) {
        super(matchs);
    }

    public TypeInfo[] getPopTypes() {
        return new TypeInfo[] { TypeInfo.CONST_INT };
    }

    public TypeInfo[] getPushTypes() {
        return new TypeInfo[0];
    }

    public QuadStatement[] getQuadCode(TypeInfo[] stack, VariableTable varTable) throws TypeException {
        Variable s0 = varTable.getDefaultStackVariable(stack.length - 1);
        return new QuadStatement[] { new QuadLookupswitch(getMatchs(), s0) };
    }

    public int getOpcode() {
        return 0xab;
    }

    public int getBytecodeSize() {
        // may be smaller depending on position in code, calculating with 3 bytes as pad
        return 12 + getMatchs().length * 8;
    }

    public String getCodeLine() {
        return "lookupswitch\n" + getTable();
    }

    public boolean isConstant() {
        return false;
    }

    public int getConstantTarget() {
        return 0;
    }
}
