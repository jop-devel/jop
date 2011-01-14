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

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;


public class DefaultFlowGraph<V, E> extends DefaultDirectedGraph<V, E>
        implements FlowGraph<V, E> {
    private static final long serialVersionUID = 1L;
    private V entry;
    private V exit;

    public DefaultFlowGraph(Class<? extends E> edgeClass, V entry, V exit) {
        super(edgeClass);
        this.entry = entry;
        this.addVertex(entry);
        this.exit = exit;
        this.addVertex(exit);
    }

    /**
     * Copy constructor
     *
     * @param srcGraph the source graph to copy
     */
    public DefaultFlowGraph(FlowGraph<V, E> srcGraph) {
        super(srcGraph.getEdgeFactory());
        this.entry = srcGraph.getEntry();
        this.exit = srcGraph.getExit();
        this.addGraph(srcGraph);
    }

    private void addGraph(DirectedGraph<V, E> srcGraph) {
        for (V vertex : srcGraph.vertexSet()) {
            this.addVertex(vertex);
        }
        for (E edge : srcGraph.edgeSet()) {
            V src = srcGraph.getEdgeTarget(edge);
            V target = srcGraph.getEdgeTarget(edge);
            this.addEdge(
                    src,
                    target,
                    edge);
        }
    }

    public V getEntry() {
        return this.entry;
    }

    public void setEntry(V n) {
        this.entry = n;
    }

    public V getExit() {
        return this.exit;
    }

    public void setExit(V n) {
        this.exit = n;
    }

}
