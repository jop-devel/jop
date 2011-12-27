/*
 * This file is part of JOP, the Java Optimized Processor
 *   see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2008-2011, Benedikt Huber (benedikt.huber@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jopdesign.common.graphutils;

import org.jgrapht.DirectedGraph;
import org.jgrapht.VertexFactory;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedSubgraph;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.traverse.AbstractGraphIterator;
import org.jgrapht.traverse.TopologicalOrderIterator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.Vector;


/**
 * Compute the loop coloring of a graph, i.e. for each node
 * the set of loops (identified by targets of back-edges) it
 * belongs to. Based on {@link TopOrder} and {@link Dominators}.
 *
 * @param <V> node type
 * @param <E> edge type
 */
public class LoopColoring<V, E> {

    private enum SimpleVisitColor {
        WHITE, GREY
    }

    private class LoopColorIterator extends AbstractGraphIterator<V, E> {
        private Stack<V> stack;
        private V hol;
        private Map<V, SimpleVisitColor> visited;

        LoopColorIterator(V headOfLoop, Collection<E> backEdges) {
            this.hol = headOfLoop;
            this.stack = new Stack<V>();
            for (E edge : backEdges) {
                stack.push(graph.getEdgeSource(edge));
            }
            this.visited = new HashMap<V, SimpleVisitColor>();
        }

        public boolean hasNext() {
            return !stack.empty();
        }

        public V next() {
            V nextV = stack.pop();
            visited.put(nextV, SimpleVisitColor.GREY);
            // push all non-visited predecessors on the stack, if the node != hol
            if (!nextV.equals(hol)) {
                for (E preEdge : graph.incomingEdgesOf(nextV)) {
                    V pre = graph.getEdgeSource(preEdge);
                    if (visited.containsKey(pre)) continue;
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
    private Map<V, List<E>> backEdgesByHOL;
    private Map<V, List<E>> exitEdges;
    private SimpleDirectedGraph<V, DefaultEdge> loopNestForest;
    private Map<E, IterationBranchLabel<V>> iterationBranchEdges;
    private Set<E> backEdges;

    public LoopColoring(DirectedGraph<V, E> graph, TopOrder<V, E> topOrder, V exit) {
        this.graph = graph;
        this.topOrder = topOrder;
    }

    public Map<V, Set<V>> getLoopColors() {
        if (loopColors == null) analyse();
        return loopColors;
    }

    private void analyse() {
        loopColors = new HashMap<V, Set<V>>();
        for (V v : graph.vertexSet()) loopColors.put(v, new TreeSet<V>());
        /* Step 1: Group backedges by Head-Of-Loop */
        backEdgesByHOL = new HashMap<V, List<E>>();
        for (E backedge : this.topOrder.getBackEdges()) {
            V hol = graph.getEdgeTarget(backedge);
            List<E> endVxs = backEdgesByHOL.get(hol);
            if (endVxs == null) endVxs = new ArrayList<E>();
            endVxs.add(backedge);
            backEdgesByHOL.put(hol, endVxs);
        }
        /* Step 2: For every (hol,endVertices) pair, perform a DFS starting at endVertices,
                  * on the RCFG with all outgoing edges of hol removed. For this purpose, we provide
                  * a special iterator.
                  */
        for (Entry<V, List<E>> loop : backEdgesByHOL.entrySet()) {
            V hol = loop.getKey();
            LoopColorIterator iter = new LoopColorIterator(hol, loop.getValue());
            while (iter.hasNext()) {
                loopColors.get(iter.next()).add(hol);
            }
        }
        computeExitEdges();
    }

    private void computeExitEdges() {
        exitEdges = new HashMap<V, List<E>>();
        /* For each edge, compute the set difference of source color and target color */
        for (E e : graph.edgeSet()) {
            V src = graph.getEdgeSource(e);
            V target = graph.getEdgeTarget(e);
            Set<V> exitSet = new HashSet<V>(this.loopColors.get(src));
            exitSet.removeAll(this.loopColors.get(target));
            for (V loop : exitSet) {
                List<E> exits = this.exitEdges.get(loop);
                if (exits == null) {
                    exits = new ArrayList<E>();
                    this.exitEdges.put(loop, exits);
                }
                exits.add(e);
            }
        }
    }

    /**
     * A loop-nest DAG has an edge from loop A to loop B, if B is an inner loop of A.
     *
     * @return The loop-nest DAG of the graph
     */
    public SimpleDirectedGraph<V, DefaultEdge> getLoopNestDAG() {
        if (loopNestForest != null) return loopNestForest;
        analyse();
        loopNestForest =
                new SimpleDirectedGraph<V, DefaultEdge>(DefaultEdge.class);
        for (V hol : backEdgesByHOL.keySet()) {
            loopNestForest.addVertex(hol);
        }
        for (V hol : backEdgesByHOL.keySet()) {
            Set<V> outerLoops = this.loopColors.get(hol);
            for (V outerLoop : outerLoops) {
                if (outerLoop.equals(hol)) continue;
                loopNestForest.addEdge(outerLoop, hol);
            }
        }
        return loopNestForest;
    }

	/** 
	 * @param hol the loop of which to compute the ancestor
	 * @param dist the distance n
	 * @return the n-th outer loop
	 * <p>Given all predecessors of the loop hol in the loop nest forest, we want the one
	 * where {@code |successors \cap loopcolor(hol)| = dist}.</p>
	 * <p>Example: Assume hol = loop 4, and the following loop nest DAG<pre>
     *     1->2,3,4,5,6       for(1: )
     *     2->3,4,5,6           for(2: )
     *     3->4,5,6               for(3: )
     *     4->5                     for(4: )
     *                                for(5: )
     *     colors(4) = {1,2,4,5}    for(6:)
     * </pre>
     * For 3, we have {@code |{4,5,6} \cap {1,2,3,4}| = 1}, so 3 is the ancestor with distance 1
     * For 1, we have {@code |{2,3,4,5,6} \cap {1,2,3,4}| = 3}, so 1 is the ancestor with distance 3 */
    public V getLoopAncestor(V hol, int dist) {
        if(dist == 0) return hol;
        V ancestor = null;
        Set<V> holColors = loopColors.get(hol);
        SimpleDirectedGraph<V, DefaultEdge> loopNestForest = getLoopNestDAG();
        for(DefaultEdge incoming : loopNestForest.incomingEdgesOf(hol)) {
        	V pred = loopNestForest.getEdgeSource(incoming);
        	loopNestForest.outgoingEdgesOf(pred);
        	int intersectSize = 0;
        	for(DefaultEdge predOutgoing : loopNestForest.outgoingEdgesOf(pred)) {
        		V predSucc = loopNestForest.getEdgeTarget(predOutgoing);
        		if(holColors.contains(predSucc)) intersectSize+=1;
        	}
        	if(intersectSize == dist) {
        		if(ancestor != null) {
        			throw new AssertionError("malformed loop nest DAG: more than one direct ancestor");
        		} else {
        			ancestor = pred;
        		}
        	}
        }
        return ancestor;
    }

    /**
     * <p>Return a traversal of the graph in 'flow' order, a topological order
     * where back edges and the loop header are treated in a special way.</p>
     * </p>
     *
     * @return The traversal as list of nodes
     */
    public List<V> getFlowTraversal() {
        List<V> flowTraversal = new ArrayList<V>();
        TopologicalOrderIterator<V, DefaultEdge> iter =
                new TopologicalOrderIterator<V, DefaultEdge>(getFlowTraversalGraph());
        while (iter.hasNext()) {
            flowTraversal.add(iter.next());
        }
        return flowTraversal;
    }

    /**
     * topological order, where back edges are replaced by exit loop edg
     *
     * @return
     */
    public DirectedGraph<V, DefaultEdge> getFlowTraversalGraph() {
        DirectedGraph<V, DefaultEdge> travGraph = new DefaultDirectedGraph<V, DefaultEdge>(DefaultEdge.class);
        for (V v : graph.vertexSet()) {
            travGraph.addVertex(v);
        }
        for (E e : graph.edgeSet()) {
            V src = graph.getEdgeSource(e);
            V target = graph.getEdgeTarget(e);
            if (isBackEdge(e)) {
                for (E exitEdge : getExitEdgesOf(target)) {
                    V newTarget = graph.getEdgeTarget(exitEdge);
                    travGraph.addEdge(src, newTarget);
                }
            } else {
                travGraph.addEdge(src, target);
            }
        }
        return travGraph;
    }

    /**
     * A node is an Head of loop, if it is the target of a back-edge
     *
     * @return
     */
    public Set<V> getHeadOfLoops() {
        if (backEdgesByHOL == null) analyse();
        return backEdgesByHOL.keySet();
    }

    /**
     * Get nodes belonging to some head of loop
     * <p>Complexity: {@code O(|V|) * (map-get + set-member)}</p>
     *
     * @param hol the head of loop
     * @return a set of nodes belonging to that loop
     */
    public Set<V> getNodesOfLoop(V hol) {
        Set<V> nodes = new HashSet<V>();
        for (V node : graph.vertexSet()) {
            Set<V> col = getLoopColor(node);
            if (col != null && col.contains(hol)) nodes.add(node);
        }
        return nodes;
    }

    /**
     * Get set of back edges
     *
     * @return
     */
    public Set<E> getBackEdges() {
        if (backEdges != null) return backEdges;
        backEdges = new HashSet<E>();
        if (backEdgesByHOL == null) analyse();
        for (List<E> edge : backEdgesByHOL.values()) {
            backEdges.addAll(edge);
        }
        return backEdges;
    }

    /**
     * Get source of back edges, grouped by head of loop
     *
     * @return a map from head-of-loop nodes to back-edge source vertices.
     */
    public Map<V, List<E>> getBackEdgesByHOL() {
        if (backEdgesByHOL == null) analyse();
        return backEdgesByHOL;
    }

    public List<E> getBackEdgesTo(V hol) {
        return backEdgesByHOL.get(hol);
    }

    /**
     * test whether the given edge is a "back-edge"
     *
     * @param edge the edge to test
     * @return
     */
    public boolean isBackEdge(E edge) {
        return getBackEdges().contains(edge);
    }


    /**
     * An edge E is an <i>exit edge</i> of an loop, if the source of E
     * is part of the loop, but its target is not.
     *
     * @return a map from head-of-loop nodes to that loop's exit edges
     */
    public Map<V, List<E>> getExitEdges() {
        if (this.exitEdges == null) analyse();
        return this.exitEdges;
    }

    public Set<V> getLoopEntrySet(E edge) {
        /* no loops */
        if (getHeadOfLoops().isEmpty()) return new HashSet<V>();
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
        if (getHeadOfLoops().isEmpty()) return new HashSet<V>();
        Set<V> setSource = new TreeSet<V>(getLoopColor(graph.getEdgeSource(edge)));
        Set<V> setTarget = getLoopColor(graph.getEdgeTarget(edge));
        setSource.removeAll(setTarget);
        return setSource;
    }

    public Set<V> getLoopColor(V node) {
        if (this.getHeadOfLoops().isEmpty()) return new HashSet<V>();
        Set<V> color = getLoopColors().get(node);
        if (color == null) {
            /* Unreachable Code */
            return new HashSet<V>();
        }
        return color;
    }

    public static class IterationBranchLabel<V> extends Pair<Set<V>, Set<V>> {
        private static final long serialVersionUID = 1L;

        public IterationBranchLabel(Set<V> fst, Set<V> snd) {
            super(fst, snd);
        }

        public Set<V> getContinues() {
            return first();
        }

        public Set<V> getExits() {
            return second();
        }

        @SuppressWarnings({"AccessingNonPublicFieldOfAnotherObject"})
        public void mergeLabel(IterationBranchLabel<V> other) {
            this.first.addAll(other.first);
            this.second.addAll(other.second);
        }

        public boolean isEmpty() {
            return this.first.isEmpty() && this.second.isEmpty();
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
     *
     * @return a map from edges to iteration branch labels
     */
    public Map<E, IterationBranchLabel<V>> getIterationBranchEdges() {
        if (iterationBranchEdges != null) return iterationBranchEdges;
        Set<V> hols = this.getHeadOfLoops();
        iterationBranchEdges = new HashMap<E, IterationBranchLabel<V>>();
        Map<V, IterationBranchLabel<V>> nodeLabels = new HashMap<V, IterationBranchLabel<V>>();
        if (hols.isEmpty()) return iterationBranchEdges;
        List<V> rTopTrav = this.topOrder.getTopologicalTraversal();
        Collections.reverse(rTopTrav);
        for (V source : rTopTrav) {
            /* mark edges */
            if (loopColors.get(source).isEmpty()) continue;
            IterationBranchLabel<V> first = null;
            boolean isIterationBranch = false;
            for (E edge : graph.outgoingEdgesOf(source)) {
                V target = graph.getEdgeTarget(edge);

                Set<V> contLoop = new HashSet<V>();
                if (hols.contains(target)) {
                    if (backEdgesByHOL.get(target).contains(edge)) {
                        contLoop.add(target);
                    }
                }

                Set<V> breakLoops = new HashSet<V>(loopColors.get(source));
                breakLoops.removeAll(loopColors.get(target));

                IterationBranchLabel<V> key = new IterationBranchLabel<V>(contLoop, breakLoops);
                if (nodeLabels.containsKey(target)) {
                    key.mergeLabel(nodeLabels.get(target));
                }
                if (!key.isEmpty()) {
                    iterationBranchEdges.put(edge, key);
                }
                if (first == null) first = key;
                else if (!key.equals(first)) isIterationBranch = true;
            }
            /* TODO: A better implementation would use postdominators */
            if (!isIterationBranch && first != null) {
                nodeLabels.put(source, first);
                for (E edge : graph.outgoingEdgesOf(source)) {
                    iterationBranchEdges.remove(edge);
                }
            }
        }
        return iterationBranchEdges;
    }

    /**
     * return an acyclic subgraph which only contains nodes from the given loop,
     * and no back-edges at all
     *
     * @param hol
     * @return
     */
    public DirectedGraph<V, E> getLinearSubgraph(V hol) {
        Set<V> nodesOfInterest;
        if (hol == null) nodesOfInterest = this.graph.vertexSet();
        else nodesOfInterest = this.getNodesOfLoop(hol);
        Set<E> edgesOfInterest = new HashSet<E>();
        for (E edge : this.graph.edgeSet()) {
            if (this.getBackEdges().contains(edge)) continue;
            V src = graph.getEdgeSource(edge);
            if (!nodesOfInterest.contains(src)) continue;
            V target = graph.getEdgeTarget(edge);
            if (!nodesOfInterest.contains(target)) continue;
            edgesOfInterest.add(edge);
        }
        return new DirectedSubgraph<V, E>(graph, nodesOfInterest, edgesOfInterest);
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
        for (V v : this.graph.vertexSet()) {
            if (this.loopColors.get(v).contains(headOfLoop)) {
                coloredVertices.add(v);
            }
        }
        /* step 2: create a copy of the subgraph, backedges linking to the original HOL */
        Map<V, V> vertexCopies = new HashMap<V, V>();
        Set<V> copiedVertices = new TreeSet<V>();
        for (V loopVertex : coloredVertices) {
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
        for (V loopVertex : coloredVertices) {
            for (E outgoing : graph.outgoingEdgesOf(loopVertex)) {
                V target = graph.getEdgeTarget(outgoing);
                V srcCopy = vertexCopies.get(loopVertex);
                // V -> HOL ==> V' -> HOL
                if (target.equals(headOfLoop)) {
                    graph.addEdge(srcCopy, target);
                }
                // V -> W | W \notin LoopSugraph ==> V' -> W
                else if (!coloredVertices.contains(target)) {
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
        for (E incoming : incomingEdges) {
            V src = graph.getEdgeSource(incoming);
            if (!coloredVertices.contains(src) && !copiedVertices.contains(src)) {
                graph.removeEdge(incoming);
                graph.addEdge(src, headOfLoopCopy);
            }
        }
    }


}

///* headers last */
//DirectedGraph<V,DefaultEdge> travGraph = new DefaultDirectedGraph<V,DefaultEdge>(DefaultEdge.class);
//for(V v : graph.vertexSet()) { 
//	travGraph.addVertex(v);			
//}
//Set<V> hols = this.getHeadOfLoops();
//for(E e: graph.edgeSet()) {
//	V src = graph.getEdgeSource(e);
//	V target = graph.getEdgeTarget(e);
//	if(hols.contains(src)) {
//		V hol = src;
//		if(! getLoopColor(target).contains(hol)) {
//			travGraph.addEdge(src,target);
//			continue;
//		}
//		/* Special case: src is hol, target is part of loop */
//		Stack<V> todo = new Stack<V>();
//		Vector<V> newPreds = new Vector<V>();
//		todo.add(hol);
//		while(! todo.empty()) {
//			V pred = todo.pop();
//			if(hols.contains(pred)) {
//				for(E toHOL : graph.incomingEdgesOf(pred)) {
//					V predPred = graph.getEdgeSource(toHOL);
//					if(getLoopColor(predPred).contains(pred)) continue;
//					todo.push(predPred);
//				}
//			} else {
//				newPreds.add(pred); /* not a hol */
//			}
//		}
//		for(V newPred : newPreds) {
//			travGraph.addEdge(newPred,target);
//		}
//	} else {
//		travGraph.addEdge(src,target);
//	}
//}
//for(V hol : hols) {
//	for(E exitEdge : this.exitEdges.get(hol)) {
//		V exit = graph.getEdgeTarget(exitEdge);
//		travGraph.addEdge(hol, exit);
//	}
//}
//return travGraph;

