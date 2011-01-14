/*
 * This file is part of JOP, the Java Optimized Processor
 * see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2010, Benedikt Huber (benedikt.huber@gmail.com)
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
import com.jopdesign.common.code.ControlFlowGraph.CFGNode;
import com.jopdesign.common.graphutils.AdvancedDOTExporter;
import com.jopdesign.common.graphutils.FlowGraph;
import com.jopdesign.common.graphutils.Pair;
import com.jopdesign.common.graphutils.TopOrder;
import com.jopdesign.common.misc.BadGraphException;
import org.jgrapht.graph.DirectedMultigraph;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

/**
 * A supergraph is similar to a call graph, but models the actual edges
 * connecting the control flow graphs.
 *
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 */
public class SuperGraph {

    /**
     * Call contexts distinguish different instances of one control flow graph
     */
    public static class CallContext implements CallStringProvider {

        private CallString cs;
        private int ccid;

        public CallContext(CallString cs) {
            this.cs = cs;
            this.ccid = 0;
        }

        /* (non-Javadoc)
                  * @see com.jopdesign.dfa.framework.CallStringProvider#getCallString()
                  */

        @Override
        public CallString getCallString() {
            return cs;
        }

        /* (non-Javadoc)
                  * @see java.lang.Object#hashCode()
                  */

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ccid;
            result = prime * result + ((cs == null) ? 0 : cs.hashCode());
            return result;
        }

        /* (non-Javadoc)
                  * @see java.lang.Object#equals(java.lang.Object)
                  */

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            CallContext other = (CallContext) obj;
            if (ccid != other.ccid)
                return false;
            if (cs == null) {
                if (other.cs != null)
                    return false;
            } else if (!cs.equals(other.cs))
                return false;
            return true;
        }

        /* (non-Javadoc)
                  * @see java.lang.Object#toString()
                  */

        @Override
        public String toString() {
            return "CallContext [cs=" + cs + ((ccid == 0) ? "]" : (", ccid=" + ccid + "]"));
        }


    }

    /**
     * Edge connecting nodes from different CFGs
     */
    public abstract static class SuperGraphEdge extends ControlFlowGraph.CFGEdge {
        private static final long serialVersionUID = 1L;

        public SuperGraphEdge(ControlFlowGraph.EdgeKind kind) {
            super(kind);
        }

        public abstract CallContext getCallContext();
    }

    /**
     * Edge representing a method invocation
     */
    public static class SuperInvokeEdge extends SuperGraphEdge {
        private static final long serialVersionUID = 1L;
        ControlFlowGraph.InvokeNode invoker;
        private CallContext context;

        public SuperInvokeEdge(ControlFlowGraph.InvokeNode node, CallContext ctx) {
            super(ControlFlowGraph.EdgeKind.INVOKE_EDGE);
            this.invoker = node;
            this.context = ctx;
        }

        public ControlFlowGraph.InvokeNode getInvokeNode() {
            return invoker;
        }

        public CallContext getCallContext() {
            return context;
        }
    }

    /**
     * Edge representing return to the invoking method
     */
    public static class SuperReturnEdge extends SuperGraphEdge {
        private static final long serialVersionUID = 1L;
        CFGNode returnNode;
        private CallContext context;

        public SuperReturnEdge(CFGNode returnNode, CallContext context) {
            super(ControlFlowGraph.EdgeKind.RETURN_EDGE);
            this.returnNode = returnNode;
            this.context = context;
        }

        public CFGNode getReturnNode() {
            return returnNode;
        }

        public CallContext getCallContext() {
            return context;
        }
    }

    /**
     * SG node forthe root method
     */
    private SuperGraphNode rootNode;

    /**
     * Set of supergraph nodes
     */
    private Vector<SuperGraphNode> superGraphNodes;

    /**
     * The JGraphT storage for the supergraph structure (build from CFGs and edges connecting control flow graphs)
     */
    private DirectedMultigraph<SuperGraphNode, SuperGraphEdge> superGraph;

    /**
     * The Java Application
     */
    private AppInfo ai;

    /**
     * (invoke,return) edge pairs
     */
    private Map<SuperInvokeEdge, SuperReturnEdge> superEdgePairs;

    public SuperGraph(AppInfo appInfo, ControlFlowGraph rootFlowGraph, int callstringLength) {
        this(appInfo, rootFlowGraph, callstringLength, CallString.EMPTY);
    }

    public SuperGraph(AppInfo appInfo,
                      ControlFlowGraph rootFlowGraph,
                      int callstringLength,
                      CallString initialCallString) {
        this.ai = appInfo;
        this.rootNode = new SuperGraphNode(rootFlowGraph, initialCallString);
        this.superGraphNodes = new Vector<SuperGraphNode>();
        this.superGraph = new DirectedMultigraph<SuperGraphNode, SuperGraphEdge>(SuperGraphEdge.class);
        this.superEdgePairs = new HashMap<SuperInvokeEdge, SuperReturnEdge>();
        createSuperGraph(callstringLength);
    }

    public Set<SuperGraphNode> getSuperGraphNodes() {

        return superGraph.vertexSet();
    }

    public Set<SuperGraphEdge> getSuperEdges() {

        return superGraph.edgeSet();
    }

    /**
     * @param edge
     * @return
     */
    public SuperGraphNode getTargetNode(SuperGraphEdge edge) {
        return superGraph.getEdgeTarget(edge);
    }


    public Map<SuperInvokeEdge, SuperReturnEdge> getSuperEdgePairs() {

        return superEdgePairs;
    }


    /** */
    public Collection<SuperInvokeEdge> incomingInvokeEdgesOf(SuperGraphNode n) {
        Vector<SuperInvokeEdge> invokeEdges = new Vector<SuperInvokeEdge>();
        for (SuperGraphEdge edge : superGraph.incomingEdgesOf(n)) {
            if (edge instanceof SuperInvokeEdge) {
                invokeEdges.add((SuperInvokeEdge) edge);
            }
        }
        return invokeEdges;
    }


    public DirectedMultigraph<SuperGraphNode, SuperGraphEdge> getGraph() {

        return superGraph;
    }

    public List<Pair<SuperInvokeEdge, SuperReturnEdge>> getCallSites(SuperGraphNode cfg) {
        Vector<Pair<SuperInvokeEdge, SuperReturnEdge>> callSites =
                new Vector<Pair<SuperInvokeEdge, SuperReturnEdge>>();
        for (SuperGraphEdge e : superGraph.incomingEdgesOf(cfg)) {
            if (e instanceof SuperInvokeEdge) {
                SuperInvokeEdge ei = (SuperInvokeEdge) e;
                callSites.add(new Pair<SuperInvokeEdge, SuperReturnEdge>(ei, superEdgePairs.get(ei)));
            }
        }
        return callSites;
    }

    /**
     * @return return all callsite invoke/return superedge pairs, grouped by the invoked method
     */
    public Map<MethodInfo, List<Pair<SuperInvokeEdge, SuperReturnEdge>>> getAllCallSites() {

        Map<MethodInfo, List<Pair<SuperInvokeEdge, SuperReturnEdge>>> iMap =
                new HashMap<MethodInfo, List<Pair<SuperInvokeEdge, SuperReturnEdge>>>();
        for (SuperGraphNode node : superGraph.vertexSet()) {

            List<Pair<SuperInvokeEdge, SuperReturnEdge>> callSites = getCallSites(node);
            MethodInfo invoked = node.getCfg().getMethodInfo();
            if (iMap.containsKey(invoked)) {
                callSites.addAll(iMap.get(invoked));
            }
            iMap.put(invoked, callSites);
        }

        return iMap;
    }


    /**
     * @return iterate over all CFG nodes in the supergraph
     */
    public CFGNodeIteratorFactory allCFGNodes() {
        return new CFGNodeIteratorFactory();
    }

    private class CFGNodeIteratorFactory implements Iterable<CFGNode> {
        @Override
        public Iterator<CFGNode> iterator() {
            return new CFGNodeIterator();
        }
    }

    private class CFGNodeIterator implements Iterator<CFGNode> {
        private Iterator<SuperGraphNode> sgIterator;
        private Iterator<CFGNode> nodeIterator;
        private Set<ControlFlowGraph> cfgsVisited;

        public CFGNodeIterator() {
            this.sgIterator = superGraph.vertexSet().iterator();
            this.cfgsVisited = new HashSet<ControlFlowGraph>();
            if (sgIterator.hasNext()) {
                sgIterator = null; // empty
            } else {
                SuperGraphNode firstNode = sgIterator.next();
                cfgsVisited.add(firstNode.getCfg());
                nodeIterator = firstNode.getCfg().getGraph().vertexSet().iterator();
            }
        }

        @Override
        public boolean hasNext() {
            if (sgIterator == null) return false;
            return nodeIterator.hasNext();
        }

        @Override
        public CFGNode next() {
            if (!nodeIterator.hasNext()) throw new NoSuchElementException("Iterator.next(): no more CFG node");
            CFGNode next = nodeIterator.next();
            if (!nodeIterator.hasNext()) {    /* See if there are more CFGs to investigate */
                while (sgIterator.hasNext()) { /* Find unvisited super graph nodes */
                    SuperGraphNode nextSuperGraphNode = sgIterator.next();
                    if (!cfgsVisited.contains(nextSuperGraphNode.getCfg())) { /* If CFG has not been visited */
                        cfgsVisited.add(nextSuperGraphNode.getCfg());         /* Mark CFG as visited */
                        nodeIterator = nextSuperGraphNode.getCfg().getGraph().vertexSet().iterator(); /* Reset Node iterator */
                        break;
                    }
                }
            }
            return next;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("SuperGraph.CFGNodeIterator does not support remove");
        }

    }

    private void createSuperGraph(int callstringLength) {

        Stack<SuperGraphNode> todo = new Stack<SuperGraphNode>();

        todo.push(rootNode);
        superGraph.addVertex(rootNode);

        while (!todo.empty()) {
            SuperGraphNode current = todo.pop();
            this.superGraphNodes.add(current);

            CallString currentCS = current.getCallString();

            for (CFGNode node : current.getCfg().getGraph().vertexSet()) {
                if (node instanceof ControlFlowGraph.InvokeNode) {
                    ControlFlowGraph.InvokeNode iNode = (ControlFlowGraph.InvokeNode) node;
                    MethodInfo impl = iNode.getImplementedMethod();
                    ControlFlowGraph invokedCFG = impl.getCode().getControlFlowGraph();
                    CallString invokedCS = currentCS.push(iNode, callstringLength);
                    SuperGraphNode invoked = new SuperGraphNode(invokedCFG, invokedCS);

                    if (!superGraph.containsVertex(invoked)) {
                        superGraph.addVertex(invoked);
                        todo.push(invoked);
                    }
                    addEdge(iNode, current, invoked);
                }
            }
        }
    }

    private void addEdge(ControlFlowGraph.InvokeNode node, SuperGraphNode invoker, SuperGraphNode invoked) {
        SuperInvokeEdge iEdge = new SuperInvokeEdge(node, invoker.getContext());
        superGraph.addEdge(invoker, invoked, iEdge);
        FlowGraph<CFGNode, ControlFlowGraph.CFGEdge> invokerGraph = invoker.getCfg().getGraph();

//		addToSet(specialInSet,invoked.getEntry(), iEdge);
//		addToSet(specialOutSet, node, iEdge);

        if (invokerGraph.outDegreeOf(node) != 1) {
            throw new AssertionError("SuperGraph: Outdegree of invoker node > 1.");
        }
        CFGNode returnNode = invokerGraph.getEdgeTarget(invokerGraph.outgoingEdgesOf(node).iterator().next());
        if (invokerGraph.inDegreeOf(returnNode) != 1) {
            throw new AssertionError("SuperGraph: Indegree of return node != 1. Maybe return node missing ?");
        }
        SuperReturnEdge rEdge = new SuperReturnEdge(returnNode, invoker.getContext());
        superGraph.addEdge(invoked, invoker, rEdge);

//		addToSet(specialOutSet,invoked.getExit(), rEdge);
//		addToSet(specialInSet, returnNode, rEdge);

        superEdgePairs.put(iEdge, rEdge);
    }

    public void exportDOT(FileWriter dotWriter) throws IOException {
        AdvancedDOTExporter.DOTNodeLabeller<SuperGraphNode> nodeLabeller =
                new AdvancedDOTExporter.DefaultNodeLabeller<SuperGraphNode>() {

                    @Override
                    public String getLabel(SuperGraphNode node) {
                        return node.toString();
                    }

                };
        AdvancedDOTExporter<SuperGraphNode, SuperGraphEdge> de = new AdvancedDOTExporter<SuperGraphNode, SuperGraphEdge>(
                nodeLabeller, null);
        de.exportDOT(dotWriter, this.superGraph);
    }

    /**
     * @return
     */
    public SuperGraphNode getRootNode() {
        return this.rootNode;
    }

    /**
     * If the graph is callgraph is acyclic, return a topological order iterator
     *
     * @return an iterator which yields N before M if there is a path of invoke edges from N to M
     * @throws BadGraphException if the graph is not acyclic
     */
    public TopOrder<SuperGraphNode, SuperGraphEdge> topologicalOrderIterator() throws BadGraphException {
        return new TopOrder<SuperGraphNode, SuperGraphEdge>(superGraph, rootNode);
    }


}
