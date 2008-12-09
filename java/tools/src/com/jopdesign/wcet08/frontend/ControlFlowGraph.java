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
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.Vector;
import org.apache.bcel.generic.INVOKEINTERFACE;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InvokeInstruction;
import org.jgrapht.graph.EdgeReversedGraph;

import com.jopdesign.build.MethodInfo;
import com.jopdesign.dfa.analyses.LoopBounds;
import com.jopdesign.wcet08.Project;
import com.jopdesign.wcet08.frontend.BasicBlock.FlowInfo;
import com.jopdesign.wcet08.frontend.BasicBlock.FlowTarget;
import com.jopdesign.wcet08.frontend.SourceAnnotations.BadAnnotationException;
import com.jopdesign.wcet08.frontend.SourceAnnotations.LoopBound;
import com.jopdesign.wcet08.graphutils.DefaultFlowGraph;
import com.jopdesign.wcet08.graphutils.FlowGraph;
import com.jopdesign.wcet08.graphutils.LoopColoring;
import com.jopdesign.wcet08.graphutils.TopOrder;
import com.jopdesign.wcet08.graphutils.TopOrder.BadGraphException;

/**
 * General purpose control flow graph, for use in WCET analysis. 
 * 
 * <p>
 * A flow graph is a directed graph with a dedicated entry and exit node.
 * Nodes include dedicated nodes (like entry, exit, split, join), basic block nodes
 * and invoke nodes. Edges carry information about the associated (branch) instruction.
 * The basic blocks associated with the CFG are stored seperately are referenced from
 * basic block nodes.
 * </p>
 * 
 * <p>
 * This class supports 
 * <ul>
 *   <li/> loop detection
 *   <li/> extracting annotations from the source code
 *   <li/> resolving virtual invokations (possible, as all methods are known at compile time)
 *   <li/> inserting split nodes for nodes with more than one successor
 * </ul></p>
 * 
 *
 * @author Benedikt Huber (benedikt.huber@gmail.com)
 *
 */
public class ControlFlowGraph {
	/**
	 * Visitor for flow graph nodes
	 */
	public interface CfgVisitor {
		public void visitSpecialNode(DedicatedNode n);
		public void visitBasicBlockNode(BasicBlockNode n);
		/**
		 * visit an invoke node. InvokeNode's won't call visitBasicBlockNode.
		 */
		public void visitInvokeNode(InvokeNode n);
	}
	
	/**
	 * Abstract base class for flow graph nodes
	 *
	 */
	public abstract class CFGNode implements Comparable<CFGNode>{
		private int id;
		protected String name;
		protected CFGNode(String name) { 
			this.id = idGen++;
			this.name = name;
		}
		public int compareTo(CFGNode o) { 
			return new Integer(this.hashCode()).compareTo(o.hashCode()); 
		}
		public String toString() { return "#"+id+" "+name; }
		public String getName()  { return name; }
		public BasicBlock getBasicBlock() { return null; }
		public int getId() { return id; }
		void setId(int newId) { this.id = newId; }
		public abstract void accept(CfgVisitor v);
	}

	/**
	 * Names for dedicated nodes (entry node, exit node)
	 */
	public enum DedicatedNodeName { ENTRY, EXIT, SPLIT, JOIN };

	/**
	 * Dedicated flow graph nodes
	 */
	public class DedicatedNode extends CFGNode {
		private DedicatedNodeName kind;
		public DedicatedNodeName getKind() { return kind; }
		private  DedicatedNode(DedicatedNodeName kind) {
			super(kind.toString());
			this.kind = kind;
		}
		@Override
		public void accept(CfgVisitor v) {
			v.visitSpecialNode(this);
		}		
	}
	private DedicatedNode splitNode() { return new DedicatedNode(DedicatedNodeName.SPLIT); }
	private DedicatedNode joinNode() { return new DedicatedNode(DedicatedNodeName.JOIN); }
	
	
	/**
	 * Flow graph nodes representing basic blocks
	 */
	public class BasicBlockNode extends CFGNode {
		protected int blockIndex;
		public BasicBlockNode(int blockIndex) {
			super("basic("+blockIndex+")");
			this.blockIndex = blockIndex;
		}
		public BasicBlock getBasicBlock() { return blocks.get(blockIndex); }
		@Override
		public void accept(CfgVisitor v) {
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
		private MethodRef referenced;
		private MethodInfo impl;
		private ControlFlowGraph fg;
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
				impl = null;
			} else {
				impl = appInfo.findStaticImplementation(referenced);
			}
		}
		@Override
		public void accept(CfgVisitor v) { 
			v.visitInvokeNode(this);
		}
		public InstructionHandle getInstructionHandle() {
			return ControlFlowGraph.this.blocks.get(blockIndex).getLastInstruction();
		}
		public MethodInfo getImplementedMethod() {
			return this.impl;
		}
		public ControlFlowGraph receiverFlowGraph() {
			if(isInterface()) return null;
			if(this.fg == null) {
				this.fg = appInfo.getFlowGraph(impl);
			}
			return this.fg;
		}
		public MethodRef getReferenced() {
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
			n.impl = impl;
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
					DISPATCH_EDGE, RETURN_EDGE, FLOW_EDGE };
	/**
	 * Edges of the flow graph
	 */
	public class CFGEdge  {
		EdgeKind kind;
		public EdgeKind getKind() { return kind; }
		public CFGEdge(EdgeKind kind) {
			this.kind = kind;
		}
		public String getName() {
			return graph.getEdgeSource(this)+"_"+graph.getEdgeTarget(this);
		}
		public String toString() {
			return (""+graph.getEdgeSource(this).id+"->"+graph.getEdgeTarget(this).id);
		}
		public CFGEdge clone() {
			return new CFGEdge(kind);			
		}
	}
	CFGEdge entryEdge() { return new CFGEdge(EdgeKind.ENTRY_EDGE); }
	CFGEdge exitEdge() { return new CFGEdge(EdgeKind.EXIT_EDGE); }
	CFGEdge nextEdge() { return new CFGEdge(EdgeKind.NEXT_EDGE); }

	/*
	 * Fields
	 * ------
	 */
	private int id; 
	public int getId() {
		return id;
	}
	private int idGen = 0;

	/* linking to java */
	private MethodInfo  methodInfo;
	private WcetAppInfo appInfo;
	private Vector<BasicBlock> blocks;
	
	/* graph */
	private FlowGraph<CFGNode, CFGEdge> graph;

	/* annotations */
	private Map<CFGNode, LoopBound> annotations;
	
	/* analysis stuff, needs to be reevaluated when graph changes */
	private TopOrder<CFGNode, CFGEdge> topOrder = null;
	private LoopColoring<CFGNode, CFGEdge> loopColoring = null;
	
	/**
	 * Build a new flow graph for the given method
	 * @param method needs attached code (<code>method.getCode() != null</code>)
	 * @throws BadGraphException if the bytecode results in an invalid flow graph
	 */
	public ControlFlowGraph(int id, WcetAppInfo wcetAi, MethodInfo method) throws BadGraphException {
		this.id = id;
		this.methodInfo = method;
		this.appInfo = wcetAi;
		createFlowGraph(method);
		check();
	}

	/* worker: create the flow graph */
	@SuppressWarnings("static-access")
	private void createFlowGraph(MethodInfo method) {
		WcetAppInfo.logger.info("creating flow graph for: "+method);
		blocks = BasicBlock.buildBasicBlocks(this.appInfo,method);
		Hashtable<Integer,BasicBlockNode> nodeTable =
			new Hashtable<Integer, BasicBlockNode>();
		graph = new DefaultFlowGraph<CFGNode,CFGEdge>(
						CFGEdge.class,
						new DedicatedNode(DedicatedNodeName.ENTRY),
						new DedicatedNode(DedicatedNodeName.EXIT));
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
		graph.addEdge(graph.getEntry(), 
					  nodeTable.get(blocks.get(0).getFirstInstruction().getPosition()), 
					  entryEdge());
		/* flow edges */
		for(BasicBlockNode bbNode : nodeTable.values()) {
			BasicBlock bb = bbNode.getBasicBlock();
			FlowInfo bbf = bb.getFlowInfo(bb.getLastInstruction());
			if(bbf.exit) { // exit edge
				graph.addEdge(bbNode, graph.getExit(), exitEdge());
			} else if(! bbf.alwaysTaken) { // next block edge
				graph.addEdge(bbNode, 
							  nodeTable.get(bbNode.getBasicBlock().getLastInstruction().getNext().getPosition()),
							  new CFGEdge(EdgeKind.NEXT_EDGE));
			}
			for(FlowTarget target: bbf.targets) { // jmps
				BasicBlockNode targetNode = nodeTable.get(target.target.getPosition());
				if(targetNode == null) {
					throw new AssertionError("No node for flow target: "+bbNode+" -> "+target);
				}
				graph.addEdge(bbNode, 
							  targetNode, 
							  new CFGEdge(target.edgeKind));
			}
		}	
	}
	
	/**
	 * load annotations for the flow graph.
	 * 
	 * @param wcaMap a map from source lines to loop bounds
	 * @throws BadAnnotationException if an annotations is missing
	 */
	public void loadAnnotations(Project p) throws BadAnnotationException {
		SortedMap<Integer, LoopBound> wcaMap = p.getAnnotations(this.methodInfo.getCli());
		this.annotations = new HashMap<CFGNode, LoopBound>();
		for(CFGNode n : this.getLoopColoring().getHeadOfLoops()) {
			BasicBlockNode headOfLoop = (BasicBlockNode) n;
			BasicBlock block = headOfLoop.getBasicBlock();
			// search for loop annotation in range
			int sourceRangeStart = BasicBlock.getLineNumber(block.getFirstInstruction());
			int sourceRangeStop = BasicBlock.getLineNumber(block.getLastInstruction());
			SortedMap<Integer,LoopBound> annots = wcaMap.subMap(sourceRangeStart, sourceRangeStop+1);
			if(annots.size() > 1) {
				String reason = "Ambigous Annotation [" + annots + "]"; 
				throw new BadAnnotationException(reason,block,sourceRangeStart,sourceRangeStop);
			}
			LoopBound loopAnnot = null;
			if(annots.size() == 1) {
				loopAnnot = annots.get(annots.firstKey());
			}
			// if we have loop bounds from DFA analysis, use them
			if(p.getDfaLoopBounds() != null) {
				LoopBounds lbs = p.getDfaLoopBounds();
				int bound = lbs.getBound(p.getDfaProgram(), block.getLastInstruction());
				if(bound < 0) {
					WcetAppInfo.logger.info("No DFA bound for " + methodInfo+":"+n);					
				} else if(loopAnnot == null) {
					WcetAppInfo.logger.info("Only DFA bound for "+methodInfo+":"+n);
					loopAnnot = LoopBound.boundedAbove(bound);
				} else {
					int loopUb = loopAnnot.getUpperBound();
					if(bound < loopUb) {
						WcetAppInfo.logger.info("DFA analysis reports a smaller upper bound :"+bound+ " < "+loopUb+
								"for "+methodInfo+":"+n);
						//loopAnnot = LoopBound.boundedAbove(bound); [currently unsafe]
					} else if (bound > loopUb) {
						WcetAppInfo.logger.info("DFA analysis reports a larger upper bound: "+bound+ " > "+loopUb+
								"for "+methodInfo+":"+n);
					} else {
						WcetAppInfo.logger.info("DFA and annotated loop bounds match: "+methodInfo+":"+n);
					}
				}
			}
			if(loopAnnot == null) {
				throw new BadAnnotationException("No loop bound annotation",
												 block,sourceRangeStart,sourceRangeStop);
			}
			this.annotations.put(headOfLoop,loopAnnot);
		}
	}
	
	/**
	 * resolve all virtual invoke nodes, and replace them by actual implementations
	 * @throws BadGraphException If the flow graph analysis (post replacement) fails
	 */
	public void resolveVirtualInvokes() throws BadGraphException {
		Vector<InvokeNode> virtualInvokes = new Vector<InvokeNode>();
		/* find virtual invokes */
		for(CFGNode n : this.graph.vertexSet()) {
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
				appInfo.findImplementations(this.methodInfo,inv.getInstructionHandle());
			if(impls.size() == 0) throw new AssertionError("No implementations for "+inv.referenced);
			if(impls.size() == 1) {
				InvokeNode implNode = inv.createImplNode(impls.get(0));
				graph.addVertex(implNode);
				for(CFGEdge inEdge : graph.incomingEdgesOf(inv)) {
					graph.addEdge(graph.getEdgeSource(inEdge), implNode, new CFGEdge(inEdge.kind));
				}
				for(CFGEdge outEdge : graph.outgoingEdgesOf(inv)) {
					graph.addEdge(implNode, graph.getEdgeTarget(outEdge), new CFGEdge(outEdge.kind));
				}
			} else { /* more than one impl, create split/join nodes */
				CFGNode split = splitNode();
				graph.addVertex(split);
				for(CFGEdge inEdge : graph.incomingEdgesOf(inv)) {
					graph.addEdge(graph.getEdgeSource(inEdge), split, new CFGEdge(inEdge.kind));
				}
				CFGNode join  = joinNode();
				graph.addVertex(join);
				for(CFGEdge outEdge : graph.outgoingEdgesOf(inv)) {
					graph.addEdge(join, graph.getEdgeTarget(outEdge), new CFGEdge(outEdge.kind));
				}
				for(MethodInfo impl : impls) {
					InvokeNode implNode = inv.createImplNode(impl);
					graph.addVertex(implNode);
					graph.addEdge(split,implNode, new CFGEdge(EdgeKind.DISPATCH_EDGE));
					graph.addEdge(implNode,join, new CFGEdge(EdgeKind.RETURN_EDGE));
				}
			}
			graph.removeVertex(inv);
		}
		this.invalidate();
		this.check();
		this.analyseFlowGraph();
	}	

	/**
	 * For all BasicBlock nodes with more than one outgoing edge,
	 * add a split node, s.t. after this transformation all basic block nodes
	 * have a single outgoing edge.
	 * @throws BadGraphException 
	 */
	public void insertSplitNodes() throws BadGraphException {
		Vector<CFGNode> trav = this.getTopOrder().getTopologicalTraversal();
		for(CFGNode n : trav) {
			if(n instanceof BasicBlockNode && graph.outDegreeOf(n) > 1) {
				DedicatedNode splitNode = this.splitNode();
				graph.addVertex(splitNode);
				/* copy, as the iterators don't work when removing elements while iterating */
				Vector<CFGEdge> outEdges = new Vector<CFGEdge>(graph.outgoingEdgesOf(n));
				/* move edges */
				for(CFGEdge e : outEdges) {
					graph.addEdge(splitNode, graph.getEdgeTarget(e),e.clone());
					graph.removeEdge(e);
				}
				graph.addEdge(n,splitNode,new CFGEdge(EdgeKind.FLOW_EDGE));
			}
		}
		this.invalidate();
		this.check();
		this.analyseFlowGraph();
	}
	
	private void check() throws BadGraphException {
		TopOrder.checkConnected(graph);
		TopOrder.checkIsExitNode(graph, this.graph.getExit());
		List<CFGNode> deads = TopOrder.findDeadNodes(graph,this.graph.getEntry());
		if(deads.size() > 0) {
			WcetAppInfo.logger.error("Found dead code - this most likely indicates a bug. "+deads);
		}
	}
	private void invalidate() {
		this.topOrder = null;
		this.loopColoring = null;
	}

	/* flow graph should have been checked before analyseFlowGraph is called */
	private void analyseFlowGraph() {
		try {			
			topOrder = new TopOrder<CFGNode, CFGEdge>(this.graph, this.graph.getEntry());
			idGen = 0;
			for(CFGNode vertex : topOrder.getTopologicalTraversal()) vertex.id = idGen++;
			for(CFGNode vertex : TopOrder.findDeadNodes(graph,this.graph.getEntry())) vertex.id = idGen++;
			loopColoring = new LoopColoring<CFGNode, CFGEdge>(this.graph,topOrder,graph.getExit());
		} catch (BadGraphException e) {
			WcetAppInfo.logger.error("Bad flow graph: "+getGraph().toString());
			throw new Error("[FATAL] Analyse flow graph failed ",e);
		}
	}
	
	/**
	 * get the method this flow graph models
	 * @return the MethodInfo the flow graph was build from
	 */
	public MethodInfo getMethodInfo() {
		return this.methodInfo;
	}

	/**
	 * the (dedicated) entry node of the flow graph 
	 * @return 
	 */
	public CFGNode getEntry() {
		return graph.getEntry();
	}
	/**
	 * the (dedicated) exit node of the flow graph
	 * @return 
	 */
	public CFGNode getExit() {
		return graph.getExit();
	}
	/**
	 * Get the actual flow graph
	 * @return 
	 */
	public FlowGraph<CFGNode, CFGEdge> getGraph() {
		return graph;
	}
	
	/**
	 * retrieve the loop bound (annotations)
	 * @return a map from head-of-loop nodes to their loop bounds
	 */
	public Map<CFGNode, LoopBound> getLoopBounds() {
		return this.annotations;
	}

	/**
	 * Calculate (cached) the "loop coloring" of the flow graph.
	 * 
	 * @return a loop coloring assigning each flowgraph node the set of loops it
	 * participates in 
	 */
	public LoopColoring<CFGNode, CFGEdge> getLoopColoring() {
		if(loopColoring == null) analyseFlowGraph();
		return loopColoring;
	}
	public TopOrder<CFGNode, CFGEdge> getTopOrder() {
		if(topOrder == null) analyseFlowGraph();
		return topOrder;
	}
	/**
	 * Get the length of the implementation
	 * @return the length in bytes
	 */
	public int getNumberOfBytes() {
		int sum = 0;
		for(BasicBlock bb : this.blocks) {
			sum += bb.getNumberOfBytes();
		}
		return sum;
	}
	public void exportDOT(File file) {
		exportDOT(file,null,null);
	}

	public void exportDOT(File file, Map<CFGNode, ?> nodeAnnotations, Map<CFGEdge, ?> edgeAnnotations) {
		CFGExport export = new CFGExport(this, nodeAnnotations, edgeAnnotations);		
		try {
			FileWriter w = new FileWriter(file);
			export.exportDOT(w, graph);
			w.close();
		} catch (IOException e) {
			e.printStackTrace();
		}				
	}
	@Override public String toString() {
		return super.toString()+this.methodInfo.getFQMethodName();
	}
}
