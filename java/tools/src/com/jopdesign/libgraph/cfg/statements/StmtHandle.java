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
package com.jopdesign.libgraph.cfg.statements;

import com.jopdesign.libgraph.cfg.block.BasicBlock;
import com.jopdesign.libgraph.cfg.block.CodeBlock;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public abstract class StmtHandle {

    public StmtHandle() {
    }

    public BasicBlock getBlock() {
        return getCode().getBasicBlock();
    }

    public abstract CodeBlock getCode();

    public abstract int getPosition();

    /**
     * Check if the statementhandle still refers to a valid statement.
     * @return true if the statement is still valid.
     */
    public boolean isValid() {
        return getStatement() != null;
    }

    /**
     * Delete the statement. This makes this handle invalid.
     */
    public void delete() {
        getCode().deleteStatement(getPosition());
    }

    /**
     * Dispose this handle. This makes the handle invalid, but
     * does not delete the statement.
     * <p>This should be called when the handle is not used anymore to free resources.</p>
     */
    public abstract void dispose();

    /**
     * Get the current statement. If the handle is invalid, returns null.
     * @return the statement, or null if the handle is invalid.
     */
    public abstract Statement getStatement();

    public BasicBlock splitBefore() {
        return getCode().getBasicBlock().splitBlock(getPosition());
    }

    public BasicBlock splitAfter() {
        return getCode().getBasicBlock().splitBlock(getPosition()+1);
    }

    /**
     * Get the next statement after this one in the block of the statement, if any.
     * @return the next statement handle in this block, or null if last one or invalid.
     */
    public StmtHandle getNext() {

        if ( !isValid() ) {
            return null;
        }

        if ( getCode().size() <= getPosition() + 2 ) {
            return null;
        }

        return getCode().getStmtHandle(getPosition() + 1);
    }

    public abstract void setStatement(Statement stmt);
}
