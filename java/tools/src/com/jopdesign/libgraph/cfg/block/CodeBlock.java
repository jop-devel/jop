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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class CodeBlock {

    private static class BasicStmtHandle extends StmtHandle {

        private CodeBlock code;
        private int pos;
        private Statement stmt;
        private int refCnt;

        public BasicStmtHandle(CodeBlock code, int pos, Statement stmt) {
            this.code = code;
            this.pos = pos;
            this.stmt = stmt;
            refCnt = 1;
        }

        public CodeBlock getCode() {
            return code;
        }

        public int getPosition() {
            return pos;
        }
        
        public Statement getStatement() {
            return stmt;
        }

        public void setStatement(Statement stmt) {
            if ( stmt == null ) {
                return;
            }
            code.setStatement(pos, stmt);
        }

        public void dispose() {
            if ( refCnt == 1 ) {
                code.handles.remove(new Integer(pos));
                stmt = null;
            } else {
                refCnt--;
            }
        }
    }

    private BasicBlock block;
    private List stmts;
    private NavigableMap handles;

    public CodeBlock(BasicBlock block) {
        this.block = block;
        this.stmts = new ArrayList();
        handles = new TreeMap();

    }

    public BasicBlock getBasicBlock() {
        return block;
    }

    /**
     * Get a list of Statements.
     * Do not modify this list.
     *
     * @return a list of Statements.
     */
    public List getStatements() {
        return stmts;
    }

    /**
     * Get the last controlflow Statement of this codeblock, or null if it has none.
     * @return the contrlolflow statement of this block.
     */
    public ControlFlowStmt getControlFlowStmt() {
        if ( stmts.size() > 0 ) {
            Statement stmt = (Statement) stmts.get(stmts.size() - 1);
            if ( stmt instanceof ControlFlowStmt ) {
                return (ControlFlowStmt) stmt;
            }
        }
        return null;
    }

    /**
     * Get number of statements in block.
     * 
     * @return number of statements.
     */
    public int size() {
        return stmts.size();
    }

    /**
     * Check if the code needs a goto at the end of the block.
     * This may be needed if the next-block edge is not the next block
     * in the block list. Therefore the result depends on the current order of the blocks.
     *
     * @return true, if the graph-compiler will insert a goto after this block, else false.
     */
    public boolean needsGoto() {
        if ( !block.hasNextBlock() ) {
            return false;
        }
        return block.getNextBlockEdge().getTargetBlock().getBlockIndex() != block.getBlockIndex() + 1;
    }

    public StmtHandle getStmtHandle(int stmt) {
        BasicStmtHandle handle = (BasicStmtHandle) handles.get(new Integer(stmt));
        if ( handle == null ) {
            handle = new BasicStmtHandle(this, stmt, (Statement) stmts.get(stmt));
            handles.put(new Integer(stmt), handle);
        } else {
            handle.refCnt++;
        }
        return handle;
    }

    public Statement deleteStatement(int pos) {
        BasicStmtHandle handle = (BasicStmtHandle) handles.remove(new Integer(pos));
        if ( handle != null ) {
            handle.stmt = null;
        }
        updateStmtHandles(pos, -1);
        return (Statement) stmts.remove(pos);
    }

    public void moveStatement(int srcPos, CodeBlock toCode) {
        moveStatement(srcPos, toCode, toCode.size());
    }

    public void moveStatement(int srcPos, CodeBlock toCode, int targetPos) {
        BasicStmtHandle handle = (BasicStmtHandle) handles.get(new Integer(srcPos));
        // TODO implement
    }

    public void moveStatements(int srcPos, int toPos, CodeBlock toCode, int targetPos) {
        int count = toPos - srcPos;

        // move statements
        for ( int i = 0; i < count; i++ ) {
            toCode.stmts.add(targetPos + i, stmts.remove(srcPos));
        }

        // move and update handles
        toCode.updateStmtHandles(targetPos, count);

        // quickndirty move of handles
        for ( int i = 0; i < count; i++ ) {
            BasicStmtHandle handle = (BasicStmtHandle) handles.remove(new Integer(srcPos + i));
            if ( handle != null ) {
                handle.code = toCode;
                handle.pos = targetPos + i;
                toCode.handles.put(new Integer(targetPos + i), handle);
            }
        }

        updateStmtHandles(toPos, -count);
    }

    public Statement getStatement(int pos) {
        return (Statement) stmts.get(pos);
    }

    // TODO maybe make them public?

    protected void addStatement(int pos, Statement stmt) {
        stmts.add(pos, stmt);
        updateStmtHandles(pos, 1);
    }

    protected Statement setStatement(int pos, Statement stmt) {
        BasicStmtHandle handle = (BasicStmtHandle) handles.get(new Integer(pos));
        if ( handle != null ) {
            handle.stmt = stmt;
        }
        return (Statement) stmts.set(pos, stmt);
    }

    /**
     * Update statement handle positions in handles map. All statements starting at a
     * given position are updated.
     *
     * @param startPos the position of the first statement to update.
     * @param offset the offset by which all statements should be updated.
     */
    private void updateStmtHandles(int startPos, int offset) {
        List tmp = new LinkedList();

        Iterator it = handles.tailMap(new Integer(startPos)).values().iterator();
        while (it.hasNext()) {
            tmp.add(it.next());
            it.remove();
        }

        // reinsert elements with offset
        for (Iterator it2 = tmp.iterator(); it2.hasNext();) {
            BasicStmtHandle handle = (BasicStmtHandle) it2.next();
            handle.pos += offset;
            handles.put(new Integer(handle.pos), handle);
        }
    }

}
