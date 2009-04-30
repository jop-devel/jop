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
package com.jopdesign.wcet.frontend;

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
import java.util.Vector;

import org.apache.bcel.generic.INVOKEINTERFACE;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InvokeInstruction;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;

import com.jopdesign.build.MethodInfo;
import com.jopdesign.dfa.analyses.LoopBounds;
import com.jopdesign.dfa.analyses.Pair;
import com.jopdesign.dfa.analyses.LoopBounds.ValueMapping;
import com.jopdesign.dfa.framework.ContextMap;
import com.jopdesign.dfa.framework.HashedString;
import com.jopdesign.wcet.Project;
import com.jopdesign.wcet.frontend.BasicBlock.FlowInfo;
import com.jopdesign.wcet.frontend.BasicBlock.FlowTarget;
import com.jopdesign.wcet.frontend.SourceAnnotations.BadAnnotationException;
import com.jopdesign.wcet.frontend.SourceAnnotations.LoopBound;
import com.jopdesign.wcet.graphutils.AdvancedDOTExporter;
import com.jopdesign.wcet.graphutils.DefaultFlowGraph;
import com.jopdesign.wcet.graphutils.FlowGraph;
import com.jopdesign.wcet.graphutils.LoopColoring;
import com.jopdesign.wcet.graphutils.MiscUtils;
import com.jopdesign.wcet.graphutils.TopOrder;
import com.jopdesign.wcet.graphutils.TopOrder.BadGraphException;

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
	public static class ControlFlowError extends Error{
		private static final long serialVersionUID = 1L;
		private ControlFlowGraph cfg;
		public ControlFlowGraph getAffectedCFG() {
			return cfg;
		}
		public ControlFlowError(String msg) {
			super("Error in Control Flow Graph: " + msg);
		}
		public ControlFlowError(String msg, ControlFlowGraph cfg) {
			this(msg);
			this.cfg = cfg;
		}
	}
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
		public void visitSummaryNode(SummaryNode n);
	}
	
	/**
	 * Abstract base class for flow graph nodes
	 *
	 */
	public abstract static class CFGNode implements Comparable<CFGNode>{
		private int id;
		protected String name;
		protected CFGNode(int id, String name) { 
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
			super(idGen++, kind.toString());
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
			super(idGen++, "basic("+blockIndex+")");
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
		private MethodInfo receiverImpl;
		private ControlFlowGraph receiverFlowGraph;
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
				receiverImpl = null;
			} else {
				receiverImpl = appInfo.findStaticImplementation(referenced);
			}
		}
		@Override
		public void accept(CfgVisitor v) { 
			v.visitInvokeNode(this);
		}
		public InstructionHandle getInstructionHandle() {
			return ControlFlowGraph.this.blocks.get(blockIndex).getLastInstruction();
		}
		/** For non-virtual methods, get the implementation of the method */
		public MethodInfo getImplementedMethod() {
			return this.receiverImpl;
		}
		/** Get all possible implementations of the invoked method */
		public List<MethodInfo> getImplementedMethods() {
			if(! isInterface()) {
				List<MethodInfo> impls = new Vector<MethodInfo>();
				impls.add(getImplementedMethod());
				return impls;
			} else {
				return appInfo.findImplementations(this.invokerFlowGraph().getMethodInfo(),
                        						   getInstructionHandle());
			}
		}
		
		/** For non-virtual methods, get the implementation of the method */
		public ControlFlowGraph receiverFlowGraph() {
			if(isInterface()) return null;
			if(this.receiverFlowGraph == null) {
				this.receiverFlowGraph = appInfo.getFlowGraph(receiverImpl);
			}
			return this.receiverFlowGraph;
		}		
		
		public ControlFlowGraph invokerFlowGraph() {
			return ControlFlowGraph.this;
		}
		public MethodRef getReferenced() {
			return referenced;
		}
		/** 
		 * @return true if the invokation denotes an interface, not an implementation
		 */
		public boolean isInterface() {
			return receiverImpl == null;
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
			n.receiverImpl = impl;
			return n;
		}
	}
	
	/**
	 * Invoke nodes (Basic block with exactly one invoke instruction).
	 */
	public class SpecialInvokeNode extends InvokeNode {
		private InstructionHandle instr;
		private MethodInfo receiverImpl;
		private ControlFlowGraph receiverFlowGraph;
		private SpecialInvokeNode(int blockIndex) {
			super(blockIndex);
		}
		public SpecialInvokeNode(int blockIndex, MethodInfo javaImpl) {
			this(blockIndex);			
			this.instr = ControlFlowGraph.this.blocks.get(blockIndex).getLastInstruction();
			this.name = "jimplBC("+javaImpl+")";
			this.receiverImpl = javaImpl;
		}
		@Override
		public void accept(CfgVisitor v) { 
			v.visitInvokeNode(this);
		}
		public InstructionHandle getInstructionHandle() {
			return instr;
		}
		public MethodInfo getImplementedMethod() {
			return this.receiverImpl;
		}
		public ControlFlowGraph invokerFlowGraph() {
			return ControlFlowGraph.this;
		}
		public ControlFlowGraph receiverFlowGraph() {
			if(this.receiverFlowGraph == null) {
				this.receiverFlowGraph = appInfo.getFlowGraph(receiverImpl);
			}
			return this.receiverFlowGraph;
		}		
		public MethodRef getReferenced() {
			return MethodRef.fromMethodInfo(receiverImpl);
		}
		/** 
		 * @return true if the invokation denotes an interface, not an implementation
		 */
		public boolean isInterface() {
			return receiverImpl == null;
		}
		@Override
		public InvokeNode createImplNode(MethodInfo impl) {
			return this; /* no dynamic dispatch */
		}
	}

	public class SummaryNode extends CFGNode {

		private ControlFlowGraph subGraph;

		public SummaryNode(String name, ControlFlowGraph subGraph) {
			super(idGen++, name);
			this.subGraph = subGraph;
		}
		public ControlFlowGraph getSubGraph() {
			return subGraph;
		}
		
		@Override
		public void accept(CfgVisitor v) {
			v.visitSummaryNode(this);
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
					DISPATCH_EDGE, 
					INVOKE_EDGE, RETURN_EDGE, FLOW_EDGE };
	/**
	 * Edges of the flow graph
	 */
	public static class CFGEdge extends DefaultEdge {
		private static final long serialVersionUID = 1L;
		EdgeKind kind;
		public EdgeKind getKind() { return kind; }
		public CFGEdge(EdgeKind kind) {
			this.kind = kind;
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
	private Project project;
	private WcetAppInfo appInfo;
	private MethodInfo  methodInfo;
	
	/* basic blocks associated with the CFG */
	private Vector<BasicBlock> blocks;
	
	/* graph */
	private FlowGraph<CFGNode, CFGEdge> graph;

	/* annotations */
	private Map<CFGNode, LoopBound> annotations;
	
	/* analysis stuff, needs to be reevaluated when graph changes */
	private TopOrder<CFGNode, CFGEdge> topOrder = null;
	private LoopColoring<CFGNode, CFGEdge> loopColoring = null;
	private Boolean isLeafMethod = null;
	public boolean isLeafMethod() {
		return isLeafMethod;
	}

	
	/**
	 * Build a new flow graph for the given method
	 * @param method needs attached code (<code>method.getCode() != null</code>)
	 * @throws BadGraphException if the bytecode results in an invalid flow graph
	 */
	public ControlFlowGraph(int id, Project p, MethodInfo method) throws BadGraphException {
		this.id = id;
		this.methodInfo = method;
		this.project = p;
		this.appInfo = p.getWcetAppInfo();
		createFlowGraph(method);
		check();
	}
	private ControlFlowGraph(int id, WcetAppInfo appInfo) {
		this.id = id;
		this.appInfo = appInfo;
		CFGNode subEntry = new DedicatedNode(DedicatedNodeName.ENTRY);
		CFGNode subExit = new DedicatedNode(DedicatedNodeName.EXIT);
		this.graph = 
			new DefaultFlowGraph<CFGNode, CFGEdge>(CFGEdge.class, subEntry, subExit);
	}
	/* worker: create the flow graph */
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
			Instruction lastInstr = bb.getLastInstruction().getInstruction();
			InvokeInstruction theInvoke = bb.getTheInvokeInstruction(); 
			if(theInvoke != null) {
				n = new InvokeNode(i,theInvoke);
			} else if (appInfo.getProcessorModel().isImplementedInJava(lastInstr)) {
				MethodInfo javaImpl = appInfo.getJavaImplementation(bb.getMethodInfo(),lastInstr);
				n = new SpecialInvokeNode(i,javaImpl);
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
				// do not connect exception edges
				if(bbNode.getBasicBlock().getLastInstruction().getInstruction().getOpcode()
				   == org.apache.bcel.Constants.ATHROW) {
					WcetAppInfo.logger.warn("Found ATHROW edge - ignoring");
				} else {
					graph.addEdge(bbNode, graph.getExit(), exitEdge());
				}
			} else if(! bbf.alwaysTaken) { // next block edge
				BasicBlockNode bbSucc = nodeTable.get(bbNode.getBasicBlock().getLastInstruction().getNext().getPosition());
				if(bbSucc == null) {
					internalError("Next Edge to non-existing next block from "+
								  bbNode.getBasicBlock().getLastInstruction());
				}
				graph.addEdge(bbNode, 
							  bbSucc,
							  new CFGEdge(EdgeKind.NEXT_EDGE));
			}
			for(FlowTarget target: bbf.targets) { // jmps
				BasicBlockNode targetNode = nodeTable.get(target.target.getPosition());
				if(targetNode == null) internalError("No node for flow target: "+bbNode+" -> "+target);
				graph.addEdge(bbNode, 
							  targetNode, 
							  new CFGEdge(target.edgeKind));
			}
		}
		this.graph.addEdge(graph.getEntry(), graph.getExit(), exitEdge());
	}
	
	private void internalError(String reason) {
		WcetAppInfo.logger.error("[INTERNAL ERROR] "+reason);
		WcetAppInfo.logger.error("CFG of "+this.getMethodInfo().getFQMethodName()+"\n");
		WcetAppInfo.logger.error(this.getMethodInfo().getMethod().getCode().toString(true));
		throw new AssertionError(reason);		
	}
	private void debugDumpGraph() {
		try {
			File tmpFile = File.createTempFile("cfg-dump", ".dot");
			FileWriter fw = new FileWriter(tmpFile);
			new AdvancedDOTExporter<CFGNode, CFGEdge>(new AdvancedDOTExporter.DefaultNodeLabeller<CFGNode>() {
				@Override public String getLabel(CFGNode node) { 
					String s = node.toString();
					if(node.getBasicBlock() != null) s+= "\n" + node.getBasicBlock().dump(); 
					return s;
				}
			}, null).exportDOT(fw, graph); fw.close();		
			WcetAppInfo.logger.error("[CFG DUMP] Dumped graph to '"+tmpFile+"'");
		} catch (IOException e) {
			WcetAppInfo.logger.error("[CFG DUMP] Dumping graph failed: "+e);
		}		
	}
	
	/**
	 * load annotations for the flow graph.
	 * 
	 * @param wcaMap a map from source lines to loop bounds
	 * @throws BadAnnotationException if an annotations is missing
	 */
	public void loadAnnotations(Project p) throws BadAnnotationException {
		SortedMap<Integer, LoopBound> wcaMap;
		try {
			wcaMap = p.getAnnotations(this.methodInfo.getCli());
		} catch (IOException e) {
			throw new BadAnnotationException("IO Error reading annotation: "+e.getMessage());
		}
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
			if(impls.size() == 0) internalError("No implementations for "+inv.referenced);
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
	/**
	 * Insert dedicates return nodes after invoke
	 * @throws BadGraphException 
	 */
	public void insertReturnNodes() throws BadGraphException {
		Vector<CFGNode> trav = this.getTopOrder().getTopologicalTraversal();
		for(CFGNode n : trav) {
			if(n instanceof InvokeNode) {
				DedicatedNode returnNode = this.splitNode();
				graph.addVertex(returnNode);
				/* copy, as the iterators don't work when removing elements while iterating */
				Vector<CFGEdge> outEdges = new Vector<CFGEdge>(graph.outgoingEdgesOf(n));
				/* move edges */
				for(CFGEdge e : outEdges) {
					graph.addEdge(returnNode, graph.getEdgeTarget(e), e.clone());
					graph.removeEdge(e);
				}
				graph.addEdge(n,returnNode,new CFGEdge(EdgeKind.RETURN_EDGE));
			}
		}
		this.invalidate();
		this.check();
		this.analyseFlowGraph();
	}
	/**
	 * Insert continue-loop nodes, to simplify order for model checker.
	 * If the head of loop has more than one incoming 'continue' edge,
	 * an redirect the continue edges.
	 * @throws BadGraphException 
	 */
	public void insertContinueLoopNodes() throws BadGraphException {
		Vector<CFGNode> trav = this.getTopOrder().getTopologicalTraversal();
		for(CFGNode n : trav) {
			if(getLoopColoring().getHeadOfLoops().contains(n)) {
				Vector<CFGEdge> backEdges = getLoopColoring().getBackEdgesTo(n);
				if(backEdges.size() > 1) {
					DedicatedNode splitNode = this.splitNode();
					graph.addVertex(splitNode);
					/* move edges */
					for(CFGEdge e : backEdges) {
						CFGNode src = graph.getEdgeSource(e);
						graph.addEdge(src, splitNode,e.clone());
						graph.removeEdge(e);
					}
					graph.addEdge(splitNode,n,new CFGEdge(EdgeKind.FLOW_EDGE));					
				}
			}
		}
		this.invalidate();
		this.check();
		this.analyseFlowGraph();
	}
	
	/**
	 * Prototype: Insert summary nodes to speed up UPPAAL search
	 * Currently only for loops which do not contain invoke() and have a single exit
	 * @throws BadGraphException 
	 */
	public void insertSummaryNodes() throws BadGraphException {
		SimpleDirectedGraph<CFGNode, DefaultEdge> loopNestForest = 
			this.getLoopColoring().getLoopNestDAG();
		TopologicalOrderIterator<CFGNode, DefaultEdge> lnfIter = 
			new TopologicalOrderIterator<CFGNode, DefaultEdge>(loopNestForest);
		Vector<CFGNode> summaryLoops = new Vector<CFGNode>();
		Set<CFGNode> marked = new HashSet<CFGNode>();
		while(lnfIter.hasNext()) {
			CFGNode hol = lnfIter.next();
			if(marked.contains(hol)) continue;
			Collection<CFGEdge> exitEdges = getLoopColoring().getExitEdgesOf(hol);
			CFGNode theTarget = null; boolean failed = false;
			for(CFGEdge e : exitEdges) {
				CFGNode target = graph.getEdgeTarget(e);
				if(theTarget == null) theTarget =target;
				else if(theTarget != target) { failed = true; break; }
			}
			if(failed) continue;
			Set<CFGNode> loopNodes = getLoopColoring().getNodesOfLoop(hol);
			for(CFGNode n : loopNodes) {
				if(n instanceof InvokeNode) { failed=true; break; }
			}
			if(failed) continue;
			summaryLoops.add(hol);
			for(CFGNode n : loopNodes) {
				marked.add(n);
			}
		}
		for(CFGNode hol : summaryLoops) {
			insertSummaryNode(hol,getLoopColoring().getExitEdgesOf(hol),getLoopColoring().getNodesOfLoop(hol));			
		}
		this.invalidate();
		this.check();
		this.analyseFlowGraph();
	}
	private void insertSummaryNode(CFGNode hol, Collection<CFGEdge> exitEdges,
			Set<CFGNode> loopNodes) {
		/* summary subgraph */
		/* create a new flow graph */
		ControlFlowGraph subCFG = new ControlFlowGraph(-1,appInfo);
		subCFG.methodInfo = methodInfo;
		subCFG.blocks = blocks;
		subCFG.annotations = annotations;
		FlowGraph<CFGNode, CFGEdge> subGraph = subCFG.graph;
		for(CFGNode n : loopNodes) {
			subGraph.addVertex(n);
		}
		for(CFGNode n : loopNodes) {
			if(n == hol) {
				subGraph.addEdge(subGraph.getEntry(),n,subCFG.entryEdge());
				for(CFGEdge e : getLoopColoring().getBackEdgesByHOL().get(hol)) {
					subGraph.addEdge(graph.getEdgeSource(e), hol, e.clone());
				}
			} else {
				for(CFGEdge e : graph.incomingEdgesOf(n)) {
					subGraph.addEdge(graph.getEdgeSource(e), n, e.clone());
				}
			}
		}
		for(CFGEdge e : exitEdges) {
			subGraph.addEdge(graph.getEdgeSource(e), subGraph.getExit(), e.clone());
		}
		try {
			FileWriter writer;
			writer = new FileWriter(File.createTempFile("subcfg", ".dot"));
			new CFGExport(subCFG).exportDOT(writer, subGraph);
			writer.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		/* summary node */
		SummaryNode summary = new SummaryNode("SUMMARY_"+hol.id,subCFG);
		Set<CFGEdge> inEdges = graph.incomingEdgesOf(hol);
		this.graph.addVertex(summary);
		for(CFGEdge e : inEdges) {
			CFGNode src = graph.getEdgeSource(e);
			graph.addEdge(src, summary, e.clone());
		}
		for(CFGEdge e : exitEdges) {
			CFGNode target = graph.getEdgeTarget(e);
			graph.addEdge(summary, target, e.clone());
		}
		this.graph.removeAllVertices(loopNodes);
		
	}
	/* Check that the graph is connectet, with entry and exit dominating resp. postdominating all nodes */
	private void check() throws BadGraphException {
		/* Remove unreachable and stuck code */
		Set<CFGNode> deads = TopOrder.findDeadNodes(graph, getEntry());
		if(! deads.isEmpty()) WcetAppInfo.logger.error("Found dead code (Exceptions ?): "+deads);			
		Set<CFGNode> stucks = TopOrder.findStuckNodes(graph, getExit());
		if(! stucks.isEmpty()) WcetAppInfo.logger.error("Found stuck code (Exceptions ?): "+stucks);			
		deads.addAll(stucks);
		if(! deads.isEmpty()) {
			graph.removeAllVertices(deads);
			this.invalidate();
		}
		/* now checks should succeed */
		try {
			TopOrder.checkIsFlowGraph(graph, getEntry(), getExit());
		} catch(BadGraphException ex) {
			debugDumpGraph();
			throw ex;
		}
	}

	private void invalidate() {
		this.topOrder = null;
		this.loopColoring = null;
		this.isLeafMethod = null;
	}

	/* flow graph should have been checked before analyseFlowGraph is called */
	private void analyseFlowGraph() {
		try {
			topOrder = new TopOrder<CFGNode, CFGEdge>(this.graph, this.graph.getEntry());
			idGen = 0;
			this.isLeafMethod = true;
			for(CFGNode vertex : topOrder.getTopologicalTraversal()) {
				if(vertex instanceof InvokeNode) this.isLeafMethod = false;
				vertex.id = idGen++;
			}
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
	public int getNumberOfWords() {
		return MiscUtils.bytesToWords(getNumberOfBytes());
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
	@SuppressWarnings("unchecked")
	public String dumpDFA() {
	    if(this.project.getDfaLoopBounds() == null) return "n/a";
		Map<InstructionHandle, ContextMap<List<HashedString>, Pair<ValueMapping>>> results = this.project.getDfaLoopBounds().getResult();
		if(results == null) return "n/a";
		StringBuilder s = new StringBuilder();
		for(CFGNode n: this.graph.vertexSet()) {
			if(n.getBasicBlock() == null) continue;
			ContextMap<List<HashedString>, Pair<ValueMapping>> r = results.get(n.getBasicBlock().getLastInstruction());
			if(r != null) {
				s.append(n);
				s.append(" :: ");
				s.append(r);
				s.append("\n");
			}
		}
		return s.toString();
	}
//	/**
//	 * get single entry single exit sets
//	 * @return
//	 */
//	public Collection<Set<CFGNode>> getSESESets() {
//		DominanceFrontiers<CFGNode, CFGEdge> df = 
//			new DominanceFrontiers<CFGNode, CFGEdge>(this.graph,graph.getEntry(),graph.getExit());
//		return df.getSingleEntrySingleExitSets();
//	}
//	public Map<CFGNode, Set<CFGEdge>> getControlDependencies() {
//		DominanceFrontiers<CFGNode, CFGEdge> df = 
//			new DominanceFrontiers<CFGNode, CFGEdge>(this.graph,graph.getEntry(),graph.getExit());
//		return df.getControlDependencies();
//	}
}
