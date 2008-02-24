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

import com.jopdesign.libgraph.cfg.statements.common.ReturnStmt;
import com.jopdesign.libgraph.cfg.statements.quad.QuadReturn;
import com.jopdesign.libgraph.cfg.statements.quad.QuadStatement;
import com.jopdesign.libgraph.cfg.variable.Variable;
import com.jopdesign.libgraph.cfg.variable.VariableTable;
import com.jopdesign.libgraph.struct.TypeException;
import com.jopdesign.libgraph.struct.type.TypeInfo;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class StackReturn extends ReturnStmt implements StackStatement {
    
    public StackReturn(TypeInfo type) {
        super(type);
    }

    public StackReturn() {
        super();
    }

    public TypeInfo[] getPopTypes() {
        TypeInfo type = getType();
        if ( type != null ) {
            return new TypeInfo[] { type };
        } else {
            return new TypeInfo[0];
        }
    }

    public TypeInfo[] getPushTypes() {
        return new TypeInfo[0];
    }

    public QuadStatement[] getQuadCode(TypeInfo[] stack, VariableTable varTable) throws TypeException {
        TypeInfo type = getType();
        if ( type != null ) {
            Variable s0 = varTable.getDefaultStackVariable(stack.length - 1);
            return new QuadStatement[] { new QuadReturn(getType(), s0) };
        } else {
            return new QuadStatement[] { new QuadReturn() };
        }
    }

    public int getOpcode() {
        if ( getType() == null ) {
            return 0xb1;
        } else {
            switch (getType().getMachineType()) {
                case TypeInfo.TYPE_INT: return 0xac;
                case TypeInfo.TYPE_LONG: return 0xad;
                case TypeInfo.TYPE_FLOAT: return 0xae;
                case TypeInfo.TYPE_DOUBLE: return 0xaf;
                case TypeInfo.TYPE_REFERENCE: return 0xb0;
            }
        }
        return -1;
    }

    public int getBytecodeSize() {
        return 1;
    }

    public String getCodeLine() {
        return "return" + (getType() != null ? "." + getType().getTypeName() : "");
    }
}
