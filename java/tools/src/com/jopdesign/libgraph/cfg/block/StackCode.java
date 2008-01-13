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
import com.jopdesign.libgraph.cfg.statements.stack.StackStatement;
import com.jopdesign.libgraph.struct.type.TypeInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class StackCode implements CodeBlock {

    private BasicBlock block;
    private List stmts;
    private TypeInfo[] startStack;
    private TypeInfo[] endStack;

    public StackCode(BasicBlock block) {
        this.block = block;
        stmts = new ArrayList();
    }

    public BasicBlock getBasicBlock() {
        return block;
    }

    /**
     * Get a list of all stackstatements of this codeblock.
     * Do not modify this list.
     * @return a list of stackstatements.
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

    public void addStatement(StackStatement stackStmt) {
        stmts.add(stackStmt);
    }

    public void insertStatement(int pos, StackStatement stackStmt) {
        stmts.add(pos, stackStmt);
    }

    public void setStatement(int pos, StackStatement stackStmt) {
        stmts.set(pos, stackStmt);
    }

    public Statement deleteStatement(int pos) {
        return deleteStackStatement(pos);
    }

    public StackStatement deleteStackStatement(int pos) {
        return (StackStatement) stmts.remove(pos);
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

    public StackStatement getStatement(int pos) {
        return (StackStatement) stmts.get(pos);
    }
}
