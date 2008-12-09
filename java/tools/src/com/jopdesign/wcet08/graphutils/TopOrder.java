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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.BellmanFordShortestPath;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.DirectedSubgraph;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgrapht.traverse.TopologicalOrderIterator;

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
	public static class BadGraphException extends Exception {
		private static final long serialVersionUID = 1L;
		public BadGraphException(String reason) { super(reason); }
	}
	private DirectedGraph<V, E> graph;
	private Vector<V> dfsOrder = null;
	private Vector<E> backEdges = null;
	private V startVertex;
	private Dominators<V, E> dominators;
	private Vector<V> topTraversal;

	/* Iterator detecting back edges using DFS search.
	 * This works for reducible graphs only.
	 */
	private class BackEdgeDetector extends DepthFirstIterator<V,E> {
		public BackEdgeDetector(DirectedGraph<V, E> g, V startVertex) {
			super(g,startVertex);
		}
		// Topological order
		@Override
		protected void encounterVertex(V vertex, E edge) {
			super.encounterVertex(vertex,edge);
			dfsOrder.add(vertex);
		}
		@Override
		protected void encounterVertexAgain(V vertex, E edge) {
			super.encounterVertexAgain(vertex,edge);
			if(getSeenData(vertex) != VisitColor.GRAY) {
			} else {
				backEdges.add(edge);
			}
		}
	}
	
	public TopOrder(DirectedGraph<V,E> graph,V startVertex) throws BadGraphException {
		this.graph = graph;
		this.startVertex = startVertex;
		analyse(false);
	}
	public TopOrder(DirectedGraph<V,E> graph,V startVertex, boolean isAcyclic) throws BadGraphException {
		this.graph = graph;
		this.startVertex = startVertex;
		analyse(isAcyclic);
	}
	private void analyse(boolean isAcyclic) throws BadGraphException {
		backEdges = new Vector<E>();
		dfsOrder = new Vector<V>();
		BackEdgeDetector iter = new BackEdgeDetector(graph,startVertex);
		while(iter.hasNext()) iter.next();
		if(isAcyclic && ! backEdges.isEmpty()) {
			E e1 = backEdges.firstElement();
			List<E> cycle = BellmanFordShortestPath.findPathBetween(graph, graph.getEdgeTarget(e1), graph.getEdgeSource(e1));			
			throw new BadGraphException("Expected acyclic graph, but found cycle: "+cycle);
		}
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
	 * Return a traversal of the graph in topological order of the corresponding back-edge
	 * free graph.
	 * @return The traversal as list of nodes
	 */
	public Vector<V> getTopologicalTraversal() {
		if(topTraversal != null) return topTraversal;
		topTraversal = new Vector<V>();
		Set<E> edgeSet = new HashSet<E>(graph.edgeSet());
		for(E backEdge : this.getBackEdges()) {
			edgeSet.remove(backEdge);			
		}
		DirectedSubgraph<V, E> subgraph = 
			new DirectedSubgraph<V,E>(graph,graph.vertexSet(),edgeSet);
		TopologicalOrderIterator<V, E> iter = 
			new TopologicalOrderIterator<V, E>((DirectedGraph<V, E>) subgraph);
		while(iter.hasNext()) {
			topTraversal.add(iter.next());
		}
		return topTraversal;
	}
	/**
	 * Get a DFS traversal of the graph
	 * @return
	 */
	public Vector<V> getDFSTraversal() {
		return dfsOrder;
	}

	/**
	 * @return the {@link Dominators} of the graph
	 */
	public Dominators<V,E> getDominators() {
		return this.dominators;
	}
	
	/* Reducability condition: For every backedge (n,h), h dominates n
	 * @throws Exception if the graph isn't reducible
	 */
	private void checkReducible() throws BadGraphException {
		for(E backEdge : getBackEdges()) {
			V n   = graph.getEdgeSource(backEdge);
			V hol = graph.getEdgeTarget(backEdge);
			if(! dominators.dominates(hol,n)) {
				throw new BadGraphException(hol+" should dominate "+ n);
			}
		}
	}
	
	/**
	 * Check wheter the given graph is (weakly) connected.
	 * <p> This is the case if
	 *  <code>|weakly connected components| = 1</code>
	 * </p>
	 * @param graph
	 * @throws BadGraphException if the graph is empty, or there is more than one weakly connected component
	 */
	public static <V,E> void checkConnected(DirectedGraph<V,E> graph) 
		throws BadGraphException {
		List<Set<V>> comps = new ConnectivityInspector<V,E>(graph).connectedSets(); 
		if(comps.size() != 1) {
			throw new BadGraphException("Expected graph with one component, but the given one has "+comps);
		}
	}
	/**
	 * Find nodes which aren't reachable from <code>entry</code>.
	 * <p> A nodes is unreachable with respect to <code>entry</code>, if there is no path
	 * from <code>entry</code> to <code>n</code>.
	 * </p>
	 * @param graph the given graph
	 * @param entry the entry node
	 * @return a list of unreachable nodes
	 */
	public static <V,E> List<V> findDeadNodes(DirectedGraph<V,E> graph, V entry) {
		/* CAVEAT: Do not use ConnectivityInspector; it consides graphs as undirected */
		BellmanFordShortestPath<V, E> bfsp = new BellmanFordShortestPath<V, E>(graph,entry);
		Vector<V> deads = new Vector<V>();
		for(V node : graph.vertexSet()) {
			if(node == entry) continue;
			if(null == bfsp.getPathEdgeList(node)) {
				deads .add(node);
			}
		}
		return deads;
	}
	/**
	 * Check that the given node is an exit node of the given flow graph.
	 * <p>
	 * That is, for all nodes n, there has to be a path from entry n to exit.
	 * </p>
	 * @param graph the flow graph
	 * @param exit the dedicated "exit" node
	 * @throws BadGraphException if there is a node n which has isn't connected to exit 
	 */
	public static <V,E> void checkIsExitNode(DirectedGraph<V,E> graph, V exit) throws BadGraphException {
		ConnectivityInspector<V, E> ci = new ConnectivityInspector<V,E>(graph);
		for(V node : graph.vertexSet()) {
			if(node != exit && ! ci.pathExists(node, exit)) {
				throw new BadGraphException("checkIsExitNode: There is no path from "+node+" to exit");				
			}
		}
	}
}
