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

/**
* @author Stefan Hepp (stefan@stefant.org)
*/
public class DefaultEdgeProvider<V,E> implements EdgeProvider<V,E> {
    private final DirectedGraph<V,E> graph;

    DefaultEdgeProvider(DirectedGraph<V, E> graph) {
        this.graph = graph;
    }

    @Override
    public Collection<E> outgoingEdgesOf(V node) {
        return graph.outgoingEdgesOf(node);
    }

    @Override
    public V getEdgeTarget(E edge) {
        return graph.getEdgeTarget(edge);
    }
}
