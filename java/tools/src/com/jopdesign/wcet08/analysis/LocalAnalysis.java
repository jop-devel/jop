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
package com.jopdesign.wcet08.analysis;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.apache.bcel.classfile.LineNumberTable;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.log4j.Logger;
import org.jgrapht.DirectedGraph;

import com.jopdesign.build.ClassInfo;
import com.jopdesign.build.MethodInfo;
import com.jopdesign.wcet08.ProcessorModel;
import com.jopdesign.wcet08.Project;
import com.jopdesign.wcet08.frontend.BasicBlock;
import com.jopdesign.wcet08.frontend.ControlFlowGraph;
import com.jopdesign.wcet08.frontend.WcetAppInfo;
import com.jopdesign.wcet08.frontend.ControlFlowGraph.CFGEdge;
import com.jopdesign.wcet08.frontend.ControlFlowGraph.CFGNode;
import com.jopdesign.wcet08.frontend.ControlFlowGraph.InvokeNode;
import com.jopdesign.wcet08.frontend.ControlFlowGraph.SummaryNode;
import com.jopdesign.wcet08.ipet.ILPModelBuilder;
import com.jopdesign.wcet08.ipet.MaxCostFlow;
import com.jopdesign.wcet08.ipet.ILPModelBuilder.CostProvider;
import com.jopdesign.wcet08.jop.CacheConfig;
import com.jopdesign.wcet08.jop.MethodCache;
import com.jopdesign.wcet08.jop.CacheConfig.StaticCacheApproximation;
import com.jopdesign.wcet08.report.ClassReport;

/**
 * Simple and fast local analysis with cache approximation.
 * For the miss-once all-fit approximation, global analysis can be used to tighten the bound.
 *
 * @author Benedikt Huber (benedikt.huber@gmail.com)
 *
 */
public class LocalAnalysis {
	class WcetKey {
		MethodInfo m;
		StaticCacheApproximation alwaysHit;
		public WcetKey(MethodInfo m, StaticCacheApproximation mode) {
			this.m = m; this.alwaysHit = mode;
		}
		@Override
		public boolean equals(Object that) {
			return (that instanceof WcetKey) ? equalsKey((WcetKey) that) : false;
		}
		private boolean equalsKey(WcetKey key) {
			return this.m.equals(key.m) && (this.alwaysHit == key.alwaysHit);
		}
		@Override
		public int hashCode() {
			return m.getFQMethodName().hashCode()+this.alwaysHit.hashCode();
		}
		@Override
		public String toString() {
			return this.m.getFQMethodName()+"["+this.alwaysHit+"]";
		}
	}
	public class WcetSolution {
		private long lpCost;
		private WcetCost cost;
		private Map<CFGNode,Long> nodeFlow;
		private Map<CFGEdge,Long> edgeFlow;
		private DirectedGraph<CFGNode, CFGEdge> graph;
		private Map<CFGNode, WcetCost> nodeCosts;
		public WcetSolution(DirectedGraph<CFGNode,CFGEdge> g, Map<CFGNode,WcetCost> nodeCosts) { 
			this.graph = g;
			this.nodeCosts= nodeCosts;
		}
		public void setSolution(long lpCost, Map<CFGEdge, Long> edgeFlow) {
			this.lpCost = lpCost;
			this.edgeFlow = edgeFlow;
			computeNodeFlow();
			computeCost();
		}
		public long getLpCost() {
			return lpCost;
		}
		public WcetCost getCost() {
			return cost;
		}
		public long getNodeFlow(CFGNode n) {
			return getNodeFlow().get(n);
		}
		public Map<CFGNode, Long> getNodeFlow() {
			return nodeFlow;
		}
		public Map<CFGEdge, Long> getEdgeFlow() {
			return edgeFlow;
		}
		/** Safety check: compare flow*cost to actual solution */
		public void checkConsistentency() {
			if(cost.getCost() != lpCost) {
				throw new AssertionError("The solution implies that the flow graph cost is " 
										 + cost.getCost() + ", but the ILP solver reported "+lpCost);
			}			
		}
		private void computeNodeFlow() {
			nodeFlow = new HashMap<CFGNode, Long>();
			for(CFGNode n : graph.vertexSet()) {
				if(graph.inDegreeOf(n) == 0) nodeFlow.put(n, 0L); // ENTRY and DEAD CODE (no flow)
				else {
					long flow = 0;
					for(CFGEdge inEdge : graph.incomingEdgesOf(n)) {
						flow+=edgeFlow.get(inEdge);
					}
					nodeFlow.put(n, flow);
				}
			}			
		}
		/* Compute cost, separating local and non-local cost */
		private void computeCost() {
			cost = new WcetCost();
			for(CFGNode n : graph.vertexSet()) {
				long flow = nodeFlow.get(n);
				cost.addLocalCost(flow * nodeCosts.get(n).getLocalCost());
				cost.addCacheCost(flow * nodeCosts.get(n).getCacheCost());
				cost.addNonLocalCost(flow * nodeCosts.get(n).getNonLocalCost());
			}			
		}
	}
	/* provide cost given a node->cost table */
	public static class MapCostProvider<T> implements CostProvider<T> {
		private Map<T, WcetCost> costMap;
		public MapCostProvider(Map<T,WcetCost> costMap) {
			this.costMap = costMap;
		}
		public long getCost(T obj) {
			WcetCost cost = costMap.get(obj);
			if(cost == null) throw new NullPointerException("Missing entry for "+obj+" in cost map");
			return cost.getCost();
		}
		
	}
	
	private static final Logger logger = Logger.getLogger(LocalAnalysis.class);
	private Project project;
	private WcetAppInfo appInfo;
	private Hashtable<WcetKey, WcetCost> wcetMap;
	private ILPModelBuilder modelBuilder;
	private ProcessorModel processor;

	public LocalAnalysis(Project project) {
		this.project = project;
		this.appInfo = project.getWcetAppInfo();
		this.processor = project.getWcetAppInfo().getProcessorModel();

		this.wcetMap = new Hashtable<WcetKey,WcetCost>();

		this.modelBuilder = new ILPModelBuilder(project);
	}
	/** WCET analyis, using the configured cache approximation strategy */
	public WcetCost computeWCET(MethodInfo m) {
		StaticCacheApproximation cacheMode = project.getConfig().getOption(CacheConfig.STATIC_CACHE_APPROX);
		return computeWCET(m,cacheMode);
	}
	/**
	 * WCET analysis of the given method, using some cache approximation scheme.
	 * <ul>
	 *  <li/>{@link CacheApproximation.ALWAYS_HIT}: Assume all method cache accesses are hits
	 *  <li/>{@link CacheApproximation.ALWAYS_MISS}: Assume all method cache accesses are misses
	 *  <li/>{@link CacheApproximation.ANALYSE_REACHABLE}: 
	 *  	<p>If for some invocation of <code>m</code>, all methods reachable from and including <code>m</code>
	 *      fit into the cache, add the cost for missing each method exactly once.
	 *      If the method isn't a leaf, the minimal number cycles is hidden.
	 *      Additionally, add the cost for missing on return.</p>
	 *      <p>Otherwise, assume invoke/return is miss, and analyze the method using ANALYSE_REACHABLE.</p>
	 * @param m the method to be analyzed
	 * @param cacheMode the cache approximation strategy
	 * @return
	 * 
	 * <p>FIXME: Logging/Report need to be cleaned up </p>
	 */
	public WcetCost computeWCET(MethodInfo m, StaticCacheApproximation cacheMode) {
		/* use memoization to speed up analysis */
		WcetKey key = new WcetKey(m,cacheMode);
		if(wcetMap.containsKey(key)) return wcetMap.get(key);

		/* check cache is big enough */
		MethodCache cache = project.getProcessorModel().getMethodCache();
		try {
			cache.checkCache(m);
		} catch (Exception e) {
			/* TODO: throw exception, not error ? */
			throw new AssertionError("Bad Method cache: "+e.getMessage());
		}

		/* build wcet map */
		ControlFlowGraph cfg = appInfo.getFlowGraph(m);
		Map<CFGNode,WcetCost> nodeCosts;
		nodeCosts = buildNodeCostMap(cfg,cacheMode);
		WcetSolution sol = runWCETComputation(key.toString(), cfg, cacheMode, nodeCosts);
		sol.checkConsistentency();
		wcetMap.put(key, sol.getCost()); 
		
		/* Logging and Report */
		if(project.reportGenerationActive()) {
			Hashtable<CFGNode, String> nodeFlowCostDescrs = new Hashtable<CFGNode, String>();
			
			for(CFGNode n : cfg.getGraph().vertexSet()) {
				if(sol.getNodeFlow(n) > 0) {
					nodeFlowCostDescrs .put(n,nodeCosts.get(n).toString());
					BasicBlock basicBlock = n.getBasicBlock();
					/* prototyping */
					if(basicBlock != null) {
						int pos = basicBlock.getFirstInstruction().getPosition();
						ClassInfo cli = basicBlock.getClassInfo();
						LineNumberTable lineNumberTable = basicBlock.getMethodInfo().getMethod().getLineNumberTable();
						int sourceLine = lineNumberTable.getSourceLine(pos);
						ClassReport cr = project.getReport().getClassReport(cli);
						Long oldCost = (Long) cr.getLineProperty(sourceLine, "cost");
						if(oldCost == null) oldCost = 0L;
						cr.addLineProperty(sourceLine, "cost", oldCost + sol.getNodeFlow(n)*nodeCosts.get(n).getCost());
						for(InstructionHandle ih : basicBlock.getInstructions()) {
							sourceLine = lineNumberTable.getSourceLine(ih.getPosition());
							cr.addLineProperty(sourceLine, "color", "red");
						}
					}
				} else {
					nodeFlowCostDescrs.put(n, ""+nodeCosts.get(n).getCost());
				}
			}
			logger.info("WCET for " + key + ": "+sol.getCost());
			Map<String,Object> stats = new Hashtable<String, Object>();
			stats.put("WCET",sol.getCost());
			stats.put("mode",cacheMode);
			stats.put("all-methods-fit-in-cache",cache.allFit(m));
			project.getReport().addDetailedReport(m,"WCET_"+cacheMode.toString(),stats,nodeFlowCostDescrs,sol.getEdgeFlow());
		}
		WcetCost cost = sol.getCost();
		cost.moveLocalToGlobalCost();
		return cost;
	}
	/**
	 * Compute the WCET of the given control flow graph
	 * @param name name for the ILP problem
	 * @param cfg the control flow graph 
	 * @param cacheMode which cache approximation to use
	 * @param nodeCosts (costs per node which should be used) (if null, calculate cost)
	 * @return the WCET for the given CFG
	 */
	public WcetSolution runWCETComputation(
			String name, 
			ControlFlowGraph cfg,
			StaticCacheApproximation cacheMode,
			Map<CFGNode,WcetCost> nodeCosts) {		
		if(nodeCosts == null)  nodeCosts = buildNodeCostMap(cfg,cacheMode);
		WcetSolution sol = new WcetSolution(cfg.getGraph(),nodeCosts);
		CostProvider<CFGNode> costProvider = new MapCostProvider<CFGNode>(nodeCosts);
		MaxCostFlow<CFGNode,CFGEdge> problem = 
			modelBuilder.buildLocalILPModel(name,cfg, costProvider);
		/* solve ILP */
		/* extract node flow, local cost, cache cost, cummulative cost */
		long maxCost = 0;
		Map<CFGEdge, Long> edgeFlow = new HashMap<CFGEdge, Long>();
		try {
			maxCost = Math.round(problem.solve(edgeFlow));
		} catch (Exception e) {
			throw new Error("Failed to solve LP problem: "+e,e);
		}
		sol.setSolution(maxCost, edgeFlow);
		return sol;
	}
	
	/**
	 * map flowgraph nodes to WCET
	 * if the node is a invoke, we need to compute the WCET for the invoked method
	 * otherwise, just take the basic block WCET
	 * @param fg 
	 * @param cacheMode cache approximation mode
	 * @return
	 */
	private Map<CFGNode, WcetCost> 
		buildNodeCostMap(ControlFlowGraph fg,StaticCacheApproximation cacheMode) {
		
		HashMap<CFGNode, WcetCost> nodeCost = new HashMap<CFGNode,WcetCost>();
		for(CFGNode n : fg.getGraph().vertexSet()) {
			nodeCost.put(n, computeCostOfNode(n, cacheMode));
		}
		return nodeCost;
	}
	
	private class LocalWcetVisitor extends WcetVisitor {
		StaticCacheApproximation cacheMode;
		public LocalWcetVisitor(Project project, StaticCacheApproximation cacheMode) {
			this.project = project;
			this.cacheMode = cacheMode;
		}
		@Override
		public void visitSummaryNode(SummaryNode n) {
			cost.addLocalCost(
			  runWCETComputation("summary", 
					             n.getSubGraph(), 
					             StaticCacheApproximation.ALWAYS_MISS,null).getCost().getCost());
		}
		@Override
		public void visitInvokeNode(InvokeNode n) {
			cost.addLocalCost(processor.getExecutionTime(n.getBasicBlock().getClassInfo(),n.getInstructionHandle().getInstruction()));
			if(n.isInterface()) {
				throw new AssertionError("Invoke node "+n.getReferenced()+" without implementation in WCET analysis - did you preprocess virtual methods ?");
			}
			recursiveWCET(n.getBasicBlock().getMethodInfo(), n.getImplementedMethod());
		}
		private void recursiveWCET(MethodInfo invoker, MethodInfo invoked) {						
			ProcessorModel proc = project.getProcessorModel();
			MethodCache cache = proc.getMethodCache();

			long cacheCost;
			WcetCost recCost = computeWCET(invoked, cacheMode);
			long nonLocalExecCost = recCost.getCost() - recCost.getCacheCost();
			long nonLocalCacheCost = recCost.getCacheCost();
			long invokeReturnCost = cache.getInvokeReturnMissCost(
					proc,
					appInfo.getFlowGraph(invoker),
                    appInfo.getFlowGraph(invoked));
			boolean allFitMode = cacheMode == StaticCacheApproximation.ALL_FIT || 
								 cacheMode == StaticCacheApproximation.ALL_FIT_LOCAL;
			if(! proc.hasMethodCache() || cacheMode == StaticCacheApproximation.ALWAYS_HIT) {
				cacheCost = 0;
			} else if(project.getCallGraph().isLeafNode(invoked)) {
				cacheCost = invokeReturnCost + nonLocalCacheCost;
			} else if(allFitMode && cache.allFit(invoked)) {
				long returnCost = cache.getMissOnReturnCost(proc, appInfo.getFlowGraph(invoker));
				if(cacheMode == StaticCacheApproximation.ALL_FIT_LOCAL) {
					/* Maybe its better not to apply the all-fit heuristic ... */
					long noAllFitCost = recCost.getCost() + invokeReturnCost;
					/* Compute cost without method cache */
					long alwaysHitCost = computeWCET(invoked, StaticCacheApproximation.ALWAYS_HIT).getCost();
					/* Compute penalty for loading each method exactly once */
					long allFitPenalty = cache.getMissOnceCummulativeCacheCost(invoked);
					long allFitCacheCost = allFitPenalty  + returnCost;
					/* Cost All-Fit: recursive + penalty for loading once + return to caller */
					long allFitCost = alwaysHitCost + allFitCacheCost;
					/* Choose the better approximation */
					if(allFitCost <= noAllFitCost) {
						cacheCost = allFitCacheCost;
						nonLocalExecCost = alwaysHitCost;
					} else {
						cacheCost = invokeReturnCost + nonLocalCacheCost;						
					}
				} else {
					GlobalAnalysis ga = new GlobalAnalysis(project);
					WcetCost allFitCost = null;
					try { allFitCost= ga.computeWCET(invoked, StaticCacheApproximation.ALL_FIT); }
					catch (Exception e) { throw new AssertionError(e); }
					cacheCost = returnCost + allFitCost.getCacheCost();
					nonLocalExecCost = allFitCost.getNonCacheCost();
				}				
			} else {
				cacheCost = invokeReturnCost + nonLocalCacheCost;				
			}
			cost.addNonLocalCost(nonLocalExecCost);
			cost.addCacheCost(cacheCost);
			logger.info("Recursive WCET computation: " + invoked.getMethod() +
					    ". cummulative cache cost: "+cacheCost+
					    " non local execution cost: "+nonLocalExecCost);
		}
	}

	private WcetCost 
		computeCostOfNode(CFGNode n,StaticCacheApproximation cacheMode) {	
		WcetVisitor wcetVisitor = new LocalWcetVisitor(project, cacheMode);
		n.accept(wcetVisitor);
		return wcetVisitor.cost;
	}
		
}
