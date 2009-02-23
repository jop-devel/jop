package com.jopdesign.wcet.frontend;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.DirectedMultigraph;

import com.jopdesign.build.MethodInfo;
import com.jopdesign.wcet.frontend.ControlFlowGraph.CFGEdge;
import com.jopdesign.wcet.frontend.ControlFlowGraph.CFGNode;
import com.jopdesign.wcet.frontend.ControlFlowGraph.EdgeKind;
import com.jopdesign.wcet.frontend.ControlFlowGraph.InvokeNode;
import com.jopdesign.wcet.graphutils.AdvancedDOTExporter;
import com.jopdesign.wcet.graphutils.FlowGraph;
import com.jopdesign.wcet.graphutils.UnmodifiableDirectedGraphAdapter;
import com.jopdesign.wcet.graphutils.AdvancedDOTExporter.DOTNodeLabeller;

/**
 * A supergraph is similar to a call graph, but models the actual edges
 * connecting the control flow graphs.
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 *
 */
public class SuperGraph extends UnmodifiableDirectedGraphAdapter<CFGNode, CFGEdge> {
	public abstract static class SuperEdge extends CFGEdge {
		private static final long serialVersionUID = 1L;
		public SuperEdge(EdgeKind kind) {
			super(kind);
		}
	}
	/** Edge representing a method invocation */
	public static class SuperInvokeEdge extends SuperEdge {
		private static final long serialVersionUID = 1L;
		InvokeNode invoker;
		public SuperInvokeEdge(InvokeNode node) {
			super(EdgeKind.INVOKE_EDGE);
			this.invoker = node;
		}
		public InvokeNode getInvokeNode() {
			return invoker;
		}
	}
	/** Edge representing return to the invoking method */
	public static class SuperReturnEdge extends SuperEdge {
		private static final long serialVersionUID = 1L;
		CFGNode returnNode;
		public SuperReturnEdge(CFGNode returnNode) {
			super(EdgeKind.RETURN_EDGE);
			this.returnNode = returnNode;
		}
		public CFGNode getReturnNode() {
			return returnNode;
		}
	}

	private ControlFlowGraph rootCfg;
	private Vector<ControlFlowGraph> controlFlowGraphs;
	private DirectedMultigraph<ControlFlowGraph, SuperEdge> superGraph;
	private WcetAppInfo ai;
	
	private Set<CFGNode> allNodes;
	private Set<CFGEdge> allEdges;
	private Map<CFGNode, ControlFlowGraph> graphOfNode;
	private Map<CFGEdge,ControlFlowGraph> graphOfEdge;
	private Map<CFGNode, Set<CFGEdge>> specialInSet;
	private Map<CFGNode, Set<CFGEdge>> specialOutSet;
	private Map<SuperInvokeEdge, SuperReturnEdge> superEdgePairs;

	public SuperGraph(WcetAppInfo appInfo, ControlFlowGraph top) {
		this.ai = appInfo;
		this.rootCfg = top;
		this.controlFlowGraphs = new Vector<ControlFlowGraph>();
		this.superGraph = new DirectedMultigraph<ControlFlowGraph,SuperEdge>(SuperEdge.class);
		this.superEdgePairs = new HashMap<SuperInvokeEdge, SuperReturnEdge>();
		createSuperGraph();
	}
	public Set<ControlFlowGraph> getControlFlowGraphs() {
		return superGraph.vertexSet();
	}
	public Map<SuperInvokeEdge,SuperReturnEdge> getSuperEdgePairs() {
		return superEdgePairs;
	}

	private void createSuperGraph() {
		allNodes = new HashSet<CFGNode>();
		allEdges = new HashSet<CFGEdge>();
		graphOfNode = new HashMap<CFGNode, ControlFlowGraph>();
		graphOfEdge = new HashMap<CFGEdge, ControlFlowGraph>();
		specialInSet = new HashMap<CFGNode, Set<CFGEdge>>();
		specialOutSet = new HashMap<CFGNode, Set<CFGEdge>>();
		Stack<ControlFlowGraph> todo = 
			new Stack<ControlFlowGraph>();
		todo.push(rootCfg);
		superGraph.addVertex(rootCfg);
		while(! todo.empty()) {
			ControlFlowGraph current = todo.pop();
			this.controlFlowGraphs.add(current);
			for(CFGNode node : current.getGraph().vertexSet()) {
				allNodes.add(node);
				graphOfNode.put(node,current);
				if(node instanceof InvokeNode) {
					InvokeNode iNode = (InvokeNode) node;
					MethodInfo impl = iNode.getImplementedMethod();
					ControlFlowGraph invokedCFG = ai.getFlowGraph(impl);
					if(! superGraph.containsVertex(invokedCFG)) {
						superGraph.addVertex(invokedCFG);
						todo.push(invokedCFG);						
					}
					addEdge(iNode, invokedCFG);
				} else {
					for(CFGEdge e : current.getGraph().outgoingEdgesOf(node)) {
						allEdges.add(e);
						graphOfEdge.put(e,current);
					}
				}
			}
		}		
	}

	private void addEdge(InvokeNode node, ControlFlowGraph invoked) {
		SuperInvokeEdge iEdge = new SuperInvokeEdge(node);
		superGraph.addEdge(node.invokerFlowGraph(), invoked, iEdge);
		allEdges.add(iEdge); 
		addToSet(specialInSet,invoked.getEntry(), iEdge);
		addToSet(specialOutSet, node, iEdge);

		FlowGraph<CFGNode, CFGEdge> invoker = node.invokerFlowGraph().getGraph();
		if(invoker.outDegreeOf(node) != 1) {
			throw new AssertionError("SuperGraph: Outdegree of invoker node > 1.");
		}
		CFGNode returnNode = invoker.getEdgeTarget(invoker.outgoingEdgesOf(node).iterator().next());
		if(invoker.inDegreeOf(returnNode) != 1) {
			throw new AssertionError("SuperGraph: Indegree of return node != 1. Maybe return node missing ?");
		}
		SuperReturnEdge rEdge = new SuperReturnEdge(returnNode);
		superGraph.addEdge(invoked, node.invokerFlowGraph(), rEdge);
		allEdges.add(rEdge);			
		addToSet(specialOutSet,invoked.getExit(), rEdge);
		addToSet(specialInSet, returnNode, rEdge);
		superEdgePairs.put(iEdge, rEdge);
	}
	private static<K,V> void addToSet(Map<K,Set<V>> map, K key, V val) {
		Set<V> set = map.get(key);
		if(set == null) {
			set = new HashSet<V>();
			map.put(key, set);
		}
		set.add(val);
	}
	private Set<CFGEdge> getInSet(CFGNode n) {
		if(specialInSet.containsKey(n)) {
			return specialInSet.get(n);
		} else {
			return graphOfNode.get(n).getGraph().incomingEdgesOf(n);
		}
	}
	private Set<CFGEdge> getOutSet(CFGNode n) {
		if(specialOutSet.containsKey(n)) {
			return specialOutSet.get(n);
		} else {
			return graphOfNode.get(n).getGraph().outgoingEdgesOf(n);
		}		
	}
	public void exportDOT(FileWriter dotWriter) throws IOException {
		DOTNodeLabeller<CFGNode> nodeLabeller =
			new AdvancedDOTExporter.DefaultNodeLabeller<CFGNode>() {

				@Override
				public String getLabel(CFGNode node) {
					if(node.getBasicBlock() != null) {
						String s;
						if(node instanceof ControlFlowGraph.InvokeNode) {
							s = "INVOKE ";
						} else {
							s = "BB ";
						}
						return s+node.getId()+" "+node.getBasicBlock().getLastInstruction().getInstruction().getName();
					}
					return super.getLabel(node);
				}
			
		};
		AdvancedDOTExporter<CFGNode, CFGEdge> de = new AdvancedDOTExporter<CFGNode, CFGEdge>(
				nodeLabeller,null);
		de.exportDOT(dotWriter, this);
	}
	public ControlFlowGraph getTopCFG() {
		return rootCfg;
	}
	public CFGNode getTopEntry() {
		return this.rootCfg.getEntry();
	}
	public CFGNode getTopExit() {
		return this.rootCfg.getExit();
	}
	public Set<CFGNode> vertexSet() {
		return allNodes;
	}
	public boolean containsVertex(CFGNode v) {
		return allNodes.contains(v);
	}
	public Set<CFGEdge> edgeSet() {
		return allEdges;
	}
	public boolean containsEdge(CFGEdge e) {
		return allEdges.contains(e);
	}
	public int inDegreeOf(CFGNode n) {
		return getInSet(n).size();
	}
	public Set<CFGEdge> incomingEdgesOf(CFGNode n) {
		return getInSet(n);
	}
	public int outDegreeOf(CFGNode n) {
		return getOutSet(n).size();
	}
	public Set<CFGEdge> outgoingEdgesOf(CFGNode n) {
		return getOutSet(n);
	}
	public Set<CFGEdge> edgesOf(CFGNode n) {
		Set<CFGEdge> edgeSet = new HashSet<CFGEdge>(getInSet(n));
		edgeSet.addAll(getOutSet(n));
		return edgeSet;
	}
	public CFGNode getEdgeSource(CFGEdge edge) {
		if(edge instanceof SuperInvokeEdge) {
			return ((SuperInvokeEdge) edge).invoker;
		} else if(edge instanceof SuperReturnEdge) {
			return superGraph.getEdgeSource((SuperEdge) edge).getExit();
		} else {
			return graphOfEdge.get(edge).getGraph().getEdgeSource(edge);
		}
	}
	public CFGNode getEdgeTarget(CFGEdge edge) {
		if(edge instanceof SuperInvokeEdge) {
			return superGraph.getEdgeTarget((SuperEdge) edge).getEntry();
		} else if(edge instanceof SuperReturnEdge) {
				return ((SuperReturnEdge)edge).returnNode;
		} else {
			return graphOfEdge.get(edge).getGraph().getEdgeTarget(edge);
		}
	}


	public double getEdgeWeight(CFGEdge arg0) {
		throw new UnsupportedOperationException("superGraph: getEdgeWeight()");
	}
	public boolean containsEdge(CFGNode arg0, CFGNode arg1) {
		throw new UnsupportedOperationException("superGraph: containsEdge(CFGNode, CFGNode)");
	}
	public Set<CFGEdge> getAllEdges(CFGNode arg0, CFGNode arg1) {
		throw new UnsupportedOperationException("superGraph: allEdges(CFGNode, CFGNode)");
	}
	public CFGEdge getEdge(CFGNode arg0, CFGNode arg1) {
		throw new UnsupportedOperationException("superGraph: getEdge(CFGNode, CFGNode)");
	}
	public EdgeFactory<CFGNode, CFGEdge> getEdgeFactory() {
		throw new UnsupportedOperationException("superGraph: getEdgeFactory()");
	}
}
