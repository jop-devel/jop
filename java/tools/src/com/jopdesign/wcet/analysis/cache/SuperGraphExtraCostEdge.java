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

/**
 * Purpose: Represents extra cost variables attached to a super graph edge
 *
 */
public class SuperGraphExtraCostEdge implements SuperGraphEdge {
	
	private final SuperGraphEdge parent;
	private final Object key;
	private final Object tag;
	private final int index;
	
	/**
	 * Create a new extra cost edge
	 * @param parent the corresponding flow edge
	 * @param key    the cost category
	 * @param tag    the cost tag (allowed to be null)
	 * @param index  extra index to distinguish several cost edges
	 */
	public SuperGraphExtraCostEdge(SuperGraphEdge parent, Object key, Object tag, int index) {

		this.parent = parent;
		this.key = key;
		this.tag = tag;
		this.index = index;
	}
	
	public SuperGraphEdge getParent() {
		
		return parent;
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
		if(hash != 0) return hash;
		final int prime = 31;
		hash = index;
		hash = parent.hashCode() + hash*prime;
		hash = key.hashCode() + hash*prime;
		if(tag != null) hash = 1+tag.hashCode() + hash*prime;
		return hash;
	}
	private int hash;

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		
		SuperGraphExtraCostEdge other = (SuperGraphExtraCostEdge) obj;
		if (index != other.index) return false;
		if (!key.equals(other.key)) return false;
		if (tag != null && !tag.equals(other.tag)) return false;
		return parent.equals(other.parent);
	}
	
	@Override
	public String toString() {
		return "{ExtraCostEdge" + this.getParent() + "/"+key+"/"+tag+"}";		
	}

	/**
	 * Generate {@code count} number of extra cost edges (category key, tag tag) for the given parent edge
	 * @param parent parent edge
	 * @param key    analysis id (key)
	 * @param tag    set id      (tag)
	 * @param count  Number of split edges to generate
	 * @return
	 */
	public static List<SuperGraphEdge> generateExtraCostEdges(SuperGraphEdge parent, Object key, Object tag, int count) {
		
		ArrayList<SuperGraphEdge> splitEdges = new ArrayList<SuperGraphEdge>();
		for(int i = 0; i < count; i++) splitEdges.add(new SuperGraphExtraCostEdge(parent,key, tag, i));
		return splitEdges;
	}
	
	/**
	 * Generate one xtra cost edges (category key, tag tag) for the given parent edge
	 * @param parent parent edge
	 * @param key    analysis id (key)
	 * @param tag    set id      (tag)
	 * @param count  Number of split edges to generate
	 * @return
	 */
	public static SuperGraphExtraCostEdge generateExtraCostEdge(SuperGraphEdge parent, Object key, Object tag) {
		
		return new SuperGraphExtraCostEdge(parent, key, tag, 0);
	}
	
}