/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2011, Benedikt Huber (benedikt@vmars.tuwien.ac.at)

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.jopdesign.common.code;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.code.ControlFlowGraph.CFGEdge;

/**
 * Purpose: Objects providing the InfeasibleEdgeDetector interface are used
 * to filter control flow graph edges which are infeasible in a certain (call) context
 *
 */
public interface InfeasibleEdgeProvider {
	
	public InfeasibleEdgeProvider NO_INFEASIBLES = new InfeasibleEdgeProvider() {
		@Override
		public List<CFGEdge> getInfeasibleEdges(ControlFlowGraph cfg,CallString cs) {
			return new ArrayList<CFGEdge>();
		}
		@Override
		public boolean isInfeasibleReceiver(MethodInfo method, CallString cs) {
			return false;
		}			
	};

	/** 
	 * Get list of infeasible edges for the given control flow graph
	 * @param cfg
	 * @param cs
	 * @return
	 */
    public Collection<CFGEdge> getInfeasibleEdges(ControlFlowGraph cfg, CallString cs);

	/**
	 * Return true if the virtual invocation of the given method is infeasible in the specified call context
	 * @param method
	 * @param cs
	 * @return
	 */
	public boolean isInfeasibleReceiver(MethodInfo method, CallString cs);
}
