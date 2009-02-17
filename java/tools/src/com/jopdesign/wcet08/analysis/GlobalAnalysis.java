package com.jopdesign.wcet08.analysis;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.jopdesign.build.MethodInfo;
import com.jopdesign.wcet08.ProcessorModel;
import com.jopdesign.wcet08.Project;
import com.jopdesign.wcet08.analysis.RecursiveAnalysis.MapCostProvider;
import com.jopdesign.wcet08.analysis.RecursiveAnalysis.RecursiveWCETStrategy;
import com.jopdesign.wcet08.frontend.SuperGraph;
import com.jopdesign.wcet08.frontend.ControlFlowGraph.CFGEdge;
import com.jopdesign.wcet08.frontend.ControlFlowGraph.CFGNode;
import com.jopdesign.wcet08.frontend.ControlFlowGraph.InvokeNode;
import com.jopdesign.wcet08.frontend.SuperGraph.SuperInvokeEdge;
import com.jopdesign.wcet08.frontend.SuperGraph.SuperReturnEdge;
import static com.jopdesign.wcet08.graphutils.MiscUtils.addToSet;
import com.jopdesign.wcet08.ipet.ILPModelBuilder;
import com.jopdesign.wcet08.ipet.LinearVector;
import com.jopdesign.wcet08.ipet.MaxCostFlow;
import com.jopdesign.wcet08.ipet.ILPModelBuilder.CostProvider;
import com.jopdesign.wcet08.ipet.MaxCostFlow.DecisionVariable;
import com.jopdesign.wcet08.jop.MethodCache;
import com.jopdesign.wcet08.jop.CacheConfig.StaticCacheApproximation;

/**
 * Global IPET-based analysis, supporting 2-block LRU caches (static)
 * and variable block caches (miss once areas).
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 *
 */
public class GlobalAnalysis {
	private Project project;
	private Map<DecisionVariable,MethodInfo> decisionVariables =
		new HashMap<DecisionVariable, MethodInfo>();
	public GlobalAnalysis(Project p) {
		this.project = p;
	}
	public WcetCost computeWCET(MethodInfo m, StaticCacheApproximation cacheMode) throws Exception {
		String key = m.getFQMethodName() + "_global_" + cacheMode;
		SuperGraph sg = new SuperGraph(project.getWcetAppInfo(),project.getFlowGraph(m));
		ILPModelBuilder imb = new ILPModelBuilder(project);
		Map<CFGNode, WcetCost> nodeCostMap = buildNodeCostMap(sg, cacheMode);
		CostProvider<CFGNode> nodeWCET = new MapCostProvider<CFGNode>(nodeCostMap);
		/* create an ILP graph for all reachable methods */
		MaxCostFlow<CFGNode, CFGEdge> maxCostFlow = imb.buildGlobalILPModel(key, sg, nodeWCET);
		if(cacheMode == StaticCacheApproximation.ALL_FIT) {
			addMissOnceCost(sg,maxCostFlow);
		}
		Map<CFGEdge, Long> flowMap = new HashMap<CFGEdge, Long>();
		Map<DecisionVariable, Boolean> cacheMissMap = new HashMap<DecisionVariable, Boolean>();
		double lpCost = maxCostFlow.solve(flowMap,cacheMissMap);
		WcetCost cost = new WcetCost();
		/* exact cost extraction */
		for(Entry<CFGEdge,Long> flowEntry : flowMap.entrySet()) {
			CFGNode target    = sg.getEdgeTarget(flowEntry.getKey());
			WcetCost nodeCost = nodeCostMap.get(target).getFlowCost(flowEntry.getValue());
			cost.addNonLocalCost(nodeCost.getLocalCost() + nodeCost.getNonLocalCost());
			if(nodeCost.getCacheCost() != 0) {
				throw new AssertionError("Local cache cost in global ILP ??");
			}
		}
		/* decision variable map */
		MethodCache cache = project.getProcessorModel().getMethodCache();
		for(Entry<DecisionVariable, Boolean> cacheMiss : cacheMissMap.entrySet()) {
			if(cacheMiss.getValue()) {
				MethodInfo mi = decisionVariables.get(cacheMiss.getKey());
				cost.addCacheCost(cache.missOnceCost(mi));
			}
		}
		long objValue = (long) (lpCost+0.5);
		if(cost.getCost() != objValue) {
			throw new AssertionError("Inconsistency: lpValue vs. extracted value: "+objValue+" / "+cost.getCost());
		}
		return cost;
	}
	/* add cost for missing each method once (ALL FIT) */
	private void addMissOnceCost(SuperGraph sg, MaxCostFlow<CFGNode,CFGEdge> maxCostFlow) {
		/* collect access sites */
		Map<MethodInfo, Set<CFGEdge>> accessEdges = 
			new HashMap<MethodInfo, Set<CFGEdge>>();
		for(Entry<SuperInvokeEdge, SuperReturnEdge> invokeSite: sg.getSuperEdgePairs().entrySet()) {
			MethodInfo invoked = invokeSite.getKey().getInvokeNode().receiverFlowGraph().getMethodInfo();
			addToSet(accessEdges, invoked, invokeSite.getKey());
			MethodInfo invoker = invokeSite.getKey().getInvokeNode().invokerFlowGraph().getMethodInfo();
			addToSet(accessEdges, invoker, invokeSite.getValue());
		}
		MethodCache cache = project.getProcessorModel().getMethodCache();
		/* For each  MethodInfo, create a binary decision variable */
		for(MethodInfo mi : accessEdges.keySet()) {
			/* sum(edges) <= b_M * |edges| */
			LinearVector<CFGEdge> lv = new LinearVector<CFGEdge>();
			for(CFGEdge e : accessEdges.get(mi)) {
				lv.add(e,1);
			}
			DecisionVariable dVar = maxCostFlow.addFlowDecision(lv);
			this.decisionVariables .put(dVar,mi);
			/* cost += b_M * missCost(M) */
			maxCostFlow.addDecisionCost(dVar, cache.missOnceCost(mi));
		}
	}
	public static class GlobalIPETStrategy implements RecursiveWCETStrategy<StaticCacheApproximation> {
		public WcetCost recursiveWCET(
				RecursiveAnalysis<StaticCacheApproximation> stagedAnalysis,
				InvokeNode n, StaticCacheApproximation cacheMode) {
			if(cacheMode != StaticCacheApproximation.ALL_FIT) {
				throw new AssertionError("Cache Mode "+cacheMode+" not supported using global IPET strategy");
			}
			Project project = stagedAnalysis.getProject();
			MethodInfo invoker = n.getBasicBlock().getMethodInfo(); 
			MethodInfo invoked = n.getImplementedMethod();
			ProcessorModel proc = project.getProcessorModel();
			MethodCache cache = proc.getMethodCache();
			long returnCost = cache.getMissOnReturnCost(proc, project.getFlowGraph(invoker));
			long invokeReturnCost = cache.getInvokeReturnMissCost(
					proc,
					project.getFlowGraph(invoker),
	                project.getFlowGraph(invoked));
			long cacheCost, nonLocalExecCost;
			if(cache.allFit(invoked) && ! project.getCallGraph().isLeafNode(invoked)) {
				GlobalAnalysis ga = new GlobalAnalysis(project);
				WcetCost allFitCost = null;
				try { allFitCost= ga.computeWCET(invoked, StaticCacheApproximation.ALL_FIT); }
				catch (Exception e) { throw new AssertionError(e); }
				cacheCost = returnCost + allFitCost.getCacheCost();
				nonLocalExecCost = allFitCost.getNonCacheCost();				
			} else {
				WcetCost recCost = stagedAnalysis.computeWCET(invoked, cacheMode);
				cacheCost = recCost.getCacheCost() + invokeReturnCost ;				
				nonLocalExecCost = recCost.getCost() - recCost.getCacheCost();
			}
			WcetCost cost = new WcetCost();
			cost.addNonLocalCost(nonLocalExecCost);
			cost.addCacheCost(cacheCost);
			Project.logger.info("Recursive WCET computation [GLOBAL IPET]: " + invoked.getMethod() +
			        		    ". cummulative cache cost: "+cacheCost+
					            " non local execution cost: "+nonLocalExecCost);
			return cost;
		}
		
	}
	/**
	 * compute execution time of basic blocks in the supergraph
	 * @param sg the supergraph, whose vertices are considered
	 * @return
	 */
	private Map<CFGNode, WcetCost> 
		buildNodeCostMap(SuperGraph sg, StaticCacheApproximation approx) {		
		HashMap<CFGNode, WcetCost> nodeCost = new HashMap<CFGNode,WcetCost>();
		Class<? extends WcetVisitor> visitor;
		switch(approx) {
		case ALWAYS_MISS: visitor = AlwaysMissVisitor.class; break;
		default: visitor = WcetVisitor.class; break; 
		}
		for(CFGNode n : sg.vertexSet()) {
			WcetCost cost = computeLocalCost(n, visitor);
			nodeCost.put(n,cost);
		}
		return nodeCost;
	}
	public WcetCost computeLocalCost(CFGNode n) {
		return computeLocalCost(n,WcetVisitor.class);
	}
	private WcetCost computeLocalCost(CFGNode n, Class<? extends WcetVisitor> c) {
		WcetVisitor wcetVisitor;
		try {
			wcetVisitor = c.newInstance();
			wcetVisitor.project = project;
		} catch (Exception e) {
			throw new AssertionError("Failed to instantiate WcetVisitor: "+c+" : "+e);
		}
		n.accept(wcetVisitor);
		return wcetVisitor.cost;
	}
	public static class AlwaysMissVisitor extends WcetVisitor {
		public void visitInvokeNode(InvokeNode n) {
			super.visitInvokeNode(n);
			ProcessorModel proc = project.getProcessorModel();
			this.cost.addCacheCost(proc.getInvokeReturnMissCost(n.invokerFlowGraph(), n.receiverFlowGraph()));
		}
	}
}
