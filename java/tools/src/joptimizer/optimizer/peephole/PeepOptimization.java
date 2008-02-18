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

/**
 * Interface for peephole optimization implementations.
 *
 * TODO add possibility to get stmt-match pattern like BCEL's findInstructions().
 *
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public interface PeepOptimization {

    void startOptimizer();

    void finishOptimizer();

    /**
     * Called before the optimization is run on a graph.
     * Checks if the graph has the required form.
     *
     * @param graph the graph which will be optimized.
     * @return true if this optimizer can be used on this graph.
     */
    boolean startGraph(ControlFlowGraph graph);

    /**
     * Get the class of the first statement this optimizer operates on.
     * @return the class of the stmt, or null for all statements.
     */
    Class getFirstStmtClass();

    /**
     * Optimize a code segment starting at a given statement.
     * @param stmt the first statement which should be processed.
     * @return the statement after which the peephole optimizer will continue, or null if nothing has been done.
     */
    StmtHandle processStatement(StmtHandle stmt);
    
}
