package com.jopdesign.wcet08.graphutils;

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
