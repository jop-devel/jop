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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.MONITORENTER;
import org.apache.bcel.generic.MONITOREXIT;

import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.code.ControlFlowGraph.CFGEdge;
import com.jopdesign.common.code.ControlFlowGraph.CFGNode;
import com.jopdesign.common.code.SuperGraph.ContextCFG;
import com.jopdesign.common.code.SuperGraph.SuperEdge;
import com.jopdesign.common.code.SuperGraph.SuperGraphEdge;
import com.jopdesign.common.code.SuperGraph.SuperGraphNode;
import com.jopdesign.common.code.SuperGraph.SuperInvokeEdge;
import com.jopdesign.common.code.SuperGraph.SuperReturnEdge;
import com.jopdesign.common.graphutils.AdvancedDOTExporter;
import com.jopdesign.common.graphutils.AdvancedDOTExporter.DOTLabeller;
import com.jopdesign.common.graphutils.Pair;
import com.jopdesign.common.misc.Filter;
import com.jopdesign.common.misc.Iterators;
import com.jopdesign.common.misc.MiscUtils;
import com.jopdesign.common.misc.MiscUtils.F1;
import com.jopdesign.wcet.WCETTool;

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
	
	private Map<ContextCFG, List<SuperGraphNode>> nodes;
	private Set<SuperGraphEdge> edges;
	private Filter<SuperGraphEdge> edgeFilter;
	private HashSet<SuperGraphEdge> otherEntries;	
	
	/** Construct a semi-closed segment (all exit edges are explicitly given and are part of the segment) */
	public Segment(SuperGraph sg, Set<SuperGraphEdge> entries, Set<SuperGraphEdge> exits) {
		this.sg = sg;
		this.entries = entries;
		this.exits = exits;
		buildSegment();
		this.edgeFilter = Filter.isContainedIn(this.edges);
	}


	/**
	 * @return the set of entry edges
	 */
	public Set<SuperGraphEdge> getEntryEdges() {
		return entries;
	}
	
	/**
	 * @param e a supergraph edge
	 * @return true, if the edge is an entry edge of the segment
	 */
	public boolean isEntryEdge(SuperGraphEdge e) {
		return this.getEntryEdges().contains(e);
	}

	/**
	 * @return The set { contextCfg (target e) | e <- entry-edges }
	 */
	public Set<ContextCFG> getEntryCFGs() {
		Iterable<ContextCFG> entryCFGs = Iterators.mapEntries(getEntryEdges(), new F1<SuperGraphEdge, ContextCFG>() {
			@Override
			public ContextCFG apply(SuperGraphEdge v) {
				return v.getTarget().getContextCFG();
			}			
		});
		return Iterators.addAll(new HashSet<ContextCFG>(), entryCFGs);
	}


	/**
	 * @return the set of exit edges
	 */
	public Set<SuperGraphEdge> getExitEdges() {
		return exits;
	}
	
	/**
	 * @param e
	 * @return true if the edge is an exit edge of the segment
	 */
	public boolean isExitEdge(SuperGraphEdge e) {
		return getExitEdges().contains(e);
	}

	public boolean includesEdge(SuperGraphEdge e) {
		return edges.contains(e);
	}


	/**
	 * @return the underlying supergraph, which may include many more methods
	 */
	public SuperGraph getSuperGraph() {
		
		return sg;
	}

	/**
	 * Collect all supergraph edges which are part of the segment.
	 * As segments are allowed to be interprocedural, we require that
	 * control flow graphs have return edges, and that return edges
	 * are in the exit set if they are not part of the segment.
	 */
	private Set<SuperGraphEdge> buildSegment() {

		nodes = new HashMap<SuperGraph.ContextCFG, List<SuperGraphNode>>();
		edges = new HashSet<SuperGraphEdge>();
		otherEntries = new HashSet<SuperGraphEdge>();
		
		HashSet<SuperGraphEdge> actualExits = new HashSet<SuperGraphEdge>();
		Stack<SuperGraphEdge> worklist = new Stack<SuperGraphEdge>();

		/* push all targets of entry edges on the worklist */
		worklist.addAll(entries);		
		while(! worklist.isEmpty()) {
			SuperGraphEdge current = worklist.pop();
			if(edges.contains(current)) continue; /* continue if marked black */
			edges.add(current); /* mark black */
			
			/* If this is an exit egde, remember that it has been visited, and continue */
			if(exits.contains(current)) {
				actualExits.add(current);
				continue;
			}
			/* Otherwise add the target node and push all successors on the worklist */
			SuperGraphNode target = current.getTarget();
			MiscUtils.addToList(nodes, target.getContextCFG(), target);
			Iterators.addAll(worklist, sg.getSuccessorEdges(current));
		}
		exits = actualExits;
		for(SuperGraphNode node : getNodes()) {
			for(SuperGraphEdge edge: sg.incomingEdgesOf(node)) {
				if(! edges.contains(edge)) {
					otherEntries.add(edge);
				}
			}
		}
		/* for all nodes find entries which are not part of the segment; this are "otherEntries" */
		return edges;
	}

	/**
	 * Create an interprocedural segment for a method
	 * @param targetMethod The method to build a segment for
	 * @param callString   The context for the method
	 * @param cfgProvider A control flow graph provider
	 * @param callStringLength Length of the callstrings
	 * @param infeasibles Information about infeasible edges (null if no information available)
	 *
	 * @return a segment representing executions of the method (not including the virtual entry and exit nodes)
	 */
	public static Segment methodSegment(MethodInfo targetMethod, CallString callString,
			CFGProvider cfgProvider, int callStringLength, InfeasibleEdgeProvider infeasibles) {
		
		if(infeasibles == null) {
			infeasibles = InfeasibleEdgeProvider.NO_INFEASIBLES; 
		}
		
		SuperGraph superGraph = new SuperGraph(cfgProvider, cfgProvider.getFlowGraph(targetMethod), callString, callStringLength, infeasibles);
		ContextCFG rootMethod = superGraph.getRootNode();
		Set<SuperGraphEdge> entryEdges = Iterators.addAll(new HashSet<SuperGraphEdge>(), superGraph.getCFGEntryEdges(rootMethod)),
                exitEdges = Iterators.addAll(new HashSet<SuperGraphEdge>(), superGraph.getCFGExitEdges(rootMethod));
		return new Segment(superGraph, entryEdges, exitEdges);
	}

	/**
	 * Create an interprocedural segment for a synchronized block. Currently we do
	 * not split basic blocks here, so either you are happy with basic block granularity,
	 * or you split the basic block while loading.
	 * @param targetBlock The block containing the monitorenter instruction
	 * @param monitorEnter The monitor enter instruction
	 * @param callString   The context for the method
	 * @param cfgProvider A control flow graph provider
	 * @param callStringLength Length of the callstrings
	 * @param infeasibles Information about infeasible edges (null if no information available)
	 *
	 * @return a segment representing executions of the synchronized block
	 */
	public static Segment synchronizedSegment(ContextCFG ccfg, CFGNode entryNode, InstructionHandle monitorEnter,
			CFGProvider cfgProvider, int callStringLength, InfeasibleEdgeProvider infeasibles) {

		if(infeasibles == null) {
			infeasibles = InfeasibleEdgeProvider.NO_INFEASIBLES;                                                 
		}
		ControlFlowGraph cfg = ccfg.getCfg();                                      
		SuperGraph superGraph = new SuperGraph(cfgProvider, cfg, ccfg.getCallString(), callStringLength, infeasibles);
		ContextCFG rootMethod = superGraph.getRootNode();                                                    

		/* lift entry edges */
		Set<SuperGraphEdge> entryEdges = Iterators.addAll(new HashSet<SuperGraphEdge>(), 
				superGraph.liftCFGEdges(rootMethod, cfg.incomingEdgesOf(entryNode)));


		/* find exit blocks (might also be in the same block) */
		/* monitorenter followed bei monitorexit in same block => segment only contains this block */
		Set<CFGEdge> monitorExitEdges = new HashSet<CFGEdge>();
		CFGNode currentNode = entryNode;
		int currentNestingLevel = 1;
		Iterator<InstructionHandle> insIter = currentNode.getBasicBlock().getInstructions().iterator();
		while(insIter.hasNext()) {
			if(insIter.next() == monitorEnter) break;
		}
		Stack<Pair<CFGNode,Integer>> todo = new Stack<Pair<CFGNode,Integer>>();
		Set<CFGNode> visited = new HashSet<CFGNode>();
		do {
			boolean isExit = false;
			while(insIter.hasNext()) {
				InstructionHandle ih = insIter.next();
				if(ih.getInstruction() instanceof MONITOREXIT) {
					/* blocks outgoing edges terminate segment */
					currentNestingLevel--;
					if(currentNestingLevel == 0) {
						isExit = true;
						Iterators.addAll(monitorExitEdges, cfg.outgoingEdgesOf(currentNode));
						break;
					}
				} else if(ih.getInstruction() instanceof MONITORENTER) {
					currentNestingLevel++;
				}
			}
			if(! isExit) {
				for(CFGNode node : cfg.getSuccessors(currentNode)) {
					todo.add(new Pair<CFGNode, Integer>(node, currentNestingLevel));
				}
			}
			currentNode = null;
			while(! todo.isEmpty()) {
				Pair<CFGNode, Integer> nextPair = todo.pop();
				CFGNode nextNode = nextPair.first();
				if(! visited.contains(nextNode)) {
					visited.add(nextNode);
					if(cfg.outgoingEdgesOf(nextNode).isEmpty()) {
						throw new AssertionError("Found monitor-exit free path from monitorenter to the end of a function. In: "+cfg);						
					} else if(nextNode.getBasicBlock() == null) {
						for(CFGNode node : cfg.getSuccessors(nextNode)) {
							todo.add(new Pair<CFGNode, Integer>(node, nextPair.second()));
						}
					} else {
						currentNode = nextNode;
						currentNestingLevel = nextPair.second();
						insIter = currentNode.getBasicBlock().getInstructions().iterator();
						break;
					}
				}
			}
		} while(currentNode != null);
		
		Set<SuperGraphEdge> exitEdges = Iterators.addAll(new HashSet<SuperGraphEdge>(), 
				superGraph.liftCFGEdges(rootMethod, monitorExitEdges));
		return new Segment(superGraph, entryEdges, exitEdges);                                               
	}


	/**
	 * Create a (sub-)segment for a method invocation (in a certain context)<br/>
	 * FIXME: Currently, this is terribly inefficient. We need to work on a good and
	 * fast support for subsegments.
	 * 
	 * @param callee root node
	 * @param supergraph The supergraph we are operating on
	 * @return a segment representing executions of the method 
	 * (neither including the invoke instruction and the return to the caller)
	 * @return
	 */
	public static Segment methodSegment(ContextCFG callee, SuperGraph superGraph) {
		return methodSegment(callee.getCfg().getMethodInfo(), callee.getCallString(),
				superGraph.getCFGProvider(), superGraph.getCallStringLength(), superGraph.getInfeasibleEdgeProvider());
	}

	/**
	 * An intraprocedural segment for a single node
	 * FIXME: horribly inefficient
	 * @param node The node to create the segment for
	 * @param supergraph The supergraph we are operating on
	 * @return a segment representing executions of the node
	 */
	public static Segment nodeSegment(SuperGraphNode target, SuperGraph supergraph) {

		SuperGraph superGraph = new SuperGraph(supergraph.getCFGProvider(), target.getCfg(), target.getContextCFG().getCallString(), 
				supergraph.getCallStringLength(), supergraph.getInfeasibleEdgeProvider());
		Set<SuperGraphEdge> entryEdges = Iterators.addAll(new HashSet<SuperGraphEdge>(), supergraph.incomingEdgesOf(target)),
                exitEdges = Iterators.addAll(new HashSet<SuperGraphEdge>(), supergraph.outgoingEdgesOf(target));
		return new Segment(superGraph, entryEdges, exitEdges);
	}


	/**
	 * @return set of all nodes in the segment
	 */
	public Iterable<SuperGraphNode> getNodes() {

		return Iterators.concat(nodes.values());
	}

	/**
	 * @param ccfg a cfg instance
	 * @return all supergraph nodes for the CFG instances
	 */
	public Collection<SuperGraphNode> getNodes(ContextCFG ccfg) {

		return nodes.get(ccfg);
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
	 * @param ccfg
	 * @return
	 */
	public Iterable<SuperEdge> incomingSuperEdgesOf(ContextCFG ccfg) {

		return edgeFilter.filter(sg.getCallGraph().incomingEdgesOf(ccfg));
	}


	/**
	 * @param node
	 * @return
	 */
	public Iterable<SuperGraphEdge> outgoingEdgesOf(SuperGraphNode node) {

		return edgeFilter.filter(sg.outgoingEdgesOf(node));
	}

	final Filter<Pair<SuperInvokeEdge, SuperReturnEdge>> superEdgePairFilter = 
			new Filter<Pair<SuperInvokeEdge, SuperReturnEdge>>() {
			@Override
			protected boolean include(Pair<SuperInvokeEdge, SuperReturnEdge> e) {
				return(edges.contains(e.first()) && edges.contains(e.second()));
			}			
		};

	/**
	 * @return all pairs of invoke/return superedges
	 */
	public Iterable<Pair<SuperInvokeEdge, SuperReturnEdge>> getCallSites() {

		Iterable<Pair<SuperInvokeEdge, SuperReturnEdge>> callSites = Iterators.concat(sg.getCallSites().values());
		return superEdgePairFilter.filter(callSites);
	}
	
	/**
	 * @param caller restriction on the call site
	 * @return those pairs of invoke/return superedges with the specified caller
	 */
    public Iterable<Pair<SuperInvokeEdge, SuperReturnEdge>> getCallSitesFrom(ContextCFG caller) {

    	return superEdgePairFilter.filter(sg.getCallSitesFrom(caller));
	}

	/* delegates to supergraph */

	public Iterable<SuperGraphEdge> liftCFGEdges(ContextCFG node, Iterable<CFGEdge> cfgEdges) {

		return sg.liftCFGEdges(node, cfgEdges);
	}


	public Iterable<ContextCFG> getCallGraphNodes() {
		
		return nodes.keySet();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Segment@");
		sb.append(super.hashCode());
		sb.append("(");
		sb.append(this.getEntryEdges());
		sb.append(" -- ");
		sb.append(this.getExitEdges());
		sb.append(")");
		return sb.toString();
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
