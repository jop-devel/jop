/*
 * Copyright (c) 2007,2008, Wolfgang Puffitsch
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
import com.jopdesign.libgraph.cfg.block.BasicBlock;
import com.jopdesign.libgraph.cfg.block.StackCode;
import com.jopdesign.libgraph.cfg.statements.StmtHandle;
import com.jopdesign.libgraph.cfg.statements.stack.StackLoad;
import com.jopdesign.libgraph.cfg.statements.stack.StackPop;
import com.jopdesign.libgraph.cfg.variable.Variable;
import com.jopdesign.libgraph.struct.ConstantValue;
import com.jopdesign.libgraph.struct.type.TypeInfo;

/**
 * @author Wolfgang Puffitsch, wpuffits@mail.tuwien.a.at
 */
public class PeepLoadPop implements PeepOptimization {
	
	public PeepLoadPop() {
    }

    public void startOptimizer() {
    }

    public void finishOptimizer() {
    }

    public boolean startGraph(ControlFlowGraph graph) {
        return graph.getType() == ControlFlowGraph.TYPE_STACK;
    }

    public Class getFirstStmtClass() {
        return StackLoad.class;
    }

    public StmtHandle processStatement(StmtHandle stmt) {

        if ( !(stmt.getStatement() instanceof StackLoad) ) {
            return null;
        }
		
		BasicBlock block = stmt.getBlock();
        StmtHandle nextStmt = stmt.getNext();

        if ( nextStmt == null || !(nextStmt.getStatement() instanceof StackPop) ) {
            return null;
        }

		stmt.delete();
		nextStmt.delete();

		return stmt;
    }


}