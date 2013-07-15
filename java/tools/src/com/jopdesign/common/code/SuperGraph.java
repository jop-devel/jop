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
import com.jopdesign.common.code.ControlFlowGraph.InvokeNode;
import com.jopdesign.common.code.ControlFlowGraph.ReturnNode;
import com.jopdesign.common.code.ControlFlowGraph.VirtualNode;
import com.jopdesign.common.code.ControlFlowGraph.VirtualNodeKind;
import com.jopdesign.common.graphutils.AdvancedDOTExporter;
import com.jopdesign.common.graphutils.Pair;
import com.jopdesign.common.graphutils.TopOrder;
import com.jopdesign.common.graphutils.AdvancedDOTExporter.DOTLabeller;
import com.jopdesign.common.misc.BadGraphException;
import com.jopdesign.common.misc.Filter;
import com.jopdesign.common.misc.MappedIterable;

import org.apache.log4j.Logger;
import org.jgrapht.graph.DirectedMultigraph;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
     * FIXME: Merge me with the callstring concept
     */
    public static class CallContext implements CallStringProvider {

        private CallString cs;
        private int ccid;

        public CallContext(CallString cs) {
            this.cs = cs;
            this.ccid = 0;
        }

        @Override
        public CallString getCallString() {
            return cs;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ccid;
            result = prime * result + ((cs == null) ? 0 : cs.hashCode());
            return result;
        }

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

        @Override
        public String toString() {
        	if(ccid == 0) {
        		return cs.toString();
        	} else {
        		return cs.toString()+"/"+ccid;
        	}
        }


    }

    /**
     * Purpose: Represents one CFG instance (in a certain context) in the supergraph.
     *
     * FIXME: Merge me with callgraph node
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
            return "CFG{" + cfg.getMethodInfo() + "," + context + "}";
        }
    }
    
    /**
     * Supergraph nodes are pairs(context-cfg, cfg-node).
     * 
     * @author Benedikt Huber (benedikt@vmars.tuwien.ac.at)
     */
    public static class SuperGraphNode {

    	private ContextCFG ccfg;
		private CFGNode node;

		public SuperGraphNode(ContextCFG ccfg, CFGNode node) {
    		this.ccfg = ccfg;
    		this.node = node;
    	}

		public CFGNode getCFGNode() {
			return node;
		}

		public ContextCFG getContextCFG() {
			return this.ccfg;
		}

		@Override
		public int hashCode() {
			return ccfg.hashCode() * 31 + node.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			SuperGraphNode other = (SuperGraphNode) obj;
			return (ccfg.equals(other.ccfg) && node.equals(other.node));
		}
		@Override
		public String toString() {
			return getCfg().getMethodInfo().getShortName() + "(" + node + ")";
		}

		/**
		 * @return return the CFG represented by the supergraph node
		 */
		public ControlFlowGraph getCfg() {
			return ccfg.getCfg();
		}
    }
    
    /**
     * Edges in a supergraph, including interprocedural edges,
     * and CFG edges.
     */
    public interface SuperGraphEdge {
        
    	public SuperGraph getSuperGraph();
        
    	/**
    	 * The source of a supergraph edge.
    	 * <ul>
    	 * <li/>For intraedges: the corresponding lifted source in the CFG
    	 * <li/>For invoke edges: the lifted node at the call site
    	 * <li/>For return edges: the lifted exit node in the invoked CFG
    	 * @param current
    	 * @return
    	 */
        public abstract SuperGraphNode getSource();
        
    	/**
    	 * The target of a supergraph edge.
    	 * <ul>
    	 * <li/>For intraedges: the corresponding lifted target in the CFG
    	 * <li/>For invoke edges: the lifted entry node of the invoked function
    	 * <li/>For return edges: the lifted return node at the call site
    	 * @param current
    	 * @return
    	 */
        public abstract SuperGraphNode getTarget();
        
    }
    
    /**
     * Intraprocedural edges in the supergraph
     */
    public final class IntraEdge implements SuperGraphEdge {
		private ContextCFG ccfg;
		private CFGEdge cfgEdge;

        public IntraEdge(ContextCFG ccfg, ControlFlowGraph.CFGEdge e) {
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
        
		/* the corresponding lifted source in the CFG */
		@Override
		public SuperGraphNode getSource() {
			ContextCFG ccfg = getContextCFG();
			CFGNode target = ccfg.getCfg().getEdgeSource(getCFGEdge());
			return new SuperGraphNode(ccfg, target);
		}

		/* the corresponding lifted target in the CFG */
		@Override
		public SuperGraphNode getTarget() {
			ContextCFG ccfg = getContextCFG();
			CFGNode target = ccfg.getCfg().getEdgeTarget(getCFGEdge());
			return new SuperGraphNode(ccfg, target);
		}

		@Override
		public SuperGraph getSuperGraph() {
         	return SuperGraph.this;
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
        	return 31 * ccfg.hashCode() + cfgEdge.hashCode();
        }

        @Override
        public String toString() {
        	return this.ccfg.getCfg().getMethodInfo().getShortName() + this.getCFGEdge().toString();
        }

    }

    /**
     * Interprocedural edges representing a method invocation or a method return
     * There are explicitly represented in the supergraph
     */
    public abstract class SuperEdge implements SuperGraphEdge {

    	private final ControlFlowGraph.InvokeNode invoker;

        private SuperEdge(ControlFlowGraph.InvokeNode invokeNode) {
            this.invoker = invokeNode;
        }

        public abstract boolean isReturnEdge();
        
        public ControlFlowGraph.InvokeNode getInvokeNode() {
            return invoker;
        }        

        public abstract ContextCFG getCaller();

        public abstract ContextCFG getCallee();

		@Override
		public SuperGraph getSuperGraph() {
         	return SuperGraph.this;
        }
    }

    /**
     * Edge representing a method invocation
     */
    public final class SuperInvokeEdge extends SuperEdge {

		private final int hashCode;

		public SuperInvokeEdge(ControlFlowGraph.InvokeNode invokeNode, ContextCFG invoker, ContextCFG invoked) {
            super(invokeNode);
            hashCode = calculateHashCode(invoker, invoked);
        }


		@Override
		public boolean isReturnEdge() {
			return false;
		}

		/*  the lifted node at the call site */
		@Override
		public SuperGraphNode getSource() {
			ContextCFG ccfg = superGraph.getEdgeSource(this);
			return new SuperGraphNode(ccfg, this.getInvokeNode());			
    	}
		
		/* the lifted entry node of the invoked function */
		@Override
		public SuperGraphNode getTarget() {
			ContextCFG ccfg = superGraph.getEdgeTarget(this);
			return new SuperGraphNode(ccfg, ccfg.getCfg().getEntry());
		}
    	
		@Override
        public ContextCFG getCaller() {
        	return superGraph.getEdgeSource(this);
        }
        
		@Override
        public ContextCFG getCallee() {
        	return superGraph.getEdgeTarget(this);
        }

        @Override
        public boolean equals(Object obj) {

        	if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;

            SuperInvokeEdge other = (SuperInvokeEdge) obj;
            if (hashCode != other.hashCode) return false;
            /* Should almost always be true */
            return(this.getInvokeNode().equals(other.getInvokeNode()) &&
            	   this.getCallee().equals(other.getCallee()) &&
            	   this.getCaller().equals(other.getCaller()));
        }
        
		private int calculateHashCode(ContextCFG invoker, ContextCFG invoked) {
			int hashCode = invoker.hashCode() ;
			hashCode = hashCode * 37 + invoked.hashCode();
			hashCode = hashCode * 37 + getInvokeNode().hashCode();
			return hashCode;
		}

        @Override
        public int hashCode() {
        	return hashCode;
        }

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("SupInvEdge@");
			sb.append(hashCode());
			sb.append("(");
			sb.append(this.getCaller());
			sb.append(" -> ");
			sb.append(this.getCallee());
			sb.append(")");
			return sb.toString();
		}
    }
    
    /**
     * Edge representing return to the invoking method
     */
    public final class SuperReturnEdge extends SuperEdge {
    	private final CFGNode returnNode;
		private final int hashCode;

		public SuperReturnEdge(ControlFlowGraph.InvokeNode invokeNode, ControlFlowGraph.CFGNode returnNode, ContextCFG invoker, ContextCFG invoked) {
            super(invokeNode);
            this.returnNode = returnNode;
            this.hashCode = calculateHashCode(invoker, invoked);
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

		/* the lifted exit node in the invoked CFG */
		@Override
		public SuperGraphNode getSource() {
			ContextCFG ccfg = superGraph.getEdgeSource(this);
			return new SuperGraphNode(ccfg, ccfg.getCfg().getExit());
		}

		/* successors are the outgoing edges of the return node */
		@Override
		public SuperGraphNode getTarget() {
			ContextCFG ccfg = superGraph.getEdgeTarget(this);
			return new SuperGraphNode(ccfg, this.getReturnNode());
		}
    	
		@Override
        public ContextCFG getCaller() {
        	return superGraph.getEdgeTarget(this);
        }
        
		@Override
        public ContextCFG getCallee() {
        	return superGraph.getEdgeSource(this);
        }
    	
        @Override
        public boolean equals(Object obj) {

        	if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;

            SuperReturnEdge other = (SuperReturnEdge) obj;
            if (hashCode != other.hashCode) return false;
            boolean r = (this.getReturnNode().equals(other.getReturnNode()) &&
            	   this.getCallee().equals(other.getCallee()) &&
            	   this.getCaller().equals(other.getCaller()));
            return r;
        }
        
		private int calculateHashCode(ContextCFG invoker, ContextCFG invoked) {
			int hashCode = invoker.hashCode() ;
			hashCode = hashCode * 37 + invoked.hashCode();
			hashCode = hashCode * 37 + getReturnNode().hashCode();
			return hashCode;
		}

        @Override
        public int hashCode() {
        	return hashCode;
        }

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("SupRetEdge@");
			sb.append(hashCode());
			sb.append("(");
			sb.append(this.getCallee());
			sb.append(" -> ");
			sb.append(this.getCaller());
			sb.append(")");
			return sb.toString();
		}

    }
    

    /**
     * @return the <b>set</b> of all distinct CFG nodes in the supergraph
     */
    public Iterable<CFGNode> cfgNodeSet() {
        return new Iterable<CFGNode>() {
            @Override
            public Iterator<CFGNode> iterator() {
                return new CFGNodeIterator();
            }        	
        };
    }

    private class CFGNodeIterator implements Iterator<CFGNode> {
        private Iterator<ContextCFG> sgIterator;
        private Iterator<CFGNode> nodeIterator;
        private Set<ControlFlowGraph> cfgsVisited;

        public CFGNodeIterator() {
            this.sgIterator = superGraph.vertexSet().iterator();
            this.cfgsVisited = new LinkedHashSet<ControlFlowGraph>();
            if (sgIterator.hasNext()) {
                sgIterator = null; // empty
            } else {
                ContextCFG firstNode = sgIterator.next();
                cfgsVisited.add(firstNode.getCfg());
                nodeIterator = firstNode.getCfg().vertexSet().iterator();
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
                    ContextCFG nextCCFG = sgIterator.next();
                    if (!cfgsVisited.contains(nextCCFG.getCfg())) { /* If CFG has not been visited */
                        cfgsVisited.add(nextCCFG.getCfg());         /* Mark CFG as visited */
                        nodeIterator = nextCCFG.getCfg().vertexSet().iterator(); /* Reset Node iterator */
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
     * Root node of the call graph
     */
    private ContextCFG rootNode;

    /**
     * The JGraphT storage for the supergraph structure (build from CFGs and edges connecting control flow graphs)
     * 
     * FIXME: should be replaced by a callgraph; to this end, we need to consolidate call context and callstring in
     * the CallGraph datastructure.
     */
    private DirectedMultigraph<ContextCFG, SuperEdge> superGraph;

    private CFGProvider cfgProvider;

    /**
     * (invoke,return) edge pairs
     */
    private Map<SuperInvokeEdge, SuperReturnEdge> superEdgePairs;

	private int callstringLength;

	private InfeasibleEdgeProvider infeasibleEdgeProvider;

    public SuperGraph(CFGProvider cfgProvider, ControlFlowGraph rootFlowGraph, int callstringLength) {
    	
        this(cfgProvider, rootFlowGraph, CallString.EMPTY, callstringLength, InfeasibleEdgeProvider.NO_INFEASIBLES);
    }

    public SuperGraph(CFGProvider cfgProvider,
            ControlFlowGraph rootCFG,
            CallString rootCallString,
            int callstringLength,
            InfeasibleEdgeProvider infeasibles) {

    	this.cfgProvider = cfgProvider;
    	this.infeasibleEdgeProvider = infeasibles;
    	this.callstringLength = callstringLength;

    	this.rootNode = new ContextCFG(rootCFG, rootCallString);
    	this.superGraph = new DirectedMultigraph<ContextCFG, SuperEdge>(SuperEdge.class);
    	this.superEdgePairs = new LinkedHashMap<SuperInvokeEdge, SuperReturnEdge>();
    	createSuperGraph();
    }

	/**
	 * @return the CFG provider used for building the supergraph
	 */
	public CFGProvider getCFGProvider() {
		return this.cfgProvider;
	}
	

	/**
	 * @return the infeasible edge provider used in this supergraph
	 */
	public InfeasibleEdgeProvider getInfeasibleEdgeProvider() {

		return this.infeasibleEdgeProvider;
	}

	/**
	 * @return the maximum length of a callstring
	 */
	public int getCallStringLength() {

		return this.callstringLength;
	}


    public DirectedMultigraph<ContextCFG, SuperEdge> getCallGraph() {
    	return superGraph;
    }
    
    public Set<ContextCFG> getCallGraphNodes() {

        return superGraph.vertexSet();
    }

    public Set<SuperEdge> getCallGraphEdges() {

        return superGraph.edgeSet();
    }

    public Map<SuperInvokeEdge, SuperReturnEdge> getSuperEdgePairs() {

        return superEdgePairs;
    }
    
	
	/**
	 * The incoming edges are generated as follows:
	 * <ul>
	 * <li/>return node: superedge returning to the caller
	 * <li/>entry  node: superedge invoking the node's method
	 * <li/>other:       intraprocedural CFG edge
	 * </ul>
	 * @param node
	 * @return
	 */
	public Iterable<SuperGraphEdge> incomingEdgesOf(SuperGraphNode node) {
		CFGNode cfgNode = node.getCFGNode();
		
		if(cfgNode instanceof ReturnNode) {
			/* return node: incoming edges are callgraph return edges */
			final ReturnNode retNode = (ReturnNode) cfgNode;	
			Set<SuperEdge> cgReturnEdges = superGraph.incomingEdgesOf(node.getContextCFG());
			return new Filter<SuperGraphEdge>() {
				@Override
				protected boolean include(SuperGraphEdge e) {
					if(! (e instanceof SuperReturnEdge)) return false;
					SuperReturnEdge retEdge = (SuperReturnEdge) e;
					return retEdge.getReturnNode().equals(retNode);
				}				
			}.<SuperGraphEdge>filter(cgReturnEdges); 

		} else if(cfgNode instanceof VirtualNode && ((VirtualNode)cfgNode).getKind() == VirtualNodeKind.ENTRY) {			
			/* entry  node: superedge invoking the node's method */
			Set<SuperEdge> cgInvokeEdges = superGraph.incomingEdgesOf(node.getContextCFG());
			return new Filter<SuperGraphEdge>() {
				@Override
				protected boolean include(SuperGraphEdge e) {
					return (e instanceof SuperInvokeEdge);
				}				
			}.<SuperGraphEdge>filter(cgInvokeEdges); 

		} else {
			
			/* standard edges: incoming edges of cfg node */
			return liftCFGEdges(node.getContextCFG(), node.getContextCFG().getCfg().incomingEdgesOf(cfgNode));
		}
	}
    
	/**
	 * The outgoing edges are generated as follows:
	 * <ul>
	 * <li/>invoke node: superedge invoking the callee
	 * <li/>exit node: superedge returning to the caller
	 * <li/>other: intraprocedural CFG edge
	 * </ul>
	 * @param node
	 * @return
	 */
	public Iterable<SuperGraphEdge> outgoingEdgesOf(SuperGraphNode node) {
		
		CFGNode cfgNode = node.getCFGNode();

		if(cfgNode instanceof InvokeNode) {
			/* invoke node: outgoing SuperInvoke edges */
			final InvokeNode invNode = (InvokeNode) cfgNode;
			Set<SuperEdge> outgoingInvokeEdges = superGraph.outgoingEdgesOf(node.getContextCFG());
			
			return new Filter<SuperGraphEdge>() {
				@Override
				protected boolean include(SuperGraphEdge e) {
					if(! (e instanceof SuperInvokeEdge)) return false;
					SuperInvokeEdge invoke = (SuperInvokeEdge) e;
					return invoke.getInvokeNode().equals(invNode);
				}				
			}.<SuperGraphEdge>filter(outgoingInvokeEdges); 
			
		} else if(cfgNode instanceof VirtualNode && ((VirtualNode)cfgNode).getKind() == VirtualNodeKind.EXIT) {
			/* exit node: outgoing SuperReturn edges */
			Set<SuperEdge> outgoingReturnEdges = superGraph.outgoingEdgesOf(node.getContextCFG());
			return new Filter<SuperGraphEdge>() {
				@Override
				protected boolean include(SuperGraphEdge e) {
					return (e instanceof SuperReturnEdge);
				}				
			}.<SuperGraphEdge>filter(outgoingReturnEdges); 

		} else {			
			/* standard edges: outgoing edges of cfg node */
			Set<CFGEdge> outgoingCFGEdges = node.getContextCFG().getCfg().outgoingEdgesOf(cfgNode);
			return liftCFGEdges(node.getContextCFG(), outgoingCFGEdges);
		}		
	}

    /**
	 * <p>Get inter- and intraprocedural successor edges</p>
	 * <p>The successors of the edge e are those edges which have e's target as source
	 * @return all successors of the target of the given supergraph edge
	 */
	public Iterable<? extends SuperGraphEdge> getSuccessorEdges(SuperGraphEdge edge) {
		
		return outgoingEdgesOf(edge.getTarget());
	}
    

	/**
	 * Get the intraprocedural entry edges for the given callgraph node
	 * @param node the callgraph node
	 * @return the CFG edges which start the execution of the given node
	 */
	public Iterable<SuperGraphEdge> getCFGEntryEdges(ContextCFG ccfg) {
		ControlFlowGraph cfg = ccfg.getCfg();
		return liftCFGEdges(ccfg, cfg.outgoingEdgesOf(cfg.getEntry()));
	}

	/**
	 * Get the intraprocedural exit edges for the given callgraph node
	 * @param node the callgraph node
	 * @return the CFG edges which end the execution of the given node
	 */
	public Iterable<SuperGraphEdge> getCFGExitEdges(ContextCFG node) {
		ControlFlowGraph cfg = node.getCfg();
		return liftCFGEdges(node, cfg.incomingEdgesOf(cfg.getExit()));
	}

	public Iterable<SuperGraphEdge> liftCFGEdges(final ContextCFG node, final Iterable<? extends CFGEdge> iEdges) {
		
		return new MappedIterable<CFGEdge, SuperGraphEdge>(iEdges) {
			@Override
			public SuperGraphEdge map(CFGEdge e) {
				return new IntraEdge(node, e);
			}
		};
	}
	
	/** Get all call sites located in the given cfg instance
	 * @param ccfg the calling cfg instance
	 * @return all list of invoke/return super edges from the given cfg instance
	 */
    public List<Pair<SuperInvokeEdge, SuperReturnEdge>> getCallSitesFrom(ContextCFG ccfg) {
        Vector<Pair<SuperInvokeEdge, SuperReturnEdge>> callSites =
                new Vector<Pair<SuperInvokeEdge, SuperReturnEdge>>();
        for (SuperEdge e : superGraph.outgoingEdgesOf(ccfg)) {
            if (e instanceof SuperInvokeEdge) {
                SuperInvokeEdge ei = (SuperInvokeEdge) e;
                callSites.add(new Pair<SuperInvokeEdge, SuperReturnEdge>(ei, superEdgePairs.get(ei)));
            }
        }
        return callSites;
    }

    /** Get all call sites invoking the given cfg instance
	 * @param ccfg the cfg instance invoked
	 * @return all list of invoke/return super edges for the call site
	 */
    public List<Pair<SuperInvokeEdge, SuperReturnEdge>> getCallSitesInvoking(ContextCFG ccfg) {
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
    public Map<MethodInfo, List<Pair<SuperInvokeEdge, SuperReturnEdge>>> getCallSites() {

        Map<MethodInfo, List<Pair<SuperInvokeEdge, SuperReturnEdge>>> iMap =
                new LinkedHashMap<MethodInfo, List<Pair<SuperInvokeEdge, SuperReturnEdge>>>();
        for (ContextCFG node : superGraph.vertexSet()) {

            List<Pair<SuperInvokeEdge, SuperReturnEdge>> callSites = getCallSitesInvoking(node);
            MethodInfo invoked = node.getCfg().getMethodInfo();
            if (iMap.containsKey(invoked)) {
                callSites.addAll(iMap.get(invoked));
            }
            iMap.put(invoked, callSites);
        }

        return iMap;
    }


    private void createSuperGraph() {

        Stack<ContextCFG> todo = new Stack<ContextCFG>();

        todo.push(rootNode);
        superGraph.addVertex(rootNode);

        while (!todo.empty()) {
            ContextCFG current = todo.pop();
            
        	if(! current.getCfg().areVirtualInvokesResolved()) {
        		throw new AssertionError("Virtual dispatch nodes not yet supported for supergraph (file a bug)");                    		
        	}

        	ControlFlowGraph currentCFG = current.getCfg();
            CallString currentCS = current.getCallString();
            Collection<CFGEdge> infeasibleEdges = infeasibleEdgeProvider.getInfeasibleEdges(currentCFG, currentCS); 

            for (CFGNode node : current.getCfg().vertexSet()) {
                if (node instanceof ControlFlowGraph.InvokeNode) {
                	/* skip node if all incoming edges are infeasible in the current call context */
                	boolean infeasible = true;
                	for(CFGEdge e : current.getCfg().incomingEdgesOf(node)) {
                		if(! infeasibleEdges.contains(e)) {
                			infeasible = false;
                		}
                	}
                	if(infeasible) continue;
                	
                    ControlFlowGraph.InvokeNode iNode = (ControlFlowGraph.InvokeNode) node;
                    Set<MethodInfo> impls = iNode.getImplementingMethods();
                    if(impls.size() == 0) {
                    	throw new AssertionError("No implementations for iNode available");
                    } else if(impls.size() != 1) {
                    	throw new AssertionError("Unresolved virtual Dispatch for " + iNode + ": " + impls);
                    }
                                        
                    for(MethodInfo impl : impls) {
                    	
                    	ControlFlowGraph invokedCFG = cfgProvider.getFlowGraph(impl);
                    	CallString invokedCS = currentCS.push(iNode, callstringLength);
                    	
                        /* skip node if receiver is infeasible in current call context */
                        if(infeasibleEdgeProvider.isInfeasibleReceiver(impl, invokedCS)) {
                        	Logger.getLogger(this.getClass()).info("createSuperGraph(): infeasible receiver "+impl);
                        	continue;
                        }

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
    	
        SuperInvokeEdge iEdge = new SuperInvokeEdge(invokeNode, invoker, invoked);
        superGraph.addEdge(invoker, invoked, iEdge);
        CFGEdge returnEdge;
        Set<CFGEdge> outEdges = invoker.getCfg().outgoingEdgesOf(invokeNode);
        if (outEdges.size() != 1) {
            throw new AssertionError("SuperGraph: Outdegree of invoker node != 1 (Missing return node?)");
        } else {
        	returnEdge = outEdges.iterator().next();
        }
        CFGNode returnNode = invoker.getCfg().getEdgeTarget(returnEdge);
        if (invoker.getCfg().incomingEdgesOf(returnNode).size() != 1) {
            throw new AssertionError("SuperGraph: Indegree of return node != 1 (Missing return node?)");
        }
        
        SuperReturnEdge rEdge = new SuperReturnEdge(invokeNode, returnNode, invoker, invoked);
        superGraph.addEdge(invoked, invoker, rEdge);

        superEdgePairs.put(iEdge, rEdge);
    }

    public void exportDOT(File dotFile) throws IOException {
        FileWriter dotWriter = new FileWriter(dotFile);
        AdvancedDOTExporter.DOTNodeLabeller<ContextCFG> nodeLabeller =
                new AdvancedDOTExporter.DefaultNodeLabeller<ContextCFG>() {

                    @Override
                    public String getLabel(ContextCFG node) {
                    	StringBuilder sb = new StringBuilder();
                    	sb.append(node.getCfg().getMethodInfo().getFQMethodName()+"\n");
                    	int i = 1;
                    	for(InvokeSite is : node.getContext().getCallString()) {
                    		sb.append(" #"+i+" "+is.getInvoker().getFQMethodName()+" / "+is.getInstructionHandle().getPosition()+"\n");
                    		i+=1;
                    	}
                        return sb.toString();
                    }

                };

        DOTLabeller<SuperEdge> edgeLabeller =
        	new AdvancedDOTExporter.DefaultDOTLabeller<SuperEdge>() {

				@Override
				public String getLabel(SuperEdge edge) {
					return "";
				}

				@Override
				public boolean setAttributes(SuperEdge edge,
						Map<String, String> ht) {
		            super.setAttributes(edge, ht);
		            if (edge instanceof SuperReturnEdge) { 
		            	ht.put("style", "dotted");
		            	ht.put("arrowhead", "empty");
		            }
		            return true;
				}
        	
        };
		AdvancedDOTExporter<ContextCFG, SuperEdge> de = new AdvancedDOTExporter<ContextCFG, SuperEdge>(
                nodeLabeller, edgeLabeller );
        de.exportDOT(dotWriter, this.superGraph);
        dotWriter.close();
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
