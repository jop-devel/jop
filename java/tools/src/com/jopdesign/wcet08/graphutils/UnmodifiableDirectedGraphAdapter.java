package com.jopdesign.wcet08.graphutils;

import java.util.Collection;
import java.util.Set;

import org.jgrapht.DirectedGraph;

public abstract class UnmodifiableDirectedGraphAdapter<V,E> implements DirectedGraph<V, E> {
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
