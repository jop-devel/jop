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

import com.jopdesign.libgraph.cfg.statements.common.GotoStmt;
import com.jopdesign.libgraph.cfg.statements.quad.QuadGoto;
import com.jopdesign.libgraph.cfg.statements.quad.QuadStatement;
import com.jopdesign.libgraph.cfg.variable.VariableTable;
import com.jopdesign.libgraph.struct.type.TypeInfo;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class StackGoto extends GotoStmt implements StackStatement {
    
    public static final int BYTE_SIZE = 3;

    public static final StackGoto GOTO = new StackGoto();

    public StackGoto() {
    }

    public TypeInfo[] getPopTypes() {
        return new TypeInfo[0];
    }

    public TypeInfo[] getPushTypes() {
        return new TypeInfo[0];
    }

    public QuadStatement[] getQuadCode(TypeInfo[] stack, VariableTable varTable) {
        return new QuadStatement[] { new QuadGoto() };
    }

    public int getOpcode() {
        // NOTICE check for GOTO_W
        return 0xa7;
    }

    public int getBytecodeSize() {
        // NOTICE check for GOTO_W
        return BYTE_SIZE;
    }
}
