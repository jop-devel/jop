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
package joptimizer.framework.actions;

import com.jopdesign.libgraph.cfg.ControlFlowGraph;
import com.jopdesign.libgraph.struct.MethodInfo;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public interface GraphAction extends Action {

    int STAGE_STACK_TO_QUAD = 0;
    int STAGE_QUAD = 1;
    int STAGE_STACK_TO_BYTECODE = 2;

    /**
     * get the default stage, during which this action should be performed.
     * @return the default graph transform stage of this action.
     */
    int getGraphStage();

    /**
     * get the required graph form for this optimization to work, as defined
     * by the constants in {@link ControlFlowGraph}.
     * 
     * @return the required type of the graph.
     */
    int getRequiredForm();

    void execute(MethodInfo methodInfo, ControlFlowGraph graph) throws ActionException;

}
