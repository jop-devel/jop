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
package com.jopdesign.wcet08.frontend;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.Map.Entry;

import org.apache.bcel.generic.InstructionHandle;
import org.apache.log4j.Logger;
import org.jgrapht.DirectedGraph;
import org.jgrapht.ext.StringNameProvider;
import org.jgrapht.graph.DefaultDirectedGraph;

import com.jopdesign.build.MethodInfo;
import com.jopdesign.wcet.WCETInstruction;
import com.jopdesign.wcet08.frontend.BasicBlock.FlowInfo;
import com.jopdesign.wcet08.frontend.BasicBlock.FlowTarget;
import com.jopdesign.wcet08.frontend.BasicBlock.InstrField;
import com.jopdesign.wcet08.frontend.SourceAnnotations.BadAnnotationException;
import com.jopdesign.wcet08.graphutils.LoopColoring;
import com.jopdesign.wcet08.graphutils.TopOrder;

/**
 * (Control) Flow Graph for WCET analysis.
 * 
 * The new architecture models the CFG as a list of a basic blocks, and a graph.
 * The graph nodes have a unique id, may point to the basic blocks, and are annotated with
 * additional information (loop colors). The edges are marked (jmp,exit-loop color, backlink)
 * as well.
 * Additionally, we identify head-of-loops, and flow constraints.
 * 
 * We support loop unpeeling and node folding.
 *
 * The graph can be used for generating IPET programs, UPPAAL models, for cache analysis, etc.
 *
 * @author Benedikt Huber (benedikt.huber@gmail.com)
 *
 */
public class FlowGraph {
	private static final Logger logger = Logger.getLogger(FlowGraph.class);	

	/* 
	 * Flow Graph Nodes 
	 * ----------------
	 */
	public class FlowGraphNode implements Comparable<FlowGraphNode>{
		private int id;
		private String name;
		public FlowGraphNode() { 
			this.id = idGen++;
			this.name = "Node"+this.id;
		}
		public FlowGraphNode(String name) { 
			this();
			this.name = name;
		}
		public int compareTo(FlowGraphNode o) { 
			return new Integer(this.hashCode()).compareTo(o.hashCode()); 
		}
		public String toString() { return name; }
		public BasicBlock getCodeBlock() { return null; }
		public int getId() { return id; }
	}
	public class BasicBlockNode extends FlowGraphNode {
		private int blockIndex;
		public BasicBlockNode(int blockIndex) {
			this.blockIndex = blockIndex;
		}
		public BasicBlock getCodeBlock() { return blocks.get(blockIndex); }
		public String toString() { return "Block#"+blockIndex; }
	}
	public class SummaryNode extends FlowGraphNode { 
		private DirectedGraph<FlowGraphNode,FlowGraphEdge> subGraph;
		public SummaryNode(int id, DirectedGraph<FlowGraphNode,FlowGraphEdge> sub){
			this.subGraph = sub;
		}
		public DirectedGraph<FlowGraphNode,FlowGraphEdge> getSubGraph() {
			return this.subGraph;
		}
	}	
	/*
	 * Flow Graph Edges
	 * ----------------
	 */
	enum EdgeKind { ENTRY_EDGE, EXIT_EDGE, NEXT_EDGE,
					GOTO_EDGE, SELECT_EDGE, BRANCH_EDGE, JSR_EDGE };
	public class FlowGraphEdge  {
		EdgeKind kind;
		public EdgeKind getKind() { return kind; }
		public FlowGraphEdge(EdgeKind kind) {
			this.kind = kind;
		}
		public String getName() {
			return graph.getEdgeSource(this)+"_"+graph.getEdgeTarget(this);
		}
		public String toString() {
			return (""+graph.getEdgeSource(this).id+"->"+graph.getEdgeTarget(this).id);
		}
	}
	FlowGraphEdge entryEdge() { return new FlowGraphEdge(EdgeKind.ENTRY_EDGE); }
	FlowGraphEdge exitEdge() { return new FlowGraphEdge(EdgeKind.EXIT_EDGE); }
	FlowGraphEdge nextEdge() { return new FlowGraphEdge(EdgeKind.NEXT_EDGE); }

	/*
	 * Fields
	 * ------
	 */
	private int idGen = 0;
	private FlowGraphNode entry, exit;
	private Vector<BasicBlock> blocks;
	private DirectedGraph<FlowGraphNode, FlowGraphEdge> graph;
	private TopOrder<FlowGraphNode, FlowGraphEdge> topOrder;
	private LoopColoring<FlowGraphNode, FlowGraphEdge> loopColoring;
	private HashMap<FlowGraphNode, Integer> annotations;
	private MethodInfo  methodInfo;
	
	public FlowGraph(JOPAppInfo cl, MethodInfo method)  {
		this.methodInfo = method;
		createFlowGraph(method);
		analyseFlowGraph();
	}
	private void createFlowGraph(MethodInfo method) {
		blocks = BasicBlock.buildBasicBlocks(method);
		Hashtable<Integer,BasicBlockNode> nodeTable =
			new Hashtable<Integer, BasicBlockNode>();
		graph = new DefaultDirectedGraph<FlowGraphNode,FlowGraphEdge>(FlowGraphEdge.class);
		entry = new FlowGraphNode("ENTRY"); graph.addVertex(entry);
		exit  = new FlowGraphNode("EXIT"); graph.addVertex(exit);
		/* Create basic block vertices */
		for(int i = 0; i < blocks.size(); i++) {
			BasicBlock bb = blocks.get(i);
			BasicBlockNode n = new BasicBlockNode(i);			
			nodeTable.put(bb.getFirstInstruction().getPosition(),n);
			graph.addVertex(n);
		}
		/* entry edge */
		graph.addEdge(entry, nodeTable.get(blocks.get(0).getFirstInstruction().getPosition()), entryEdge());
		/* flow edges */
		for(BasicBlockNode bbNode : nodeTable.values()) {
			BasicBlock bb = bbNode.getCodeBlock();
			FlowInfo bbf = (FlowInfo) bb.getLastInstruction().getAttribute(InstrField.FLOW_INFO);
			if(bbf.exit) { // exit edge
				graph.addEdge(bbNode, this.exit, exitEdge());
			} else if(! bbf.alwaysTaken) { // next block edge
				graph.addEdge(bbNode, 
							  nodeTable.get(bbNode.getCodeBlock().getLastInstruction().getNext().getPosition()),
							  new FlowGraphEdge(EdgeKind.NEXT_EDGE));
			}
			for(FlowTarget target: bbf.targets) { // jmps
				graph.addEdge(bbNode, 
							  nodeTable.get(target.target.getPosition()), 
							  new FlowGraphEdge(target.edgeKind));
			}
		}		
	}
	public void loadAnnotations(SortedMap<Integer,Integer> wcaMap) throws BadAnnotationException {
		this.annotations = new HashMap<FlowGraphNode, Integer>();
		for(Entry<FlowGraphNode,Vector<FlowGraphNode>> e : 
			loopColoring.getHeadOfLoops().entrySet()) {
			BasicBlockNode headOfLoop = (BasicBlockNode) e.getKey();
			BasicBlock block = headOfLoop.getCodeBlock();
			// search for loop annotation in range
			int lb = (Integer) block.getFirstInstruction().getAttribute(InstrField.LINE_NUMBER);
			int ub = (Integer) block.getLastInstruction().getAttribute(InstrField.LINE_NUMBER);
			SortedMap<Integer,Integer> annots = wcaMap.subMap(lb, ub+1);
			if(annots.size() == 1) {
				this.annotations.put(headOfLoop,annots.get(annots.firstKey()));
			} else {
				throw new BadAnnotationException(
						(annots.isEmpty() ? "Missing Annotation [" : "Ambigous Annotation [") + wcaMap + "]",
						block,
						lb,ub);
			}
		}
	}

	private void analyseFlowGraph() {
		topOrder = new TopOrder<FlowGraphNode, FlowGraphEdge>(this.graph, this.entry);
		loopColoring = new LoopColoring<FlowGraphNode, FlowGraphEdge>(this.graph,topOrder);
	}
	
	public FlowGraphNode getEntry() {
		return entry;
	}
	public FlowGraphNode getExit() {
		return exit;
	}
	public DirectedGraph<FlowGraphNode, FlowGraphEdge> getGraph() {
		return graph;
	}
	public LoopColoring<FlowGraphNode, FlowGraphEdge> getLoopColoring() {
		return loopColoring;
	}
	
	class NodeLabelProvider extends StringNameProvider<FlowGraphNode> {
		@Override
		public String getVertexName(FlowGraphNode node) {
			if(node.getCodeBlock() != null) {
				return node.getCodeBlock().getLastInstruction().toString();
			} else {
				return node.toString();
			}
		}
		
	}
	public boolean isHeadOfLoop(BasicBlockNode n) {
		return this.loopColoring.getHeadOfLoops().containsKey(n);
	}
	public boolean isBackEdge(FlowGraphEdge edge) {
		return this.topOrder.getBackEdges().contains(edge);
	}
	public Set<FlowGraphNode> getLoopEntrySet(FlowGraphEdge edge) {
		Set<FlowGraphNode> setSource = getLoopColor(graph.getEdgeSource(edge));
		Set<FlowGraphNode> setTarget = new TreeSet<FlowGraphNode>(getLoopColor(graph.getEdgeTarget(edge)));
		setTarget.removeAll(setSource);
		return setTarget;
	}
	public Set<FlowGraphNode> getLoopExitSet(FlowGraphEdge edge) {
		Set<FlowGraphNode> setSource = new TreeSet<FlowGraphNode>(getLoopColor(graph.getEdgeSource(edge)));
		Set<FlowGraphNode> setTarget = getLoopColor(graph.getEdgeTarget(edge));
		setSource.removeAll(setTarget);
		return setSource;
	}
	public Set<FlowGraphNode> getLoopColor(FlowGraphNode node) {
		return loopColoring.getLoopColors().get(node);
	}
	public int getNumberOfBytes() {
		int sum = 0;
		for(BasicBlock bb : this.blocks) {
			sum += bb.getNumberOfBytes();
		}
		return sum;
	}
	public Map<FlowGraphNode, Integer> getLoopBounds() {
		return this.annotations;
	}
	public Collection<FlowGraphNode> getHeadOfLoops() {
		return this.loopColoring.getHeadOfLoops().keySet();
	}
	public Collection<FlowGraphEdge> getBackEdgesTo(FlowGraphNode hol) {
		Vector<FlowGraphEdge> edges = new Vector<FlowGraphEdge>();
		for(FlowGraphNode n : this.loopColoring.getHeadOfLoops().get(hol)) {
			edges.add(graph.getEdge(n, hol));
		}
		return edges;
	}
	public Collection<FlowGraphEdge> getExitEdgesOf(FlowGraphNode hol) {
		return this.loopColoring.getExitEdges().get(hol);
	}
	public void exportDOT(File file) {
		exportDOT(file,null,null);
	}
	public void exportDOT(File file, Map<FlowGraphNode, ?> nodeAnnotations, Map<FlowGraphEdge, ?> edgeAnnotations) {
		FlowGraphExport export = new FlowGraphExport(this, nodeAnnotations, edgeAnnotations);		
		try {
			FileWriter w = new FileWriter(file);
			export.exportDOT(w, graph);
			w.close();
		} catch (IOException e) {
			e.printStackTrace();
		}				
	}
	public MethodInfo getMethodInfo() {
		return this.methodInfo;
	}
	
	/**
	 * Estimate the WCET of a basic block (only local effects) for debugging purposes
	 * @param b the basic block
	 * @return the cost of executing the basic block, without cache misses
	 */
	public int basicBlockWCETEstimate(BasicBlock b) {
		int wcet = 0;
		for(InstructionHandle ih : b.getInstructions()) {
			int jopcode = ((JOPAppInfo) this.methodInfo.getCli().appInfo).getJOpCode(b.getClassInfo(), ih.getInstruction());
			int opCost = WCETInstruction.getCycles(jopcode,false,0);						
			wcet += opCost;
		}
		return wcet;
	}
}
