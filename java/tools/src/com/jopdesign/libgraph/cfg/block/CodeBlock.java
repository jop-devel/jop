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

import com.jopdesign.libgraph.cfg.statements.ControlFlowStmt;
import com.jopdesign.libgraph.cfg.statements.Statement;
import com.jopdesign.libgraph.cfg.statements.StmtHandle;

import java.util.List;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public interface CodeBlock {

    BasicBlock getBasicBlock();

    /**
     * Get a list of Statements.
     * Do not modify this list.
     * @return a list of Statements.
     */
    List getStatements();

    /**
     * Get the last controlflow Statement of this codeblock, or null if it has none.
     * @return the contrlolflow statement of this block.
     */
    ControlFlowStmt getControlFlowStmt();

    /**
     * Get number of statements in block.
     * @return number of statements.
     */
    int size();

    StmtHandle getStmtHandle(int stmt);

    Statement deleteStatement(int pos);
}
