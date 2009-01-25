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
package com.jopdesign.libgraph.cfg;

import com.jopdesign.libgraph.cfg.block.StackCode;
import com.jopdesign.libgraph.cfg.statements.stack.StackStatement;
import com.jopdesign.libgraph.struct.type.TypeInfo;

import java.util.Iterator;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class StackEmulator {

    private TypeInfo[] stack;
    private int maxSize;
    private int size;

    public StackEmulator() {
        stack = new TypeInfo[3];
        maxSize = 0;
        size = 0;
    }

    public void init(TypeInfo[] stack) {
        this.stack = new TypeInfo[stack.length + 3];
        System.arraycopy(stack, 0, this.stack, 0, stack.length);
        maxSize = stack.length;
        size = maxSize;
    }

    /**
     * Change the current stack without resetting the maximum stack size.
     * @param stack the new stackinfo to set.
     */
    public void setStack(TypeInfo[] stack) {
        this.stack = new TypeInfo[stack.length + 3];
        System.arraycopy(stack, 0, this.stack, 0, stack.length);
        size = stack.length;
        maxSize = Math.max(size, maxSize);
    }

    /**
     * Update the stack using a statement.
     * @param stmt the statement to process
     * @return the current stack size after the statement has been executed.
     * @throws GraphException if the stack gets invalid.
     */
    public int processStmt(StackStatement stmt) throws GraphException {
        TypeInfo[] pop = stmt.getPopTypes();
        TypeInfo[] push = stmt.getPushTypes();

        // pop old values
        // TODO check if popped type matches
        size -= pop.length;

        if ( size < 0 ) {
            throw new GraphException("Stack has negative size.");
        }

        // push new values
        int newSize = size + push.length;

        // resize stack
        if ( newSize > stack.length ) {
            TypeInfo[] newStack = new TypeInfo[newSize + 3];
            System.arraycopy(stack, 0, newStack, 0, size);
            stack = newStack;
        }

        System.arraycopy(push, 0, stack, size, push.length);

        size = newSize;
        maxSize = Math.max(maxSize, size);

        return size;
    }

    public TypeInfo[] getCurrentStack() {
        TypeInfo[] rs = new TypeInfo[size];
        System.arraycopy(stack, 0, rs, 0, size);
        return rs;
    }

    public int getMaxStackSize() {
        return maxSize;
    }

    public void processCode(StackCode code) throws GraphException {
        for (Iterator it = code.getStatements().iterator(); it.hasNext();) {
            StackStatement stmt = (StackStatement) it.next();
            processStmt(stmt);
        }
    }
}
