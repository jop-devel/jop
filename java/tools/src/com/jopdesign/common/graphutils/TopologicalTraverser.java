/*
 * This file is part of JOP, the Java Optimized Processor
 *   see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2011, Stefan Hepp (stefan@stefant.org).
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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class TopologicalTraverser<V,E> {

    private final DirectedGraph<V, E> graph;
    private final NodeVisitor<V> visitor;
    private final Queue<V> queue;

    private Map<V,Integer> indegreeMap;
    private Set<V> marked;

    public TopologicalTraverser(DirectedGraph<V,E> graph, NodeVisitor<V> visitor) {
        this(graph, visitor, new LinkedList<V>());
    }

    public TopologicalTraverser(DirectedGraph<V,E> graph, NodeVisitor<V> visitor, Queue<V> queue) {
        this.graph = graph;
        this.visitor = visitor;
        this.queue = queue;
        initialize();
    }

    public DirectedGraph<V, E> getGraph() {
        return graph;
    }

    public NodeVisitor<V> getVisitor() {
        return visitor;
    }

    public void traverse() {
        // find roots
        List<V> roots = new LinkedList<V>();
        for (V node : graph.vertexSet()) {
            if (isRoot(node)) {
                roots.add(node);
            }
        }

        traverse(roots);
    }

    public void traverse(Collection<V> roots) {

        for (V node : roots) {
            queue.add(node);
        }

        // traverse the graph by removing edges, queue nodes which become new nodes
        while (!queue.isEmpty()) {
            V node  = queue.remove();

            boolean down = visitor.visitNode(node);

            for (E edge : graph.outgoingEdgesOf(node)) {
                V target = graph.getEdgeTarget(edge);
                removeEdge(edge, target);

                if (isRoot(target)) {
                    // check if there was at least one parent which wanted to go down
                    if (down || isMarked(target)) {
                        queue.add(target);
                    }
                } else if (down) {
                    mark(target);
                }
            }

        }

    }


    protected void initialize() {
        indegreeMap = new LinkedHashMap<V, Integer>();
        marked = new LinkedHashSet<V>();
    }

    public boolean isRoot(V node) {
        Integer val = indegreeMap.get(node);
        return (val != null && val == 0) || (val == null && graph.inDegreeOf(node) == 0);
    }

    protected void removeEdge(E edge, V target) {
        Integer val = indegreeMap.get(target);
        if (val != null) {
            indegreeMap.put(target, val-1);
        } else {
            indegreeMap.put(target, graph.inDegreeOf(target)-1);
        }
    }

    protected void mark(V node) {
        // TODO maybe not the nicest method, but it works..
        marked.add(node);
    }

    protected boolean isMarked(V node) {
        return marked.contains(node);
    }

}
