package com.jopdesign.wcet.analysis;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.jopdesign.build.MethodInfo;
import com.jopdesign.wcet.Project;
import com.jopdesign.wcet.frontend.ControlFlowGraph;
import com.jopdesign.wcet.frontend.ControlFlowGraph.CFGEdge;
import com.jopdesign.wcet.frontend.ControlFlowGraph.CFGNode;
import com.jopdesign.wcet.frontend.ControlFlowGraph.InvokeNode;
import com.jopdesign.wcet.frontend.SourceAnnotations.LoopBound;
import com.jopdesign.wcet.graphutils.ProgressMeasure;

/** While implementing progress measure, I found that they can be used for tree based
 *  WCET analysis. Why not ?
 *  This can be implemented in almost linear (graph size) time, 
 *  but our suboptimal implementation depends the depth of the loop nest tree. 
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 */
public class TreeAnalysis {
	private class LocalCostVisitor extends WcetVisitor {
		public LocalCostVisitor(Project p) { super(p); }

		@Override
		public void visitInvokeNode(InvokeNode n) {
			visitBasicBlockNode(n);
			cost.addCacheCost(project.getProcessorModel().getInvokeReturnMissCost(
					n.invokerFlowGraph(), 
					n.receiverFlowGraph()));
			cost.addNonLocalCost(methodWCET.get(n.getImplementedMethod()));
		}
		
	}
	private Project project;
	private HashMap<MethodInfo, Long> methodWCET;

	public TreeAnalysis(Project p) {
		this.project = p;
	}
	public long computeWCET(MethodInfo targetMethod) {
		this.methodWCET = new HashMap<MethodInfo,Long>();
		List<MethodInfo> reachable = project.getCallGraph().getImplementedMethods(targetMethod);
		Collections.reverse(reachable);
		for(MethodInfo mi: reachable) {
			ControlFlowGraph cfg = project.getFlowGraph(mi);
			Map<CFGNode, Long> localCost = new HashMap<CFGNode,Long>();
			LocalCostVisitor lcv = new LocalCostVisitor(project);
			for(CFGNode n : cfg.getGraph().vertexSet()) {
				localCost.put(n, lcv.computeCost(n).getCost()); 
			}
			Map<CFGNode,Integer> loopBounds = new HashMap<CFGNode, Integer>();
			for(Entry<CFGNode, LoopBound> lb : cfg.getLoopBounds().entrySet()) {
				loopBounds.put(lb.getKey(), lb.getValue().getUpperBound());
			}
			ProgressMeasure<CFGNode, CFGEdge> pm = 
				new ProgressMeasure<CFGNode, CFGEdge>(cfg.getGraph(),cfg.getLoopColoring(),loopBounds ,localCost);
			long wcet = pm.getMaxProgress().get(cfg.getExit());
			methodWCET.put(mi, wcet);
		}
		return methodWCET.get(targetMethod);
	}
}
