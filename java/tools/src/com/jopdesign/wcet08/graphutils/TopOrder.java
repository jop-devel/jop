/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2008, Benedikt Huber (benedikt.huber@gmail.com)

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.jopdesign.wcet08.graphutils;

import java.util.Hashtable;
import java.util.Vector;

import org.jgrapht.DirectedGraph;
import org.jgrapht.traverse.DepthFirstIterator;

public class TopOrder<V,E> {
	private DirectedGraph<V, E> graph;
	private Hashtable<V,Integer> topOrder = null;
	private Vector<V> dfsOrder = null;
	private Vector<E> backEdges = null;
	private V startVertex;
	private class BackEdgeDetector extends DepthFirstIterator<V,E> {
		private int gen;
		public BackEdgeDetector(DirectedGraph<V, E> g, V startVertex) {
			super(g,startVertex);
			this.gen = 1;
		}
		// Topological order
		@Override
		protected void encounterVertex(V vertex, E edge) {
			super.encounterVertex(vertex,edge);
			topOrder.put(vertex, gen++);
			dfsOrder.add(vertex);
		}
		@Override
		protected void encounterVertexAgain(V vertex, E edge) {
			super.encounterVertexAgain(vertex,edge);
			if(getSeenData(vertex) == VisitColor.BLACK) {
				int sourceOrder = topOrder.get(graph.getEdgeSource(edge));
				int targetOrder = topOrder.get(graph.getEdgeTarget(edge));
				topOrder.put(vertex, Math.max(targetOrder, sourceOrder+1));
			} else {
				backEdges.add(edge);
			}
		}
	}
	public TopOrder(DirectedGraph<V,E> graph,V startVertex) {
		this.graph = graph;
		this.startVertex = startVertex;
	}
	public Vector<E> getBackEdges() {
		if(backEdges != null) return backEdges; // Caching 
		backEdges = new Vector<E>();
		topOrder = new Hashtable<V, Integer>();
		dfsOrder = new Vector<V>();
		BackEdgeDetector iter = new BackEdgeDetector(graph,startVertex);
		while(iter.hasNext()) iter.next();
		return this.backEdges;
	}
	public Hashtable<V,Integer> getTopOrder() {
		if(topOrder == null) getBackEdges();
		return topOrder;
	}
	public Vector<V> getDfsOrder() {
		if(dfsOrder == null) getBackEdges();
		return dfsOrder;
	}
}
