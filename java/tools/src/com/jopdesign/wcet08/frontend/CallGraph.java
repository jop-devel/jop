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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.bcel.generic.EmptyVisitor;
import org.apache.bcel.generic.INVOKEINTERFACE;
import org.apache.bcel.generic.INVOKESPECIAL;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.Visitor;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionList;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgrapht.traverse.TopologicalOrderIterator;

import com.jopdesign.build.ClassInfo;
import com.jopdesign.build.MethodInfo;
import com.jopdesign.wcet08.frontend.JOPAppInfo.MethodNotFoundException;
import com.jopdesign.wcet08.graphutils.AdvancedDOTExporter;
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
		private Set<Pair<ClassInfo,String>> referencedMethods;
		public CallGraphBuilderVisitor(MethodImplNode node) {
			this.methodNode = node;
			this.referencedMethods = new HashSet<Pair<ClassInfo,String>>();
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
			Pair<ClassInfo, String> methodRef = appInfo.getReferenced(this.methodNode.method, inv);			
			if(this.referencedMethods.contains(methodRef)) return;
			if(isStatic) {
				MethodInfo refdMethod = appInfo.findStaticImplementation(methodRef.fst(),methodRef.snd());
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
		public abstract Pair<ClassInfo,String> getReferencedMethod();
		/**
		 * @return the implementation referenced, or null if no implementation is referenced
		 */
		public abstract MethodInfo getMethodImpl();
		/**
		 * build the subgraph rooted at the given callgraph node 
		 */
		public abstract void build();

		protected void buildRecursive() {
			for(DefaultEdge e : callGraph.outgoingEdgesOf(this)) {
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
		@Override public Pair<ClassInfo,String> getReferencedMethod() { 
			return new Pair<ClassInfo, String>(method.getCli(),method.methodId);
		}
		
		@Override public boolean isAbstractNode() { return false; }
		@Override public int hashCode() { return method.getMethod().hashCode(); }
		@Override public boolean equals(Object that) {
			return (that instanceof MethodImplNode) ? 
				   (method.getMethod().equals(((MethodImplNode) that).method.getMethod())) : 
				   false;
		}
		@Override public void build() {
			if(this.method.getCode() == null) return; // no impl available
			InstructionList il = new InstructionList(this.method.getCode().getCode());
			CallGraphBuilderVisitor cgBuilderVisitor = new CallGraphBuilderVisitor(this); 
			for(Instruction i : il.getInstructions()) {
				i.accept(cgBuilderVisitor);
			}
			super.buildRecursive();
		}
		@Override public String toString() {
			return method.getFQMethodName();
		}
	}
	class MethodIFaceNode extends CallGraphNode {
		private ClassInfo receiver;
		private String method;
		public MethodIFaceNode(Pair<ClassInfo, String> methodRef) {
			this.receiver = methodRef.fst();
			this.method = methodRef.snd();
		}
		@Override public boolean isAbstractNode() { return true; }
		@Override public int hashCode() { 
			return receiver.hashCode() + method.hashCode();
		}
		@Override public boolean equals(Object that) {
			return (that instanceof MethodIFaceNode) ?
				   (receiver.equals(((MethodIFaceNode)that).receiver) && 
				    method.equals(((MethodIFaceNode)that).method)) :
				   false;
		}
		@Override public void build() {
			for(MethodInfo mImpl : appInfo.findImplementations(receiver,method)) {	
				addEdge(this, new MethodImplNode(mImpl));
			}
			super.buildRecursive();
		}
		@Override public String toString() {
			return "[IFACE] "+ receiver.clazz.getClassName() + "." + method;
		}
		@Override
		public Pair<ClassInfo, String> getReferencedMethod() {
			return new Pair<ClassInfo, String>(this.receiver,this.method);
		}
		@Override public MethodInfo getMethodImpl() { return null; }
	}
	
	// Fields
	// ~~~~~~
	private JOPAppInfo appInfo;
	private MethodImplNode rootNode;
	private DirectedGraph<CallGraphNode, DefaultEdge> callGraph;
	private Vector<MethodNotFoundException> errors;
	private HashSet<ClassInfo> classInfos;
	private HashMap<Pair<ClassInfo,String>,CallGraphNode> methodInfos;
	private TopOrder<CallGraphNode,DefaultEdge> topOrder;

	/**
	 * Initialize a CallGraph object.
	 */
	protected CallGraph(JOPAppInfo appInfo, MethodInfo rootMethod) {
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
	public static CallGraph buildCallGraph(JOPAppInfo cli, String className, String methodSig) 
							throws MethodNotFoundException {
		MethodInfo rootMethod = cli.searchMethod(className,methodSig);
		CallGraph cg = new CallGraph(cli,rootMethod);
		cg.build();
		return cg;
	}
	private void build() throws MethodNotFoundException {
		this.errors = new Vector<MethodNotFoundException>();
		this.rootNode.build();
		if(! errors.isEmpty()) throw errors.get(0);
		/* Compute set of classes and methods */
		classInfos = new HashSet<ClassInfo>();
		for(CallGraphNode cgn : callGraph.vertexSet()) {
			classInfos.add(cgn.getReferencedMethod().fst());
		}
		methodInfos = new HashMap<Pair<ClassInfo,String>,CallGraphNode>();
		for(CallGraphNode cgn : callGraph.vertexSet()) {
			methodInfos.put(cgn.getReferencedMethod(),cgn);
		}		
		CycleDetector<CallGraphNode, DefaultEdge> cycDetect = 
			new CycleDetector<CallGraphNode, DefaultEdge>(callGraph);
		if(cycDetect.detectCycles()) throw new AssertionError("Cyclic callgraph");
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
	public Set<Pair<ClassInfo, String>> getMethods() {
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

	public Iterator<CallGraphNode> getReachableMethods(Pair<ClassInfo,String> m) {
		DepthFirstIterator<CallGraphNode, DefaultEdge> dfi = 
			new DepthFirstIterator<CallGraphNode, DefaultEdge>(callGraph,getNode(m));
		dfi.setCrossComponentTraversal(false);
		return dfi;		
	}
	public Iterator<CallGraphNode> getReachableMethods(MethodInfo m) {
		return getReachableMethods(new Pair<ClassInfo, String>(m.getCli(),m.methodId));
	}
	

	protected void addEdge(CallGraphNode src, CallGraphNode target) {
		callGraph.addVertex(target);
		callGraph.addEdge(src, target);
	}
	
	protected CallGraphNode getNode(Pair<ClassInfo,String> m) {
		return methodInfos.get(m);
	}
	public TopOrder<CallGraphNode,DefaultEdge> getTopologicalOrder() {
		return this.topOrder;
	}
}
