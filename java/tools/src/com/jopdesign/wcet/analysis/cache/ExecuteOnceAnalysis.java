package com.jopdesign.wcet.analysis.cache;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.TopologicalOrderIterator;

import com.jopdesign.build.MethodInfo;
import com.jopdesign.wcet.Project;
import com.jopdesign.wcet.frontend.ControlFlowGraph;
import com.jopdesign.wcet.frontend.CallGraph.CallGraphNode;
import com.jopdesign.wcet.frontend.ControlFlowGraph.CFGNode;
import com.jopdesign.wcet.frontend.ControlFlowGraph.InvokeNode;

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
 *                        we have 4! nested loops.
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
			ControlFlowGraph cfg = project.getFlowGraph(scope.getMethodImpl());
			Set<MethodInfo> inLoop = new HashSet<MethodInfo>();
			for(CFGNode node : cfg.getGraph().vertexSet()) {
				if(! (node instanceof InvokeNode)) continue;
				InvokeNode iNode = (InvokeNode) node;
				if(! cfg.getLoopColoring().getLoopColor(node).isEmpty()) {
					for(MethodInfo impl : iNode.getImplementedMethods()) {
						inLoop.add(impl);
						for(MethodInfo rImpl : project.getCallGraph().getReachableImplementations(impl)) {
							inLoop.add(rImpl);
						}
					}
				}
			}
			inLoopSet .put(scope, inLoop);
		}
	}
	
	public boolean isExecutedOnce(CallGraphNode scope, CFGNode node) {
		ControlFlowGraph cfg = node.getControlFlowGraph();
		Set<MethodInfo> inLoopMethods = inLoopSet.get(scope);
		if(! inLoopMethods.contains(cfg.getMethodInfo())) {
			return cfg.getLoopColoring().getLoopColor(node).size() == 0;
		} else {
			return false;
		}
	}
	
}
