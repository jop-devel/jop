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

import com.jopdesign.common.AppInfo;
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
import com.jopdesign.common.misc.MiscUtils;
import com.jopdesign.common.type.MethodRef;
import org.apache.bcel.Constants;
import org.apache.bcel.generic.INVOKEINTERFACE;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
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
import java.util.HashMap;
import java.util.HashSet;
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

        public ControlFlowGraph getAffectedCFG() {
            return cfg;
        }
    }

    /**
     * Visitor for flow graph nodes
     */
    public interface CfgVisitor {
        void visitSpecialNode(DedicatedNode n);

        void visitBasicBlockNode(BasicBlockNode n);

        /**
         * visit an invoke node. InvokeNode's won't call visitBasicBlockNode.
         * @param n the visited node
         */
        void visitInvokeNode(InvokeNode n);

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

        public BasicBlock getBasicBlock() {
            return null;
        }

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

        protected void dispose() {
        }
    }

    /**
     * Names for dedicated nodes (entry node, exit node)
     */
    public enum DedicatedNodeName { ENTRY, EXIT, SPLIT, JOIN }

    /**
     * Dedicated flow graph nodes
     */
    public class DedicatedNode extends CFGNode {
        private DedicatedNodeName kind;

        public DedicatedNodeName getKind() {
            return kind;
        }

        private DedicatedNode(DedicatedNodeName kind) {
            super(idGen++, kind.toString());
            this.kind = kind;
        }

        @Override
        public void accept(CfgVisitor v) {
            v.visitSpecialNode(this);
        }
    }

    // FIXME: [wcet-frontend] Remove the ugly ih.getAttribute() hack for CFG Nodes

    private static final Object KEY_CFGNODE = new HashedString("ControlFlowGraph.CFGNode");

    /**
     * Get the basic block node associated with an instruction handle
     */
    public static BasicBlockNode getHandleNode(InstructionHandle ih) {
        BasicBlockNode blockNode = (BasicBlockNode) ih.getAttribute(KEY_CFGNODE);
        if (blockNode == null) {
            String errMsg = "No basic block recorded for instruction " + ih.toString(true);
            logger.error(errMsg);
            return null;
        }
        return blockNode;
    }

    /**
     * Flow graph nodes representing basic blocks
     */
    public class BasicBlockNode extends CFGNode {
        protected BasicBlock block;

        public BasicBlockNode(BasicBlock block) {
            super(idGen++, "basic(" + blocks.indexOf(block) + ")");
            this.block = block;
            for (InstructionHandle ih : block.getInstructions()) {
                ih.addAttribute(KEY_CFGNODE, this);
            }
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

    /* IDEA: summary nodes (sub flowgraphs) */

    /**
     * Invoke nodes (Basic block with exactly one invoke instruction).
     */
    public class InvokeNode extends BasicBlockNode {
        private InvokeInstruction instr;
        private MethodRef referenced;
        private MethodInfo receiverImpl;
        private InvokeNode instantiatedFrom;

        private InvokeNode(BasicBlock block) {
            super(block);
        }

        public InvokeNode(BasicBlock block, InvokeInstruction instr) {
            super(block);
            this.instr = instr;
            this.referenced = methodInfo.getReferencedMethod(instr);
            this.name = "invoke(" + this.referenced + ")";
            /* if virtual / interface, this method has to be resolved first */
            if ((instr instanceof INVOKEINTERFACE) || (instr instanceof INVOKEVIRTUAL)) {
                receiverImpl = null;
            } else {
                receiverImpl = referenced.getMethodInfo();
            }

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
        public MethodInfo getImplementedMethod() {
            return this.receiverImpl;
        }

        /**
         * @return all possible implementations of the invoked method
         */
        public List<MethodInfo> getImplementedMethods() {
            return getImplementedMethods(CallString.EMPTY);
        }

        /**
         * @param ctx the callstring of the invocation
         * @return all possible implementations of the invoked method in
         * the given context
         */
        public List<MethodInfo> getImplementedMethods(CallString ctx) {
            if (!isVirtual()) {
                List<MethodInfo> impls = new ArrayList<MethodInfo>(1);
                impls.add(getImplementedMethod());
                return impls;
            } else {
                InvokeSite invokeSite = getMethodInfo().getCode().getInvokeSite(getInstructionHandle());
                return appInfo.findImplementations(invokeSite, ctx);
            }
        }

        /**
         * For non-virtual methods, get the implementation of the method
         * @return the CFG of the implementing receiver or null if unknown.
         */
        public ControlFlowGraph receiverFlowGraph() {
            if (isVirtual()) return null;
            return receiverImpl.getCode().getControlFlowGraph();
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
        public boolean isVirtual() {
            return receiverImpl == null;
        }

        /**
         * If this is the implementation of a virtual/interface invoke instruction,
         * return the InvokeNode for the virtual invoke instruction.
         * TODO: This can be removed, if we ever remove {@link ControlFlowGraph#resolveVirtualInvokes()}
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
    }

    /**
     * Invoke nodes (Basic block with exactly one invoke instruction).
     */
    public class SpecialInvokeNode extends InvokeNode {
        private InstructionHandle instr;
        private MethodInfo receiverImpl;
        private ControlFlowGraph receiverFlowGraph;

        private SpecialInvokeNode(BasicBlock block) {
            super(block);
        }

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
            if (this.receiverFlowGraph == null) {
                this.receiverFlowGraph = receiverImpl.getCode().getControlFlowGraph();
            }
            return this.receiverFlowGraph;
        }

        /**
         * @return true if the invokation denotes an interface, not an implementation
         */
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

    }
    /*
     * Flow Graph Edges
     * ----------------
     */

    /**
     * Type of flow graph edges
     */
    public enum EdgeKind {
        ENTRY_EDGE, EXIT_EDGE, NEXT_EDGE,
        GOTO_EDGE, SELECT_EDGE, BRANCH_EDGE, JSR_EDGE,
        DISPATCH_EDGE,
        INVOKE_EDGE, RETURN_EDGE, FLOW_EDGE, LOW_LEVEL_EDGE
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

        public CFGEdge clone() {
            return new CFGEdge(kind);
        }
    }

    private DedicatedNode splitNode() {
        return new DedicatedNode(DedicatedNodeName.SPLIT);
    }

    private DedicatedNode joinNode() {
        return new DedicatedNode(DedicatedNodeName.JOIN);
    }

    private CFGEdge entryEdge() {
        return new CFGEdge(EdgeKind.ENTRY_EDGE);
    }

    private CFGEdge exitEdge() {
        return new CFGEdge(EdgeKind.EXIT_EDGE);
    }

    private CFGEdge nextEdge() {
        return new CFGEdge(EdgeKind.NEXT_EDGE);
    }

    /*
     * Fields
     * ------
     */
    private int idGen = 0;

    /* linking to java */
    private AppInfo appInfo;
    private MethodInfo methodInfo;

    /* basic blocks associated with the CFG */
    private List<BasicBlock> blocks;

    /* graph */
    private FlowGraph<CFGNode, CFGEdge> graph;
    private Set<CFGNode> deadNodes;

    /* analysis stuff, needs to be reevaluated when graph changes */
    private TopOrder<CFGNode, CFGEdge> topOrder = null;
    private LoopColoring<CFGNode, CFGEdge> loopColoring = null;
    private Boolean isLeafMethod = null;

    public boolean isLeafMethod() {
        return isLeafMethod;
    }


    /**
     * Build a new flow graph for the given method
     *
     * @param method needs attached code (<code>method.getCode() != null</code>)
     * @throws BadGraphException if the bytecode results in an invalid flow graph
     */
    public ControlFlowGraph(MethodInfo method) throws BadGraphException {
        this.methodInfo = method;
        this.appInfo = method.getAppInfo();
        createFlowGraph(method);
        check();
    }

    private ControlFlowGraph(AppInfo appInfo) {
        this.appInfo = appInfo;
        CFGNode subEntry = new DedicatedNode(DedicatedNodeName.ENTRY);
        CFGNode subExit = new DedicatedNode(DedicatedNodeName.EXIT);
        this.graph =
                new DefaultFlowGraph<CFGNode, CFGEdge>(CFGEdge.class, subEntry, subExit);
        this.deadNodes = new HashSet<CFGNode>();
    }

    /* worker: create the flow graph */

    private void createFlowGraph(MethodInfo method) {
        logger.info("creating flow graph for: " + method);
        blocks = BasicBlock.buildBasicBlocks(method.getCode());
        Map<Integer, BasicBlockNode> nodeTable =
                new HashMap<Integer, BasicBlockNode>();
        graph = new DefaultFlowGraph<CFGNode, CFGEdge>(
                CFGEdge.class,
                new DedicatedNode(DedicatedNodeName.ENTRY),
                new DedicatedNode(DedicatedNodeName.EXIT));
        /* Create basic block vertices */
        for (BasicBlock bb : blocks) {
            BasicBlockNode n;
            Instruction lastInstr = bb.getLastInstruction().getInstruction();
            InvokeInstruction theInvoke = bb.getTheInvokeInstruction();
            if (theInvoke != null) {
                n = new InvokeNode(bb, theInvoke);
            } else if (appInfo.getProcessorModel().isImplementedInJava(lastInstr)) {
                MethodInfo javaImpl = appInfo.getProcessorModel().getJavaImplementation(appInfo,
                                            bb.getMethodInfo(),lastInstr);
                n = new SpecialInvokeNode(bb, javaImpl);
            } else {
                n = new BasicBlockNode(bb);
            }
            nodeTable.put(bb.getFirstInstruction().getPosition(), n);
            graph.addVertex(n);
        }
        /* entry edge */
        graph.addEdge(graph.getEntry(),
                nodeTable.get(blocks.get(0).getFirstInstruction().getPosition()),
                entryEdge());
        /* flow edges */
        for (BasicBlockNode bbNode : nodeTable.values()) {
            BasicBlock bb = bbNode.getBasicBlock();
            FlowInfo bbf = BasicBlock.getFlowInfo(bb.getLastInstruction());
            if (bbf.isExit()) { // exit edge
                // do not connect exception edges
                if (bbNode.getBasicBlock().getLastInstruction().getInstruction().getOpcode()
                        == Constants.ATHROW) {
                    logger.warn("Found ATHROW edge - ignoring");
                } else {
                    graph.addEdge(bbNode, graph.getExit(), exitEdge());
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
        this.graph.addEdge(graph.getEntry(), graph.getExit(), exitEdge());
    }

    public void compile() {
        // TODO implement
    }

    /**
     * Clean up all known references to the objects of this graph (i.e. InstructionHandle attributes,..)
     */
    public void dispose() {
        // TODO remove BBNode references from InstructionHandles

    }

    private void internalError(String reason) {
        logger.error("[INTERNAL ERROR] " + reason);
        logger.error("CFG of " + this.getMethodInfo().getFQMethodName() + "\n");
        // TODO check this!
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

    public List<BasicBlock> getBlocks() {
        return blocks;
    }

    /**
     * Create a new map of loopbounds for CFG nodes. The map is not cached, try to use 
     * {@link BasicBlock#getLoopBound()} or {@link CFGNode#getLoopBound()} instead.
     *
     * @see CFGNode#getLoopBound()
     * @see BasicBlock#getLoopBound()  
     * @return a new mapping of loopbounds to cfg nodes. Contains only nodes which have basic 
     *         blocks with loopbounds attached.
     */
    public Map<CFGNode, LoopBound> buildLoopBounds() {
        Map<CFGNode, LoopBound> map = new HashMap<CFGNode, LoopBound>();
        for (CFGNode node : graph.vertexSet()) {
            LoopBound lb = node.getLoopBound();
            if (lb != null) {
                map.put(node,lb);
            }
        }
        return map;
    }

    /**
     * resolve all virtual invoke nodes, and replace them by actual implementations
     *
     * @throws BadGraphException If the flow graph analysis (post replacement) fails
     */
    public void resolveVirtualInvokes() throws BadGraphException {
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
            // TODO resolve with callstring?
            List<MethodInfo> impls = inv.getImplementedMethods();
            if (impls.size() == 0) internalError("No implementations for " + inv.referenced);
            if (impls.size() == 1) {
                InvokeNode implNode = inv.createImplNode(impls.get(0), inv);
                graph.addVertex(implNode);
                for (CFGEdge inEdge : graph.incomingEdgesOf(inv)) {
                    graph.addEdge(graph.getEdgeSource(inEdge), implNode, new CFGEdge(inEdge.kind));
                }
                for (CFGEdge outEdge : graph.outgoingEdgesOf(inv)) {
                    graph.addEdge(implNode, graph.getEdgeTarget(outEdge), new CFGEdge(outEdge.kind));
                }
            } else { /* more than one impl, create split/join nodes */
                CFGNode split = splitNode();
                graph.addVertex(split);
                for (CFGEdge inEdge : graph.incomingEdgesOf(inv)) {
                    graph.addEdge(graph.getEdgeSource(inEdge), split, new CFGEdge(inEdge.kind));
                }
                CFGNode join = joinNode();
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
     * @throws BadGraphException
     */
    public void insertSplitNodes() throws BadGraphException {
        List<CFGNode> trav = this.getTopOrder().getTopologicalTraversal();
        for (CFGNode n : trav) {
            if (n instanceof BasicBlockNode && graph.outDegreeOf(n) > 1) {
                DedicatedNode splitNode = this.splitNode();
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
     * @throws BadGraphException
     */
    public void insertReturnNodes() throws BadGraphException {
        List<CFGNode> trav = this.getTopOrder().getTopologicalTraversal();
        for (CFGNode n : trav) {
            if (n instanceof InvokeNode) {
                DedicatedNode returnNode = this.splitNode();
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
     * @throws BadGraphException
     */
    public void insertContinueLoopNodes() throws BadGraphException {
        List<CFGNode> trav = this.getTopOrder().getTopologicalTraversal();
        for (CFGNode n : trav) {
            if (getLoopColoring().getHeadOfLoops().contains(n)) {
                List<CFGEdge> backEdges = getLoopColoring().getBackEdgesTo(n);
                if (backEdges.size() > 1) {
                    DedicatedNode splitNode = this.splitNode();
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
     * @throws BadGraphException
     */
    public void insertSummaryNodes() throws BadGraphException {
        SimpleDirectedGraph<CFGNode, DefaultEdge> loopNestForest =
                this.getLoopColoring().getLoopNestDAG();
        TopologicalOrderIterator<CFGNode, DefaultEdge> lnfIter =
                new TopologicalOrderIterator<CFGNode, DefaultEdge>(loopNestForest);
        List<CFGNode> summaryLoops = new ArrayList<CFGNode>();
        Set<CFGNode> marked = new HashSet<CFGNode>();
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
                subGraph.addEdge(subGraph.getEntry(), n, subCFG.entryEdge());
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

    /* Check that the graph is connectet, with entry and exit dominating resp. postdominating all nodes */

    private void check() throws BadGraphException {
        /* Remove unreachable and stuck code */
        deadNodes = TopOrder.findDeadNodes(graph, getEntry());
        if (!deadNodes.isEmpty()) logger.error("Found dead code (Exceptions ?): " + deadNodes);
        Set<CFGNode> stucks = TopOrder.findStuckNodes(graph, getExit());
        if (!stucks.isEmpty()) logger.error("Found stuck code (Exceptions ?): " + stucks);
        deadNodes.addAll(stucks);
        if (!deadNodes.isEmpty()) {
            graph.removeAllVertices(deadNodes);
            this.invalidate();
        }
        /* now checks should succeed */
        try {
            TopOrder.checkIsFlowGraph(graph, getEntry(), getExit());
        } catch (BadGraphException ex) {
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
            for (CFGNode vertex : topOrder.getTopologicalTraversal()) {
                if (vertex instanceof InvokeNode) this.isLeafMethod = false;
                vertex.id = idGen++;
            }
            for (CFGNode vertex : TopOrder.findDeadNodes(graph, this.graph.getEntry())) vertex.id = idGen++;
            loopColoring = new LoopColoring<CFGNode, CFGEdge>(this.graph, topOrder, graph.getExit());
        } catch (BadGraphException e) {
            logger.error("Bad flow graph: " + getGraph().toString());
            throw new Error("[FATAL] Analyse flow graph failed ", e);
        }
    }

    /**
     * get link to application
     *
     * @return
     */
    public AppInfo getAppInfo() {
        return this.appInfo;
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
     * the (dedicated) entry node of the flow graph
     *
     * @return
     */
    public CFGNode getEntry() {
        return graph.getEntry();
    }

    /**
     * the (dedicated) exit node of the flow graph
     *
     * @return
     */
    public CFGNode getExit() {
        return graph.getExit();
    }

    /**
     * Get the actual flow graph
     *
     * @return
     */
    public FlowGraph<CFGNode, CFGEdge> getGraph() {
        return graph;
    }

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
