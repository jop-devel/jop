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

import java.util.Collection;
import java.util.Set;

public abstract class UnmodifiableDirectedGraphAdapter<V, E> implements DirectedGraph<V, E> {
    private static final String UNMODIFIABLE = "this graph is unmodifiable";

    public E addEdge(V arg0, V arg1) {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    public boolean addEdge(V arg0, V arg1, E arg2) {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    public boolean addVertex(V arg0) {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    public boolean removeAllEdges(Collection<? extends E> arg0) {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    public Set<E> removeAllEdges(V arg0, V arg1) {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    public boolean removeAllVertices(Collection<? extends V> arg0) {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    public boolean removeEdge(E arg0) {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    public E removeEdge(V arg0, V arg1) {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    public boolean removeVertex(V arg0) {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }
}
