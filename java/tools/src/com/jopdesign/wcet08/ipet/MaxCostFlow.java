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

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

import org.jgrapht.DirectedGraph;

import com.jopdesign.wcet08.ipet.LinearConstraint.ConstraintType;

/**
 * Graphs for use in solving Implicit Path Enumeration (IPET) problems.
 * These are graphs with a dedicated source and sink, cost labels for nodes and max-flow labels for edges.
 * Without additional constraints, the max-flow max-cost problem can be solved using e.g.
 * [Goldberg97].
 * When we add additional linear constraints (path constraints), we have to use an ILP solver.
 * 
 * [Goldberg97]  An efficient implementation of a scaling minimum-cost flow algorithm (1997). A. Goldberg. 
 *
 * @author Benedikt Huber (benedikt.huber@gmail.com)
 *
 */
public class MaxCostFlow<V,E> { 
	private DirectedGraph<V, E> graph;
	private Map<E,Integer> idMap;
	private Map<Integer,E> revMap;
	private Map<V,Long> costObjective;
	private Vector<LinearConstraint<E>> flowConstraints;
	private V entry;
	private V exit;
	public int numVars() {
		return this.idMap.size();
	}
	public MaxCostFlow(DirectedGraph<V,E> g, V entry, V exit) {
		this.graph = g;
		this.entry = entry;
		this.exit  = exit;
		this.costObjective = new HashMap<V, Long>();
		this.flowConstraints = new Vector<LinearConstraint<E>>();
		generateMapping();
		addBasicFlowConstraints();
	}
	private void generateMapping() {
		this.idMap = new HashMap<E, Integer>();
		this.revMap = new HashMap<Integer, E>();
		for(E e : graph.edgeSet()) {
			int key = idMap.size() + 1;
			idMap.put(e, key);			
			revMap.put(key, e);
		}
	}
	/* 
	 * For all nodes but entry and exit, flow incoming = flow outgoing
	 * 
	 */
	private void addBasicFlowConstraints() {
		for(V node : graph.vertexSet()) {
			if(node.equals(this.entry)) continue;
			if(node.equals(this.exit)) continue;
			LinearConstraint<E> flowConstraint = new LinearConstraint<E>(ConstraintType.Equal);
			for(E ingoing : graph.incomingEdgesOf(node)) {
				flowConstraint.addLHS(ingoing);
			}
			for(E outgoing : graph.outgoingEdgesOf(node)) {
				flowConstraint.addRHS(outgoing);
			}
			addEdgeConstraint(flowConstraint);
		}		
	}
	public void addCost(V n, long cost) {
		if(cost == 0) return;
		this.costObjective.put(n,
				(costObjective.containsKey(n) ? costObjective.get(n) : 0) + cost);
	}
	public void addEdgeConstraint(LinearConstraint<E> c) {
		this.flowConstraints.add(c);
	}
	public String toString() {
		StringBuffer s = new StringBuffer();
		s.append("Max-Cost-Flow problem with cost vector: ");
		boolean first = true;
		for(Entry<V,Long> e: costObjective.entrySet()) {			
			if(first) first = false;
			else s.append(" + ");
			s.append(e.getValue());
			s.append(' ');
			s.append(e.getKey());
		}
		s.append("\nFlow\n");
		for(LinearConstraint<E> lc : flowConstraints) {
			s.append(lc);
			s.append('\n');
		}
		return s.toString();
	}
	public double solve(Map<E,Long> flowMapOut, boolean integralFlow) throws Exception {
		LpSolveWrapper wrapper = new LpSolveWrapper(this.idMap, integralFlow);
		for(LinearConstraint<E> lc : flowConstraints) {
			wrapper.addConstraint(lc);
		}
		LinearVector<E> costVec = new LinearVector<E>();
		// build cost objective
		for(Entry<V,Long> e : this.costObjective.entrySet()) {
			long costFactor = e.getValue();
			for(E edge : this.graph.incomingEdgesOf(e.getKey())) {
				costVec.add(edge, costFactor);
			}
		}
		wrapper.setObjective(costVec);
		double[] objVec = new double[this.graph.edgeSet().size()];
		double sol = Math.round(wrapper.solve(objVec));
		if(flowMapOut != null) {
			for(int i = 0; i < objVec.length; i++) {
				flowMapOut.put(revMap.get(i+1), Math.round(objVec[i]));
			}
		}
		return sol;
	}
}
