/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2008, Benedikt Huber (benedikt.huber@gmail.com)

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
package com.jopdesign.wcet08.ipet;

import java.util.Vector;

import com.jopdesign.wcet08.Project;
import com.jopdesign.wcet08.frontend.ControlFlowGraph;
import com.jopdesign.wcet08.frontend.ControlFlowGraph.CFGEdge;
import com.jopdesign.wcet08.frontend.ControlFlowGraph.CFGNode;
import com.jopdesign.wcet08.graphutils.LoopColoring;
import com.jopdesign.wcet08.ipet.LinearConstraint.ConstraintType;

/**
 * Perform local (per method) WCET analysis using DFA+IPET.
 * The local analysis is preceeded by a static (e.g. dataflow) analysis, 
 * which in turn invokes local analysis for referenced methods.
 * 
 * @author Benedikt Huber (benedikt.huber@gmail.com)
 */
public class LocalAnalysis {
	/**
	 * Implementors provide cost for objects of type T
	 * @param <T>
	 */
	public interface CostProvider<T> {
		public long getCost(T obj);
	}
	Project project;
	public LocalAnalysis(Project project) {
		this.project = project;
	}
	/**
	 * Create a max-cost maxflow problem for the given flow graph graph, based on a 
	 * given node to cost mapping.
	 * @param key a unique identifier for the problem (for reporting)
	 * @param g the graph
	 * @param nodeWCET cost of nodes
	 * @return The max-cost maxflow problem
	 */
	public MaxCostFlow<CFGNode,CFGEdge> 
		buildWCETProblem(String key, ControlFlowGraph g,CostProvider<CFGNode> nodeWCET) {
		Vector<FlowConstraint> flowCs = computeFlowConstraints(g);
		MaxCostFlow<CFGNode,CFGEdge> maxflow = 
			new MaxCostFlow<CFGNode,CFGEdge>(key,g.getGraph(),g.getEntry(),g.getExit());
		for(CFGNode n : g.getGraph().vertexSet()) {
			maxflow.setCost(n, nodeWCET.getCost(n));
		}
		for(FlowConstraint c : flowCs) maxflow.addFlowConstraint(c);
		return maxflow;
	}
	/**
	 * Compute flow constraints for the given flow graph
	 * @param g the flow graph
	 * @return A list of flow constraints
	 */
	public Vector<FlowConstraint> computeFlowConstraints(ControlFlowGraph g) {
		Vector<FlowConstraint> constraints = new Vector<FlowConstraint>();
		// sum (e_Entry_x) = 1		
		FlowConstraint entryConstraint = new FlowConstraint(ConstraintType.Equal);
		entryConstraint.addRHS(1);
		for(CFGEdge entryEdge : g.getGraph().outgoingEdgesOf(g.getEntry())) {
			entryConstraint.addLHS(entryEdge);
		}
		constraints.add(entryConstraint);
		// sum (e_y_Exit) = 1
		FlowConstraint exitConstraint = new FlowConstraint(ConstraintType.Equal);
		exitConstraint.addRHS(1);
		for(CFGEdge exitEdge : g.getGraph().incomingEdgesOf(g.getExit())) {
			exitConstraint.addLHS(exitEdge);
		}
		constraints.add(exitConstraint);
		// - for each loop with bound B
		// -- sum(exit_loop_edges) * B <= sum(continue_loop_edges)
		LoopColoring<CFGNode, CFGEdge> loops = g.getLoopColoring();
		for(CFGNode hol : loops.getHeadOfLoops()) {
			FlowConstraint loopConstraint = new FlowConstraint(ConstraintType.Equal);
			if(g.getLoopBounds().get(hol) == null) {
				throw new Error("No loop bound recorder for head of loop: "+hol+
								" : "+g.getLoopBounds());
			}
			int lhsMultiplicity = g.getLoopBounds().get(hol).getUpperBound();
			for(CFGEdge exitEdge : loops.getExitEdgesOf(hol)) {
				loopConstraint.addLHS(exitEdge,lhsMultiplicity);
			}
			for(CFGEdge continueEdge : loops.getBackEdgesTo(hol)) {
				loopConstraint.addRHS(continueEdge);
			}
			constraints.add(loopConstraint);
		}
		return constraints;
	}
}
