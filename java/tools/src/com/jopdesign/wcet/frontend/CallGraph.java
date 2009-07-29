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

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgrapht.traverse.TopologicalOrderIterator;

import com.jopdesign.build.ClassInfo;
import com.jopdesign.build.MethodInfo;
import com.jopdesign.wcet.frontend.ControlFlowGraph.CFGNode;
import com.jopdesign.wcet.frontend.ControlFlowGraph.InvokeNode;
import com.jopdesign.wcet.frontend.WcetAppInfo.MethodNotFoundException;
import com.jopdesign.wcet.graphutils.AdvancedDOTExporter;
import com.jopdesign.wcet.graphutils.DirectedCycleDetector;
import com.jopdesign.wcet.graphutils.Pair;

/**
 * <p>Java call graph, whose nodes represent control flow graphs and 
 *     dynamic dispatches.</p>
 * <p>If some instruction in the flow graph represented by {@code MethodImplNode m1}
 * possibly invokes a {@code MethodImplNode m2},there is an edge from {@code m1}
 * to {@code m2}.</p>
 * @author Benedikt Huber (benedikt.huber@gmail.com)
 *
 */
public class CallGraph {
	/** 
	 * Call graph nodes referencing methods. <br/>
	 */
	public class CallGraphNode {
		private MethodInfo method;
		public CallGraphNode(MethodInfo m) { 
			this.method = m; 
		}
		public MethodInfo getMethodImpl() { return this.method; }		
		public MethodRef getReferencedMethod() { 
			return new MethodRef(method.getCli(),method.methodId);
		}
		
		public int hashCode() { return method.hashCode(); }
		public boolean equals(Object that) {
			return (that instanceof CallGraphNode) ? 
				   (method.equals(((CallGraphNode) that).method)) : 
				   false;
		}
		public String toString() {
			return method.getFQMethodName();
		}
	}
	// Fields
	// ~~~~~~
	private WcetAppInfo appInfo;
	private CallGraphNode rootNode;
	private DirectedGraph<CallGraphNode, DefaultEdge> callGraph;

	private HashSet<ClassInfo> classInfos;
	private HashMap<MethodInfo,CallGraphNode> nodeMap;

	// Caching Fields
	// ~~~~~~~~~~~~~~
	private HashMap<CallGraphNode, Integer> maxDistanceToRoot = null;
	private HashMap<CallGraphNode, CallGraphNode> maxCallstackDAG = null;
	private HashMap<CallGraphNode, Integer> subgraphHeight = null;
	private CallGraphNode maxCallStackLeaf = null;
	private void invalidate() {
		maxCallStackLeaf = null;
		maxDistanceToRoot = null;
		maxCallstackDAG = null;
		subgraphHeight = null;
	}
	
	/**
	 * Initialize a CallGraph object.
	 * @param appInfo    Uplink to the application info.
	 * @param rootMethod The root method of the callgraph (not abstract).
	 */
	protected CallGraph(WcetAppInfo appInfo, MethodInfo rootMethod) {
		this.appInfo = appInfo;
		this.callGraph = new DefaultDirectedGraph<CallGraphNode,DefaultEdge>(DefaultEdge.class);
		this.rootNode = new CallGraphNode(rootMethod);
		this.callGraph.addVertex(rootNode);
	}

	/**
	 * Build a callgraph rooted at the given method
	 * @param appInfo   The application (with classes loaded)
	 * @param className The class where the root method of the callgraph is located
	 * @param methodSig The root method of the call graph. Either a plain method name
	 *                   (e.g. "measure"), if unique, or a method with signature (e.g. "measure()Z")
	 * @throws MethodNotFoundException 
	 */
	public static CallGraph buildCallGraph(WcetAppInfo appInfo, String className, String methodSig) 
							throws MethodNotFoundException {
		MethodInfo rootMethod = appInfo.searchMethod(className,methodSig);
		CallGraph cg = new CallGraph(appInfo,rootMethod);
		cg.build();
		return cg;
	}
	/* building */
	private void build() throws MethodNotFoundException {
		this.buildGraph();		
		/* Compute set of classes and methods */
		classInfos = new HashSet<ClassInfo>();
		for(CallGraphNode cgn : callGraph.vertexSet()) {
			classInfos.add(cgn.getReferencedMethod().getReceiver());
		}
		Pair<List<CallGraphNode>,List<CallGraphNode>> cycle = 
			DirectedCycleDetector.findCycle(callGraph,rootNode);
		if(cycle != null) {
			for(DefaultEdge e : callGraph.edgeSet()) {
				CallGraphNode src = callGraph.getEdgeSource(e);
				CallGraphNode target = callGraph.getEdgeTarget(e);
				System.err.println(""+src+" --> "+target);
			}
			throw new AssertionError(cyclicCallGraphMsg(cycle));
		}
		invalidate();
	}	
	private static String cyclicCallGraphMsg(Pair<List<CallGraphNode>, List<CallGraphNode>> cycleWithPrefix) {
		List<CallGraphNode> cycle = cycleWithPrefix.snd();
		List<CallGraphNode> prefix = cycleWithPrefix.fst();
		StringBuffer sb = new StringBuffer();
		sb.append("Cyclic Callgraph !\n");
		sb.append("One cycle is:\n");
		for(CallGraphNode cn : cycle) sb.append("  "+cn+"\n");
		sb.append("Reachable via:\n");
		for(CallGraphNode cn : prefix) sb.append("  "+cn+"\n");
		return sb.toString();
	}
	private void buildGraph() {
		nodeMap = new HashMap<MethodInfo, CallGraphNode>();
		Stack<CallGraphNode> todo = 
			new Stack<CallGraphNode>();
		nodeMap.put(this.rootNode.getMethodImpl(), rootNode);
		todo.push(rootNode);
		while(! todo.empty()) {
			CallGraphNode current = todo.pop();
			ControlFlowGraph currentCFG = appInfo.getFlowGraph(current.getMethodImpl());
			for(CFGNode node : currentCFG.getGraph().vertexSet()) {
				if(node instanceof InvokeNode) {
					InvokeNode iNode = (InvokeNode) node;
					for(MethodInfo impl : iNode.getImplementedMethods()) {
						CallGraphNode cgn;
						if(! nodeMap.containsKey(impl)) {
							cgn = new CallGraphNode(impl);
							nodeMap.put(impl,cgn);
							callGraph.addVertex(cgn);
							todo.push(cgn);
						} else {
							cgn = nodeMap.get(impl);
						}
						callGraph.addEdge(current, cgn);
					}
				}
			}				
		}
	}		
	private CallGraphNode getNode(MethodInfo m) {
		return nodeMap.get(m);
	}
	/* calculate the depth of each node, the height of the subgraph
	 * rooted at that node, and a longest path map.
	 */
	private void calculateDepthAndHeight() {
		if(this.maxDistanceToRoot != null) return; // caching
		this.maxDistanceToRoot = new HashMap<CallGraphNode,Integer>();
		this.maxCallStackLeaf = this.getRootNode();
		this.maxCallstackDAG  = new HashMap<CallGraphNode,CallGraphNode>();
		this.subgraphHeight = new HashMap<CallGraphNode, Integer>();
		/* calculate longest distance to root and max call stack DAG */
		Vector<CallGraphNode> toList = new Vector<CallGraphNode>();
		TopologicalOrderIterator<CallGraphNode, DefaultEdge> toIter =
			new TopologicalOrderIterator<CallGraphNode, DefaultEdge>(callGraph);
		int globalMaxDist = 0;
		while(toIter.hasNext()) {
			CallGraphNode node = toIter.next();
			toList.add(node);
			int maxDist = 0;
			CallGraphNode maxCallStackPred = null;
			for(DefaultEdge e : callGraph.incomingEdgesOf(node)) {
				CallGraphNode pred = callGraph.getEdgeSource(e);
				int distViaPred = maxDistanceToRoot.get(pred) + 1;
				if(distViaPred > maxDist) {
					maxDist = distViaPred;
					maxCallStackPred = pred;
				}
			}
			this.maxDistanceToRoot.put(node,maxDist);
			if(maxCallStackPred != null) this.maxCallstackDAG.put(node,maxCallStackPred);
			if(maxDist > globalMaxDist) this.maxCallStackLeaf = node;
		}
		/* calculate subgraph height */
		Collections.reverse(toList);
		for(CallGraphNode n : toList) {
			int maxHeight = 0;
			for(DefaultEdge e : callGraph.outgoingEdgesOf(n)) {
				int predHeight = subgraphHeight.get(callGraph.getEdgeTarget(e));
				maxHeight = Math.max(maxHeight, predHeight + 1);
			}
			subgraphHeight.put(n, maxHeight);
		}
	}

	/**
	 * Export callgraph as .dot file
	 * @param w
	 * @throws IOException
	 */
	public void exportDOT(Writer w) throws IOException {
		new AdvancedDOTExporter<CallGraphNode, DefaultEdge>().exportDOT(w, this.callGraph);
	}
	
	public ClassInfo getRootClass() {
		return rootNode.method.getCli();
	}
	
	public MethodInfo getRootMethod() {
		return rootNode.method;
	}
	
	public CallGraphNode getRootNode() {
		return rootNode;
	}

	public Set<ClassInfo> getClassInfos() {
		return classInfos;
	}
	
	/**
	 * get non-abstract methods, in topological order
	 * requires an acyclic callgraph.
	 * @return
	 */
	public List<MethodInfo> getImplementedMethods(MethodInfo rootMethod) {
		List<MethodInfo> implemented = new Vector<MethodInfo>();
		HashSet<MethodInfo> reachable = new HashSet<MethodInfo>(getReachableImplementations(rootMethod));
		TopologicalOrderIterator<CallGraphNode, DefaultEdge> ti = 
			new TopologicalOrderIterator<CallGraphNode, DefaultEdge>(callGraph);
		while(ti.hasNext()) {
			MethodInfo m = ti.next().getMethodImpl();
			if(m != null && reachable.contains(m)) implemented.add(m);
		}		
		return implemented;
	}
	
	/**
	 * get non-abstract methods reachable from the given method, in DFS order
	 * @return
	 */
	public List<MethodInfo> getReachableImplementations(MethodInfo rootMethod) {
		List<MethodInfo> implemented = new Vector<MethodInfo>();
		CallGraphNode root = this.getNode(rootMethod);
		DepthFirstIterator<CallGraphNode, DefaultEdge> ti =
			new DepthFirstIterator<CallGraphNode, DefaultEdge>(callGraph,root);
		ti.setCrossComponentTraversal(false);
		while(ti.hasNext()) {
			MethodInfo m = ti.next().getMethodImpl();
			if(m == null) throw new AssertionError("Abstract method in callgraph");
			implemented.add(m);
		}		
		return implemented;
	}
		
	/** Get methods possibly directly invoked from the given method */
	public List<CallGraphNode> getReferencedMethods(MethodInfo m) {
		CallGraphNode node = getNode(m);
		Vector<CallGraphNode> succs = new Vector<CallGraphNode>();
		for(DefaultEdge e : callGraph.outgoingEdgesOf(node)) {
			succs.add(callGraph.getEdgeTarget(e));
		}
		return succs;
	}
	/**
	 * Return true when the given method does not invoke any other methods
	 * @param node
	 * @return
	 */
	public boolean isLeafNode(CallGraphNode node) {
		return callGraph.outDegreeOf(node) == 0;
	}
	
	public boolean isLeafNode(MethodInfo mi) {
		return isLeafNode(getNode(mi));
	}

	public int getMaxNodeDepth(MethodInfo mi) {
		calculateDepthAndHeight();
		return this.maxDistanceToRoot.get(getNode(mi));
	}

	/**
	 * Get the maximum height of the call stack.
	 * <p>A leaf method has height 1, an abstract method's height is the
	 * maximum height of its children, and the height of an implemented method
	 * is the maximum height of its children + 1. <p>
	 * @return
	 */
	public Vector<CallGraphNode> getMaximalCallStack() {
		if(maxCallStackLeaf == null) calculateDepthAndHeight();
		CallGraphNode n = this.maxCallStackLeaf;
		Vector<CallGraphNode> maxCallStack = new Vector<CallGraphNode>();
		maxCallStack.add(n);
		while(maxCallstackDAG.containsKey(n)) {
			n = maxCallstackDAG.get(n);
			maxCallStack.add(n);			
		}
		Collections.reverse(maxCallStack);
		return maxCallStack;
	}

	 public int getMaxHeight() {
		calculateDepthAndHeight();
		return this.subgraphHeight.get(this.rootNode);
	}

	 public ControlFlowGraph getLargestMethod() {
		ControlFlowGraph largest = null;
		int maxBytes = 0;
		for(MethodInfo mi : this.getImplementedMethods(this.rootNode.method)) {
			ControlFlowGraph cfg = appInfo.getFlowGraph(mi);
			int bytes = cfg.getNumberOfBytes();
			if(bytes > maxBytes) {
				largest = cfg;
				maxBytes = bytes;
			}
		}
		return largest;
	}
	public int getTotalSizeInBytes() {
		int bytes = 0;
		for (MethodInfo mi : this.getImplementedMethods(this.rootNode.method)) {
			 bytes += appInfo.getFlowGraph(mi).getNumberOfBytes();
		}
		return bytes;
	}
	
}
