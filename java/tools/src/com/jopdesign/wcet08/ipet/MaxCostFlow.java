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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

import lpsolve.LpSolveException;

import org.jgrapht.DirectedGraph;

import com.jopdesign.wcet08.Config;
import com.jopdesign.wcet08.graphutils.IDProvider;
import com.jopdesign.wcet08.ipet.LinearConstraint.ConstraintType;

/**
 * Max-Cost-Max-Flow solver with additional linear constraints
 * (see Implicit Path Enumeration (IPET)).
 * <p>
 * A MCMF problem consists of a directed graph with a dedicated source and sink node, 
 * cost labels for nodes and linear edge constraints.
 * </p>
 * <p>
 * Note: With simple (constant-per-edge) flow constraints, the problem could be solved using e.g.
 * [Goldberg97]. With arbitrary flow constraints, this is an ILP problem.
 * </p>
 * <p>
 * [Goldberg97]  An efficient implementation of a scaling minimum-cost flow algorithm (1997). A. Goldberg. <br/>
 * </p> 
 * @author Benedikt Huber (benedikt.huber@gmail.com)
 *
 * @param <V> the node type
 * @param <E> the edge type
 */
public class MaxCostFlow<V,E> { 
	private DirectedGraph<V, E> graph;
	private Map<E,Integer> idMap;
	private Map<Integer,E> revMap;
	private Map<V,Long> costObjective;
	private Vector<LinearConstraint<E>> flowConstraints;
	private V entry;
	private V exit;
	private IDProvider<E> idProvider;
	private String key;

	/**
	 * Initialize the MCMF problem with the given graph
	 * @param g the graph
	 * @param entry the source node
	 * @param exit the sink node
	 */
	public MaxCostFlow(String key, DirectedGraph<V,E> g, V entry, V exit) {
		this.key = key;
		this.graph = g;
		this.entry = entry;
		this.exit  = exit;
		this.costObjective = new HashMap<V, Long>();
		this.flowConstraints = new Vector<LinearConstraint<E>>();
		generateMapping();
		addBasicFlowConstraints();
	}
	/**
	 * Set the cost for a node <code>n</code>: for each unit of flow passing <code>n</code>,
	 * <code>cost</code> is added to the total cost of the solution.
	 * @param n
	 * @param cost
	 */
	public void setCost(V n, long cost) {
		if(cost == 0) return;
		this.costObjective.put(n,
				(costObjective.containsKey(n) ? costObjective.get(n) : 0) + cost);
	}
	
	/**
	 * Add a linear flow constraint 
	 * @param constraint of the form <code>a x <=> c</code>, where <code>x</code>
	 * 		             is a vector of edges.
	 */
	public void addFlowConstraint(LinearConstraint<E> constraint) {
		this.flowConstraints.add(constraint);
	}
	
	/**
	 * Solve this MCMF problem using {@link LpSolveWrapper}.
	 * @param flowMapOut write solution into this map, assigning a flow to each edge
	 * @return the cost of the solution
	 * @throws Exception if the ILP solver fails
	 */
	public double solve(Map<E,Long> flowMapOut) throws Exception {
		LpSolveWrapper<E> wrapper = 
			new LpSolveWrapper<E>(graph.edgeSet().size(),true,this.idProvider);
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
		wrapper.setObjective(costVec,true);
		double[] objVec = new double[this.graph.edgeSet().size()];
		double sol = Math.round(wrapper.solve(objVec));
		if(flowMapOut != null) {
			for(int i = 0; i < objVec.length; i++) {
				flowMapOut.put(revMap.get(i+1), Math.round(objVec[i]));
			}
		}
		if(Config.instance().doDumpIPL()) {
			dumpILP(wrapper);
		}
		return sol;
	}
	private void dumpILP(LpSolveWrapper<E> wrapper) throws LpSolveException {
		File outFile = Config.instance().getOutFile(Config.sanitizeFileName(this.key + ".ilp"));
		wrapper.dumpToFile(outFile);
		FileWriter fw = null;
		try {
			fw = new FileWriter(outFile,true);
		} catch (IOException e1) {
			throw new LpSolveException("Failed to open ILP file");
		}
		try {
			fw.append("/* Mapping: \n");
			for(Entry<E,Integer> e : this.idMap.entrySet()) {
				fw.append("    "+e.getKey() + " -> C" + e.getValue() + "\n");
			}
			fw.append(this.toString());
			fw.append("*/\n");
		} catch (IOException e) {
			throw new LpSolveException("Failed to write to ILP file");
		} finally {
			try {
				fw.close();
			} catch (IOException e) {
				throw new LpSolveException("Failed to close ILP file");
			}
		}
	}
	/* generate a bijective mapping between edges and integers */
	private void generateMapping() {
		idMap = new HashMap<E, Integer>();
		this.revMap = new HashMap<Integer, E>();
		for(E e : graph.edgeSet()) {
			int key = idMap.size() + 1;
			idMap.put(e, key);			
			revMap.put(key, e);
		}
		this.idProvider = new IDProvider<E>() {
			/* Note: No closures in java, so idMap/revMap have to be instance variables */
			public E fromID(int id) { return revMap.get(id); }
			public int getID(E t)   { return idMap.get(t);   }
		};
	}
	/* 
	 * For all nodes but entry and exit, flow incoming = flow outgoing 
	 * flow outgoing(source) = flow ingoing(sink) = 1
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
			addFlowConstraint(flowConstraint);
		}		
	}
	@Override
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
}
