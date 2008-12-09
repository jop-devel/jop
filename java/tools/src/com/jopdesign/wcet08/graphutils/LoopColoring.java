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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.Vector;
import java.util.Map.Entry;

import org.jgrapht.DirectedGraph;
import org.jgrapht.VertexFactory;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.EdgeReversedGraph;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.traverse.AbstractGraphIterator;

import com.jopdesign.wcet08.frontend.ControlFlowGraph.CFGNode;

/**
 * Compute the loop coloring of a graph, i.e. for each node
 * the set of loops (identifie by targets of back-edges) it 
 * belongs to. Based on {@link TopOrder} and {@link Dominators}.
 *
 * @param <V> node type
 * @param <E> edge type
 */
public class LoopColoring<V,E> {
	private enum SimpleVisitColor { WHITE,GREY };
	private class LoopColorIterator extends AbstractGraphIterator<V,E> {
		private Stack<V> stack;
		private V hol;
		private Map<V,SimpleVisitColor> visited;
		LoopColorIterator(V headOfLoop, Collection<E> backEdges) {
			this.hol = headOfLoop;
			this.stack = new Stack<V>();
			for(E edge: backEdges) {
				stack.push(graph.getEdgeSource(edge));
			}
			this.visited = new Hashtable<V, SimpleVisitColor>();
		}
		public boolean hasNext() {
			return ! stack.empty();
		}
		public V next() {
			V nextV = stack.pop();
			visited.put(nextV, SimpleVisitColor.GREY);
			// push all non-visited predecessors on the stack, if the node != hol
			if(! nextV.equals(hol)) {
				for(E preEdge : graph.incomingEdgesOf(nextV)) {
					V pre = graph.getEdgeSource(preEdge);
					if(visited.containsKey(pre)) continue;					
					stack.push(pre);
					visited.put(pre, SimpleVisitColor.WHITE);
				}
			}
			return nextV;
		}
	}
	
	private DirectedGraph<V, E> graph;
	private TopOrder<V, E> topOrder;
	private Map<V, Set<V>> loopColors;
	private Map<V, Vector<E>> backEdgesByHOL;
	private Map<V, List<E>> exitEdges;
	private SimpleDirectedGraph<V, DefaultEdge> loopNestForest;
	private Map<E,IterationBranchLabel<V>> iterationBranchEdges;
	private Set<E> backEdges;
	private TopOrder<V, E> rTopOrder;
	private Dominators<V, E> rDoms;

	public LoopColoring(DirectedGraph<V,E> graph, TopOrder<V,E> topOrder, V exit) {
		this.graph = graph;
		this.topOrder = topOrder;
		this.rDoms = new Dominators<V, E>(new EdgeReversedGraph<V, E>(graph),exit);
	}

	public Map<V,Set<V>> getLoopColors() {
		if(loopColors == null) analyse();
		return loopColors;
	}
	private void analyse() {
		loopColors = new Hashtable<V, Set<V>>();
		for(V v : graph.vertexSet()) loopColors.put(v, new TreeSet<V>());
		/* Step 1: Group backedges by Head-Of-Loop */
		backEdgesByHOL = new Hashtable<V,Vector<E>>();
		for(E backedge : this.topOrder.getBackEdges()) {
			V hol = graph.getEdgeTarget(backedge);
			Vector<E> endVxs = backEdgesByHOL.get(hol);
			if(null == endVxs) endVxs = new Vector<E>();
			endVxs.add(backedge);
			backEdgesByHOL.put(hol, endVxs);
		}
		/* Step 2: For every (hol,endVertices) pair, perform a DFS starting at endVertices,
		 * on the RCFG with all outgoing edges of hol removed. For this purpose, we provide
		 * a special iterator.
		 */
		for(Entry<V, Vector<E>> loop : backEdgesByHOL.entrySet()) {
			V hol = loop.getKey();
			LoopColorIterator iter = new LoopColorIterator(hol, loop.getValue());
			while(iter.hasNext()) {
				loopColors.get(iter.next()).add(hol);
			}
		}
		computeExitEdges();
	}
	private void computeExitEdges() {
		exitEdges = new Hashtable<V, List<E>>();
		/* For each edge, compute the set difference of source color and target color */
		for(E e : graph.edgeSet()) {
			V src = graph.getEdgeSource(e);
			V target = graph.getEdgeTarget(e);
			Set<V> exitSet = new HashSet<V>(this.loopColors.get(src));
			exitSet.removeAll(this.loopColors.get(target));
			for(V loop : exitSet) {
				List<E> exits = this.exitEdges.get(loop);
				if(exits == null) {
					exits = new Vector<E>();
					this.exitEdges.put(loop, exits);
				}
				exits.add(e);
			}
		}
	}
	/**
	 * A loop-nest forest has an edge from loop A to loop B, if B is an inner loop of A.
	 * @return The loop-nest forest of the graph
	 */
	public SimpleDirectedGraph<V,DefaultEdge> getLoopNestForest() {
		if(loopNestForest != null) return loopNestForest;
		analyse();
		loopNestForest = 
			new SimpleDirectedGraph<V, DefaultEdge>(DefaultEdge.class);
		for(V hol : backEdgesByHOL.keySet()) { loopNestForest.addVertex(hol); }
		for(V hol : backEdgesByHOL.keySet()) {
			Set<V> outerLoops = this.loopColors.get(hol);
			for(V outerLoop : outerLoops) {
				if(outerLoop.equals(hol)) continue;
				loopNestForest.addEdge(outerLoop, hol);
			}
		}
		return loopNestForest;
	}
	
	/**
	 * A node is an Head of loop, if it is the target of a back-edge
     */
	public Set<V> getHeadOfLoops() {
		if(backEdgesByHOL == null) analyse();
		return backEdgesByHOL.keySet();		
	}
	/**
	 * Get set of back edges
	 */
	public Set<E> getBackEdges() {
		if(backEdges != null) return backEdges;
		backEdges = new HashSet<E>();
		if(backEdgesByHOL == null) analyse();
		for(Vector<E> edge : backEdgesByHOL.values()) {
			backEdges.addAll(edge);
		}
		return backEdges;
	}
	/**
	 * Get source of back edges, grouped by head of loop
	 * @return a map from head-of-loop nodes to back-edge source vertices.
	 */
	public Map<V, Vector<E>> getBackEdgesByHOL() {
		if(backEdgesByHOL == null) analyse();
		return backEdgesByHOL;
	}
	public Vector<E> getBackEdgesTo(CFGNode hol) {
		return backEdgesByHOL.get(hol);
	}
	/**
	 * test whether the given edge is a "back-edge"
	 * @param edge the edge to test
	 * @return
	 */
	public boolean isBackEdge(E edge) {
		return getBackEdges().contains(edge);
	}
	

	/**
	 * An edge E is an <i>exit edge</i> of an loop, if the source of E
	 * is part of the loop, but its target is not.
	 * @return a map from head-of-loop nodes to that loop's exit edges
	 */
	public Map<V,List<E>> getExitEdges() {
		if(this.exitEdges == null) analyse();
		return this.exitEdges;
	}
	
	public Set<V> getLoopEntrySet(E edge) {
		/* no loops */
		if(getHeadOfLoops().isEmpty()) return new HashSet<V>();		
		Set<V> setSource = getLoopColor(graph.getEdgeSource(edge));
		Set<V> setTarget = new TreeSet<V>(getLoopColor(graph.getEdgeTarget(edge)));
		setTarget.removeAll(setSource);
		return setTarget;
	}
	public Collection<E> getExitEdgesOf(V hol) {
		return getExitEdges().get(hol);
	}
	public Set<V> getLoopExitSet(E edge) {
		/* no loops */
		if(getHeadOfLoops().isEmpty()) return new HashSet<V>();
		Set<V> setSource = new TreeSet<V>(getLoopColor(graph.getEdgeSource(edge)));
		Set<V> setTarget = getLoopColor(graph.getEdgeTarget(edge));
		setSource.removeAll(setTarget);
		return setSource;
	}
	public Set<V> getLoopColor(V node) {
		if(this.getHeadOfLoops().isEmpty()) new HashSet<V>();
		return getLoopColors().get(node);
	}

	public static class IterationBranchLabel<V> extends Pair<Set<V>,Set<V>>{
		private static final long serialVersionUID = 1L;
		public IterationBranchLabel(Set<V> fst, Set<V> snd) {
			super(fst, snd);
		}
		public Set<V> getContinues() { return fst(); }
		public Set<V> getExits() { return snd(); }
		public void mergeLabel(IterationBranchLabel<V> other) {
			this.fst.addAll(other.fst);
			this.snd.addAll(other.snd);
		}
		public boolean isEmpty() {
			return this.fst.isEmpty() && this.snd.isEmpty();
		}
	}
	/**
	 * Return a map from edges to iteration branch labels <code>(continue n, break N)</code>
	 * <p>
	 * Iteration branches are nodes s.t. following different edges leads to different
	 * looping behavior.
	 * An edge is marked as <code>continue n</code> when following this edge inevitably leads
	 * to another iteration of loop <code>n</code>.
	 * An edges is marked as <code>break n</code> when following this edge inevitably leads to
	 * leaving loop <code>n</code>
	 * </p>
	 * <p>Nodes are traversed in reverse topological order of the graph without backedges</p>
	 * @return a map from edges to iteration branch labels
	 */
	public Map<E,IterationBranchLabel<V>> getIterationBranchEdges() {
		if(iterationBranchEdges != null) return iterationBranchEdges;
		Set<V> hols = this.getHeadOfLoops();
		iterationBranchEdges = new HashMap<E,IterationBranchLabel<V>>();
		Map<V, IterationBranchLabel<V>> nodeLabels = new HashMap<V,IterationBranchLabel<V>>();
		if(hols.isEmpty()) return iterationBranchEdges;
		Vector<V> rTopTrav = this.topOrder.getTopologicalTraversal();
		Collections.reverse(rTopTrav);
		for(V source : rTopTrav) {
			/* mark edges */
			if(loopColors.get(source).isEmpty()) continue;
			IterationBranchLabel<V> first = null;
			boolean isIterationBranch = false;
			for(E edge : graph.outgoingEdgesOf(source)) {
				V target = graph.getEdgeTarget(edge);

				Set<V> contLoop   = new HashSet<V>();
				if(hols.contains(target)) {
					if(backEdgesByHOL.get(target).contains(edge)) {
						contLoop.add(target);
					}
				}
				
				Set<V> breakLoops = new HashSet<V>(loopColors.get(source));
				breakLoops.removeAll(loopColors.get(target));				
				
				IterationBranchLabel<V> key = new IterationBranchLabel<V>(contLoop,breakLoops);
				if(nodeLabels.containsKey(target)) { key.mergeLabel(nodeLabels.get(target)); }
				if(! key.isEmpty()) {
					iterationBranchEdges.put(edge, key);
				}
				if(first == null) first = key;
				else if(! key.equals(first)) isIterationBranch = true;
			}
			/* TODO: A better implementation would use postdominators */
			if(! isIterationBranch && first != null) {				
				nodeLabels.put(source, first);
				for(E edge : graph.outgoingEdgesOf(source)) {
					iterationBranchEdges.remove(edge);
				}
			}
		}		
		return iterationBranchEdges;
	}
	
	
	/* EXPERIMENTAL - NOT USED follows */
	/* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
	/*
	 * To unpeel a loop, we create a copy of the subgraph of all colored nodes.
	 * Then we redirect the ingoing edges of the head-of-loop to the copy of head-of-loop,
	 * and the back edges in the copied subgraph to the old head of loop.
	 * 
	 * We need to provide callbacks when copying vertices/edges
	 */
	
	/**
	 * TODO: move unpeel loops
	 * Unpeel the loop given by the headOfLoop vertex
	 * - modifies graph (adds new vertices via factory)
	 * - updates loopColors
	 * - invalidates topOrder,doms, loopNestForest, headOfLoops
	 */
	public void unpeelLoop(V headOfLoop, VertexFactory<V> factory) {
		/* invalidate */
		this.topOrder = null;
		this.loopNestForest = null;
		this.backEdgesByHOL = null;
		/* step 1: find all vertices involved in the loop */
		Set<V> coloredVertices = new TreeSet<V>();
		for(V v : this.graph.vertexSet()) {
			if(this.loopColors.get(v).contains(headOfLoop)) {
				coloredVertices.add(v);
			}
		}
		/* step 2: create a copy of the subgraph, backedges linking to the original HOL */
		Map<V,V> vertexCopies = new Hashtable<V,V>();
		Set<V> copiedVertices = new TreeSet<V>();
		for(V loopVertex : coloredVertices) {
			V copy = factory.createVertex();
			vertexCopies.put(loopVertex, copy);
			copiedVertices.add(copy);
			graph.addVertex(copy);
			// The copied vertex has the same loop color as the old one, except for the
			// given head of loop
			Set<V> loopColorCopy = new TreeSet<V>();
			loopColorCopy.addAll(this.loopColors.get(loopVertex));
			loopColorCopy.remove(headOfLoop);
			loopColors.put(copy, loopColorCopy);
		}
		for(V loopVertex : coloredVertices) {
			for(E outgoing : graph.outgoingEdgesOf(loopVertex)) {
				V target = graph.getEdgeTarget(outgoing);
				V srcCopy = vertexCopies.get(loopVertex);
				// V -> HOL ==> V' -> HOL
				if(target.equals(headOfLoop)) {
					graph.addEdge(srcCopy, target);
				}
				// V -> W | W \notin LoopSugraph ==> V' -> W
				else if(! coloredVertices.contains(target)) {
					graph.addEdge(srcCopy, target);
				}
				// V -> W | W \in LoopSubgraph ==> V' -> W'
				else {
					graph.addEdge(srcCopy, vertexCopies.get(target));
				}
			}
		}
		/* Step 3: Update headOfLoops, loopColors */
		/* Step 4: Redirect ingoing, not backlink edges to HOL to copy-of-HOL */
		V headOfLoopCopy = vertexCopies.get(headOfLoop);
		Vector<E> incomingEdges = new Vector<E>();
		incomingEdges.addAll(graph.incomingEdgesOf(headOfLoop));
		for(E incoming : incomingEdges) {
			V src = graph.getEdgeSource(incoming);
			if(! coloredVertices.contains(src) && ! copiedVertices.contains(src)) {
				graph.removeEdge(incoming);
				graph.addEdge(src, headOfLoopCopy);
			}
		}
	}

}
