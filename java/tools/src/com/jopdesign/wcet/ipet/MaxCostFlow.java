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
package com.jopdesign.wcet.ipet;

import com.jopdesign.common.graphutils.IDProvider;
import com.jopdesign.wcet.ipet.LinearConstraint.ConstraintType;
import lpsolve.LpSolveException;
import org.jgrapht.DirectedGraph;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

/**
 * @deprecated Use IPETBuilder and IPETSolver instead
 * 
 * Max-Cost-Network-Flow solver with additional linear constraints
 * (see Implicit Path Enumeration (IPET)).
 * <p>
 * A MCNF problem consists of a directed graph with a dedicated source and sink node, 
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
	/**
	 * Data type encapsulating decision variables.<br/>
	 * Equality: Object Identity
	 */
	public static class DecisionVariable {
		private int id;
		private DecisionVariable(int id) {
			this.id = id;
		}
	}
	
	// Implies that this will only work if flow is < 10 billion for each node
	private static final long BIGM = Long.MAX_VALUE;

	private DirectedGraph<V, E> graph;
	private Map<E,Integer> idMap;
	private Map<DecisionVariable, Integer> dMap;
	private int dGen;
	private Map<Integer,E> revMap;
	private Map<V,Long> nodeCostObjective;
	private Vector<LinearConstraint<E>> flowConstraints;
	private Vector<LinearConstraint<Object>> extraConstraints;
	private LinearVector<Object> extraCost;
	private V entry;
	private V exit;
	private IDProvider<Object> idProvider;
	private String key;
	private HashMap<Integer, DecisionVariable> dRevMap;
    private File outFile = null;

	/**
	 * if set, the problem will be dumped into a file
	 * @param outFile the file to dump to, or null to disable dumping
	 * 
	 */
	public void setDumpILP(File outFile) {
		this.outFile = outFile;
	}

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
		this.nodeCostObjective = new HashMap<V, Long>();
		this.flowConstraints = new Vector<LinearConstraint<E>>();
		this.extraConstraints = new Vector<LinearConstraint<Object>>();
		this.extraCost = new LinearVector<Object>();
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
		this.nodeCostObjective.put(n,
				(nodeCostObjective.containsKey(n) ? nodeCostObjective.get(n) : 0) + cost);
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
	 * @param flowMapOut if not null, write solution into this map, assigning a flow to each edge
	 * @return the cost of the solution
	 * @throws Exception if the ILP solver fails
	 */
	public double solve(Map<E,Long> flowMapOut) throws Exception {
		return solve(flowMapOut,null);
	}
	/**
	 * Solve this MCMF problem using {@link LpSolveWrapper}.
	 * @param flowMapOut if not null, write solution into this map, assigning a flow to each edge
	 * @param decisionsOut if not null, write assignments to decision variable in this map
	 * @return the cost of the solution
	 * @throws Exception if the ILP solver fails
	 */
	public double solve(Map<E,Long> flowMapOut, Map<DecisionVariable,Boolean> decisionsOut) throws Exception {
		LpSolveWrapper<Object> wrapper = 
			new LpSolveWrapper<Object>(dGen-1,true,this.idProvider);
		for(DecisionVariable dv : dMap.keySet()) {
			wrapper.setBinary(dv);
		}
		for(LinearConstraint<E> lc : flowConstraints) {
			wrapper.addConstraint(lc);
		}
		for(LinearConstraint<Object> extra : extraConstraints) {
			wrapper.addConstraint(extra);
		}
		LinearVector<Object> costVec = new LinearVector<Object>(extraCost);
		// build cost objective
		for(Entry<V,Long> e : this.nodeCostObjective.entrySet()) {
			long costFactor = e.getValue();
			for(E edge : this.graph.incomingEdgesOf(e.getKey())) {
				costVec.add(edge, costFactor);
			}
		}
		wrapper.setObjective(costVec,true);
		double[] objVec = new double[dGen-1];
		wrapper.freeze();
		if(this.outFile != null) {
			dumpILP(wrapper);
		}
		double sol = Math.round(wrapper.solve(objVec));
		if(flowMapOut != null) {
			int i = 0;
			while(revMap.containsKey(i+1)) {
				flowMapOut.put(revMap.get(i+1), Math.round(objVec[i]));
				i++;
			}
			if(decisionsOut != null) {				
				while(dRevMap.containsKey(i+1)) {
					decisionsOut.put(dRevMap.get(i+1), objVec[i] > 0.5);
					i++;
				}
			}
		}
		return sol;
	}

	private void dumpILP(LpSolveWrapper<?> wrapper) throws LpSolveException {
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
			for(Entry<DecisionVariable, Integer> dv : this.dMap.entrySet()) {
				fw.append("    "+dv.getKey() + " -> C" + dv.getValue() + "\n");
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

	/* generate a bijective mapping between edges/decision variables and integers */
	private void generateMapping() {
		idMap = new HashMap<E, Integer>();
		this.revMap = new HashMap<Integer, E>();
		for(E e : graph.edgeSet()) {
			int key = idMap.size() + 1;
			idMap.put(e, key);			
			revMap.put(key, e);
		}
		dMap = new HashMap<DecisionVariable, Integer>();
		dRevMap = new HashMap<Integer,DecisionVariable>();
		dGen = idMap.size() + 1;
		/* create ID provider */
		this.idProvider = new IDProvider<Object>() {
			/* Note: No closures in java, so idMap/revMap have to be instance variables */
			public Object fromID(int id) { return revMap.get(id); }
			public int getID(Object t)   { 
				Integer key = idMap.get(t);
				if(key != null) return key.intValue();
				else return dMap.get(t).intValue();
			}
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
		for(Entry<V,Long> e: nodeCostObjective.entrySet()) {			
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
