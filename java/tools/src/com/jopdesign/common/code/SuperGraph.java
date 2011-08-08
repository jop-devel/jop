/*
 * This file is part of JOP, the Java Optimized Processor
 * see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2010-2011, Benedikt Huber (benedikt.huber@gmail.com)
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

import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.code.ControlFlowGraph.CFGNode;
import com.jopdesign.common.code.ControlFlowGraph.CFGEdge;
import com.jopdesign.common.code.ControlFlowGraph.DedicatedNode;
import com.jopdesign.common.code.ControlFlowGraph.DedicatedNodeName;
import com.jopdesign.common.code.ControlFlowGraph.InvokeNode;
import com.jopdesign.common.graphutils.AdvancedDOTExporter;
import com.jopdesign.common.graphutils.FlowGraph;
import com.jopdesign.common.graphutils.Pair;
import com.jopdesign.common.graphutils.TopOrder;
import com.jopdesign.common.misc.BadGraphException;

import org.apache.log4j.Logger;
import org.jgrapht.graph.DirectedMultigraph;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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
 * <p>A supergraph merges call graph and control flow graph representations.
 * It has two kind of edges: intraprocedural edges {@code IntraEdge}, which
 * connect CFG nodes within one CFG, and interprocedural edges ({@code SuperEdge}),
 * which connect CFG nodes from different CFGs.</p>
 * <p>There may be several instances of a control flow graph in the supergraph,
 * which are distinguished by their call context</p>
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
     * Purpose: Represents one CFG instance (in a certain context) in the supergraph.
     * A context is represented by
     * <ol><li/> the callstring
     * <li/> the context id
     * </ol> The latter is useful to distinguish CFG instances, which share the same callstring
     *
     * @author Benedikt Huber (benedikt@vmars.tuwien.ac.at)
     */
    public class ContextCFG {

        private ControlFlowGraph cfg;
        private CallContext context;

        public ContextCFG(ControlFlowGraph cfg, CallString cs) {
            this(cfg, new CallContext(cs));
        }

        public ContextCFG(ControlFlowGraph cfg, CallContext ctx) {
            this.cfg = cfg;
            this.context = ctx;
        }

        /** @return the cfg */
        public ControlFlowGraph getCfg() {

        	return cfg;
        }

        /** @return the callstring of this CFG instance */
        public CallString getCallString() {

        	return context.getCallString();
        }

        /**
         * @return the context, distinguishing two supergraph nodes for the
         *         same control flow graph
         */
        public SuperGraph.CallContext getContext() {
        	
            return context;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((cfg == null) ? 0 : cfg.hashCode());
            result = prime * result + ((context == null) ? 0 : context.hashCode());
            return result;
        }

        @Override
        @SuppressWarnings({"AccessingNonPublicFieldOfAnotherObject"})
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;

            ContextCFG other = (ContextCFG) obj;
            if (cfg == null) {
                if (other.cfg != null)
                    return false;
            } else if (!cfg.equals(other.cfg))
                return false;
            if (context == null) {
                if (other.context != null)
                    return false;
            } else if (!context.equals(other.context))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "ContextCFG [" + cfg.getMethodInfo() + ", context=" + context + "]";
        }
    }
    
    /**
     * Edges in a supergraph, including interprocedural edges,
     * and CFG edges.
     */
    public abstract static class SuperGraphEdge {
        
    	/**
         * Type of super graph edges
         */
        public enum EdgeKind {
        	INVOKE_EDGE, RETURN_EDGE,
        	CFG_EDGE
        };

        private static final long serialVersionUID = 1L;
		private EdgeKind kind;

        public SuperGraphEdge(EdgeKind kind) {
            this.kind = kind;
        }

        public EdgeKind getEdgeKind()  {
        	return kind;
        }
        
    }
    
    /**
     * Intraprocedural edges in the supergraph
     */
    public static class IntraEdge extends SuperGraphEdge {
        private static final long serialVersionUID = 1L;
		private ContextCFG ccfg;
		private CFGEdge cfgEdge;

        public IntraEdge(ContextCFG ccfg, ControlFlowGraph.CFGEdge e) {
            super(EdgeKind.CFG_EDGE);
            this.ccfg = ccfg;
            this.cfgEdge = e;
        }

        public ContextCFG getContextCFG() {
        
        	return ccfg;
        }

        public ControlFlowGraph getCFG() {
        	
        	return ccfg.getCfg();
        }
        
        public CallContext getCallContext() {

        	return ccfg.getContext();
        }

        public CFGEdge getCFGEdge() {
        	
        	return cfgEdge;
        }        
        
        @Override
        public boolean equals(Object obj) {

        	if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;

            IntraEdge other = (IntraEdge) obj;
            return(this.ccfg.equals(other.getContextCFG()) && this.getCFGEdge().equals(other.getCFGEdge()));
        }
        
        @Override
        public int hashCode() {
        	final int prime = 31;
        	int r = this.getEdgeKind().hashCode();
        	r = prime * r + ccfg.hashCode();
        	r = prime * r + cfgEdge.hashCode();
        	return r;
        }
    }

    /**
     * Interprocedural edges representing a method invocation or a method return
     * There are explicitly represented in the supergraph
     */
    public abstract class SuperEdge extends SuperGraphEdge {
        private static final long serialVersionUID = 1L;
        private ControlFlowGraph.InvokeNode invoker;

        
        private SuperEdge(SuperGraphEdge.EdgeKind kind, ControlFlowGraph.InvokeNode invokeNode) {
            super(kind);
            this.invoker = invokeNode;
        }

        public abstract boolean isReturnEdge();
        
        public ControlFlowGraph.InvokeNode getInvokeNode() {
            return invoker;
        }        

        public ContextCFG getCaller() {
        	return superGraph.getEdgeSource(this);
        }
        
        public ContextCFG getCallee() {
        	return superGraph.getEdgeTarget(this);
        }

    }

    /**
     * Edge representing a method invocation
     */
    public class SuperInvokeEdge extends SuperEdge {

		public SuperInvokeEdge(ControlFlowGraph.InvokeNode invokeNode) {
            super(EdgeKind.INVOKE_EDGE, invokeNode);
        }

		@Override
		public boolean isReturnEdge() {
			return false;
		}
    	
    }
    
    /**
     * Edge representing return to the invoking method
     */
    public class SuperReturnEdge extends SuperEdge {
    	private CFGNode returnNode;

		public SuperReturnEdge(ControlFlowGraph.InvokeNode invokeNode, ControlFlowGraph.CFGNode returnNode) {
            super(EdgeKind.RETURN_EDGE, invokeNode);
            this.returnNode = returnNode;
        }

        /** 
         * Get the node control flow returns to after the invocation
         */
		public CFGNode getReturnNode() {
			return returnNode;
		}

		@Override
		public boolean isReturnEdge() {
			return true;
		}
    	
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
        private Iterator<ContextCFG> sgIterator;
        private Iterator<CFGNode> nodeIterator;
        private Set<ControlFlowGraph> cfgsVisited;

        public CFGNodeIterator() {
            this.sgIterator = superGraph.vertexSet().iterator();
            this.cfgsVisited = new HashSet<ControlFlowGraph>();
            if (sgIterator.hasNext()) {
                sgIterator = null; // empty
            } else {
                ContextCFG firstNode = sgIterator.next();
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
                    ContextCFG nextSuperGraphNode = sgIterator.next();
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
    /**
     * SG node for the root method
     */
    private ContextCFG rootNode;

    /**
     * The JGraphT storage for the supergraph structure (build from CFGs and edges connecting control flow graphs)
     * FIXME: should be replaced by a callgraph; to this end, we need to consolidate call context and call string in
     * the CallGraph datastructure.
     */
    private DirectedMultigraph<ContextCFG, SuperEdge> superGraph;

    private CFGProvider cfgProvider;

    /**
     * (invoke,return) edge pairs
     */
    private Map<SuperInvokeEdge, SuperReturnEdge> superEdgePairs;

    public SuperGraph(CFGProvider cfgProvider, ControlFlowGraph rootFlowGraph, int callstringLength) {
        this(cfgProvider, rootFlowGraph, callstringLength, CallString.EMPTY);
    }

    public SuperGraph(CFGProvider cfgProvider,
                      ControlFlowGraph rootFlowGraph,
                      int callstringLength,
                      CallString initialCallString) {
    	
        this.cfgProvider = cfgProvider;
        this.rootNode = new ContextCFG(rootFlowGraph, initialCallString);
        this.superGraph = new DirectedMultigraph<ContextCFG, SuperEdge>(SuperEdge.class);
        this.superEdgePairs = new HashMap<SuperInvokeEdge, SuperReturnEdge>();
        createSuperGraph(callstringLength);
    }

    public Set<ContextCFG> getSuperGraphNodes() {

        return superGraph.vertexSet();
    }

    public Set<SuperEdge> getSuperEdges() {

        return superGraph.edgeSet();
    }

    public Map<SuperInvokeEdge, SuperReturnEdge> getSuperEdgePairs() {

        return superEdgePairs;
    }


    
    public Collection<SuperInvokeEdge> outgoingInvokeEdgesOf(ContextCFG n, InvokeNode callSite) {
    	
    	return filterInvokeEdges(superGraph.outgoingEdgesOf(n), callSite);
    }

    public Collection<SuperInvokeEdge> incomingInvokeEdgesOf(ContextCFG n) {
    	
    	return filterInvokeEdges(superGraph.incomingEdgesOf(n), null);
    }

    private Vector<SuperInvokeEdge> filterInvokeEdges(Collection<? extends SuperEdge> coll, InvokeNode callSite) {

        Vector<SuperInvokeEdge> invokeEdges = new Vector<SuperInvokeEdge>();
        for (SuperGraphEdge edge : coll) {
            if (! (edge instanceof SuperInvokeEdge)) continue;
            SuperInvokeEdge invEdge = (SuperInvokeEdge) edge;
            CallString callString = superGraph.getEdgeTarget(invEdge).getCallString();
            if (callSite != null && ! callString.isEmpty()) {
            	if(callString.first().getInstructionHandle() != callSite.getInstructionHandle()) continue;
            }
            invokeEdges.add(invEdge);
        }
        return invokeEdges;    	
    }
    
    public Collection<SuperReturnEdge> outgoingReturnEdgesOf(ContextCFG n) {
    	
    	return filterReturnEdges(superGraph.outgoingEdgesOf(n));
    }

    private Vector<SuperReturnEdge> filterReturnEdges(Collection<? extends SuperEdge> coll) {

        Vector<SuperReturnEdge> returnEdges = new Vector<SuperReturnEdge>();
        for (SuperGraphEdge edge : coll) {
            if (! (edge instanceof SuperReturnEdge)) continue;
            returnEdges.add((SuperReturnEdge) edge);
        }
        return returnEdges;    	
    }

    /**
	 * <p>Get inter- and intraprocedural successor edges</p>
	 * The successors are generated as follows:
	 * <ul>
	 * <li/>edge is intraprocedural, target is invoke node: superedge invoking the callee
	 * <li/>edge is intraprocedural, target is exit node: superedge returning to the caller
	 * <li/>edge is intraprocedural, other target: intraprocedural CFG edge
	 * <li/>edge is interprocedural, invoke: entry edge of the invoked method
	 * <li/>edge is interprocedural, return: outgoing edges of the return node
	 * </ul>
	 * @param edge the reference edge
	 * @return all successors of the target of the given supergraph edge
	 */
	public Collection<SuperGraphEdge> getSuccessorEdges(SuperGraphEdge edge) {
		
		List<SuperGraphEdge> succEdges = new ArrayList<SuperGraphEdge>();
		switch(edge.getEdgeKind()) {
		case CFG_EDGE:
		{
			/* successors of a control flow graph node */
			IntraEdge intraEdge = (IntraEdge) edge;
			ContextCFG ccfg = intraEdge.getContextCFG();
			FlowGraph<CFGNode, CFGEdge> intraGraph = ccfg.getCfg().getGraph();
			CFGNode succNode = intraGraph.getEdgeTarget(intraEdge.getCFGEdge());
			
			if(succNode instanceof InvokeNode) {
				InvokeNode invNode = (InvokeNode) succNode;
				/* invoke node: successors are SuperInvoke edges */
				succEdges.addAll(outgoingInvokeEdgesOf(intraEdge.getContextCFG(), invNode));
				
			} else if(succNode instanceof DedicatedNode && ((DedicatedNode)succNode).getKind() == DedicatedNodeName.EXIT) {
				/* exit node: successors are SuperReturn edges */
				succEdges.addAll(outgoingReturnEdgesOf(intraEdge.getContextCFG()));				
			} else {
				/* standard edges: successors are outgoing edges of target node */
				for(CFGEdge succEdge : intraGraph.outgoingEdgesOf(succNode)) {
					succEdges.add(new IntraEdge(ccfg, succEdge));
				}				
			}
			break;
		}
		case INVOKE_EDGE:
		{
			/* successor is the entry edge of the invoked implementations */
			SuperInvokeEdge invokeEdge = (SuperInvokeEdge) edge;
			ContextCFG ccfg = superGraph.getEdgeTarget(invokeEdge);
			for(CFGEdge e: ccfg.getCfg().getGraph().outgoingEdgesOf(ccfg.getCfg().getEntry())) {
				succEdges.add(new IntraEdge(ccfg,e));
			}
			break;
		}
		case RETURN_EDGE:
			/* successors are the outgoing edges of the return node */
			SuperReturnEdge returnEdge = (SuperReturnEdge) edge;
			ContextCFG ccfg = superGraph.getEdgeTarget(returnEdge);
			for(CFGEdge succ : ccfg.getCfg().getGraph().outgoingEdgesOf(returnEdge.getReturnNode())) {
				succEdges.add(new IntraEdge(ccfg, succ));
			}
			break;
		}

		return succEdges;
	}


    public DirectedMultigraph<ContextCFG, SuperEdge> getGraph() {

        return superGraph;
    }

    public List<Pair<SuperInvokeEdge, SuperReturnEdge>> getCallSites(ContextCFG ccfg) {
        Vector<Pair<SuperInvokeEdge, SuperReturnEdge>> callSites =
                new Vector<Pair<SuperInvokeEdge, SuperReturnEdge>>();
        for (SuperEdge e : superGraph.incomingEdgesOf(ccfg)) {
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
        for (ContextCFG node : superGraph.vertexSet()) {

            List<Pair<SuperInvokeEdge, SuperReturnEdge>> callSites = getCallSites(node);
            MethodInfo invoked = node.getCfg().getMethodInfo();
            if (iMap.containsKey(invoked)) {
                callSites.addAll(iMap.get(invoked));
            }
            iMap.put(invoked, callSites);
        }

        return iMap;
    }




    private void createSuperGraph(int callstringLength) {

        Stack<ContextCFG> todo = new Stack<ContextCFG>();

        todo.push(rootNode);
        superGraph.addVertex(rootNode);

        while (!todo.empty()) {
            ContextCFG current = todo.pop();
            
        	if(! current.getCfg().areVirtualInvokesResolved()) {
        		throw new AssertionError("Virtual dispatch nodes not yet supported for supergraph (file a bug)");                    		
        	}
            
            CallString currentCS = current.getCallString();

            for (CFGNode node : current.getCfg().getGraph().vertexSet()) {
                if (node instanceof ControlFlowGraph.InvokeNode) {
                    ControlFlowGraph.InvokeNode iNode = (ControlFlowGraph.InvokeNode) node;
                    Set<MethodInfo> impls = iNode.getImplementedMethods();
                    if(impls.size() == 0) {
                    	throw new AssertionError("No implementations for iNode available");
                    } else if(impls.size() != 1) {
                    	throw new AssertionError("Unresolved virtual Dispatch for " + iNode + ": " + impls);
                    }
                    for(MethodInfo impl : impls) {
                    	ControlFlowGraph invokedCFG = cfgProvider.getFlowGraph(impl);
                    	CallString invokedCS = currentCS.push(iNode, callstringLength);
                    	ContextCFG invoked = new ContextCFG(invokedCFG, invokedCS);

                    	if (!superGraph.containsVertex(invoked)) {
                    		superGraph.addVertex(invoked);
                    		todo.push(invoked);
                    	}
                        addEdge(iNode, current, invoked);
                    }
                }
            }
        }
    }

    private void addEdge(ControlFlowGraph.InvokeNode invokeNode, ContextCFG invoker, ContextCFG invoked) {
    	
        SuperInvokeEdge iEdge = new SuperInvokeEdge(invokeNode);
        superGraph.addEdge(invoker, invoked, iEdge);
        FlowGraph<CFGNode, CFGEdge> invokerGraph = invoker.getCfg().getGraph();

        if (invokerGraph.outDegreeOf(invokeNode) != 1) {
            throw new AssertionError("SuperGraph: Outdegree of invoker node != 1 (Missing return node?)");
        }
        CFGNode returnNode = invokerGraph.getEdgeTarget(invokerGraph.outgoingEdgesOf(invokeNode).iterator().next());
        if (invokerGraph.inDegreeOf(returnNode) != 1) {
            throw new AssertionError("SuperGraph: Indegree of return node != 1 (Missing return node?)");
        }
        SuperReturnEdge rEdge = new SuperReturnEdge(invokeNode, returnNode);
        superGraph.addEdge(invoked, invoker, rEdge);

        superEdgePairs.put(iEdge, rEdge);
    }

    public void exportDOT(FileWriter dotWriter) throws IOException {
    	
        AdvancedDOTExporter.DOTNodeLabeller<ContextCFG> nodeLabeller =
                new AdvancedDOTExporter.DefaultNodeLabeller<ContextCFG>() {

                    @Override
                    public String getLabel(ContextCFG node) {
                        return node.toString();
                    }

                };
        AdvancedDOTExporter<ContextCFG, SuperEdge> de = new AdvancedDOTExporter<ContextCFG, SuperEdge>(
                nodeLabeller, null);
        de.exportDOT(dotWriter, this.superGraph);
    }

    /**
     * @return the root node of the supergraph
     */
    public ContextCFG getRootNode() {
    	
        return this.rootNode;
    }

    /**
     * If the graph is callgraph is acyclic, return a topological order iterator
     *
     * @return an iterator which yields N before M if there is a path of invoke edges from N to M
     * @throws BadGraphException if the graph is not acyclic
     */
    public TopOrder<ContextCFG, SuperEdge> topologicalOrderIterator() throws BadGraphException {
        return new TopOrder<ContextCFG, SuperEdge>(superGraph, rootNode);
    }

}
