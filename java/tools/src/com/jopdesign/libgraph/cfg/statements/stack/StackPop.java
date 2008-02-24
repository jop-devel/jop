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
import com.jopdesign.libgraph.cfg.statements.quad.QuadStatement;
import com.jopdesign.libgraph.cfg.variable.VariableTable;
import com.jopdesign.libgraph.struct.TypeException;
import com.jopdesign.libgraph.struct.type.TypeInfo;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class StackPop extends AbstractStatement implements StackStatement {

    private TypeInfo[] types;

    public StackPop(TypeInfo type) {
        this.types = new TypeInfo[] { type };
    }

    public StackPop(TypeInfo[] types) {
        this.types = types;
    }

    public boolean canThrowException() {
        return false;
    }

    public String getCodeLine() {
        if ( types.length > 1 ) {
            return "pop" + types.length;
        }
        return "pop";
    }

    public int getPopCount() {
        return types.length;
    }

    public TypeInfo[] getPopTypes() {
        return types;
    }

    public TypeInfo[] getPushTypes() {
        return new TypeInfo[0];
    }

    public QuadStatement[] getQuadCode(TypeInfo[] stack, VariableTable varTable) throws TypeException {
        return new QuadStatement[0];
    }

    public int getOpcode() {
        int popSize = getPopSize();
        if ( popSize > 2 ) {
            return -1;
        }
        return popSize == 2 ? 0x58 : 0x57;
    }

    public int getBytecodeSize() {
        return 1;
    }

    public int getPopSize() {
        int size = 0;
        for (int i = 0; i < types.length; i++) {
            size += types[i].getLength();
        }
        return size;
    }
}
