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
import com.jopdesign.libgraph.cfg.statements.quad.QuadStatement;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class QuadCode implements CodeBlock {
    
    private BasicBlock block;
    private List stmts;

    public QuadCode(BasicBlock block) {
        this.block = block;
        this.stmts = new ArrayList();
    }

    public BasicBlock getBasicBlock() {
        return block;
    }

    /**
     * Get a list of all statements of this codeblock.
     * Do not modify this list.
     *
     * @return a list of QuadStatements.
     *
     * @see CodeBlock#getStatements()
     */
    public List getStatements() {
        return stmts;
    }

    public ControlFlowStmt getControlFlowStmt() {
        if ( stmts.size() > 0 ) {
            Statement stmt = (Statement) stmts.get(stmts.size() - 1);
            if ( stmt instanceof ControlFlowStmt ) {
                return (ControlFlowStmt) stmt;
            }
        }
        return null;
    }

    public int size() {
        return stmts.size();
    }

    public StmtHandle getStmtHandle(int stmt) {
        return new StmtHandle(this, stmt, (Statement) stmts.get(stmt));
    }

    public void addStatement(QuadStatement quadStmt) {
        stmts.add(quadStmt);
    }

    public void insertStatement(int pos, QuadStatement quadStmt) {
        stmts.add(pos, quadStmt);
    }

    public Statement deleteStatement(int pos) {
        return deleteQuadStatement(pos);
    }

    public QuadStatement deleteQuadStatement(int pos) {
        return (QuadStatement) stmts.remove(pos);
    }

    public QuadStatement getStatement(int pos) {
        return (QuadStatement) stmts.get(pos);
    }

    public void setStatement(int i, QuadStatement stmt) {
        stmts.set(i, stmt);
    }
}
