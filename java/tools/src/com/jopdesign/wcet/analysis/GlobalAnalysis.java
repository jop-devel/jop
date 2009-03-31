package com.jopdesign.wcet.analysis;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.jopdesign.build.MethodInfo;
import com.jopdesign.wcet.ProcessorModel;
import com.jopdesign.wcet.Project;
import com.jopdesign.wcet.analysis.RecursiveAnalysis.MapCostProvider;
import com.jopdesign.wcet.analysis.RecursiveAnalysis.RecursiveWCETStrategy;
import com.jopdesign.wcet.frontend.SuperGraph;
import com.jopdesign.wcet.frontend.ControlFlowGraph.CFGEdge;
import com.jopdesign.wcet.frontend.ControlFlowGraph.CFGNode;
import com.jopdesign.wcet.frontend.ControlFlowGraph.InvokeNode;
import com.jopdesign.wcet.frontend.SuperGraph.SuperInvokeEdge;
import com.jopdesign.wcet.frontend.SuperGraph.SuperReturnEdge;
import com.jopdesign.wcet.ipet.ILPModelBuilder;
import com.jopdesign.wcet.ipet.LinearVector;
import com.jopdesign.wcet.ipet.MaxCostFlow;
import com.jopdesign.wcet.ipet.ILPModelBuilder.CostProvider;
import com.jopdesign.wcet.ipet.MaxCostFlow.DecisionVariable;
import com.jopdesign.wcet.jop.MethodCache;
import com.jopdesign.wcet.jop.CacheConfig.StaticCacheApproximation;

import static com.jopdesign.wcet.graphutils.MiscUtils.addToSet;

/**
 * Global IPET-based analysis, supporting variable block caches (all fit region approximation).
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
			WcetCost cost = new WcetCost();
			if(cache.allFit(invoked) && ! project.getCallGraph().isLeafNode(invoked)) {
				GlobalAnalysis ga = new GlobalAnalysis(project);
				WcetCost allFitCost = null;
				try { allFitCost= ga.computeWCET(invoked, StaticCacheApproximation.ALL_FIT); }
				catch (Exception e) { throw new AssertionError(e); }
				cost.addCacheCost(returnCost + allFitCost.getCacheCost());
				cost.addNonLocalCost(allFitCost.getNonCacheCost());
				cost.addPotentialCacheFlushes(1);
				//System.err.println("Potential cache flush: "+invoked+" from "+invoker);
			} else {
				WcetCost recCost = stagedAnalysis.computeWCET(invoked, cacheMode);
				cost.addCacheCost(recCost.getCacheCost() + invokeReturnCost);				
				cost.addNonLocalCost(recCost.getCost() - recCost.getCacheCost());
			}
			Project.logger.info("Recursive WCET computation [GLOBAL IPET]: " + invoked.getMethod() +
			        		    ". cummulative cache cost: "+ cost.getCacheCost()+
					            ", execution cost: "+ cost.getNonCacheCost());
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
		GlobalVisitor visitor;
		switch(approx) {
		case ALWAYS_MISS: visitor = new GlobalVisitor(project,true); break;
		default: visitor = new GlobalVisitor(project,false); break; 
		}
		for(CFGNode n : sg.vertexSet()) {
			WcetCost cost = visitor.computeCost(n);
			nodeCost.put(n,cost);
		}
		return nodeCost;
	}
	public static class GlobalVisitor extends WcetVisitor {
		private boolean addAlwaysMissCost;
		public GlobalVisitor(Project p, boolean addAlwaysMissCost) {
			super(p);
			this.addAlwaysMissCost = addAlwaysMissCost;
		}
		public void visitInvokeNode(InvokeNode n) {
			visitBasicBlockNode(n);
			if(addAlwaysMissCost) {
				ProcessorModel proc = project.getProcessorModel();
				this.cost.addCacheCost(proc.getInvokeReturnMissCost(
						n.invokerFlowGraph(), 
						n.receiverFlowGraph()));
			}
		}
	}
}
