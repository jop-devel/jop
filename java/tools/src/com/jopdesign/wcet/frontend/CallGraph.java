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

import java.io.FileWriter;
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
import com.jopdesign.dfa.framework.CallString;
import com.jopdesign.wcet.frontend.ControlFlowGraph.CFGNode;
import com.jopdesign.wcet.frontend.ControlFlowGraph.InvokeNode;
import com.jopdesign.wcet.frontend.WcetAppInfo.MethodNotFoundException;
import com.jopdesign.wcet.graphutils.AdvancedDOTExporter;
import com.jopdesign.wcet.graphutils.DirectedCycleDetector;
import com.jopdesign.wcet.graphutils.Pair;

import static com.jopdesign.wcet.graphutils.MiscUtils.addToList;

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
	 * An optional callstring is used to provide context sensitivity
	 */
	public static class CallGraphNode {
		private MethodInfo method;
		private CallString context;
		public CallGraphNode(MethodInfo m, CallString context) {
			this.method = m;
			this.context = context;
		}
		public MethodInfo getMethodImpl() { 
			return this.method; 
		}
		public MethodRef getReferencedMethod() {
			return new MethodRef(method.getCli(),method.methodId);
		}
		public CallString getCallString() {
			return this.context;
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			return prime * context.hashCode() + method.hashCode();
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)  return true;
			if (obj == null)  return false;
			if (getClass() != obj.getClass()) return false;

			CallGraphNode other = (CallGraphNode) obj;
			if(! method.equals(other.method)) return false;
			return context.equals(other.context);
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer("CallGraphNode(");
			sb.append(method.getFQMethodName());
			if(! context.isEmpty()) {
				sb.append(",");
				sb.append(context.toString());
			}
			sb.append(")");
			return sb.toString();
		}

	}
	// Fields
	// ~~~~~~
	private WcetAppInfo appInfo;
	private CallGraphNode rootNode;
	private DirectedGraph<CallGraphNode, DefaultEdge> callGraph;

	private HashSet<ClassInfo> classInfos;

	/** The first callgraph node acts as key, the second one is
	 *   the actual object in the callgraph
	 */
	private HashMap<CallGraphNode, CallGraphNode> nodeMap;
	private HashMap<MethodInfo, List<CallGraphNode>> methodMap;

	// Caching Fields
	// ~~~~~~~~~~~~~~
	private HashMap<CallGraphNode, Integer> maxDistanceToRoot = null;
	private HashMap<CallGraphNode, CallGraphNode> maxCallstackDAG = null;
	private HashMap<CallGraphNode, Integer> subgraphHeight = null;
	private CallGraphNode maxCallStackLeaf = null;
	private HashMap<MethodInfo,Boolean> leafNodeCache;
	private void invalidate() {
		maxCallStackLeaf = null;
		maxDistanceToRoot = null;
		maxCallstackDAG = null;
		subgraphHeight = null;
		leafNodeCache = new HashMap<MethodInfo, Boolean>();
	}

	/**
	 * Initialize a CallGraph object.
	 * @param appInfo    Uplink to the application info.
	 * @param rootMethod The root method of the callgraph (not abstract).
	 */
	protected CallGraph(WcetAppInfo appInfo, MethodInfo rootMethod) {
		this.appInfo = appInfo;
		this.callGraph = new DefaultDirectedGraph<CallGraphNode,DefaultEdge>(DefaultEdge.class);
		this.rootNode = new CallGraphNode(rootMethod,CallString.EMPTY);
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
	public static CallGraph buildCallGraph(WcetAppInfo appInfo,
										   String className,
										   String methodSig)
							throws MethodNotFoundException {
		MethodInfo rootMethod = appInfo.searchMethod(className,methodSig);
		CallGraph cg = new CallGraph(appInfo,rootMethod);
		cg.build();
		return cg;
	}
	/* building */
	private void build() throws MethodNotFoundException {
		this.buildGraph();
		
		/** Debug export */
		try {
			FileWriter fw = new FileWriter("/tmp/callgraph.dot");
			this.exportDOT(fw);
			fw.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		/* Compute set of classes */
		classInfos = new HashSet<ClassInfo>();
		for(MethodInfo mi : methodMap.keySet()) {
			classInfos.add(mi.getCli());
		}
		/* Check the callgraph is cycle free */
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
	
	/* Build the callgraph.
	 * NEW: now we also use callstrings to get a more precise call graph model
	 */
	private void buildGraph() {
		/* Get callstring length */
		int ctxDepth = appInfo.getCallstringLength();
		/* Initialize DFS data structures and lookup maps */
		nodeMap = new HashMap<CallGraphNode, CallGraphNode>();		
		nodeMap.put(rootNode, rootNode);
		Stack<CallGraphNode> todo = new Stack<CallGraphNode>();
		todo.push(rootNode);
		methodMap = new HashMap<MethodInfo, List<CallGraphNode>>();
		addToList(methodMap,rootNode.method,rootNode);

		while(! todo.empty()) {
			CallGraphNode current = todo.pop();
			CallString callstring = current.getCallString();
			ControlFlowGraph currentCFG = appInfo.getFlowGraph(current.getMethodImpl());

			for(CFGNode node : currentCFG.getGraph().vertexSet()) {
				if(node instanceof InvokeNode) {
					InvokeNode iNode = (InvokeNode) node;
					for(MethodInfo impl : iNode.getVirtualNode().getImplementedMethods(callstring)) {
						//System.out.println("Implemented Methods: "+impl+" from "+iNode.getBasicBlock().getMethodInfo().methodId+" in context "+callstring.toStringVerbose());
						CallString newCallString = current.getCallString().push(iNode, ctxDepth);
						CallGraphNode cgnLookup = new CallGraphNode(impl, newCallString);
						CallGraphNode cgn = nodeMap.get(cgnLookup);
						if(cgn == null) {
							cgn = cgnLookup;
							nodeMap.put(cgn,cgn);
							addToList(methodMap,impl,cgn);
							callGraph.addVertex(cgn);
							todo.push(cgn);
						}
						callGraph.addEdge(current, cgn);
					}
				}
			}
		}
	}
	
	/** Get node for a method info and call context */
	public CallGraphNode getNode(MethodInfo m, CallString cs) {
		return nodeMap.get(new CallGraphNode(m,cs));
	}
	
	/** Get all nodes matching the given method info */
	public List<CallGraphNode> getNodes(MethodInfo m) {
		if(this.methodMap.get(m) == null) {
			throw new AssertionError("No callgraph nodes for "+ m);
		}
		return this.methodMap.get(m);
	}
	
	/* calculate the depth of each node, the height of the subgraph
	 * rooted at that node, and a maximum call-stack tree.
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
	 * Get non-abstract methods, in topological order.
	 * 
	 * Requires an acyclic callgraph.
	 * @return
	 */
	public List<MethodInfo> getImplementedMethods(MethodInfo rootMethod) {
		List<MethodInfo> implemented = new Vector<MethodInfo>();
		HashSet<MethodInfo> reachable = new HashSet<MethodInfo>(getReachableImplementations(rootMethod));
		TopologicalOrderIterator<CallGraphNode, DefaultEdge> ti = topDownIterator();
		while(ti.hasNext()) {
			MethodInfo m = ti.next().getMethodImpl();
			if(m != null && reachable.contains(m)) implemented.add(m);
		}
		return implemented;
	}

	/** Return a top-down (topological) iterator for the callgraph 
	 * @return A topological order iterator
	 */
	public TopologicalOrderIterator<CallGraphNode, DefaultEdge> topDownIterator() {
		return new TopologicalOrderIterator<CallGraphNode, DefaultEdge>(callGraph);
	}

	/**
	 * get non-abstract methods reachable from the given method, in DFS order
	 * @return
	 */
	public List<MethodInfo> getReachableImplementations(MethodInfo rootMethod) {
		List<MethodInfo> implemented = new Vector<MethodInfo>();
		CallGraphNode root = this.getNode(rootMethod, CallString.EMPTY);
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
		List<CallGraphNode> nodes = getNodes(m);
		Vector<CallGraphNode> succs = new Vector<CallGraphNode>();
		for(CallGraphNode node : nodes) {
			for(DefaultEdge e : callGraph.outgoingEdgesOf(node)) {
				succs.add(callGraph.getEdgeTarget(e));
			}
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

	public boolean isLeafMethod(MethodInfo mi) {
		/* Using caching, as this method is used quite often */
		Boolean isLeafNode = leafNodeCache.get(mi);
		if(isLeafNode != null) return isLeafNode;
		isLeafNode = true;
		for(CallGraphNode node : getNodes(mi)) {
			if(! isLeafNode(node)) {
				isLeafNode = false;
				break;
			}			
		}
		leafNodeCache.put(mi,isLeafNode);
		return isLeafNode;
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
