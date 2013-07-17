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

import com.jopdesign.common.graphutils.DFSTraverser.DFSEdgeType;
import com.jopdesign.common.graphutils.DFSTraverser.EmptyDFSVisitor;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.SimpleDirectedGraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Find back edges for a graph and create a DAG from a directed graph.
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class BackEdgeFinder<V,E> {

    private class BackEdgeVisitor extends EmptyDFSVisitor<V,E> {
        @Override
        public boolean visitNode(V parent, E edge, V node, DFSEdgeType type, Collection<E> outEdges, int depth) {
            if (type == DFSEdgeType.BACK_EDGE) {
                backEdges.add(edge);
            }
            return true;
        }
    }

    private final List<E> backEdges;
    private final DirectedGraph<V, E> graph;


    public BackEdgeFinder(DirectedGraph<V,E> graph) {
        this.graph = graph;
        backEdges = new ArrayList<E>();
        update();
    }

    public void update() {
        DFSTraverser<V,E> traverser = new DFSTraverser<V,E>(new BackEdgeVisitor());
        traverser.traverse(graph);
    }

    /**
     * @return a list of all back edges in the graph. If the graph has been modified since this class has been created,
     *         {@link #update()}  must be called first to get the correct results.
     */
    public List<E> getBackEdges() {
        return backEdges;
    }

    /**
     * If the underlying graph has been modified after this class has been created, {@link #update()} must be called
     * first.
     * @return a new acyclic graph, same as the underlying graph but with all backedges removed. The graph is not backed
     *         by the underlying graph.
     */
    public SimpleDirectedGraph<V,E> createDAG() {
        SimpleDirectedGraph<V,E> newGraph = new SimpleDirectedGraph<V, E>(graph.getEdgeFactory());

        for (V node : graph.vertexSet()) {
            newGraph.addVertex(node);
        }

        Set<E> edgeSet = new LinkedHashSet<E>(graph.edgeSet());
        edgeSet.removeAll(backEdges);

        for (E edge : edgeSet) {
            newGraph.addEdge(graph.getEdgeSource(edge), graph.getEdgeTarget(edge));
        }

        return newGraph;
    }


}
