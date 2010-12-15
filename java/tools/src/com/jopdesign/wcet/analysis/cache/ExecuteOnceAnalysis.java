package com.jopdesign.wcet.analysis.cache;

import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.code.CallString;
import com.jopdesign.common.code.ControlFlowGraph;
import com.jopdesign.wcet.Project;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.TopologicalOrderIterator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * This is a primitive, simple analysis to find blocks which are only executed once
 * in a scope.
 * <p>
 * For this purpose, the nodes of each scope are split into the root method and
 * referenced methods. Those referenced methods which might be called more than once
 * are marked with {@code *}, the others with {@code 1}.
 * </p><p>
 * If we need to know whether a node is executed at most once in a scope, we check
 * whether the containing method is marked with {@code root}, {@code *} or {@code 1}. 
 * In the first case, we check whether the containing basic block is part of a loop.
 * </p>
 * TODO: [scope-analysis] More efficient implementation of ExecuteOnceAnalysis. Currently
 *                        we have 4 nested loops.
 *                        
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 *
 */
public class ExecuteOnceAnalysis {
	private Project project;
	private Map<CallGraphNode,Set<MethodInfo>> inLoopSet;

	public ExecuteOnceAnalysis(Project p) {
		this.project = p;
		analyze();
	}
	private void analyze() {
		inLoopSet = new HashMap<CallGraphNode, Set<MethodInfo>>();
		/* Top Down the Scope Graph */
		TopologicalOrderIterator<CallGraphNode, DefaultEdge> iter =
			project.getCallGraph().topDownIterator();

		while(iter.hasNext()) {
			CallGraphNode scope = iter.next();
			scope = new CallGraphNode(scope.getMethodImpl(), CallString.EMPTY); /* Remove call string */
			ControlFlowGraph cfg = project.getFlowGraph(scope.getMethodImpl());
			Set<MethodInfo> inLoop = new HashSet<MethodInfo>();
			for(ControlFlowGraph.CFGNode node : cfg.getGraph().vertexSet()) {
				if(! (node instanceof ControlFlowGraph.InvokeNode)) continue;
				ControlFlowGraph.InvokeNode iNode = (ControlFlowGraph.InvokeNode) node;
				if(! cfg.getLoopColoring().getLoopColor(node).isEmpty()) {
					for(MethodInfo impl : iNode.getImplementedMethods()) {
						inLoop.add(impl);
						for(MethodInfo rImpl : project.getCallGraph().getImplementationsReachableFromMethod(impl)) {
							inLoop.add(rImpl);
						}
					}
				}
			}
			inLoopSet .put(scope, inLoop);
		}
	}
	
	public boolean isExecutedOnce(CallGraphNode scope, ControlFlowGraph.CFGNode node) {
		ControlFlowGraph cfg = node.getControlFlowGraph();
		Set<MethodInfo> inLoopMethods = inLoopSet.get(scope);
		if(inLoopMethods == null) {
			Logger.getLogger("Object Cache Analysis").warning("No loop information for " + scope.getMethodImpl().getFQMethodName() + " @ " +
					scope.getCallString().toString());
			return false;
		}
		if(! inLoopMethods.contains(cfg.getMethodInfo())) {
			return cfg.getLoopColoring().getLoopColor(node).size() == 0;
		} else {
			return false;
		}
	}
	
}
