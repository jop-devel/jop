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

    private TypeInfo type;
    private int cnt;

    public StackPop(TypeInfo type) {
        this.type = type;
        cnt = 1;
    }

    public StackPop(TypeInfo type, int cnt) {
        this.type = type;
        this.cnt = cnt;
    }

    public boolean canThrowException() {
        return false;
    }

    public String getCodeLine() {
        if ( cnt > 1 ) {
            return "pop" + cnt;
        }
        return "pop";
    }

    public int getPopCount() {
        return cnt;
    }

    public TypeInfo[] getPopTypes() {
        TypeInfo[] types = new TypeInfo[cnt];
        for (int i = 0; i < types.length; i++) {
            types[i] = type;
        }
        return types;
    }

    public TypeInfo[] getPushTypes() {
        return new TypeInfo[0];
    }

    public int getClockCycles() {
        return 0;
    }

    public QuadStatement[] getQuadCode(TypeInfo[] stack, VariableTable varTable) throws TypeException {
        return new QuadStatement[0];
    }
}
