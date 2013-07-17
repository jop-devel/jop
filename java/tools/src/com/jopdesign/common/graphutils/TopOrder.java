/*
 * This file is part of JOP, the Java Optimized Processor
 *   see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2008, Benedikt Huber (benedikt.huber@gmail.com)
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

import com.jopdesign.common.misc.BadGraphException;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.BellmanFordShortestPath;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.DirectedSubgraph;
import org.jgrapht.graph.EdgeReversedGraph;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgrapht.traverse.TopologicalOrderIterator;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Given a rooted, directed graph, identify back-edges.
 * A back-edge is an edge <code>B -> A</code> s.t. A dominates B w.r.t to Entry.
 * If removing the back-edges leads to a DAG, compute a topological order using that graph.
 *
 * @param <V> node type
 * @param <E> edge type
 */
public class TopOrder<V, E> {

    private DirectedGraph<V, E> graph;
    private List<V> dfsOrder = null;
    private List<E> backEdges = null;
    private V startVertex;
    private Dominators<V, E> dominators;
    private List<V> topTraversal;

    /* Iterator detecting back edges using DFS search.
         * This works for reducible graphs only.
         */

    private class BackEdgeDetector extends DepthFirstIterator<V, E> {

        public BackEdgeDetector(DirectedGraph<V, E> g, V startVertex) {
            super(g, startVertex);
        }

        // Topological order

        @Override
        protected void encounterVertex(V vertex, E edge) {
            super.encounterVertex(vertex, edge);
            dfsOrder.add(vertex);
        }

        @Override
        protected void encounterVertexAgain(V vertex, E edge) {
            super.encounterVertexAgain(vertex, edge);
            if (getSeenData(vertex) != VisitColor.GRAY) {
            } else {
                backEdges.add(edge);
            }
        }
    }

    public TopOrder(DirectedGraph<V, E> graph, V startVertex) throws BadGraphException {
        this.graph = graph;
        this.startVertex = startVertex;
        analyse(false);
    }

    public TopOrder(DirectedGraph<V, E> graph, V startVertex, boolean isAcyclic) throws BadGraphException {
        this.graph = graph;
        this.startVertex = startVertex;
        analyse(isAcyclic);
    }

    private void analyse(boolean isAcyclic) throws BadGraphException {
        backEdges = new ArrayList<E>();
        dfsOrder = new ArrayList<V>();
        BackEdgeDetector iter = new BackEdgeDetector(graph, startVertex);
        while (iter.hasNext()) iter.next();
        if (isAcyclic && !backEdges.isEmpty()) {
            E e1 = backEdges.get(0);
            List<E> cycle = BellmanFordShortestPath.findPathBetween(graph, graph.getEdgeTarget(e1), graph.getEdgeSource(e1));
            throw new BadGraphException("Expected acyclic graph, but found cycle: " + cycle);
        }
        this.dominators = new Dominators<V, E>(this.graph, dfsOrder);
        checkReducible();
    }

    /**
     * An edge B->A is a back-edge if A dominates B w.r.t. <code>Entry</code>.
     *
     * @return the back-edges of this graph
     */
    public List<E> getBackEdges() {
        return backEdges;
    }

    /**
     * Return a traversal of the graph in topological order of the corresponding back-edge
     * free graph.
     *
     * @return The traversal as list of nodes
     */
    public List<V> getTopologicalTraversal() {
        if (topTraversal != null) return topTraversal;
        topTraversal = new LinkedList<V>();
        Set<E> edgeSet = new LinkedHashSet<E>(graph.edgeSet());
        for (E backEdge : this.getBackEdges()) {
            edgeSet.remove(backEdge);
        }
        DirectedSubgraph<V, E> subgraph =
                new DirectedSubgraph<V, E>(graph, graph.vertexSet(), edgeSet);
        TopologicalOrderIterator<V, E> iter =
                new TopologicalOrderIterator<V, E>((DirectedGraph<V, E>) subgraph);
        while (iter.hasNext()) {
            topTraversal.add(iter.next());
        }
        return topTraversal;
    }

    /**
     * Get a DFS traversal of the graph
     *
     * @return
     */
    public List<V> getDFSTraversal() {
        return dfsOrder;
    }

    /**
     * @return the {@link Dominators} of the graph
     */
    public Dominators<V, E> getDominators() {
        return this.dominators;
    }

    /* Reducability condition: For every backedge (n,h), h dominates n
         * @throws Exception if the graph isn't reducible
         */

    private void checkReducible() throws BadGraphException {
        for (E backEdge : getBackEdges()) {
            V n = graph.getEdgeSource(backEdge);
            V hol = graph.getEdgeTarget(backEdge);
            if (!dominators.dominates(hol, n)) {
                throw new BadGraphException(hol + " should dominate " + n);
            }
        }
    }

    /**
     * Get the connected components of the graph
     *
     * @param graph
     * @return
     */
    public static <V, E> List<Set<V>> getComponents(DirectedGraph<V, E> graph) {
        return new ConnectivityInspector<V, E>(graph).connectedSets();
    }

    /**
     * Check wheter the given graph is (weakly) connected.
     * <p> This is the case if
     * <code>|weakly connected components| = 1</code>
     * </p>
     *
     * @param graph
     * @throws BadGraphException if the graph is empty, or there is more than one weakly connected component
     */
    public static <V, E> void checkConnected(DirectedGraph<V, E> graph)
            throws BadGraphException {
        List<Set<V>> comps = getComponents(graph);
        if (comps.size() != 1) {
            throw new BadGraphException("Expected graph with one component, but the given one has " + comps);
        }
    }

    /**
     * Find nodes which aren't reachable from <code>entry</code>.
     * <p> A nodes is unreachable with respect to <code>entry</code>, if there is no path
     * from <code>entry</code> to <code>n</code>.
     * </p>
     *
     * @param graph the given graph
     * @param entry the entry node
     * @return a list of unreachable nodes
     */
    public static <V, E> Set<V> findDeadNodes(DirectedGraph<V, E> graph, V entry) {
        /* CAVEAT: Do not use ConnectivityInspector; it considers graphs as undirected */
        BellmanFordShortestPath<V, E> bfsp = new BellmanFordShortestPath<V, E>(graph, entry);
        Set<V> deads = new LinkedHashSet<V>();
        for (V node : graph.vertexSet()) {
            if (node == entry) continue;
            if (bfsp.getPathEdgeList(node) == null) {
                deads.add(node);
            }
        }
        return deads;
    }

    /**
     * Find nodes which which have no path to exit.
     *
     * @param graph the given graph
     * @param exit  the exit node
     * @return a list of stuck nodes
     */
    public static <V, E> Set<V> findStuckNodes(DirectedGraph<V, E> graph, V exit) {
        /* CAVEAT: Do not use ConnectivityInspector; it considers graphs as undirected */
        BellmanFordShortestPath<V, E> bfspRev = new BellmanFordShortestPath<V, E>(
                new EdgeReversedGraph<V, E>(graph),
                exit);
        Set<V> stucks = new LinkedHashSet<V>();
        for (V node : graph.vertexSet()) {
            if (node == exit) continue;
            if (bfspRev.getPathEdgeList(node) == null) {
                stucks.add(node);
            }
        }
        return stucks;
    }

    /**
     * Check that the given graph is a flowgraph:
     * <ul>
     * <li/> There is a path from the entry to every nodes, and entry dominates all nodes
     * <li/> There is a path from every node to exit, and exit postdominates all nodes
     * </ul>
     *
     * @param graph the graph to check
     * @param entry the entry node
     * @param exit  the exit node
     * @throws BadGraphException if the graph is not a flow graph
     */
    public static <V, E> void checkIsFlowGraph(DirectedGraph<V, E> graph, V entry, V exit)
            throws BadGraphException {
        Set<V> deads = findDeadNodes(graph, entry);
        Set<V> stucks = findStuckNodes(graph, exit);
        if (!deads.isEmpty()) {
            throw new BadGraphException("checkIsFlowGraph: There is no path from entry to " + deads);
        }
        if (!stucks.isEmpty()) {
            throw new BadGraphException("checkIsFlowGraph: There is no path to exit from " + stucks);
        }

        Dominators<V, E> doms = new Dominators<V, E>(graph, entry);
        Dominators<V, E> rdoms = new Dominators<V, E>(new EdgeReversedGraph<V, E>(graph), exit);
        for (V node : graph.vertexSet()) {
            if (!doms.dominates(entry, node)) {
                throw new BadGraphException("checkIsFlowGraph: Entry does not dominate " + node);
            }
            if (!rdoms.dominates(exit, node)) {
                throw new BadGraphException("checkIsFlowGraph: Exit does not postdominate " + node);
            }
        }
    }
}
