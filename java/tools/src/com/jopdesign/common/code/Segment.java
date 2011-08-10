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

package com.jopdesign.common.code;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.code.SuperGraph.ContextCFG;
import com.jopdesign.common.code.SuperGraph.SuperGraphEdge;
import com.jopdesign.common.code.SuperGraph.SuperGraphNode;
import com.jopdesign.common.code.SuperGraph.SuperInvokeEdge;
import com.jopdesign.common.code.SuperGraph.SuperReturnEdge;
import com.jopdesign.common.graphutils.AdvancedDOTExporter;
import com.jopdesign.common.graphutils.AdvancedDOTExporter.DOTLabeller;
import com.jopdesign.common.misc.Filter;
import com.jopdesign.common.misc.IteratorUtilities;

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

	private Set<SuperGraphNode> nodes;
	private Set<SuperGraphEdge> edges;
	private Filter<SuperGraphEdge> edgeFilter;
	
	
	
	/** Construct a semi-closed segment (all exit edges are explicitly given and are part of the segment) */
	public Segment(SuperGraph sg, Set<SuperGraphEdge> entries, Set<SuperGraphEdge> exits) {
		this.sg = sg;
		this.entries = entries;
		this.exits = exits;
		collectSegmentEdges();
		this.edgeFilter = Filter.isContainedIn(this.edges);
	}


	/**
	 * @return the set of entry edges
	 */
	public Set<SuperGraphEdge> getEntryEdges() {
		return entries;
	}
	
	/**
	 * @return the set of exit edges
	 */
	public Set<SuperGraphEdge> getExitEdges() {
		return exits;
	}
	
	public boolean includeEdge(SuperGraphEdge e) {
		return edges.contains(e);
	}

	/**
	 * Collect all supergraph edges which are part of the segment.
	 * As segments are allowed to be interprocedural, we require that
	 * control flow graphs have return edges, and that return edges
	 * are in the exit set if they are not part of the segment.
	 */
	private Set<SuperGraphEdge> collectSegmentEdges() {
		edges = new HashSet<SuperGraphEdge>();
		nodes = new HashSet<SuperGraphNode>();
		HashSet<SuperGraphEdge> actualExits = new HashSet<SuperGraphEdge>();
		Stack<SuperGraphEdge> worklist = new Stack<SuperGraphEdge>();

		/* push all targets of entry edges on the worklist */
		worklist.addAll(entries);		
		while(! worklist.isEmpty()) {
			SuperGraphEdge current = worklist.pop();
			if(edges.contains(current)) continue; /* continue if marked black */
			edges.add(current); /* mark black */
			for(SuperGraphEdge succ : sg.getSuccessorEdges(current)) {
				if(! exits.contains(current)) {
					nodes.add(current.getTarget());
					worklist.add(succ); /* (re-)mark grey */
				} else {
					actualExits.add(current);
				}
			}
		}
		exits = actualExits;
		return edges;
	}

	/**
	 * Create an interprocedural segment for a method
	 *
	 * @param project      Reference to the wcet tool
	 * @param targetMethod The method to build a segment for
	 * @param callString   The context for the method
	 * @return
	 */
	public static Segment methodSegment(CFGProvider tool, MethodInfo targetMethod,
			CallString callString, int callStringLength) {

		SuperGraph sg = new SuperGraph(tool, tool.getFlowGraph(targetMethod), callStringLength);
		ContextCFG rootNode = sg.getRootNode();
		Set<SuperGraphEdge> entryEdges = IteratorUtilities.addAll(new HashSet<SuperGraphEdge>(), sg.getCFGEntryEdges(rootNode)),
		                    exitEdges = IteratorUtilities.addAll(new HashSet<SuperGraphEdge>(), sg.getCFGExitEdges(rootNode));
		return new Segment(sg, entryEdges, exitEdges);
	}

	/**
	 * @return set of all nodes in the segment
	 */
	public Iterable<SuperGraphNode> getNodes() {

		return nodes;
	}

	/**
	 * @return set of all edges in the segment
	 */
	public Iterable<SuperGraphEdge> getEdges() {

		return edges;
	}

	/**
	 * @param node
	 * @return
	 */
	public Iterable<SuperGraphEdge> incomingEdgesOf(SuperGraphNode node) {

		return edgeFilter.filter(sg.incomingEdgesOf(node));
	}

	/**
	 * @param node
	 * @return
	 */
	public Iterable<SuperGraphEdge> outgoingEdgesOf(SuperGraphNode node) {

		return edgeFilter.filter(sg.outgoingEdgesOf(node));
	}

	/**
	 * Export to DOT file
	 * @param dotFile
	 * @throws IOException
	 */
	public void exportDOT(File dotFile) throws IOException {

		FileWriter dotWriter = new FileWriter(dotFile);
		AdvancedDOTExporter.DOTNodeLabeller<SuperGraphNode> nodeLabeller =
			new AdvancedDOTExporter.DefaultNodeLabeller<SuperGraphNode>() {

			@Override
			public String getLabel(SuperGraphNode node) {
				StringBuilder sb = new StringBuilder();
				/* for entry nodes: method + call string */
				if(node.getCFGNode().getId() == 0) {
					sb.append(node.getContextCFG().getCfg().getMethodInfo().getFQMethodName()+"\n");
					int i = 1;
					for(InvokeSite is : node.getContextCFG().getCallString()) {
						sb.append(" #"+i+" "+is.getInvoker().getFQMethodName()+" / "+is.getInstructionHandle().getPosition()+"\n");
						i+=1;
					}					
				} 
				/* for other nodes: basic block export */
				else {
					sb.append(node.getCFGNode().toString());
				}
				return sb.toString();
			}

		};

		DOTLabeller<SuperGraphEdge> edgeLabeller =
			new AdvancedDOTExporter.DefaultDOTLabeller<SuperGraphEdge>() {

			@Override
			public String getLabel(SuperGraphEdge edge) {
				return "";
			}

			@Override
			public boolean setAttributes(SuperGraphEdge edge,
					Map<String, String> ht) {
				super.setAttributes(edge, ht);
				if (edge instanceof SuperReturnEdge) { 
					ht.put("style", "dotted");
					ht.put("arrowhead", "empty");
				} else if(edge instanceof SuperInvokeEdge) {
					ht.put("style", "dotted");					
				}
				return true;
			}

		};
		AdvancedDOTExporter<SuperGraphNode, SuperGraphEdge> de = new AdvancedDOTExporter<SuperGraphNode, SuperGraphEdge>(
				nodeLabeller, edgeLabeller );
		de.exportDOTDiGraph(dotWriter, this.getNodes(), this.getEdges(), 
				new AdvancedDOTExporter.GraphAdapter<SuperGraphNode, SuperGraphEdge>() {
			@Override
			public SuperGraphNode getEdgeSource(SuperGraphEdge e) {
				return e.getSource();
			}
			@Override
			public SuperGraphNode getEdgeTarget(SuperGraphEdge e) {
				return e.getTarget();
			}
		});
		dotWriter.close();
	}



}
