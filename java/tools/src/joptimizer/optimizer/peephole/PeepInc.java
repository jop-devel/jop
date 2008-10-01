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
import com.jopdesign.libgraph.cfg.statements.stack.StackIInc;
import com.jopdesign.libgraph.cfg.statements.stack.StackLoad;
import com.jopdesign.libgraph.cfg.statements.stack.StackPush;
import com.jopdesign.libgraph.cfg.statements.stack.StackBinop;
import com.jopdesign.libgraph.cfg.statements.stack.StackStore;
import com.jopdesign.libgraph.cfg.variable.Variable;
import com.jopdesign.libgraph.struct.ConstantValue;
import com.jopdesign.libgraph.struct.type.TypeInfo;

/**
 * @author Wolfgang Puffitsch, wpuffits@mail.tuwien.a.at
 */
public class PeepInc implements PeepOptimization {
	
	public PeepInc() {
    }

    public void startOptimizer() {
    }

    public void finishOptimizer() {
    }

    public boolean startGraph(ControlFlowGraph graph) {
        return graph.getType() == ControlFlowGraph.TYPE_STACK;
    }

    public Class getFirstStmtClass() {
        return StackIInc.class;
    }

    public StmtHandle processStatement(StmtHandle stmt) {

        if ( !(stmt.getStatement() instanceof StackIInc) ) {
            return null;
        }

		StackIInc inc = (StackIInc) stmt.getStatement();

// 		System.out.println("IINC");

		Variable var = inc.getIncVariable();
		int increment = inc.getIncrement();

 		BasicBlock block = stmt.getBlock();
		StackCode code = block.getStackCode();

// 		for (int i = 0; i < block.getCodeBlock().size(); i++) {
// 			System.out.println(block.getCodeBlock().getStatement(i));
// 		}

		int index = stmt.getPosition();
		code.deleteStatement(index);
		code.insertStatement(index+0, new StackLoad(TypeInfo.CONST_INT, var));
		code.insertStatement(index+1, new StackPush(new ConstantValue(TypeInfo.CONST_INT, increment)));
		code.insertStatement(index+2, new StackBinop(TypeInfo.CONST_INT, StackBinop.OP_ADD));
		code.insertStatement(index+3, new StackStore(TypeInfo.CONST_INT, var));

// 		System.out.println("->");
// 		for (int i = 0; i < block.getCodeBlock().size(); i++) {
// 			System.out.println(block.getCodeBlock().getStatement(i));
// 		}

// 		System.out.println(code.getStmtHandle(0));

		return stmt;
    }


}