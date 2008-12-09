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

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.bcel.generic.ANEWARRAY;
import org.apache.bcel.generic.ATHROW;
import org.apache.bcel.generic.EmptyVisitor;
import org.apache.bcel.generic.INVOKEINTERFACE;
import org.apache.bcel.generic.INVOKESPECIAL;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.NEW;
import org.apache.bcel.generic.NEWARRAY;
import org.apache.bcel.generic.Visitor;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionList;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgrapht.traverse.TopologicalOrderIterator;

import com.jopdesign.build.ClassInfo;
import com.jopdesign.build.MethodInfo;
import com.jopdesign.wcet.WCETInstruction;
import com.jopdesign.wcet08.frontend.WcetAppInfo.MethodNotFoundException;
import com.jopdesign.wcet08.graphutils.AdvancedDOTExporter;
import com.jopdesign.wcet08.graphutils.DirectedCycleDetector;
import com.jopdesign.wcet08.graphutils.Pair;
import com.jopdesign.wcet08.graphutils.TopOrder;

/**
 * Java CallGraph, based on JGraphT. Supports interfaces.
 * 
 * @author Benedikt Huber (benedikt.huber@gmail.com)
 *
 */
public class CallGraph {
	public class CallGraphBuilderVisitor extends EmptyVisitor implements Visitor {
		private MethodImplNode methodNode;
		private Set<MethodRef> referencedMethods;
		public CallGraphBuilderVisitor(MethodImplNode node) {
			this.methodNode = node;
			this.referencedMethods = new HashSet<MethodRef>();
		}
		public void visitInstruction(Instruction ii) {
			/* FIXME: [NO THROW HACK] */
			if(WCETInstruction.isInJava(ii.getOpcode())) {				
				if(ii instanceof ATHROW || ii instanceof NEW || 
				   ii instanceof NEWARRAY || ii instanceof ANEWARRAY) return;
				MethodInfo javaImpl = appInfo.getJavaImpl(methodNode.method.getCli(),ii);
				buildCallGraphEdge(MethodRef.fromMethodInfo(javaImpl), true);
			} else {
				ii.accept(this);
			}
		}
		@Override
		public void visitINVOKESPECIAL(INVOKESPECIAL obj) {
			// see http://eduunix.cn/index2/html/java/Oreilly%20-%20Java%20Virtual%20Machine/ref--33.html
			// Used to implement <init>, <super> and private instance methods
			// The type of the receiver is statically known
			buildCallGraphEdge(obj,true);
		}

		@Override
		public void visitINVOKEINTERFACE(INVOKEINTERFACE obj) {
			buildCallGraphEdge(obj,false);
		}

		@Override
		public void visitINVOKEVIRTUAL(INVOKEVIRTUAL obj) {
			buildCallGraphEdge(obj,false);
		}

		@Override
		public void visitINVOKESTATIC(INVOKESTATIC i) {
			if(appInfo.isSpecialInvoke(methodNode.method.getCli(), i)) return;
			buildCallGraphEdge(i,true);
		}
		
		private void buildCallGraphEdge(InvokeInstruction inv, boolean isStatic) {
			MethodRef methodRef = appInfo.getReferenced(this.methodNode.method, inv);	
			buildCallGraphEdge(methodRef,isStatic);
		}
		private void buildCallGraphEdge(MethodRef methodRef, boolean isStatic) {
			if(this.referencedMethods.contains(methodRef)) return;
			if(isStatic) {
				MethodInfo refdMethod = appInfo.findStaticImplementation(methodRef);
				if(refdMethod == null) {
					throw new AssertionError("Could not find referenced STATIC method: "+methodRef);
				}				
				addEdge(this.methodNode, 
						new MethodImplNode(refdMethod));
			} else {
				addEdge(this.methodNode, 
						new MethodIFaceNode(methodRef));				
			}
			this.referencedMethods.add(methodRef);
		}
	}

	/** 
	 * Call graph nodes referencing methods.
	 * <br/>
	 * Is important to override {@link equals()} and {@link hashCode()} !! 
	 */
	public abstract class CallGraphNode {
		/**
		 * query whether the node is abstract (interface node)
		 * @return true if the node refers to a method interface rather than an implementation
		 */
		public abstract boolean isAbstractNode();
		/**
		 * return the method referenced by the callgraph node
		 * @return a pair of the receiver's class and the method's id (name+signature)
		 */
		public abstract MethodRef getReferencedMethod();
		/**
		 * @return the implementation referenced, or null if no implementation is referenced
		 */
		public abstract MethodInfo getMethodImpl();
		/**
		 * build the subgraph rooted at the given callgraph node.
		 * Only needs to be called once for each node.
		 */
		public abstract void build();

		protected void buildRecursive() {
			for(DefaultEdge e : callGraph.outgoingEdgesOf(this)) {
				CallGraphNode target = callGraph.getEdgeTarget(e);
				if(hasBeenBuild(target)) continue;
				callGraph.getEdgeTarget(e).build();
			}
		}
	}

	class MethodImplNode extends CallGraphNode {
		private MethodInfo method;
		public MethodImplNode(MethodInfo m) { 
			this.method = m; 
		}
		@Override public MethodInfo getMethodImpl() { return this.method; }		
		@Override public MethodRef getReferencedMethod() { 
			return new MethodRef(method.getCli(),method.methodId);
		}
		
		@Override public boolean isAbstractNode() { return false; }
		@Override public int hashCode() { return method.getMethod().hashCode(); }
		@Override public boolean equals(Object that) {
			return (that instanceof MethodImplNode) ? 
				   (method.getMethod().equals(((MethodImplNode) that).method.getMethod())) : 
				   false;
		}
		@Override public void build() {
			markBuild(this);
			if(this.method.getMethodGen() == null) return; // no impl available
			InstructionList il = this.method.getMethodGen().getInstructionList();
			CallGraphBuilderVisitor cgBuilderVisitor = new CallGraphBuilderVisitor(this); 
			for(Instruction i : il.getInstructions()) {
				cgBuilderVisitor.visitInstruction(i);
			}
			super.buildRecursive();
		}
		@Override public String toString() {
			return method.getFQMethodName();
		}
	}
	class MethodIFaceNode extends CallGraphNode {
		private MethodRef methodRef;
		public MethodIFaceNode(MethodRef methodRef) {
			this.methodRef = methodRef;
		}
		@Override public boolean isAbstractNode() { return true; }
		@Override public int hashCode() { 
			return methodRef.hashCode();
		}
		@Override public boolean equals(Object that) {
			return (that instanceof MethodIFaceNode) ?
				   (methodRef.equals(((MethodIFaceNode)that).methodRef)) :
				   false;
		}
		@Override public void build() {
			markBuild(this);
			for(MethodInfo mImpl : appInfo.findImplementations(methodRef)) {	
				addEdge(this, new MethodImplNode(mImpl));
			}
			super.buildRecursive();
		}
		@Override public String toString() {
			return "[IFACE] "+ methodRef.toString();
		}
		@Override
		public MethodRef getReferencedMethod() {
			return this.methodRef;
		}
		@Override public MethodInfo getMethodImpl() { return null; }
	}
	
	// Fields
	// ~~~~~~
	private WcetAppInfo appInfo;
	private MethodImplNode rootNode;
	private DirectedGraph<CallGraphNode, DefaultEdge> callGraph;

	private HashSet<ClassInfo> classInfos;
	private HashMap<MethodRef,CallGraphNode> methodInfos;

	private TopOrder<CallGraphNode,DefaultEdge> topOrder;

	/* building */
	private Vector<MethodNotFoundException> errors;
	private HashSet<CallGraphNode> buildSet;
	private boolean hasBeenBuild(CallGraphNode n) {
		return this.buildSet.contains(n);
	}
	private void markBuild(CallGraphNode n) {
		this.buildSet.add(n);
	}
	/**
	 * Initialize a CallGraph object.
	 */
	protected CallGraph(WcetAppInfo appInfo, MethodInfo rootMethod) {
		this.appInfo = appInfo;
		this.callGraph = new DefaultDirectedGraph<CallGraphNode,DefaultEdge>(DefaultEdge.class);
		this.rootNode = new MethodImplNode(rootMethod);
		this.callGraph.addVertex(rootNode);
	}

	/**
	 * Build a callgraph rooted at the given method
	 * @param cli The class loader (with classes loaded)
	 * @param className The class where the root method of the callgraph is located
	 * @param methodSig The root method of the call graph. Either a plain method name
	 * (e.g. "measure"), if unique, or a method with signature (e.g. "measure()Z")
	 * @throws MethodNotFoundException 
	 */
	public static CallGraph buildCallGraph(WcetAppInfo cli, String className, String methodSig) 
							throws MethodNotFoundException {
		MethodInfo rootMethod = cli.searchMethod(className,methodSig);
		CallGraph cg = new CallGraph(cli,rootMethod);
		cg.build();
		return cg;
	}
	private void build() throws MethodNotFoundException {
		this.errors = new Vector<MethodNotFoundException>();
		this.buildSet = new HashSet<CallGraphNode>();
		this.rootNode.build();
		if(! errors.isEmpty()) throw errors.get(0);
		/* Compute set of classes and methods */
		classInfos = new HashSet<ClassInfo>();
		for(CallGraphNode cgn : callGraph.vertexSet()) {
			classInfos.add(cgn.getReferencedMethod().getReceiver());
		}
		methodInfos = new HashMap<MethodRef,CallGraphNode>();
		for(CallGraphNode cgn : callGraph.vertexSet()) {
			methodInfos.put(cgn.getReferencedMethod(),cgn);
		}	
		Pair<List<CallGraphNode>,List<CallGraphNode>> cycle = 
			DirectedCycleDetector.findCycle(callGraph,rootNode);
		if(cycle != null) {
			throw new AssertionError("Cyclic callgraph !. One cycle is *** "+cycle.snd()+
									 " *** reachable via "+cycle.fst());
		}
	}

	public void exportDOT(Writer w) throws IOException {
		new AdvancedDOTExporter<CallGraphNode, DefaultEdge>().exportDOT(w, this.callGraph);
	}

	public ClassInfo getRootClass() {
		return rootNode.method.getCli();
	}
	public MethodInfo getRootMethod() {
		return rootNode.method;
	}

	public Set<ClassInfo> getClassInfos() {
		return classInfos;
	}
	public Set<MethodRef> getMethods() {
		return methodInfos.keySet();
	}
	/**
	 * get non-abstract methods, in topological orer
	 * @return
	 */
	public List<MethodInfo> getImplementedMethods() {
		List<MethodInfo> implemented = new Vector<MethodInfo>();
		TopologicalOrderIterator<CallGraphNode, DefaultEdge> ti = 
			new TopologicalOrderIterator<CallGraphNode, DefaultEdge>(callGraph);
		while(ti.hasNext()) {
			MethodInfo m = ti.next().getMethodImpl();
			if(m != null) implemented.add(m);
		}		
		return implemented;
	}
	public Iterator<CallGraphNode> getReachableMethods(MethodRef m) {
		DepthFirstIterator<CallGraphNode, DefaultEdge> dfi = 
			new DepthFirstIterator<CallGraphNode, DefaultEdge>(callGraph,getNode(m));
		dfi.setCrossComponentTraversal(false);
		return dfi;		
	}
	public Iterator<CallGraphNode> getReachableMethods(MethodInfo m) {
		return getReachableMethods(MethodRef.fromMethodInfo(m));
	}
	public Iterator<CallGraphNode> getReferencedMethods(MethodInfo m) {
		Vector<CallGraphNode> refd = new Vector<CallGraphNode>();
		CallGraphNode node = getNode(MethodRef.fromMethodInfo(m));
		Vector<DefaultEdge> edgeSet = new Vector<DefaultEdge>(callGraph.outgoingEdgesOf(node));
		while(! edgeSet.isEmpty()) {
			Vector<DefaultEdge> edgeSet2 = new Vector<DefaultEdge>();
			for(DefaultEdge e : edgeSet) {
				CallGraphNode target = callGraph.getEdgeTarget(e);
				if(target.isAbstractNode()) {
					edgeSet2.addAll(callGraph.outgoingEdgesOf(target));
				} else {
					refd.add(target);
				}
			}
			edgeSet = edgeSet2;
		}
		return refd.iterator();
	}
	

	protected void addEdge(CallGraphNode src, CallGraphNode target) {
		callGraph.addVertex(target);
		callGraph.addEdge(src, target);
	}
	protected CallGraphNode getNode(MethodRef m) {
		return methodInfos.get(m);
	}
	public TopOrder<CallGraphNode,DefaultEdge> getTopologicalOrder() {
		return this.topOrder;
	}
	public boolean isLeafNode(CallGraphNode vertex) {
		return callGraph.outDegreeOf(vertex) == 0;
	}
	public boolean isLeafNode(MethodInfo mi) {
		return isLeafNode(getNode(MethodRef.fromMethodInfo(mi)));
	}

	/**
	 * Return if the given reference points to a leaf node,
	 * i.e., a method which doesn't invoke any other methods.
	 * @param ref
	 * @return
	 */
	public boolean isLeafNode(MethodRef ref) {
		return this.callGraph.outDegreeOf(getNode(ref)) == 0;
	}
	/**
	 * Get the maximum height of the call stack.
	 * <p>A leaf method has height 1, an abstract method's height is the
	 * maximum height of its children, and the height of an implemented method
	 * is the maximum height of its children + 1. <p>
	 * @return
	 */
	public Vector<CallGraphNode> getMaximalCallStack() {
		Map<CallGraphNode, Integer> depth = new HashMap<CallGraphNode,Integer>();
		HashMap<CallGraphNode, CallGraphNode> prev = new HashMap<CallGraphNode, CallGraphNode>();
		CallGraphNode deepestLeaf = rootNode;

		TopologicalOrderIterator<CallGraphNode, DefaultEdge> toIter =
			new TopologicalOrderIterator<CallGraphNode, DefaultEdge>(callGraph);
		depth.put(rootNode, 0);
		int maxDepth = 0;
		Set<CallGraphNode> visited = new HashSet<CallGraphNode>();
		while(toIter.hasNext()) {
			CallGraphNode n = toIter.next();
			visited.add(n);
			int thisDepth = depth.get(n);
			for(DefaultEdge e :callGraph.outgoingEdgesOf(n)) {
				CallGraphNode target = callGraph.getEdgeTarget(e);
				if(visited.contains(target)) {
					throw new AssertionError("Bad implementation of topological order iterator in jgrapht ?");
				}
				int oldDepth;
				{
					Integer tmp = depth.get(target);
					oldDepth = tmp == null ? 0 : tmp.intValue();
				}
				if(thisDepth+1 > oldDepth) {
					depth.put(target,thisDepth+1);
					prev.put(target,n);
					if(thisDepth + 1 > maxDepth) {
						maxDepth = thisDepth + 1;
						deepestLeaf = target;
					}
				}
			}			
		}
		Vector<CallGraphNode> maxCallStack = new Vector<CallGraphNode>();
		CallGraphNode n = deepestLeaf;
		while(prev.containsKey(n)) {
			maxCallStack.add(n);
			n = prev.get(n);
		}
		maxCallStack.add(n);
		Collections.reverse(maxCallStack);
		return maxCallStack;
	}

	 public int getMaxHeight() {
		return this.getMaximalCallStack().size();
	}
	
}
