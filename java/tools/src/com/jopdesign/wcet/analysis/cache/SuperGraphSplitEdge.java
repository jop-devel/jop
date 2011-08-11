/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2011, Benedikt Huber (benedikt@vmars.tuwien.ac.at)

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

package com.jopdesign.wcet.analysis.cache;

import java.util.ArrayList;
import java.util.List;

import com.jopdesign.common.code.SuperGraph;
import com.jopdesign.common.code.SuperGraph.SuperGraphEdge;
import com.jopdesign.common.code.SuperGraph.SuperGraphNode;

public class SuperGraphSplitEdge implements SuperGraphEdge {
	
	private int index;
	private Object key;
	private SuperGraphEdge parent;

	public SuperGraphSplitEdge(SuperGraphEdge parent, Object key, int index) {
		this.parent = parent;
		this.key = key;
		this.index = index;
	}
	
	@Override
	public SuperGraphNode getSource() {
		return parent.getSource();
	}

	@Override
	public SuperGraphNode getTarget() {
		return parent.getTarget();
	}

	@Override
	public SuperGraph getSuperGraph() {
		return parent.getSuperGraph();
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		return prime * parent.hashCode() + key.hashCode() + index;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		SuperGraphSplitEdge other = (SuperGraphSplitEdge) obj;
		if (index != other.index) return false;
		if (!key.equals(other.key)) return false;
		return parent.equals(other.parent);
	}

	/**
	 * Generate count split edges (in the category key) for the given parent edge
	 * @param parent
	 * @param key
	 * @param count
	 * @return
	 */
	public static List<SuperGraphEdge> generateSplitEdges(SuperGraphEdge parent, Object key, int count) {
		
		ArrayList<SuperGraphEdge> splitEdges = new ArrayList<SuperGraphEdge>();
		for(int i = 0; i < count; i++) splitEdges.add(new SuperGraphSplitEdge(parent,key,i));
		return splitEdges;
	}
}