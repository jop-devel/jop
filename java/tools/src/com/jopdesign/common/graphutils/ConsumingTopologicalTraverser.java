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

import java.util.Queue;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class ConsumingTopologicalTraverser<V,E> extends TopologicalTraverser<V,E> {

    public ConsumingTopologicalTraverser(DirectedGraph<V, E> graph, NodeVisitor<V> visitor) {
        super(graph, visitor);
    }

    public ConsumingTopologicalTraverser(DirectedGraph<V, E> graph, NodeVisitor<V> visitor, Queue<V> queue) {
        super(graph, visitor, queue);
    }

    @Override
    public boolean isRoot(V node) {
        return getGraph().inDegreeOf(node) == 0;
    }

    @Override
    protected void removeEdge(E edge, V target) {
        getGraph().removeEdge(edge);
    }
}
