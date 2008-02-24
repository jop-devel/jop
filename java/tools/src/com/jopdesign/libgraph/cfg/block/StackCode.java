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
package com.jopdesign.libgraph.cfg.block;

import com.jopdesign.libgraph.cfg.statements.stack.StackGoto;
import com.jopdesign.libgraph.cfg.statements.stack.StackStatement;
import com.jopdesign.libgraph.struct.type.TypeInfo;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class StackCode extends CodeBlock {

    private TypeInfo[] startStack;
    private TypeInfo[] endStack;

    public StackCode(BasicBlock block) {
        super(block);
    }

    public void addStatement(StackStatement stackStmt) {
        addStatement(size(), stackStmt);
    }

    public void insertStatement(int pos, StackStatement stackStmt) {
        addStatement(pos, stackStmt);
    }

    public void setStackStatement(int pos, StackStatement stackStmt) {
        setStatement(pos, stackStmt);
    }

    public StackStatement deleteStackStatement(int pos) {
        return (StackStatement) deleteStatement(pos);
    }
    
    public StackStatement getStackStatement(int pos) {
        return (StackStatement) getStatement(pos);
    }

    public TypeInfo[] getStartStack() {
        return startStack;
    }

    public void setStartStack(TypeInfo[] startStack) {
        this.startStack = startStack;
    }

    public TypeInfo[] getEndStack() {
        return this.endStack;
    }

    public void setEndStack(TypeInfo[] endStack) {
        this.endStack = endStack;
    }

    public int getBytecodeSize() {
        int size = 0;

        for (int i = 0; i < size(); i++) {
            size += getStackStatement(i).getBytecodeSize();
        }

        if ( needsGoto() ) {
            size += StackGoto.BYTE_SIZE;
        }

        return size;
    }

}
