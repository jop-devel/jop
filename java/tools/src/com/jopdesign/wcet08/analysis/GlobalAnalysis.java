package com.jopdesign.wcet08.analysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.jopdesign.build.MethodInfo;
import com.jopdesign.wcet08.Project;
import com.jopdesign.wcet08.analysis.BlockWCET.WcetVisitor;
import com.jopdesign.wcet08.analysis.BlockWCET.AlwaysMissVisitor;
import com.jopdesign.wcet08.analysis.CacheConfig.CacheApproximation;
import com.jopdesign.wcet08.analysis.SimpleAnalysis.MapCostProvider;
import com.jopdesign.wcet08.frontend.SuperGraph;
import com.jopdesign.wcet08.frontend.ControlFlowGraph.CFGEdge;
import com.jopdesign.wcet08.frontend.ControlFlowGraph.CFGNode;
import com.jopdesign.wcet08.frontend.SuperGraph.SuperInvokeEdge;
import com.jopdesign.wcet08.frontend.SuperGraph.SuperReturnEdge;
import com.jopdesign.wcet08.ipet.ILPModelBuilder;
import com.jopdesign.wcet08.ipet.LinearVector;
import com.jopdesign.wcet08.ipet.MaxCostFlow;
import com.jopdesign.wcet08.ipet.ILPModelBuilder.CostProvider;
import com.jopdesign.wcet08.ipet.MaxCostFlow.DecisionVariable;

/**
 * Global IPET-based analysis, supporting 2-block LRU caches (static)
 * and variable block caches (miss once areas).
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 *
 */
public class GlobalAnalysis {
	private Project project;
	private BlockWCET blockBuilder;
	public GlobalAnalysis(Project p) {
		this.project = p;
		this.blockBuilder = new BlockWCET(p);
	}
	public WcetCost computeWCET(MethodInfo m, CacheApproximation cacheMode) throws Exception {
		String key = m.getFQMethodName() + "_global_" + cacheMode;
		SuperGraph sg = new SuperGraph(project.getWcetAppInfo(),project.getFlowGraph(m));
		ILPModelBuilder imb = new ILPModelBuilder(project);
		Map<CFGNode, WcetCost> nodeCostMap = buildNodeCostMap(sg, cacheMode);
		CostProvider<CFGNode> nodeWCET = new MapCostProvider<CFGNode>(nodeCostMap);
		/* create an ILP graph for all reachable methods */
		MaxCostFlow<CFGNode, CFGEdge> maxCostFlow = imb.buildGlobalILPModel(key, sg, nodeWCET);
		if(cacheMode == CacheApproximation.ALL_FIT) {
			addMissOnceCost(sg,maxCostFlow);
		}
		/* TODO: exact cost extraction */
		double lpCost = maxCostFlow.solve(null);
		WcetCost cost = new WcetCost();
		cost.addLocalCost(Math.round(lpCost));
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
		/* For each  MethodInfo, create a binary decision variable */
		for(MethodInfo mi : accessEdges.keySet()) {
			/* sum(edges) <= b_M * |edges| */
			LinearVector<CFGEdge> lv = new LinearVector<CFGEdge>();
			for(CFGEdge e : accessEdges.get(mi)) {
				lv.add(e,1);
			}
			DecisionVariable dVar = maxCostFlow.addFlowDecision(lv);
			/* cost += b_M * missCost(M) */
			maxCostFlow.addDecisionCost(dVar, blockBuilder.getMissCost(mi));
		}
	}
	/* TODO: DUP */
	private static<K,V> void addToSet(Map<K,Set<V>> map,K key, V val) {
		Set<V> set = map.get(key);
		if(set == null) {
			set = new HashSet<V>();
			map.put(key,set);
		}
		set.add(val);
	}
	/**
	 * compute execution time of basic blocks in the supergraph
	 * @param sg the supergraph, whose vertices are considered
	 * @return
	 */
	private Map<CFGNode, WcetCost> 
		buildNodeCostMap(SuperGraph sg, CacheApproximation approx) {		
		HashMap<CFGNode, WcetCost> nodeCost = new HashMap<CFGNode,WcetCost>();
		Class<? extends WcetVisitor> visitor;
		switch(approx) {
		case ALWAYS_MISS: visitor = AlwaysMissVisitor.class; break;
		default: visitor = WcetVisitor.class; break; 
		}
		for(CFGNode n : sg.vertexSet()) {
			WcetCost cost = blockBuilder.computeLocalCost(n, visitor);
			nodeCost.put(n,cost);
		}
		return nodeCost;
	}
}
