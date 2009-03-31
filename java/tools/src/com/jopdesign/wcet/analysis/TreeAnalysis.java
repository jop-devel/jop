package com.jopdesign.wcet.analysis;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.jopdesign.build.MethodInfo;
import com.jopdesign.wcet.Project;
import com.jopdesign.wcet.frontend.ControlFlowGraph;
import com.jopdesign.wcet.frontend.ControlFlowGraph.BasicBlockNode;
import com.jopdesign.wcet.frontend.ControlFlowGraph.CFGEdge;
import com.jopdesign.wcet.frontend.ControlFlowGraph.CFGNode;
import com.jopdesign.wcet.frontend.ControlFlowGraph.CfgVisitor;
import com.jopdesign.wcet.frontend.ControlFlowGraph.DedicatedNode;
import com.jopdesign.wcet.frontend.ControlFlowGraph.InvokeNode;
import com.jopdesign.wcet.frontend.ControlFlowGraph.SummaryNode;
import com.jopdesign.wcet.frontend.SourceAnnotations.LoopBound;
import com.jopdesign.wcet.graphutils.ProgressMeasure;
import com.jopdesign.wcet.graphutils.ProgressMeasure.RelativeProgress;

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
	private class ProgressVisitor implements CfgVisitor {
		private Map<MethodInfo, Long> subProgress;

		public ProgressVisitor(Map<MethodInfo, Long> subProgress) {
			this.subProgress = subProgress;
		}
		private long progress;
		public void visitBasicBlockNode(BasicBlockNode n) {
			progress = 1;
		}

		public void visitInvokeNode(InvokeNode n) {
			progress = 1 + subProgress.get(n.getImplementedMethod());
		}

		public void visitSpecialNode(DedicatedNode n) {
			progress = 1;			
		}

		public void visitSummaryNode(SummaryNode n) {
			progress = 1;
		}
		public long getProgress(CFGNode n) {
			n.accept(this);
			return progress;
		}
	}
	private Project project;
	private HashMap<MethodInfo, Long> methodWCET;

	public TreeAnalysis(Project p) {
		this.project = p;
	}
	public Map<MethodInfo,Map<CFGEdge, RelativeProgress<CFGNode>>> 
		computeProgress(MethodInfo targetMethod) {
		return computeProgress(targetMethod,false);
	}
	/* FIXME: filter leaf methods is really a ugly hack,
	 * but needs some work to play nice with uppaal eliminate-leaf-methods optimizations
	 */
	public Map<MethodInfo,Map<CFGEdge, RelativeProgress<CFGNode>>> 
		computeProgress(MethodInfo targetMethod, boolean filterLeafMethod) {
		HashMap<MethodInfo, Long> maxProgress = new HashMap<MethodInfo,Long>();
		HashMap<MethodInfo,Map<CFGEdge,RelativeProgress<CFGNode>>> relativeProgress =
			new HashMap<MethodInfo, Map<CFGEdge,RelativeProgress<CFGNode>>>();
		List<MethodInfo> reachable = project.getCallGraph().getImplementedMethods(targetMethod);
		Collections.reverse(reachable);
		for(MethodInfo mi: reachable) {
			ControlFlowGraph cfg = project.getFlowGraph(mi);
			Map<CFGNode, Long> localProgress = new HashMap<CFGNode,Long>();
			ProgressVisitor progressVisitor = new ProgressVisitor(maxProgress);
			for(CFGNode n : cfg.getGraph().vertexSet()) {
				localProgress.put(n, progressVisitor.getProgress(n)); 
			}
			ProgressMeasure<CFGNode, CFGEdge> pm = 
				new ProgressMeasure<CFGNode, CFGEdge>(cfg.getGraph(),cfg.getLoopColoring(),
													  extractUBs(cfg.getLoopBounds()) ,localProgress);
			long progress = pm.getMaxProgress().get(cfg.getExit());
			/* FIXME: _UGLY_ hack */
			if(filterLeafMethod && cfg.isLeafMethod()) {
				maxProgress.put(mi, 0L);
			} else {
				maxProgress.put(mi, progress);
			}
			relativeProgress.put(mi, pm.computeRelativeProgress());
		}
		System.out.println("Progress Measure (max): "+maxProgress.get(targetMethod));
		return relativeProgress;
	}
	private Map<CFGNode, Integer> extractUBs(Map<CFGNode, LoopBound> loopBounds) {
		Map<CFGNode, Integer> ubMap = new HashMap<CFGNode, Integer>();
		for(Entry<CFGNode, LoopBound> entry : loopBounds.entrySet()) { 
			ubMap.put(entry.getKey(),entry.getValue().getUpperBound()); 
		}
		return ubMap;
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
			ProgressMeasure<CFGNode, CFGEdge> pm = 
				new ProgressMeasure<CFGNode, CFGEdge>(cfg.getGraph(),cfg.getLoopColoring(),
													  extractUBs(cfg.getLoopBounds()) ,localCost);
			long wcet = pm.getMaxProgress().get(cfg.getExit());
			methodWCET.put(mi, wcet);
		}
		return methodWCET.get(targetMethod);
	}
}
