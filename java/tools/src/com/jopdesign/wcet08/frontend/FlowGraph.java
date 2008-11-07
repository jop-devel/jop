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
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.Map.Entry;

import org.apache.bcel.generic.INVOKEINTERFACE;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InvokeInstruction;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;

import com.jopdesign.build.ClassInfo;
import com.jopdesign.build.MethodInfo;
import com.jopdesign.wcet.WCETInstruction;
import com.jopdesign.wcet08.frontend.BasicBlock.FlowInfo;
import com.jopdesign.wcet08.frontend.BasicBlock.FlowTarget;
import com.jopdesign.wcet08.frontend.BasicBlock.InstrField;
import com.jopdesign.wcet08.frontend.SourceAnnotations.BadAnnotationException;
import com.jopdesign.wcet08.graphutils.LoopColoring;
import com.jopdesign.wcet08.graphutils.Pair;
import com.jopdesign.wcet08.graphutils.TopOrder;
import com.jopdesign.wcet08.graphutils.TopOrder.BadGraphException;

/**
 * (Control) Flow Graph for WCET analysis.
 * 
 * The new architecture models the CFG as a list of a basic blocks, and a graph.
 * The graph nodes have a unique id, may point to the basic blocks, and are annotated with
 * additional information (loop colors). The edges are marked (jmp,exit-loop color, backlink)
 * as well.
 * Additionally, we identify head-of-loops, and flow constraints.
 * 
 * We plan to support loop unpeeling and node folding.
 *
 * The graph can be used for generating IPET programs, UPPAAL models, for cache analysis, etc.
 *
 * @author Benedikt Huber (benedikt.huber@gmail.com)
 *
 */
public class FlowGraph {
	/**
	 * Visitor for flow graph nodes
	 */
	public interface FlowGraphVisitor {
		public void visitSpecialNode(DedicatedNode n);
		public void visitBasicBlockNode(BasicBlockNode n);
		public void visitInvokeNode(InvokeNode n);
	}
	
	/**
	 * Abstract base class for flow graph nodes
	 *
	 */
	public abstract class FlowGraphNode implements Comparable<FlowGraphNode>{
		private int id;
		protected String name;
		protected FlowGraphNode(String name) { 
			this.id = idGen++;
			this.name = "#"+id+" "+name;
		}
		public int compareTo(FlowGraphNode o) { 
			return new Integer(this.hashCode()).compareTo(o.hashCode()); 
		}
		public String toString() { return name; }
		public String getName()  { return name; }
		public BasicBlock getCodeBlock() { return null; }
		public int getId() { return id; }
		public abstract void accept(FlowGraphVisitor v);
	}

	/**
	 * Names for dedicated nodes (entry node, exit node)
	 */
	enum DedicatedNodeName { ENTRY, EXIT, SPLIT, JOIN };

	/**
	 * Dedicated flow graph nodes
	 */
	public class DedicatedNode extends FlowGraphNode {
		private DedicatedNodeName kind;
		public DedicatedNodeName getKind() { return kind; }
		private  DedicatedNode(DedicatedNodeName kind) {
			super(kind.toString());
			this.kind = kind;
		}
		@Override
		public void accept(FlowGraphVisitor v) {
			v.visitSpecialNode(this);
		}		
	}
	private DedicatedNode splitNode() { return new DedicatedNode(DedicatedNodeName.SPLIT); }
	private DedicatedNode joinNode() { return new DedicatedNode(DedicatedNodeName.JOIN); }
	
	
	/**
	 * Flow graph nodes representing basic blocks
	 */
	public class BasicBlockNode extends FlowGraphNode {
		private int blockIndex;
		public BasicBlockNode(int blockIndex) {
			super("basic("+blockIndex+")");
			this.blockIndex = blockIndex;
		}
		public BasicBlock getCodeBlock() { return blocks.get(blockIndex); }
		public String toString() { return "Block#"+blockIndex; }
		@Override
		public void accept(FlowGraphVisitor v) {
			v.visitBasicBlockNode(this);
		}
		public int getBlockIndex() { return blockIndex; }
	}

	/* IDEA: summary nodes (sub flowgraphs) */
	/**
	 * Invoke nodes (Basic block with exactly one invoke instruction).
	 */
	public class InvokeNode extends BasicBlockNode {
		private InvokeInstruction instr;
		private MethodInfo impl;
		private Pair<ClassInfo, String> referenced;
		private InvokeNode(int blockIndex) {
			super(blockIndex);
		}
		public InvokeNode(int blockIndex, InvokeInstruction instr) {
			super(blockIndex);			
			this.instr = instr;
			this.referenced = appInfo.getReferenced(methodInfo, instr);
			this.name = "invoke("+this.referenced+")";
			/* if virtual / interface, this method has to be resolved first */
			if((instr instanceof INVOKEINTERFACE) || (instr instanceof INVOKEVIRTUAL)) {
			} else {
				this.impl = referenced.fst().getMethodInfo(referenced.snd());
			}
		}
		@Override
		public void accept(FlowGraphVisitor v) { 
			v.visitBasicBlockNode(this);
			v.visitInvokeNode(this);
		}
		public MethodInfo getImpl() {
			return impl;
		}
		public Pair<ClassInfo, String> getReferenced() {
			return referenced;
		}
		/** 
		 * @return true if the invokation denotes an interface, not an implementation
		 */
		public boolean isInterface() {
			return impl == null;
		}
		/**
		 * Create an implementation node from this node
		 * @param impl the implementing method 
		 * @return
		 */
		public InvokeNode createImplNode(MethodInfo impl) {
			InvokeNode n = new InvokeNode(this.getBlockIndex());
			n.name = "invoke("+this.referenced+")";
			n.instr=this.instr;
			n.referenced=this.referenced;
			n.impl=impl;
			return n;
		}
	}
	/*
	 * Flow Graph Edges
	 * ----------------
	 */

	/**
	 * Type of flow graph edges
	 */
	enum EdgeKind { ENTRY_EDGE, EXIT_EDGE, NEXT_EDGE,
					GOTO_EDGE, SELECT_EDGE, BRANCH_EDGE, JSR_EDGE,
					DISPATCH_EDGE, RETURN_EDGE };
	/**
	 * Edges of the flow graph
	 */
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
	private JOPAppInfo appInfo;
	
	/**
	 * Build a new flow graph for the given method
	 * @param method needs attached code (<code>method.getCode() != null</code>)
	 * @throws BadGraphException if the bytecode results in an invalid flow graph
	 */
	public FlowGraph(MethodInfo method) throws BadGraphException  {
		this.methodInfo = method;
		this.appInfo = (JOPAppInfo) method.getCli().appInfo;
		createFlowGraph(method);
		analyseFlowGraph();
	}
	/* worker: create the flow graph */
	private void createFlowGraph(MethodInfo method) {
		blocks = BasicBlock.buildBasicBlocks(method);
		Hashtable<Integer,BasicBlockNode> nodeTable =
			new Hashtable<Integer, BasicBlockNode>();
		graph = new DefaultDirectedGraph<FlowGraphNode,FlowGraphEdge>(FlowGraphEdge.class);
		entry = new DedicatedNode(DedicatedNodeName.ENTRY);
		graph.addVertex(entry);
		exit  = new DedicatedNode(DedicatedNodeName.EXIT);
		graph.addVertex(exit);
		/* Create basic block vertices */
		for(int i = 0; i < blocks.size(); i++) {
			BasicBlock bb = blocks.get(i);
			BasicBlockNode n;
			InvokeInstruction theInvoke = bb.getTheInvokeInstruction(); 
			if(theInvoke != null) {
				n = new InvokeNode(i,theInvoke);
			} else {
				n = new BasicBlockNode(i);
			}
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
	
	/**
	 * load annotations for the flow graph.
	 * TODO: this is very simple, and should be improved.
	 * 
	 * @param wcaMap a map from source lines to loop bounds
	 * @throws BadAnnotationException if an annotations is missing
	 */
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
	/**
	 * resolve all virtual invoke nodes, and replace them by actual implementations
	 */
	public void resolveVirtualInvokes() {
		Vector<InvokeNode> virtualInvokes = new Vector<InvokeNode>();
		/* find virtual invokes */
		for(FlowGraphNode n : this.graph.vertexSet()) {
			if(n instanceof InvokeNode) {
				InvokeNode in = (InvokeNode) n;
				if(in.isInterface()) {
					virtualInvokes.add(in);
				}
			}
		}
		/* replace them */
		for(InvokeNode inv : virtualInvokes) {
			List<MethodInfo> impls = 
				appInfo.findImplementations(inv.referenced.fst(), inv.referenced.snd());
			if(impls.size() == 0) throw new AssertionError("No implementations for "+inv.referenced);
			if(impls.size() == 1) {
				InvokeNode implNode = inv.createImplNode(impls.get(0));
				graph.addVertex(implNode);
				for(FlowGraphEdge inEdge : graph.incomingEdgesOf(inv)) {
					graph.addEdge(graph.getEdgeSource(inEdge), implNode, new FlowGraphEdge(inEdge.kind));
				}
				for(FlowGraphEdge outEdge : graph.outgoingEdgesOf(inv)) {
					graph.addEdge(implNode, graph.getEdgeTarget(outEdge), new FlowGraphEdge(outEdge.kind));
				}
			} else { /* more than one impl, create split/join nodes */
				FlowGraphNode split = splitNode();
				graph.addVertex(split);
				for(FlowGraphEdge inEdge : graph.incomingEdgesOf(inv)) {
					graph.addEdge(graph.getEdgeSource(inEdge), split, new FlowGraphEdge(inEdge.kind));
				}
				FlowGraphNode join  = joinNode();
				graph.addVertex(join);
				for(FlowGraphEdge outEdge : graph.outgoingEdgesOf(inv)) {
					graph.addEdge(join, graph.getEdgeTarget(outEdge), new FlowGraphEdge(outEdge.kind));
				}
				for(MethodInfo impl : impls) {
					InvokeNode implNode = inv.createImplNode(impl);
					graph.addVertex(implNode);
					graph.addEdge(split,implNode, new FlowGraphEdge(EdgeKind.DISPATCH_EDGE));
					graph.addEdge(implNode,join, new FlowGraphEdge(EdgeKind.RETURN_EDGE));
				}
			}
			graph.removeVertex(inv);
		}
	}	

	/* worker: run loop detection */
	private void analyseFlowGraph() throws BadGraphException {
		TopOrder.checkConnected(graph);
		topOrder = new TopOrder<FlowGraphNode, FlowGraphEdge>(this.graph, this.entry);
		loopColoring = new LoopColoring<FlowGraphNode, FlowGraphEdge>(this.graph,topOrder);
	}

	public MethodInfo getMethodInfo() {
		return this.methodInfo;
	}

	/**
	 * @return the (dedicated) entry node of the flow graph 
	 */
	public FlowGraphNode getEntry() {
		return entry;
	}
	/**
	 * @return the (dedicated) exit node of the flow graph
	 */
	public FlowGraphNode getExit() {
		return exit;
	}
	/**
	 * @return the underlying graph datastructure
	 */
	public DirectedGraph<FlowGraphNode, FlowGraphEdge> getGraph() {
		return graph;
	}
	/**
	 * @return the loop coloring of the flow graph
	 */
	public LoopColoring<FlowGraphNode, FlowGraphEdge> getLoopColoring() {
		return loopColoring;
	}
	
	public Collection<FlowGraphNode> getHeadOfLoops() {
		return this.loopColoring.getHeadOfLoops().keySet();
	}
	public boolean isHeadOfLoop(BasicBlockNode n) {
		return this.loopColoring.getHeadOfLoops().containsKey(n);
	}
	
	public Collection<FlowGraphEdge> getBackEdgesTo(FlowGraphNode hol) {
		Vector<FlowGraphEdge> edges = new Vector<FlowGraphEdge>();
		for(FlowGraphNode n : this.loopColoring.getHeadOfLoops().get(hol)) {
			edges.add(graph.getEdge(n, hol));
		}
		return edges;
	}
	public boolean isBackEdge(FlowGraphEdge edge) {
		return this.topOrder.getBackEdges().contains(edge);
	}

	public Set<FlowGraphNode> getLoopEntrySet(FlowGraphEdge edge) {
		if(this.loopColoring.getHeadOfLoops().isEmpty()) return new HashSet<FlowGraphNode>();
		Set<FlowGraphNode> setSource = getLoopColor(graph.getEdgeSource(edge));
		Set<FlowGraphNode> setTarget = new TreeSet<FlowGraphNode>(getLoopColor(graph.getEdgeTarget(edge)));
		setTarget.removeAll(setSource);
		return setTarget;
	}
	public Collection<FlowGraphEdge> getExitEdgesOf(FlowGraphNode hol) {
		return this.loopColoring.getExitEdges().get(hol);
	}
	public Set<FlowGraphNode> getLoopExitSet(FlowGraphEdge edge) {
		if(this.loopColoring.getHeadOfLoops().isEmpty()) return new HashSet<FlowGraphNode>();
		Set<FlowGraphNode> setSource = new TreeSet<FlowGraphNode>(getLoopColor(graph.getEdgeSource(edge)));
		Set<FlowGraphNode> setTarget = getLoopColor(graph.getEdgeTarget(edge));
		setSource.removeAll(setTarget);
		return setSource;
	}
	public Set<FlowGraphNode> getLoopColor(FlowGraphNode node) {
		if(this.loopColoring.getHeadOfLoops().isEmpty()) return new HashSet<FlowGraphNode>();
		return loopColoring.getLoopColors().get(node);
	}
	public Map<FlowGraphNode, Integer> getLoopBounds() {
		return this.annotations;
	}
	public void exportDOT(File file) {
		exportDOT(file,null,null);
	}

	
	
	public int getNumberOfBytes() {
		int sum = 0;
		for(BasicBlock bb : this.blocks) {
			sum += bb.getNumberOfBytes();
		}
		return sum;
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
}
