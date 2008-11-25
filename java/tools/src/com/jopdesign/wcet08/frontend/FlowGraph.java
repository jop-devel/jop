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
import org.apache.bcel.generic.INVOKEINTERFACE;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InvokeInstruction;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;

import com.jopdesign.build.MethodInfo;
import com.jopdesign.dfa.analyses.LoopBounds;
import com.jopdesign.wcet.WCETInstruction;
import com.jopdesign.wcet08.Project;
import com.jopdesign.wcet08.frontend.BasicBlock.FlowInfo;
import com.jopdesign.wcet08.frontend.BasicBlock.FlowTarget;
import com.jopdesign.wcet08.frontend.SourceAnnotations.BadAnnotationException;
import com.jopdesign.wcet08.frontend.SourceAnnotations.LoopBound;
import com.jopdesign.wcet08.graphutils.IDProvider;
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
 * </ul></p>
 * 
 * <p>
 * Planned:
 * <ul>
 *   <li/> loop unpeeling
 *   <li/> folding
 * </ul></p>
 * *
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
		/**
		 * visit an invoke node. InvokeNode's won't call visitBasicBlockNode.
		 */
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
		public BasicBlock getBasicBlock() { return null; }
		public int getId() { return id; }
		void setId(int newId) { this.id = newId; }
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
		protected int blockIndex;
		public BasicBlockNode(int blockIndex) {
			super("basic("+blockIndex+")");
			this.blockIndex = blockIndex;
		}
		public BasicBlock getBasicBlock() { return blocks.get(blockIndex); }
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
		private MethodRef referenced;
		private MethodInfo impl;
		private FlowGraph fg;
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
		public void accept(FlowGraphVisitor v) { 
			v.visitInvokeNode(this);
		}
		public InstructionHandle getInstructionHandle() {
			return FlowGraph.this.blocks.get(blockIndex).getLastInstruction();
		}
		public MethodInfo getImplementedMethod() {
			return this.impl;
		}
		public FlowGraph getFlowGraph() {
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
	private FlowGraphNode entry, exit;
	private DirectedGraph<FlowGraphNode, FlowGraphEdge> graph;

	/* annotations */
	private Map<FlowGraphNode, LoopBound> annotations;
	
	/* analysis stuff, needs to be reevaluted when graph changes */
	private TopOrder<FlowGraphNode, FlowGraphEdge> topOrder = null;
	private LoopColoring<FlowGraphNode, FlowGraphEdge> loopColoring = null;
	
	/**
	 * Build a new flow graph for the given method
	 * @param method needs attached code (<code>method.getCode() != null</code>)
	 * @throws BadGraphException if the bytecode results in an invalid flow graph
	 */
	public FlowGraph(int id, WcetAppInfo wcetAi, MethodInfo method) throws BadGraphException {
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
			BasicBlock bb = bbNode.getBasicBlock();
			FlowInfo bbf = bb.getFlowInfo(bb.getLastInstruction());
			if(bbf.exit) { // exit edge
				graph.addEdge(bbNode, this.exit, exitEdge());
			} else if(! bbf.alwaysTaken) { // next block edge
				graph.addEdge(bbNode, 
							  nodeTable.get(bbNode.getBasicBlock().getLastInstruction().getNext().getPosition()),
							  new FlowGraphEdge(EdgeKind.NEXT_EDGE));
			}
			for(FlowTarget target: bbf.targets) { // jmps
				BasicBlockNode targetNode = nodeTable.get(target.target.getPosition());
				if(targetNode == null) {
					throw new AssertionError("No node for flow target: "+bbNode+" -> "+target);
				}
				graph.addEdge(bbNode, 
							  targetNode, 
							  new FlowGraphEdge(target.edgeKind));
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
		this.annotations = new HashMap<FlowGraphNode, LoopBound>();
		for(FlowGraphNode n : this.getHeadOfLoops()) {
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
					WcetAppInfo.logger.info("No DFA bound for " + n);					
				} else if(loopAnnot == null) {
					WcetAppInfo.logger.info("Only DFA bound for "+ n);
					loopAnnot = LoopBound.boundedAbove(bound);
				} else {
					int loopUb = loopAnnot.getUpperBound();
					if(bound < loopUb) {
						WcetAppInfo.logger.warn("DFA analysis reports a smaller upper bound :"+bound+ " < "+loopUb);
						//loopAnnot = LoopBound.boundedAbove(bound); [currently unsafe]
					} else if (bound > loopUb) {
						WcetAppInfo.logger.warn("DFA analysis reports a larger upper bound: "+bound+ " > "+loopUb);
					} else {}
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
				appInfo.findImplementations(this.methodInfo,inv.getInstructionHandle());
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
		this.invalidate();
		this.check();
	}	

	/**
	 * Freeze the flowgraph into an ArrayGraph, and assign the nodes ids from 0 to n-1.
	 * The entry is garantueed to be node 0, and the exit node n-1
	 */
	/*
	 * NOT YET IN CVS
	public ArrayGraph<FlowGraphNode,FlowGraphEdge> getArrayGraph() {
		if(topOrder == null) analyseFlowGraph();
		int i = 0;		
		for(FlowGraphNode n : this.topOrder.getTopologicalTraversal()) {
			n.setId(i++);
		}
		return new ArrayGraph<FlowGraphNode,FlowGraphEdge>(
				new IDProvider<FlowGraphNode>() {
					public FlowGraphNode fromID(int id) { return null; }
					public int getID(FlowGraphNode t) {
						return t.getId();
					}},graph);
	}
	*/
	private void check() throws BadGraphException {
		TopOrder.checkConnected(graph);
		TopOrder.checkIsExitNode(graph, this.exit);
		List<FlowGraphNode> deads = TopOrder.findDeadNodes(graph,this.entry);
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
			topOrder = new TopOrder<FlowGraphNode, FlowGraphEdge>(this.graph, this.entry);
			loopColoring = new LoopColoring<FlowGraphNode, FlowGraphEdge>(this.graph,topOrder);
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
	public FlowGraphNode getEntry() {
		return entry;
	}
	/**
	 * the (dedicated) exit node of the flow graph
	 * @return 
	 */
	public FlowGraphNode getExit() {
		return exit;
	}
	/**
	 * Get the actual flow graph
	 * @return 
	 */
	public DirectedGraph<FlowGraphNode, FlowGraphEdge> getGraph() {
		return graph;
	}
	
	/**
	 * retrieve the loop bound (annotations)
	 * @return a map from head-of-loop nodes to their loop bounds
	 */
	public Map<FlowGraphNode, LoopBound> getLoopBounds() {
		return this.annotations;
	}

	/**
	 * Calculate (cached) the "loop coloring" of the flow graph.
	 * 
	 * @return a loop coloring assigning each flowgraph node the set of loops it
	 * participates in 
	 */
	public LoopColoring<FlowGraphNode, FlowGraphEdge> getLoopColoring() {
		if(loopColoring == null) analyseFlowGraph();
		return loopColoring;
	}
	/**
	 * Calculate (cached) the "head of loops" of the flow graph
	 * 
	 * @return the set of "head of loop" nodes
	 */
	public Collection<FlowGraphNode> getHeadOfLoops() {
		if(loopColoring == null) analyseFlowGraph();
		return this.loopColoring.getHeadOfLoops().keySet();
	}
	/**
	 * check wheter the given basic block node is a "head of loop"
	 * @param n the node to check
	 * @return
	 */
	public boolean isHeadOfLoop(BasicBlockNode n) {
		if(loopColoring == null) analyseFlowGraph();
		return this.loopColoring.getHeadOfLoops().containsKey(n);
	}
	/**
	 * Get "back edges" to the given flow graph node
	 * @param hol a "head of loop" node 
	 * @return
	 */
	public Collection<FlowGraphEdge> getBackEdgesTo(FlowGraphNode hol) {
		if(loopColoring == null) analyseFlowGraph();
		Vector<FlowGraphEdge> edges = new Vector<FlowGraphEdge>();
		for(FlowGraphNode n : this.loopColoring.getHeadOfLoops().get(hol)) {
			edges.add(graph.getEdge(n, hol));
		}
		return edges;
	}
	/**
	 * test whether the given edge is a "back-edge"
	 * @param edge the edge to test
	 * @return
	 */
	public boolean isBackEdge(FlowGraphEdge edge) {
		if(topOrder == null) analyseFlowGraph();
		return this.topOrder.getBackEdges().contains(edge);
	}

	public Set<FlowGraphNode> getLoopEntrySet(FlowGraphEdge edge) {
		if(loopColoring == null) analyseFlowGraph();
		/* no loops */
		if(this.loopColoring.getHeadOfLoops().isEmpty()) return new HashSet<FlowGraphNode>();
		
		Set<FlowGraphNode> setSource = getLoopColor(graph.getEdgeSource(edge));
		Set<FlowGraphNode> setTarget = new TreeSet<FlowGraphNode>(getLoopColor(graph.getEdgeTarget(edge)));
		setTarget.removeAll(setSource);
		return setTarget;
	}
	public Collection<FlowGraphEdge> getExitEdgesOf(FlowGraphNode hol) {
		if(loopColoring == null) analyseFlowGraph();
		return this.loopColoring.getExitEdges().get(hol);
	}
	public Set<FlowGraphNode> getLoopExitSet(FlowGraphEdge edge) {
		if(loopColoring == null) analyseFlowGraph();
		/* no loops */
		if(this.loopColoring.getHeadOfLoops().isEmpty()) return new HashSet<FlowGraphNode>();
		Set<FlowGraphNode> setSource = new TreeSet<FlowGraphNode>(getLoopColor(graph.getEdgeSource(edge)));
		Set<FlowGraphNode> setTarget = getLoopColor(graph.getEdgeTarget(edge));
		setSource.removeAll(setTarget);
		return setSource;
	}
	public Set<FlowGraphNode> getLoopColor(FlowGraphNode node) {
		if(loopColoring == null) analyseFlowGraph();
		if(this.loopColoring.getHeadOfLoops().isEmpty()) return new HashSet<FlowGraphNode>();
		return loopColoring.getLoopColors().get(node);
	}
	
	public void exportDOT(File file) {
		exportDOT(file,null,null);
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
	/**
	 * Estimate the WCET of a basic block (only local effects) for debugging purposes
	 * @param b the basic block
	 * @return the cost of executing the basic block, without cache misses
	 */
	public int basicBlockWCETEstimate(BasicBlock b) {
		int wcet = 0;
		for(InstructionHandle ih : b.getInstructions()) {
			int jopcode = appInfo.getJOpCode(b.getClassInfo(), ih.getInstruction());
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
	@Override public String toString() {
		return super.toString()+this.methodInfo.getFQMethodName();
	}
}
