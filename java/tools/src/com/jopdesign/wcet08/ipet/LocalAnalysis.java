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

import java.util.Map;
import java.util.Vector;

import com.jopdesign.wcet08.Project;
import com.jopdesign.wcet08.frontend.FlowGraph;
import com.jopdesign.wcet08.frontend.FlowGraph.FlowGraphEdge;
import com.jopdesign.wcet08.frontend.FlowGraph.FlowGraphNode;
import com.jopdesign.wcet08.ipet.LinearConstraint.ConstraintType;

/**
 * Perform local, i.e. per method, WCET analysis using DFA+IPET.
 * The local analysis takes is preceeded by a dataflow (or simple static) analysis, 
 * which in turn invokes local analysis for smaller methods.
 * 
 * @author Benedikt Huber (benedikt.huber@gmail.com)
 */
public class LocalAnalysis {
	Project project;
	public LocalAnalysis(Project project) {
		this.project = project;
	}
	public MaxCostFlow<FlowGraphNode,FlowGraphEdge> buildWCETProblem(FlowGraph g,Map<FlowGraphNode,Long> nodeWCET) {
		Vector<FlowConstraint> flowCs = computeFlowConstraints(g);
		MaxCostFlow<FlowGraphNode,FlowGraphEdge> maxflow = 
			new MaxCostFlow<FlowGraphNode,FlowGraphEdge>(g.getGraph(),g.getEntry(),g.getExit());
		for(FlowGraphNode n : g.getGraph().vertexSet()) {
			Long cost = nodeWCET.get(n);
			maxflow.setCost(n, cost);
		}
		for(FlowConstraint c : flowCs) maxflow.addFlowConstraint(c);
		return maxflow;
	}
	/**
	 * compute flow constraints
	 * @param g
	 * @return
	 */
	public Vector<FlowConstraint> computeFlowConstraints(FlowGraph g) {
		Vector<FlowConstraint> constraints = new Vector<FlowConstraint>();
		// sum (e_Entry_x) = 1		
		FlowConstraint entryConstraint = new FlowConstraint(ConstraintType.Equal);
		entryConstraint.addRHS(1);
		for(FlowGraphEdge entryEdge : g.getGraph().outgoingEdgesOf(g.getEntry())) {
			entryConstraint.addLHS(entryEdge);
		}
		constraints.add(entryConstraint);
		// sum (e_y_Exit) = 1
		FlowConstraint exitConstraint = new FlowConstraint(ConstraintType.Equal);
		exitConstraint.addRHS(1);
		for(FlowGraphEdge exitEdge : g.getGraph().incomingEdgesOf(g.getExit())) {
			exitConstraint.addLHS(exitEdge);
		}
		constraints.add(exitConstraint);
		// - for each loop with bound B
		// -- sum(exit_loop_edges) * B <= sum(continue_loop_edges) 
		for(FlowGraphNode hol : g.getHeadOfLoops()) {
			FlowConstraint loopConstraint = new FlowConstraint(ConstraintType.Equal);
			int lhsMultiplicity = g.getLoopBounds().get(hol);
			for(FlowGraphEdge exitEdge : g.getExitEdgesOf(hol)) {
				loopConstraint.addLHS(exitEdge,lhsMultiplicity);
			}
			for(FlowGraphEdge continueEdge : g.getBackEdgesTo(hol)) {
				loopConstraint.addRHS(continueEdge);
			}
			constraints.add(loopConstraint);
		}
		return constraints;
	}
}
