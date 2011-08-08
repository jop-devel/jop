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

package com.jopdesign.wcet.analysis;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import com.jopdesign.common.code.SuperGraph;
import com.jopdesign.common.code.SuperGraph.SuperGraphEdge;

/**
 * Purpose: A segment represents subsets of execution traces.
 * It is characterized by a set of entry edges (begin of segment),
 * a set of exit edges (end of segment). Together with the supergraph
 * this implies the set of nodes which are part of the segment.
 * 
 * @author Benedikt Huber (benedikt@vmars.tuwien.ac.at)
 *
 */
public class Segment {

	private SuperGraph sg;
	private Set<SuperGraphEdge> entries;
	private Set<SuperGraphEdge> exits;
	private Set<SuperGraphEdge> edges;

	/** Construct a semi-closed segment (all exit edges are explicitly given and are part of the segment) */
	public Segment(SuperGraph sg, Set<SuperGraphEdge> entries, Set<SuperGraphEdge> exits) {
		this.sg = sg;
		this.entries = entries;
		this.exits = exits;
		this.edges = collectSegmentEdges();
	}

	/**
	 * Collect all supergraph edges which are part of the segment.
	 * As segments are allowed to be interprocedural, we require that
	 * control flow graphs have return edges, and that return edges
	 * are in the exit set if they are not part of the segment.
	 */
	private Set<SuperGraphEdge> collectSegmentEdges() {
		Set<SuperGraphEdge> edges = new HashSet<SuperGraphEdge>();
		Stack<SuperGraphEdge> worklist = new Stack<SuperGraphEdge>();

		/* push all targets of entry edges on the worklist */
		worklist.addAll(entries);		
		while(! worklist.isEmpty()) {
			SuperGraphEdge current = worklist.pop();
			if(edges.contains(current)) continue; /* continue if marked black */
			edges.add(current); /* mark black */
			for(SuperGraphEdge succ : sg.getSuccessorEdges(current)) {
				if(! exits.contains(current)) {
					worklist.add(succ); /* (re-)mark grey */
				}
			}
		}
		return edges;
	}
}
