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
package com.jopdesign.wcet08.graphutils;

import java.util.Hashtable;
import java.util.Vector;

import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.traverse.DepthFirstIterator;

import com.jopdesign.wcet08.frontend.FlowGraph.FlowGraphEdge;
import com.jopdesign.wcet08.frontend.FlowGraph.FlowGraphNode;

/**
 * Given a rooted, directed graph, identify back-edges.
 * A back-edge is an edge <code>B -> A</code> s.t. A dominates B w.r.t to Entry.
 * If removing the back-edges leads to a DAG, compute a topological order using that graph.
 * @param <V> node type
 * @param <E> edge type
 */
public class TopOrder<V,E> {
	/**
	 * Raised if the given graph isn't reducible, i.e. there are cycles without back-edges,
	 * or if there is more than one component.
	 */
	public static class BadFlowGraphException extends Exception {
		private static final long serialVersionUID = 1L;
		public BadFlowGraphException(String reason) { super(reason); }
	}
	private DirectedGraph<V, E> graph;
	private Hashtable<V,Integer> topOrder = null;
	private Vector<V> dfsOrder = null;
	private Vector<E> backEdges = null;
	private V startVertex;
	private Dominators<V, E> dominators;

	/* Iterator detecting back edges using DFS search.
	 * This works for reducible graphs only.
	 */
	private class BackEdgeDetector extends DepthFirstIterator<V,E> {
		private int gen;
		public BackEdgeDetector(DirectedGraph<V, E> g, V startVertex) {
			super(g,startVertex);
			this.gen = 1;
		}
		// Topological order
		@Override
		protected void encounterVertex(V vertex, E edge) {
			super.encounterVertex(vertex,edge);
			topOrder.put(vertex, gen++);
			dfsOrder.add(vertex);
		}
		@Override
		protected void encounterVertexAgain(V vertex, E edge) {
			super.encounterVertexAgain(vertex,edge);
			if(getSeenData(vertex) == VisitColor.BLACK) {
				int sourceOrder = topOrder.get(graph.getEdgeSource(edge));
				int targetOrder = topOrder.get(graph.getEdgeTarget(edge));
				topOrder.put(vertex, Math.max(targetOrder, sourceOrder+1));
			} else {
				backEdges.add(edge);
			}
		}
	}
	
	public TopOrder(DirectedGraph<V,E> graph,V startVertex) throws BadFlowGraphException {
		this.graph = graph;
		this.startVertex = startVertex;
		analyse();
	}
	private void analyse() throws BadFlowGraphException {
		backEdges = new Vector<E>();
		topOrder = new Hashtable<V, Integer>();
		dfsOrder = new Vector<V>();
		BackEdgeDetector iter = new BackEdgeDetector(graph,startVertex);
		while(iter.hasNext()) iter.next();
		this.dominators = new Dominators<V,E>(this.graph,dfsOrder);
		checkReducible();
	}
	/**
	 * An edge B->A is a back-edge if A dominates B w.r.t. <code>Entry</code>.
	 * @return the back-edges of this graph
	 */
	public Vector<E> getBackEdges() {
		return backEdges; 
	}
	/**
	 * Get a topological order <code>TOPO</code> mapping nodes to integers.
	 * If you remove all back-edges in the graph, <code>TOPO(n) &lt; TOPO(m)</code> if there
	 * is a path from n to m.
	 * @return
	 */
	public Hashtable<V,Integer> getTopOrder() {
		return topOrder;
	}
	/**
	 * Get a DFS traversal of the graph
	 * @return
	 */
	public Vector<V> getDfsOrder() {
		return dfsOrder;
	}
	/**
	 * @return the {@link Dominators} of the graph
	 */
	public Dominators getDominators() {
		return this.dominators;
	}
	
	/* Reducability condition: For every backedge (n,h), h dominates n
	 * @throws Exception if the graph isn't reducible
	 */
	private void checkReducible() throws BadFlowGraphException {
		for(E backEdge : getBackEdges()) {
			V n   = graph.getEdgeSource(backEdge);
			V hol = graph.getEdgeTarget(backEdge);
			if(! dominators.dominates(hol,n)) {
				throw new BadFlowGraphException(hol+" should dominate "+ n);
			}
		}
	}
	/**
	 * Check wheter the given graph is connected <code>|weakly connected components| = 1</code>
	 * @param graph
	 * @throws BadFlowGraphException
	 */
	public static void checkConnected(DirectedGraph<FlowGraphNode, FlowGraphEdge> graph) 
		throws BadFlowGraphException {
		int comps = new ConnectivityInspector<FlowGraphNode, FlowGraphEdge>(graph).connectedSets().size(); 
		if(comps != 1) {
			throw new BadFlowGraphException("Expected graph with one component, but the given one has "+comps);
		}
	}

}
