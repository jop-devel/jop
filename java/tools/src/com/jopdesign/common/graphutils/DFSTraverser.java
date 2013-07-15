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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This is an implementation of a DFS graph traverser for directed graphs which classifies edges
 * and allows usage of an edge filter to create a subgraph view of a graph or to create the graph on the fly,
 * and applies a visitor to all reached nodes
 *
 * This is an implementation of the algorithm found here:
 * http://cs.wellesley.edu/~cs231/fall01/dfs.pdf
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class DFSTraverser<V,E> {

    public enum DFSEdgeType {
        ROOT, TREE_EDGE, FORWARD_EDGE, CROSS_EDGE, BACK_EDGE;

        public boolean isFirstVisit() {
            return this == ROOT || this == TREE_EDGE;
        }
    }

    public interface DFSVisitor<V,E> {
        /**
         * Called before the childs of a node are visited. This method can tell the traverser to skip visiting childs.
         * Although this could also be done by the filter by returning an empty outgoing edge set for this node,
         * it is sometimes more efficient to let the visitor decide (too) and separates the view of the graph from
         * the traversal algorithm.
         *
         * @param parent parent node of the edge. Null if edge type is ROOT.
         * @param edge the edge from parent to the current node. Null if edge type is ROOT.
         * @param node the currently visited node. If the edge type is not TREE_EDGE or ROOT, the node has already been
         *        visited.
         * @param type the edge type.
         * @param outEdges the outgoing edges of the current node as returned by the EdgeFilter.
         * @param depth the number of edges up to the root along the currently processed nodes.
         * @return true if the outgoing edges should be visited. If false is returned, the childs of the current node
         *         might still be visited from another node. If the current node has already been visited, the return
         *         value is (currently) ignored, as the traverser never descends from visited nodes.
         */
        boolean visitNode(V parent, E edge, V node, DFSEdgeType type, Collection<E> outEdges, int depth);

        /**
         * Called after all childs of a node have been visited from this node.
         *
         * @param parent parent node of the edge. Null if edge type is ROOT.
         * @param edge the edge from parent to the current node. Null if edge type is ROOT.
         * @param node the currently visited node. If the edge type is not TREE_EDGE or ROOT, the node has already been
         *        visited.
         * @param type the edge type.
         * @param outEdges the outgoing edges of the current node as returned by the EdgeFilter.
         * @param depth the number of edges up to the root along the currently processed nodes.
         */
        void finishNode(V parent, E edge, V node, DFSEdgeType type, Collection<E> outEdges, int depth);
    }

    public static class EmptyDFSVisitor<V,E> implements DFSVisitor<V,E> {
        @Override
        public boolean visitNode(V parent, E edge, V node, DFSEdgeType type, Collection<E> outEdges, int depth) {
            if (type.isFirstVisit()) {
                // call simplified version of this method
                preorder(node);
            }
            return true;
        }

        @Override
        public void finishNode(V parent, E edge, V node, DFSEdgeType type, Collection<E> outEdges, int depth) {
            postorder(node);
        }

        public void preorder(V node) {
        }

        public void postorder(V node) {
        }
    }

    private final DFSVisitor<V,E> visitor;
    // We use 'not contained' as WHITE, negative value as GREY, positive value as BLACK. Zero is not a valid timestamp.
    private final Map<V, Integer> discovery;

    private int time;

    public DFSTraverser(DFSVisitor<V,E> visitor) {
        this.visitor = visitor;
        discovery = new LinkedHashMap<V, Integer>();
        time = 0;
    }

    public DFSVisitor<V,E> getVisitor() {
        return visitor;
    }

    public void reset() {
        time = 0;
        discovery.clear();
    }

    public void traverse(DirectedGraph<V,E> graph) {
        List<V> roots = new ArrayList<V>();
        // We could simply start at all nodes with color WHITE (immediatly before starting at that node)
        // but we could get a lot of cross edges which are actually forward edges or tree edges.
        for (V node : graph.vertexSet()) {
            if (graph.inDegreeOf(node) == 0) {
                roots.add(node);
            }
        }
        traverse(new DefaultEdgeProvider<V,E>(graph), roots);
    }

    public void traverse(DirectedGraph<V,E> graph, Collection<V> roots) {
        traverse(new DefaultEdgeProvider<V,E>(graph), roots);
    }

    public void traverse(EdgeProvider<V,E> provider, Collection<V> roots) {
        for (V root : roots) {
            traverse(provider, root);
        }
    }

    public void traverse(EdgeProvider<V,E> provider, V node) {
        // skip visited
        if (discovery.containsKey(node)) return;
        // start with the node as root..
        traverse(provider, null, null, node, 0);
    }

    private void traverse(EdgeProvider<V,E> provider, V parent, E edge, V node, int depth) {
        Integer ts = discovery.get(node);

        // get type of edge parent->node
        DFSEdgeType type;
        if (parent == null) {
            type = DFSEdgeType.ROOT;
        } else if (ts == null) {
            // WHITE
            type = DFSEdgeType.TREE_EDGE;
        } else if (ts < 0) {
            // GREY
            type = DFSEdgeType.BACK_EDGE;
        } else {
            // BLACK; parent node is always either GREY or null
            int pts = -discovery.get(parent);
            type = pts <= ts ? DFSEdgeType.FORWARD_EDGE : DFSEdgeType.CROSS_EDGE;
        }

        // Visit the node regardless of type.. We re-visit nodes over BACK/FORWARD/CROSS-edges so that the
        // visitor is notified about those edges, but do not descend down
        Collection<E> outEdges = provider.outgoingEdgesOf(node);
        boolean descend = visitor.visitNode(parent, edge, node, type, outEdges, depth);

        if (ts != null) {
            // skip non-white nodes
            return;
        }

        time++;
        int currTime = time;

        if (descend) {
            // mark as GREY
            discovery.put(node, -currTime);

            for (E out : outEdges) {
                traverse(provider, node, out, provider.getEdgeTarget(out), depth+1);
            }
        }

        visitor.finishNode(parent, edge, node, type, outEdges, depth);

        // mark as BLACK
        discovery.put(node, currTime);

        time++;
    }

}
