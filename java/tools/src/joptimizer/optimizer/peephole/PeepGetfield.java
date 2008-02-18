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
package joptimizer.optimizer.peephole;

import com.jopdesign.libgraph.cfg.ControlFlowGraph;
import com.jopdesign.libgraph.cfg.statements.StmtHandle;
import com.jopdesign.libgraph.cfg.statements.common.GetfieldStmt;
import com.jopdesign.libgraph.cfg.statements.common.PutfieldStmt;
import com.jopdesign.libgraph.cfg.statements.stack.StackDup;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class PeepGetfield implements PeepOptimization {
    
    public PeepGetfield() {
    }

    public void startOptimizer() {
    }

    public void finishOptimizer() {
    }

    public boolean startGraph(ControlFlowGraph graph) {
        return graph.getType() == ControlFlowGraph.TYPE_STACK;
    }

    public Class getFirstStmtClass() {
        return PutfieldStmt.class;
    }

    public StmtHandle processStatement(StmtHandle stmt) {

        PutfieldStmt put = (PutfieldStmt) stmt.getStatement();
        StmtHandle nextStmt = stmt.getNext();

        if ( nextStmt == null || !(nextStmt.getStatement() instanceof GetfieldStmt) ) {
            return null;
        }

        GetfieldStmt get = (GetfieldStmt) nextStmt.getStatement();

        if ( !get.getConstantField().equals(put.getConstantField()) ) {
            return null;
        }

        if ( get.getConstantField().isAnonymous() || get.getConstantField().getFieldInfo().isSynchronized() ) {
            return null;
        }

        stmt.setStatement(new StackDup(put.getConstantField().getFieldInfo().getType()));
        nextStmt.setStatement(put);

        return nextStmt;
    }
}
