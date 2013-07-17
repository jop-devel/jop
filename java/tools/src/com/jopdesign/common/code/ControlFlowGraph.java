/*
 * This file is part of JOP, the Java Optimized Processor
 *   see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2008, Benedikt Huber (benedikt.huber@gmail.com)
 * Copyright (C) 2010, Stefan Hepp (stefan@stefant.org)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.jopdesign.common.code;

import com.jopdesign.common.AppEventHandler;
import com.jopdesign.common.AppInfo;
import com.jopdesign.common.ImplementationFinder;
import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.graphutils.AdvancedDOTExporter;
import com.jopdesign.common.graphutils.AdvancedDOTExporter.DOTLabeller;
import com.jopdesign.common.graphutils.AdvancedDOTExporter.DOTNodeLabeller;
import com.jopdesign.common.graphutils.DefaultFlowGraph;
import com.jopdesign.common.graphutils.FlowGraph;
import com.jopdesign.common.graphutils.LoopColoring;
import com.jopdesign.common.graphutils.TopOrder;
import com.jopdesign.common.logger.LogConfig;
import com.jopdesign.common.misc.BadGraphException;
import com.jopdesign.common.misc.HashedString;
import com.jopdesign.common.misc.Iterators;
import com.jopdesign.common.misc.MiscUtils;
import com.jopdesign.common.misc.MiscUtils.F1;
import com.jopdesign.common.type.MethodRef;
import org.apache.bcel.Constants;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.log4j.Logger;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.jopdesign.common.code.BasicBlock.FlowInfo;
import static com.jopdesign.common.code.BasicBlock.FlowTarget;

/**
 * General purpose control flow graph, for use in WCET analysis.
 * <p/>
 * <p>
 * A flow graph is a directed graph with a dedicated entry and exit node.
 * Nodes include dedicated nodes (like entry, exit, split, join), basic block nodes
 * and invoke nodes. Edges carry information about the associated (branch) instruction.
 * The basic blocks associated with the CFG are stored seperately are referenced from
 * basic block nodes.
 * </p>
 * <p/>
 * <p>
 * This class supports
 * <ul>
 * <li/> loop detection
 * <li/> extracting annotations from the source code
 * <li/> resolving virtual invokations (possible, as all methods are known at compile time)
 * <li/> inserting split nodes for nodes with more than one successor
 * </ul></p>
 *
 * @author Benedikt Huber (benedikt.huber@gmail.com)
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class ControlFlowGraph {

    private static final Logger logger = Logger.getLogger(LogConfig.LOG_CFG + ".ControlFlowGraph");

    @SuppressWarnings({"UncheckedExceptionClass"})
    public static class ControlFlowError extends Error {
        private static final long serialVersionUID = 1L;
        private ControlFlowGraph cfg;

        public ControlFlowError(String msg) {
            super("Error in Control Flow Graph: " + msg);
        }
        public ControlFlowError(String msg, ControlFlowGraph cfg) {
            this(msg);
            this.cfg = cfg;
        }
        public ControlFlowError(String message, Throwable cause) {
            super("Error in Control Flow Graph: "+message, cause);
        }

        public ControlFlowGraph getAffectedCFG() {
            return cfg;
        }
    }

    /*---------------------------------------------------------------------------*
     * CFG Node classes
     *---------------------------------------------------------------------------*/

    /**
     * Visitor for flow graph nodes
     */
    public interface CfgVisitor {
    	/**
    	 * visit a special kind of node without basic blocks
    	 */
        void visitVirtualNode(VirtualNode n);

    	/**
    	 * visit a basic block node
    	 */
        void visitBasicBlockNode(BasicBlockNode n);

        /**
         * visit an invoke node. InvokeNode's won't call visitBasicBlockNode.
         * @param n the visited node
         */
        void visitInvokeNode(InvokeNode n);

        /**
         * visit a return node. Return ndoes won't call visitSpecialNode.
         * @param n the visited node
         */
        void visitReturnNode(ReturnNode n);

        /**
         * visit a summary node
         * @param n the visited node
         */
        void visitSummaryNode(SummaryNode n);
    }

    /**
     * Abstract base class for flow graph nodes
     */
    public abstract class CFGNode implements Comparable<CFGNode> {
        private int id;
        protected String name;

        protected CFGNode(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int compareTo(CFGNode o) {
            return new Integer(this.hashCode()).compareTo(o.hashCode());
        }

        public String toString() {
            return "#" + id + " " + name;
        }

        public String getName() {
            return name;
        }

        public abstract BasicBlock getBasicBlock();

        /**
         * This is a helper function to access {@link BasicBlock#getLoopBound()}.
         * <p>
         * LoopBounds can only be attached to BasicBlocks, since they are stored with InstructionHandles.
         * </p> 
         * @return the LoopBound of the basic block, or null if not set.
         */
        public LoopBound getLoopBound() {
            BasicBlock bb = getBasicBlock();
            if (bb != null) {
                return bb.getLoopBound();
            }
            return null;
        }
        
        public int getId() {
            return id;
        }

        void setId(int newId) {
            this.id = newId;
        }

        public abstract void accept(CfgVisitor v);

        public ControlFlowGraph getControlFlowGraph() {
            return ControlFlowGraph.this;
        }

        protected void register() { }

        protected boolean isRegistered() { return false; }

        protected void dispose() { }
    }

    /**
     * Names for special purpose nodes (entry node, exit node, split and join nodes, return nodes)
     */
    public enum VirtualNodeKind { ENTRY, EXIT, SPLIT, JOIN, RETURN }

    /**
     * Special purpose virtual nodes (without basic blocks attached)
     */
    public class VirtualNode extends CFGNode {
        private VirtualNodeKind kind;

        public VirtualNodeKind getKind() {
            return kind;
        }

        private VirtualNode(VirtualNodeKind kind) {
            super(idGen++, kind.toString());
            this.kind = kind;
        }

        @Override
        public BasicBlock getBasicBlock() {
            return null;
        }

        @Override
        public void accept(CfgVisitor v) {
            v.visitVirtualNode(this);
        }
    }

    /**
     * Dedicated flow graph nodes
     */
    public class ReturnNode extends VirtualNode {

        private InvokeNode invokeNode;

		private ReturnNode(InvokeNode invNode) {
            super(VirtualNodeKind.RETURN);
            this.invokeNode = invNode; 
        }

        @Override
        public void accept(CfgVisitor v) {
            v.visitReturnNode(this);
        }
    }
    /*---------------------------------------------------------------------------*
     * CFG BasicBlock node classes
     *---------------------------------------------------------------------------*/

    /**
     * Flow graph nodes representing basic blocks
     */
    public class BasicBlockNode extends CFGNode {
        protected final BasicBlock block;

        public BasicBlockNode(BasicBlock block) {
            super(idGen++, "basic(" + blocks.indexOf(block) + ")");
            this.block = block;
        }

        @Override
        protected void register() {
            for (InstructionHandle ih : block.getInstructions()) {
                // TODO to support multiple registered CFGs per method, add a Map<ControlFlowGraph,BasicBlockNode> instead
                ih.addAttribute(KEY_CFGNODE, this);
            }
        }

        @Override
        protected boolean isRegistered() {
            if (block.getInstructions().isEmpty()) return false;
            return this.equals(getHandleNode(block.getFirstInstruction(),true));
        }

        @Override
        protected void dispose() {
            for (InstructionHandle ih : block.getInstructions()) {
                ih.removeAttribute(KEY_CFGNODE);
            }
        }

        public BasicBlock getBasicBlock() {
            return block;
        }

        @Override
        public void accept(CfgVisitor v) {
            v.visitBasicBlockNode(this);
        }
    }

    /** Invoke nodes (Basic block with exactly one instruction)
     * transfer control to a different method.
	 */
    public class InvokeNode extends BasicBlockNode {
        private InvokeInstruction instr;
        private MethodRef referenced;
        private MethodInfo receiverImpl;
        private InvokeNode instantiatedFrom;

        private InvokeNode(BasicBlock block) {
            super(block);
        }

        public InvokeNode(BasicBlock block, InstructionHandle instr) {
            super(block);
            this.instr = (InvokeInstruction) instr.getInstruction();
            // TODO keep the InvokeSite instead of instr+referenced .. 
            InvokeSite invokeSite = methodInfo.getCode().getInvokeSite(instr);
            this.referenced = invokeSite.getInvokeeRef();
            /* if virtual / interface, this method has to be resolved first */
            if (invokeSite.isVirtual()) {
                receiverImpl = null;
            } else {
                receiverImpl = referenced.getMethodInfo();
            }
            this.name = "invoke(" + this.referenced + ")";
        }

        @Override
        public void accept(CfgVisitor v) {
            v.visitInvokeNode(this);
        }

        public InstructionHandle getInstructionHandle() {
            return block.getLastInstruction();
        }

        /**
         * @return For non-virtual methods, get the implementation of the method
         */
        public MethodInfo getImplementingMethod() {
            return receiverImpl;
        }

        /**
         * @return all possible implementations of the invoked method, using the context and the implementation finder
         *         of the CFG.
         */
        public Set<MethodInfo> getImplementingMethods() {
            return getImplementingMethods(context, implementationFinder);
        }

        public InvokeSite getInvokeSite() {
            return getMethodInfo().getCode().getInvokeSite(getInstructionHandle());
        }

        /**
         * @param ctx the callstring of the invocation
         * @param finder the finder to use for finding implementations of this invokesite.
         * @return all possible implementations of the invoked method in
         * the given context
         */
        public Set<MethodInfo> getImplementingMethods(CallString ctx, ImplementationFinder finder) {
            if (!isVirtual()) {
                return Collections.singleton(getImplementingMethod());
            } else {
                return finder.findImplementations(ctx.push(getInvokeSite()));
            }
        }

        /**
         * For non-virtual methods, get the implementation of the method
         * @return the CFG of the implementing receiver or null if unknown.
         */
        public ControlFlowGraph receiverFlowGraph() {
            if (isVirtual()) return null;
            // TODO if context and implementationFinder is set, use them instead
            return getImplementingMethod().getCode().getControlFlowGraph(false);
        }

        public ControlFlowGraph invokerFlowGraph() {
            return ControlFlowGraph.this;
        }

        public MethodRef getReferenced() {
            return referenced;
        }

        /**
         * @return true if the invocation denotes an interface, not an implementation
         */
        public boolean isVirtual() {
            return receiverImpl == null;
        }

        /**
         * @return true if this node is a nonvirtual instance of a virtual node, false if this node is either virtual or statically resolved.
         */
        public boolean isVirtualInstance() {
            return instantiatedFrom != null;
        }

        /**
         * If this is the implementation of a virtual/interface invoke instruction,
         * return the InvokeNode for the virtual invoke instruction.
         * TODO: This can be removed, if we ever remove {@link ControlFlowGraph#resolveVirtualInvokes()}
         * @return the node representing the virtual invoke or null if not instantiated from a virtual invoke.
         */
        public InvokeNode getVirtualNode() {
            if (this.instantiatedFrom != null) return this.instantiatedFrom;
            else return this;
        }

        /**
         * Create an implementation node from this node
         *
         * @param impl    the implementing method
         * @param virtual invoke node for the virtual method
         * @return a new nonvirtual invoke node
         */
        @SuppressWarnings({"AccessingNonPublicFieldOfAnotherObject"})
        public InvokeNode createImplNode(MethodInfo impl, InvokeNode virtual) {
            InvokeNode n = new InvokeNode(block);
            n.name = "invoke(" + impl.getFQMethodName() + ")";
            n.instr = this.instr;
            n.referenced = this.referenced;
            n.receiverImpl = impl;
            n.instantiatedFrom = virtual;
            return n;
        }

        @Override
        protected void register() {
            if (instantiatedFrom == null) {
                // only register the 'virtual' node, since we do not register multiple nodes per handle
                super.register();
            } else {
                instantiatedFrom.register();
            }
        }

        @Override
        protected boolean isRegistered() {
            if (instantiatedFrom == null) {
                return super.isRegistered();
            } else {
                return instantiatedFrom.isRegistered();
            }
        }
    }

    /**
     * SpecialInvokeNode represents target-specific invokes, e.g. bytecodes
     * implemented in Java
     */
    public class SpecialInvokeNode extends InvokeNode {
        private InstructionHandle instr;
        private MethodInfo receiverImpl;

        public SpecialInvokeNode(BasicBlock block, MethodInfo javaImpl) {
            super(block);
            this.instr = block.getLastInstruction();
            this.name = "jimplBC(" + javaImpl + ")";
            this.receiverImpl = javaImpl;
        }

        @Override
        public void accept(CfgVisitor v) {
            v.visitInvokeNode(this);
        }

        @Override
        public InstructionHandle getInstructionHandle() {
            return instr;
        }

        @Override
        public MethodInfo getImplementingMethod() {
            return this.receiverImpl;
        }

        @Override
        public ControlFlowGraph receiverFlowGraph() {
            // TODO if context and implementationFinder is set, use them instead
            return receiverImpl.getCode().getControlFlowGraph(false);
        }

        /**
         * @return true if the invokation denotes an interface, not an implementation
         */
        @Override
        public boolean isVirtual() {
            return receiverImpl == null;
        }

        @Override
        public InvokeNode createImplNode(MethodInfo impl, InvokeNode _) {
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
        
        @Override
        public BasicBlock getBasicBlock() {
        	throw new UnsupportedOperationException("getBasicBlock() is meaningless for SummaryNode");
        }

    }

    /*---------------------------------------------------------------------------*
     * CFG edge classes
     *---------------------------------------------------------------------------*/

    /**
     * Type of flow graph edges
     */
    public enum EdgeKind {
        ENTRY_EDGE, EXIT_EDGE, NEXT_EDGE,
        GOTO_EDGE, SELECT_EDGE, BRANCH_EDGE, JSR_EDGE,
        DISPATCH_EDGE,
        RETURN_EDGE, FLOW_EDGE, LOW_LEVEL_EDGE
    }

    /**
     * Edges of the flow graph
     */
    public static class CFGEdge extends DefaultEdge {
        private static final long serialVersionUID = 1L;
        private EdgeKind kind;

        public CFGEdge(EdgeKind kind) {
            this.kind = kind;
        }

        public EdgeKind getKind() {
            return kind;
        }

        @SuppressWarnings({"CloneDoesntCallSuperClone"})
        public CFGEdge clone() {
            return new CFGEdge(kind);
        }
    }

    /*---------------------------------------------------------------------------*
     * Fields
     *---------------------------------------------------------------------------*/

    // FIXME: [wcet-frontend] Remove the ugly ih.getAttribute() hack for CFG Nodes
    private static final Object KEY_CFGNODE = new HashedString("ControlFlowGraph.CFGNode");

    private int idGen = 0;

    /* linking to java */
    private AppInfo appInfo;
    private MethodInfo methodInfo;

    /* basic blocks associated with the CFG */
    private List<BasicBlock> blocks;

    /* graph */
    private FlowGraph<CFGNode, CFGEdge> graph;
    private Set<CFGNode> deadNodes;
	private boolean hasThrowEdges;

    /* this we need for resolveVirtualInvokes() */
    private CallString context;
    private ImplementationFinder implementationFinder;

    /* analysis stuff, needs to be reevaluated when graph changes */
    private TopOrder<CFGNode, CFGEdge> topOrder = null;
    private LoopColoring<CFGNode, CFGEdge> loopColoring = null;
    private Boolean isLeafMethod = null;

    private boolean clean = false;
    private boolean virtualInvokesResolved = false;
    private boolean hasReturnNodes = false;
    private boolean hasContinueLoopNodes = false;
    private boolean hasSplitNodes = false;
    private boolean hasSummaryNodes = false;

    private static boolean ignoreATHROW = false;

    /*---------------------------------------------------------------------------*
     * CFG Creation
     *---------------------------------------------------------------------------*/

    /**
     * Build a new flow graph for the given method
     *
     * @param method needs attached code (<code>method.getCode() != null</code>)
     * @throws BadGraphException if the bytecode results in an invalid flow graph
     */
    public ControlFlowGraph(MethodInfo method) throws BadGraphException {
        this(method, CallString.EMPTY, method.getAppInfo());
    }

    /**
     * Build a new flow graph for the given method
     *
     * @param method needs attached code (<code>method.getCode() != null</code>)
     * @param context the callstring leading to this method, used to resolve virtual invokes. Can be empty.
     * @param finder the implementation finder to find implementations of an invokesite (who would have guessed? :) )
     * @throws BadGraphException if the bytecode results in an invalid flow graph
     */
    public ControlFlowGraph(MethodInfo method, CallString context, ImplementationFinder finder) throws BadGraphException {
        this.methodInfo = method;
        // we set this in the constructor instead of passing it to resolveVirtualInvokes, so that it is harder to
        // use this incorrectly (ie. attaching a callgraph for a context to the context-less MethodCode)
        this.context = context;
        this.implementationFinder = finder;
        this.appInfo = method.getAppInfo();
        createFlowGraph(method);
        check();
        sendCreateGraphEvent();
    }


	/**
	 * @return true if there virtual dispatch nodes have been removed
	 */
	public boolean areVirtualInvokesResolved() {		
		return virtualInvokesResolved;
	}
	
    /**
     * @param ignore true to disable ATHROW warnings during callgraph creation.
     */
    public static void setIgnoreATHROW(boolean ignore) {
        ignoreATHROW = ignore;
    }

    private ControlFlowGraph(AppInfo appInfo) {
        this.appInfo = appInfo;
        CFGNode subEntry = new VirtualNode(VirtualNodeKind.ENTRY);
        CFGNode subExit = new VirtualNode(VirtualNodeKind.EXIT);
        this.graph =
                new DefaultFlowGraph<CFGNode, CFGEdge>(CFGEdge.class, subEntry, subExit);
        this.deadNodes = new LinkedHashSet<CFGNode>();
        // TODO should we do this? currently only used to create an internal subgraph
        //sendCreateGraphEvent();
    }

    /* worker: create the flow graph */

    private void createFlowGraph(MethodInfo method) {
        logger.debug("creating flow graph for: " + method);
        Map<Integer, BasicBlockNode> nodeTable =
                new LinkedHashMap<Integer, BasicBlockNode>();
        graph = new DefaultFlowGraph<CFGNode, CFGEdge>(
                CFGEdge.class,
                new VirtualNode(VirtualNodeKind.ENTRY),
                new VirtualNode(VirtualNodeKind.EXIT));
        blocks = new ArrayList<BasicBlock>();

        /* Create basic block vertices */
        int i = 0;
        for (BasicBlock bb : BasicBlock.buildBasicBlocks(method.getCode())) {
            BasicBlockNode n = addBasicBlock(i++, bb);
            nodeTable.put(bb.getFirstInstruction().getPosition(), n);
        }

        /* entry edge */
        graph.addEdge(graph.getEntry(),
                nodeTable.get(blocks.get(0).getFirstInstruction().getPosition()),
                createEntryEdge());
        /* flow edges */
        hasThrowEdges = false;
        for (BasicBlockNode bbNode : nodeTable.values()) {
            BasicBlock bb = bbNode.getBasicBlock();
            FlowInfo bbf = bb.getExitFlowInfo();
            if (bbf.isExit()) { // exit edge
                // do not connect exception edges
                if (bbNode.getBasicBlock().getLastInstruction().getInstruction().getOpcode()
                        == Constants.ATHROW)
                {
                    if (ignoreATHROW) {
                        // Yep, you are looking at a hack.. We know that ATHROW is bad, no need to spam the log output.
                        if (logger.isTraceEnabled()) {
                            logger.trace("Found ATHROW edge in " + this.toString() + " - ignoring (results in dead and stuck code)");
                        }
                    } else {
                        logger.warn("Found ATHROW edge in " + this.toString() + " - ignoring (results in dead and stuck code)");
                    }
                    hasThrowEdges = true;
                } else {
                    graph.addEdge(bbNode, graph.getExit(), createExitEdge());
                }
            } else if (!bbf.isAlwaysTaken()) { // next block edge
                BasicBlockNode bbSucc = nodeTable.get(bbNode.getBasicBlock().getLastInstruction().getNext().getPosition());
                if (bbSucc == null) {
                    internalError("Next Edge to non-existing next block from " +
                            bbNode.getBasicBlock().getLastInstruction());
                }
                graph.addEdge(bbNode,
                        bbSucc,
                        new CFGEdge(EdgeKind.NEXT_EDGE));
            }
            for (FlowTarget target : bbf.getTargets()) { // jmps
                BasicBlockNode targetNode = nodeTable.get(target.getTarget().getPosition());
                if (targetNode == null) internalError("No node for flow target: " + bbNode + " -> " + target);
                graph.addEdge(bbNode,
                        targetNode,
                        new CFGEdge(target.getEdgeKind()));
            }
        }
        // FIXME: Is this really sensible? I cannot remember we I added this edge
        // this.graph.addEdge(graph.getEntry(), graph.getExit(), createExitEdge());
    }

    private void sendCreateGraphEvent() {
        for (AppEventHandler e : appInfo.getEventHandlers()) {
            e.onCreateControlFlowGraph(this);
        }
    }


    /*---------------------------------------------------------------------------*
     * CFG modify, compile, dispose
     *---------------------------------------------------------------------------*/

    /**
     * Add a basic block to this graph. The instruction list of the block must not be empty.
     *
     * @param insertBefore insert the block at this position in the block list.
     * @param bb block to add
     * @return the new block node, either an InvokeNode, SpecialInvokeNode or BasicBlockNode, depending on the
     *   contained instructions.
     */
    public BasicBlockNode addBasicBlock(int insertBefore, BasicBlock bb) {
        BasicBlockNode n;
        Instruction lastInstr = bb.getLastInstruction().getInstruction();
        InstructionHandle theInvoke = bb.getTheInvokeInstruction();

        // This needs to be done before creating the Node, else blocks.indexOf returns -1
        blocks.add(insertBefore, bb);

        if (theInvoke != null) {
            n = new InvokeNode(bb, theInvoke);
        } else if (appInfo.getProcessorModel().isImplementedInJava(methodInfo, lastInstr)) {
            MethodInfo javaImpl = appInfo.getProcessorModel().getJavaImplementation(appInfo,
                                        bb.getMethodInfo(),lastInstr);
            n = new SpecialInvokeNode(bb, javaImpl);
        } else {
            n = new BasicBlockNode(bb);
        }

        graph.addVertex(n);
        return n;
    }

    /**
     * Create a new basic block and add it to the graph as a node.
     * @param insertBefore the position to add the block to in the block list.
     * @return a new BasicBlockNode with an empty basic block.
     */
    public BasicBlockNode createBasicBlock(int insertBefore) {
        BasicBlock bb = new BasicBlock(methodInfo.getCode());
        blocks.add(insertBefore, bb);
        BasicBlockNode bbn = new BasicBlockNode(bb);
        graph.addVertex(bbn);
        return bbn;
    }

    /**
     * Compile the CFG back into an instruction list and store it in the associated
     * MethodCode.
     * <p>
     * We do not order the blocks here, this is a separate optimization
     * Also, we do not insert jump instructions if the fallthrough edge of a block does not
     * link to the next block in the block list. Instead an error is raised.
     * </p>
     * TODO create method to insert jump blocks where necessary
     */
    public void compile() {
        InstructionList il = new InstructionList();

        Object[] attributes = {KEY_CFGNODE};

        Map<BasicBlock, BasicBlockNode> blockMap = buildBlockNodeMap();

        for (int i = 0; i < blocks.size(); i++) {
            BasicBlock bb = blocks.get(i);
            BasicBlockNode bbn = blockMap.get(bb);

            if (bbn == null) {
                // Basic block without a node in the graph? Dead code due to removal of exception-errors or
                // infeasible edges..
                continue;
            }
            // bb.appendTo(il, attributes);

            for (CFGEdge e : graph.outgoingEdgesOf(bbn)) {
                if (e.getKind() != EdgeKind.NEXT_EDGE) continue;
                BasicBlock target = graph.getEdgeTarget(e).getBasicBlock();

                // TODO edge targets a SPLIT node, handle correctly ..
                if(target == null) {
                    throw new ControlFlowError("Block "+i+" does fallthrough to a virtual node of type " +
                    		((VirtualNode) graph.getEdgeTarget(e)).getKind() + " in " + methodInfo);
                }
                if (blocks.get(i+1) != target) {
                    throw new ControlFlowError("Block "+i+" does not fall through to the next block in "+methodInfo);
                }
            }
        }

        // TODO compile is not yet fully implemented (retarget, exception-handlers, ..)

        // methodInfo.getCode().setInstructionList(il);
    }

    /**
     * Clean up all known references to the objects of this graph (i.e. InstructionHandle attributes,..)
     */
    public void dispose() {
        for (CFGNode node : graph.vertexSet()) {
            node.dispose();
        }
    }


    /*---------------------------------------------------------------------------*
     * Standard getter
     *---------------------------------------------------------------------------*/

    public AppInfo getAppInfo() {
        return this.appInfo;
    }

    /**
     * @return the context for which this callgraph is valid.
     */
    public CallString getContext() {
        return context;
    }

    public ImplementationFinder getImplementationFinder() {
        return implementationFinder;
    }

    /**
     * get the method this flow graph models
     *
     * @return the MethodInfo the flow graph was build from
     */
    public MethodInfo getMethodInfo() {
        return this.methodInfo;
    }

    /**
     * @return the (dedicated) entry node of the flow graph
     */
    public CFGNode getEntry() {
        return graph.getEntry();
    }

    /**
     * @return the (dedicated) exit node of the flow graph
     */
    public CFGNode getExit() {
        return graph.getExit();
    }

    /**
     * @return Get the actual flow graph
     */
    public FlowGraph<CFGNode, CFGEdge> getGraph() {

    	return graph;
    }

    public Set<CFGNode> vertexSet() {
		
		return graph.vertexSet();
	}

	public Set<CFGEdge> edgeSet() {

		return graph.edgeSet();
	}

	public CFGEdge getEdge(BasicBlockNode source, BasicBlockNode target) {

		return graph.getEdge(source, target);
	}
	
	public CFGNode getEdgeSource(CFGEdge edge) {
		
		return graph.getEdgeSource(edge);
	}

	public CFGNode getEdgeTarget(CFGEdge edge) {
		
		return graph.getEdgeTarget(edge);
	}
		
	public Set<CFGEdge> outgoingEdgesOf(CFGNode node) {

		return graph.outgoingEdgesOf(node);
	}
	
	public Set<CFGEdge> incomingEdgesOf(CFGNode node) {

		return graph.incomingEdgesOf(node);
	}

    public List<BasicBlock> getBlocks() {
    	
        return blocks;
    }

	public Iterable<CFGNode> getSuccessors(CFGNode currentBlock) {

		return Iterators.mapEntries(graph.outgoingEdgesOf(currentBlock), new F1<CFGEdge, CFGNode>() {
			@Override
			public CFGNode apply(CFGEdge e) {
				return graph.getEdgeTarget(e);
			}
			
		});
	}
	
    public Map<BasicBlock, BasicBlockNode> buildBlockNodeMap() {
        Map<BasicBlock, BasicBlockNode> map = new LinkedHashMap<BasicBlock, BasicBlockNode>(blocks.size());
        for (CFGNode node : graph.vertexSet()) {
            if (node instanceof BasicBlockNode) {
                BasicBlockNode bbn = (BasicBlockNode) node;
                map.put(bbn.getBasicBlock(), bbn);
            }
        }
        return map;
    }

    /**
     * Set the basic block nodes to the instruction handles of the method code.
     * <p>
     * You should make sure that {@link #dispose()} is called if this is used when this graph is not used anymore,
     * else the blocks and hence the whole graph will stay attached to the instructions.
     * </p>
     * For now only one CFG per method can be registered.
     */
    public void registerHandleNodes() {
        for (CFGNode node : graph.vertexSet()) {
            node.register();
        }
    }

    /**
     * Get the node for an instruction handle. This only works if {@link #registerHandleNodes()} has been called first.
     *
     * @param ih The instruction handle of a method which has this CFG associated with it
     * @return The basic block node associated with an instruction handle
     */
    public BasicBlockNode getHandleNode(InstructionHandle ih) {
        return getHandleNode(ih, false);
    }

    public BasicBlockNode getHandleNode(InstructionHandle ih, boolean ignoreMissingNodes) {
        BasicBlockNode blockNode = (BasicBlockNode) ih.getAttribute(KEY_CFGNODE);
        if (blockNode == null && !ignoreMissingNodes) {
            String errMsg = "No basic block recorded for instruction " + ih.toString(true);
            logger.error(errMsg);
            return null;
        }
        return blockNode;
    }

    public boolean isLeafMethod() {
        return isLeafMethod;
    }


    /*---------------------------------------------------------------------------*
     * Resolve invokes, insert analysis nodes, makes the graph "dirty"
     *---------------------------------------------------------------------------*/

    /**
     * @return returns false after additional nodes for analyses have been inserted using any of the
     *  insert* methods or after resolving the invoke nodes.
     */
    public boolean isClean() {
        return clean;
    }

    /**
     * resolve all virtual invoke nodes, and replace them by actual implementations.
     * <p>
     * This uses the context and the implementation finder passed to the constructor of this graph.
     * </p>
     *
     * @throws BadGraphException If the flow graph analysis (post replacement) fails
     */
    @SuppressWarnings({"AccessingNonPublicFieldOfAnotherObject"})
    public void resolveVirtualInvokes() throws BadGraphException {

        // Hack to make this optional
        if (virtualInvokesResolved) return;
        virtualInvokesResolved = true;
        clean = false;
        
        if(hasReturnNodes) {
        	throw new AssertionError("Virtuals need to be resolved before inserting return nodes (file a bug)");
        }

        List<InvokeNode> virtualInvokes = new ArrayList<InvokeNode>();
        /* find virtual invokes */
        for (CFGNode n : this.graph.vertexSet()) {
            if (n instanceof InvokeNode) {
                InvokeNode in = (InvokeNode) n;
                if (in.isVirtual()) {
                    virtualInvokes.add(in);
                }
            }
        }
        /* replace them */
        for (InvokeNode inv : virtualInvokes) {
            // Magic: this uses the context and the implementation finder passed to the constructor of this graph.
            Set<MethodInfo> impls = inv.getImplementingMethods();

            if (impls.size() == 0) internalError("No implementations for " + inv.referenced + " possibly invoked from " + inv.getBasicBlock().getMethodInfo());
            if (impls.size() == 1) {
                InvokeNode implNode = inv.createImplNode(impls.iterator().next(), inv);
                graph.addVertex(implNode);
                for (CFGEdge inEdge : graph.incomingEdgesOf(inv)) {
                    graph.addEdge(graph.getEdgeSource(inEdge), implNode, new CFGEdge(inEdge.kind));
                }
                for (CFGEdge outEdge : graph.outgoingEdgesOf(inv)) {
                    graph.addEdge(implNode, graph.getEdgeTarget(outEdge), new CFGEdge(outEdge.kind));
                }
            } else { /* more than one impl, create split/join nodes */
                CFGNode split = createSplitNode();
                graph.addVertex(split);
                for (CFGEdge inEdge : graph.incomingEdgesOf(inv)) {
                    graph.addEdge(graph.getEdgeSource(inEdge), split, new CFGEdge(inEdge.kind));
                }
                CFGNode join = createJoinNode();
                graph.addVertex(join);
                for (CFGEdge outEdge : graph.outgoingEdgesOf(inv)) {
                    graph.addEdge(join, graph.getEdgeTarget(outEdge), new CFGEdge(outEdge.kind));
                }
                for (MethodInfo impl : impls) {
                    InvokeNode implNode = inv.createImplNode(impl, inv);
                    graph.addVertex(implNode);
                    graph.addEdge(split, implNode, new CFGEdge(EdgeKind.DISPATCH_EDGE));
                    graph.addEdge(implNode, join, new CFGEdge(EdgeKind.RETURN_EDGE));
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
     *
     * @throws BadGraphException if the graph check after the transformation fails
     */
    public void insertSplitNodes() throws BadGraphException {

        if (hasSplitNodes) return;
        hasSplitNodes = true;
        clean = false;

        List<CFGNode> trav = this.getTopOrder().getTopologicalTraversal();
        for (CFGNode n : trav) {
            if (n instanceof BasicBlockNode && graph.outDegreeOf(n) > 1) {
                VirtualNode splitNode = this.createSplitNode();
                graph.addVertex(splitNode);
                /* copy, as the iterators don't work when removing elements while iterating */
                List<CFGEdge> outEdges = new ArrayList<CFGEdge>(graph.outgoingEdgesOf(n));
                /* move edges */
                for (CFGEdge e : outEdges) {
                    graph.addEdge(splitNode, graph.getEdgeTarget(e), e.clone());
                    graph.removeEdge(e);
                }
                graph.addEdge(n, splitNode, new CFGEdge(EdgeKind.FLOW_EDGE));
            }
        }
        this.invalidate();
        this.check();
        this.analyseFlowGraph();
    }

    /**
     * Insert dedicates return nodes after invoke
     *
     * @throws BadGraphException if the graph check after the transformation fails
     */
    public void insertReturnNodes() throws BadGraphException {

        if (hasReturnNodes) return;
        hasReturnNodes = true;
        clean = false;

        List<CFGNode> trav = this.getTopOrder().getTopologicalTraversal();
        for (CFGNode n : trav) {
            if (n instanceof InvokeNode) {
                ReturnNode returnNode = new ReturnNode((InvokeNode) n);
                graph.addVertex(returnNode);
                /* copy, as the iterators don't work when removing elements while iterating */
                List<CFGEdge> outEdges = new ArrayList<CFGEdge>(graph.outgoingEdgesOf(n));
                /* move edges */
                for (CFGEdge e : outEdges) {
                    graph.addEdge(returnNode, graph.getEdgeTarget(e), e.clone());
                    graph.removeEdge(e);
                }
                graph.addEdge(n, returnNode, new CFGEdge(EdgeKind.RETURN_EDGE));
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
     *
     * @throws BadGraphException if the graph check after the transformation fails
     */
    public void insertContinueLoopNodes() throws BadGraphException {

        if (hasContinueLoopNodes) return;
        hasContinueLoopNodes = true;
        clean = false;

        List<CFGNode> trav = this.getTopOrder().getTopologicalTraversal();
        for (CFGNode n : trav) {
            if (getLoopColoring().getHeadOfLoops().contains(n)) {
                List<CFGEdge> backEdges = getLoopColoring().getBackEdgesTo(n);
                if (backEdges.size() > 1) {
                    VirtualNode splitNode = this.createSplitNode();
                    graph.addVertex(splitNode);
                    /* move edges */
                    for (CFGEdge e : backEdges) {
                        CFGNode src = graph.getEdgeSource(e);
                        graph.addEdge(src, splitNode, e.clone());
                        graph.removeEdge(e);
                    }
                    graph.addEdge(splitNode, n, new CFGEdge(EdgeKind.FLOW_EDGE));
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
     *
     * @throws BadGraphException if the graph check after the transformation fails
     */
    public void insertSummaryNodes() throws BadGraphException {

        if (hasSummaryNodes) return;
        hasSummaryNodes = true;
        clean = false;

        SimpleDirectedGraph<CFGNode, DefaultEdge> loopNestForest =
                this.getLoopColoring().getLoopNestDAG();
        TopologicalOrderIterator<CFGNode, DefaultEdge> lnfIter =
                new TopologicalOrderIterator<CFGNode, DefaultEdge>(loopNestForest);
        List<CFGNode> summaryLoops = new ArrayList<CFGNode>();
        Set<CFGNode> marked = new LinkedHashSet<CFGNode>();
        while (lnfIter.hasNext()) {
            CFGNode hol = lnfIter.next();
            if (marked.contains(hol)) continue;
            Collection<CFGEdge> exitEdges = getLoopColoring().getExitEdgesOf(hol);
            CFGNode theTarget = null;
            boolean failed = false;
            for (CFGEdge e : exitEdges) {
                CFGNode target = graph.getEdgeTarget(e);
                if (theTarget == null) theTarget = target;
                else if (theTarget != target) {
                    failed = true;
                    break;
                }
            }
            if (failed) continue;
            Set<CFGNode> loopNodes = getLoopColoring().getNodesOfLoop(hol);
            for (CFGNode n : loopNodes) {
                if (n instanceof InvokeNode) {
                    failed = true;
                    break;
                }
            }
            if (failed) continue;
            summaryLoops.add(hol);
            for (CFGNode n : loopNodes) {
                marked.add(n);
            }
        }
        for (CFGNode hol : summaryLoops) {
            insertSummaryNode(hol, getLoopColoring().getExitEdgesOf(hol), getLoopColoring().getNodesOfLoop(hol));
        }
        this.invalidate();
        this.check();
        this.analyseFlowGraph();
    }

    @SuppressWarnings({"AccessingNonPublicFieldOfAnotherObject"})
    private void insertSummaryNode(CFGNode hol, Collection<CFGEdge> exitEdges,
                                   Set<CFGNode> loopNodes) {
        /* summary subgraph */
        /* create a new flow graph */
        ControlFlowGraph subCFG = new ControlFlowGraph(appInfo);
        subCFG.methodInfo = methodInfo;
        subCFG.blocks = blocks;
        FlowGraph<CFGNode, CFGEdge> subGraph = subCFG.graph;
        for (CFGNode n : loopNodes) {
            subGraph.addVertex(n);
        }
        for (CFGNode n : loopNodes) {
            if (n == hol) {
                subGraph.addEdge(subGraph.getEntry(), n, subCFG.createEntryEdge());
                for (CFGEdge e : getLoopColoring().getBackEdgesByHOL().get(hol)) {
                    subGraph.addEdge(graph.getEdgeSource(e), hol, e.clone());
                }
            } else {
                for (CFGEdge e : graph.incomingEdgesOf(n)) {
                    subGraph.addEdge(graph.getEdgeSource(e), n, e.clone());
                }
            }
        }
        for (CFGEdge e : exitEdges) {
            subGraph.addEdge(graph.getEdgeSource(e), subGraph.getExit(), e.clone());
        }
        try {
            // TODO hmm, maybe make dump optional :)
            FileWriter writer;
            writer = new FileWriter(File.createTempFile("subcfg", ".dot"));
            new CFGExport(subCFG).exportDOT(writer, subGraph);
            writer.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        /* summary node */
        SummaryNode summary = new SummaryNode("SUMMARY_" + hol.id, subCFG);
        Set<CFGEdge> inEdges = graph.incomingEdgesOf(hol);
        this.graph.addVertex(summary);
        for (CFGEdge e : inEdges) {
            CFGNode src = graph.getEdgeSource(e);
            graph.addEdge(src, summary, e.clone());
        }
        for (CFGEdge e : exitEdges) {
            CFGNode target = graph.getEdgeTarget(e);
            graph.addEdge(summary, target, e.clone());
        }
        this.graph.removeAllVertices(loopNodes);

    }


    /*---------------------------------------------------------------------------*
     * Various CFG analyses
     *---------------------------------------------------------------------------*/

    /**
     * Create a new map of loopbounds for CFG nodes. The map is not cached, try to use
     * {@link BasicBlock#getLoopBound()} or {@link CFGNode#getLoopBound()} instead.
     *
     * @see CFGNode#getLoopBound()
     * @see BasicBlock#getLoopBound()
     * @return a new mapping of loopbounds to cfg nodes. Contains only nodes which have basic
     *         blocks with loopbounds attached.
     */
    public Map<CFGNode, LoopBound> buildLoopBoundMap() {
        Map<CFGNode, LoopBound> map = new LinkedHashMap<CFGNode, LoopBound>();
        for (CFGNode node : graph.vertexSet()) {
            LoopBound lb = node.getLoopBound();
            if (lb != null) {
                map.put(node,lb);
            }
        }
        return map;
    }

    /* Check that the graph is connectet, with entry and exit dominating resp. postdominating all nodes */

    /**
     * Calculate (cached) the "loop coloring" of the flow graph.
     *
     * @return a loop coloring assigning each flowgraph node the set of loops it
     *         participates in
     */
    public LoopColoring<CFGNode, CFGEdge> getLoopColoring() {
        if (loopColoring == null) analyseFlowGraph();
        return loopColoring;
    }

    public TopOrder<CFGNode, CFGEdge> getTopOrder() {
        if (topOrder == null) analyseFlowGraph();
        return topOrder;
    }

    /**
     * Get the length of the implementation
     *
     * @return the length in bytes
     */
    public int getNumberOfBytes() {
        int sum = 0;
        for (BasicBlock bb : this.blocks) {
            sum += bb.getNumberOfBytes();
        }
        return sum;
    }

    public int getNumberOfWords() {
        return MiscUtils.bytesToWords(getNumberOfBytes());
    }

//  /**
//   * get single entry single exit sets
//   * @return
//   */
//  public Collection<Set<CFGNode>> getSESESets() {
//  	DominanceFrontiers<CFGNode, CFGEdge> df =
//  		new DominanceFrontiers<CFGNode, CFGEdge>(this.graph,graph.getEntry(),graph.getExit());
//  	return df.getSingleEntrySingleExitSets();
//  }
//
//  public Map<CFGNode, Set<CFGEdge>> getControlDependencies() {
//  	DominanceFrontiers<CFGNode, CFGEdge> df =
//  		new DominanceFrontiers<CFGNode, CFGEdge>(this.graph,graph.getEntry(),graph.getExit());
//  	return df.getControlDependencies();
//  }

    /*---------------------------------------------------------------------------*
     * Export graph
     *---------------------------------------------------------------------------*/

    public void exportDOT(File file) throws IOException {
        //noinspection unchecked
        exportDOT(file, (Map) null, null);
    }

    public void exportDOT(File file, DOTNodeLabeller<CFGNode> nl, DOTLabeller<CFGEdge> el) throws IOException {
        CFGExport export = new CFGExport(this, nl, el);
        FileWriter w = new FileWriter(file);
        export.exportDOT(w, graph);
        w.close();
    }

    public void exportDOT(File file, Map<CFGNode, ?> nodeAnnotations, Map<CFGEdge, ?> edgeAnnotations) throws IOException {
        CFGExport export = new CFGExport(this, nodeAnnotations, edgeAnnotations);
        FileWriter w = new FileWriter(file);
        export.exportDOT(w, graph);
        w.close();
    }

    @Override
    public String toString() {
        return super.toString() + this.methodInfo.getFQMethodName();
    }


    /*---------------------------------------------------------------------------*
     * Private methods
     *---------------------------------------------------------------------------*/

    private void check() throws BadGraphException {
        /* Remove unreachable and stuck code */
        deadNodes = TopOrder.findDeadNodes(graph, getEntry());
        Set<CFGNode> stuckNodes = TopOrder.findStuckNodes(graph, getExit());
        if (( !deadNodes.isEmpty() || !stuckNodes.isEmpty()) && !hasThrowEdges) {
			if(!deadNodes.isEmpty()) {
				logger.error("Unexpected dead nodes in " +this.toString()+ " (no ATHROW edges): "+deadNodes);
			}
			if(!stuckNodes.isEmpty()) {
				logger.error("Unexpected stuck nodes  in " +this.toString()+ " (no ATHROW edges): "+stuckNodes);
			}
        }
        Set<CFGNode> unreachableNodes = stuckNodes; 
        unreachableNodes.addAll(deadNodes);
        if (!unreachableNodes.isEmpty()) {
            graph.removeAllVertices(unreachableNodes);
            this.invalidate();
        }
        /* now checks should succeed */
        try {
            TopOrder.checkIsFlowGraph(graph, getEntry(), getExit());
            checkInvokeNodes();
        } catch (BadGraphException ex) {
            debugDumpGraph();
            throw ex;
        }
    }

    /* check invoke and return nodes */
    private void checkInvokeNodes() throws BadGraphException  {
    	
    	for(CFGNode n :this.getGraph().vertexSet()) {
    		if(n instanceof InvokeNode) {
    			if(hasReturnNodes) {
    				if(this.getGraph().outDegreeOf(n) != 1) {
    					throw new BadGraphException("CFG with return nodes but outdegree of invoke node != 1");
    				}
    				CFGEdge re = getGraph().outgoingEdgesOf(n).iterator().next();
    				if(re.getKind() != EdgeKind.RETURN_EDGE) {
    					throw new BadGraphException("CFG with return nodes but return edge has wrong kind");    					
    				}
    				CFGNode r = getGraph().getEdgeTarget(re);
    				if(getGraph().inDegreeOf(r) != 1) {
    					throw new BadGraphException("CFG with return nodes but indegree of return node != 1");    					
    				}
    			}
    		}
    	}
    }
    
    private void invalidate() {
        this.topOrder = null;
        this.loopColoring = null;
        this.isLeafMethod = null;
    }

    /* flow graph should have been checked before analyseFlowGraph is called */

    @SuppressWarnings({"AccessingNonPublicFieldOfAnotherObject"})
    private void analyseFlowGraph() {
        try {
            topOrder = new TopOrder<CFGNode, CFGEdge>(this.graph, this.graph.getEntry());
            idGen = 0;
            this.isLeafMethod = true;
            for (CFGNode vertex : topOrder.getTopologicalTraversal()) {
                if (vertex instanceof InvokeNode) this.isLeafMethod = false;
                vertex.id = idGen++;
            }
            for (CFGNode vertex : TopOrder.findDeadNodes(graph, this.graph.getEntry())) vertex.id = idGen++;
            loopColoring = new LoopColoring<CFGNode, CFGEdge>(this.graph, topOrder, graph.getExit());
        } catch (BadGraphException e) {
            logger.error("Bad flow graph: " + getGraph().toString());
            throw new ControlFlowError("[FATAL] Analyse flow graph failed ", e);
        }
    }

    private void internalError(String reason) {
        logger.error("[INTERNAL ERROR] " + reason);
        logger.error("CFG of " + this.getMethodInfo().getFQMethodName() + "\n");
        logger.error(this.getMethodInfo().getMethod(false).getCode().toString(true));
        throw new AssertionError(reason);
    }

    private void debugDumpGraph() {
        try {
            File tmpFile = File.createTempFile("cfg-dump", ".dot");
            FileWriter fw = new FileWriter(tmpFile);
            new AdvancedDOTExporter<CFGNode, CFGEdge>(new AdvancedDOTExporter.DefaultNodeLabeller<CFGNode>() {
                @Override
                public String getLabel(CFGNode node) {
                    String s = node.toString();
                    if (node.getBasicBlock() != null) s += "\n" + node.getBasicBlock().dump();
                    return s;
                }
            }, null).exportDOT(fw, graph);
            fw.close();
            logger.error("[CFG DUMP] Dumped graph to '" + tmpFile + "'");
        } catch (IOException e) {
            logger.error("[CFG DUMP] Dumping graph failed: " + e);
        }
    }

    private VirtualNode createSplitNode() {
        return new VirtualNode(VirtualNodeKind.SPLIT);
    }

    private VirtualNode createJoinNode() {
        return new VirtualNode(VirtualNodeKind.JOIN);
    }

    private CFGEdge createEntryEdge() {
        return new CFGEdge(EdgeKind.ENTRY_EDGE);
    }

    private CFGEdge createExitEdge() {
        return new CFGEdge(EdgeKind.EXIT_EDGE);
    }

    private CFGEdge createNextEdge() {
        return new CFGEdge(EdgeKind.NEXT_EDGE);
    }


}
