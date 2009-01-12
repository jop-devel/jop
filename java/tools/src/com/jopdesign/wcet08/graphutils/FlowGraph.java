package com.jopdesign.wcet08.graphutils;

import org.jgrapht.DirectedGraph;

public interface FlowGraph<V, E> extends DirectedGraph<V, E> {
	public V getEntry();
	public V getExit(); 
}
