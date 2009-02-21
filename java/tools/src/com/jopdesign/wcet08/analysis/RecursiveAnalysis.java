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
import java.util.Map.Entry;

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
import com.jopdesign.wcet08.jop.MethodCache;
import com.jopdesign.wcet08.jop.CacheConfig.DynCacheApproximation;
import com.jopdesign.wcet08.jop.CacheConfig.StaticCacheApproximation;
import com.jopdesign.wcet08.report.ClassReport;

/**
 * Simple and fast local analysis, with the possibility to use more expensive analysis
 * methods (global IPET for miss-once fit-all, UPPAAL) for parts of the program.
 * 
 * @author Benedikt Huber (benedikt.huber@gmail.com)
 *
 */
public class RecursiveAnalysis<Context> {
	/** Used for configuring recursive WCET caluclation */
	public interface RecursiveWCETStrategy<Context> {
		public WcetCost recursiveWCET(RecursiveAnalysis<Context> stagedAnalysis, InvokeNode invocation, Context ctx);
	}
	/** Key for caching recursive WCET calculations */
	private class WcetKey {
		MethodInfo m;
		Context ctx;
		public WcetKey(MethodInfo m, Context mode) {
			this.m = m; this.ctx = mode;
		}
		@Override
		public boolean equals(Object that) {
			return (that instanceof RecursiveAnalysis.WcetKey) ? equalsKey((WcetKey) that) : false;
		}
		private boolean equalsKey(WcetKey key) {
			return this.m.equals(key.m) && (this.ctx.equals(key.ctx));
		}
		@Override
		public int hashCode() {
			return m.getFQMethodName().hashCode()+this.ctx.hashCode();
		}
		@Override
		public String toString() {
			return this.m.getFQMethodName()+"["+this.ctx+"]";
		}
	}
	/** Solution to local WCET problem */
	public class LocalWCETSolution {
		private long lpCost;
		private WcetCost cost;
		private Map<CFGNode,Long> nodeFlow;
		private Map<CFGEdge,Long> edgeFlow;
		private DirectedGraph<CFGNode, CFGEdge> graph;
		private Map<CFGNode, WcetCost> nodeCosts;
		public LocalWCETSolution(DirectedGraph<CFGNode,CFGEdge> g, Map<CFGNode,WcetCost> nodeCosts) { 
			this.graph = g;
			this.nodeCosts= nodeCosts;
		}
		public Map<CFGNode, WcetCost> getNodeCostMap() {
			return nodeCosts;
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
		public WcetCost getTotalCost() {
			WcetCost tCost = cost.clone();
			tCost.moveLocalToGlobalCost();
			return tCost;
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
				cost.addPotentialCacheFlushes((int)flow * nodeCosts.get(n).getPotentialCacheFlushes());
			}			
		}
	}
	/** Provide execution cost using a node->cost table */
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
	
	private static final Logger logger = Logger.getLogger(RecursiveAnalysis.class);
	private Project project;
	private WcetAppInfo appInfo;
	private Hashtable<WcetKey, WcetCost> wcetMap;
	private ILPModelBuilder modelBuilder;
	private ProcessorModel processor;
	private RecursiveWCETStrategy<Context> recursiveWCET;

	public RecursiveAnalysis(Project project, RecursiveWCETStrategy<Context> recursiveStrategy) {
		this.project = project;
		this.appInfo = project.getWcetAppInfo();
		this.processor = project.getWcetAppInfo().getProcessorModel();

		this.wcetMap = new Hashtable<WcetKey,WcetCost>();

		this.modelBuilder = new ILPModelBuilder(project);
		this.recursiveWCET = recursiveStrategy;
	}
	/**
	 * WCET analysis of the given method, using some strategy for recursive WCET calculation and cache
	 * approximation.cache approximation scheme.
	 * @param m the method to be analyzed
	 * @return
	 * 
	 * <p>FIXME: Logging/Report need to be cleaned up </p>
	 */
	public WcetCost computeWCET(MethodInfo m, Context ctx) {
		/* use memoization to speed up analysis */
		WcetKey key = new WcetKey(m,ctx);
		if(wcetMap.containsKey(key)) return wcetMap.get(key);
		/* compute solution */
		LocalWCETSolution sol = runWCETComputation(key.toString(), appInfo.getFlowGraph(m), ctx);
		sol.checkConsistentency();
		recordCost(key, sol.getCost());
		/* Logging and Report */
		if(project.reportGenerationActive()) updateReport(key, sol);
		return sol.getTotalCost();
	}
	
	private void updateReport(WcetKey key, LocalWCETSolution sol) {
		Map<CFGNode,WcetCost> nodeCosts = sol.getNodeCostMap();
		Hashtable<CFGNode, String> nodeFlowCostDescrs = new Hashtable<CFGNode, String>();
		MethodInfo m = key.m;
		for(Entry<CFGNode, WcetCost> entry: nodeCosts.entrySet()) {
			CFGNode n = entry.getKey();
			WcetCost cost = entry.getValue();
			if(sol.getNodeFlow(n) > 0) {
				nodeFlowCostDescrs.put(n,cost.toString());
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
		stats.put("mode",key.ctx);
		stats.put("all-methods-fit-in-cache",project.getProcessorModel().getMethodCache().allFit(m));
		project.getReport().addDetailedReport(m,"WCET_"+key.ctx.toString(),stats,nodeFlowCostDescrs,sol.getEdgeFlow());
	}

	/**
	 * Compute the WCET of the given control flow graph
	 * @param name name for the ILP problem
	 * @param cfg the control flow graph 
	 * @param ctx the context to use
	 * @return the WCET for the given CFG
	 */
	public LocalWCETSolution runWCETComputation(String name, ControlFlowGraph cfg, Context ctx) {		
		Map<CFGNode,WcetCost> nodeCosts = buildNodeCostMap(cfg,ctx);
		LocalWCETSolution sol = new LocalWCETSolution(cfg.getGraph(),nodeCosts);
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
		buildNodeCostMap(ControlFlowGraph fg,Context ctx) {
		
		HashMap<CFGNode, WcetCost> nodeCost = new HashMap<CFGNode,WcetCost>();
		for(CFGNode n : fg.getGraph().vertexSet()) {
			nodeCost.put(n, computeCostOfNode(n, ctx));
		}
		return nodeCost;
	}
	private WcetCost 
	computeCostOfNode(CFGNode n ,Context ctx) {	
		WcetVisitor wcetVisitor = new LocalWcetVisitor(project, ctx);
		n.accept(wcetVisitor);
		return wcetVisitor.cost;
	}
	
	private class LocalWcetVisitor extends WcetVisitor {
		Context ctx;
		public LocalWcetVisitor(Project project, Context ctx) {
			this.project = project;
			this.ctx = ctx;
		}
		@Override
		public void visitSummaryNode(SummaryNode n) {
			cost.addCost(runWCETComputation("summary",n.getSubGraph(),ctx).getCost());
		}
		@Override
		public void visitInvokeNode(InvokeNode n) {
			cost.addLocalCost(processor.getExecutionTime(n.getBasicBlock().getMethodInfo(),n.getInstructionHandle().getInstruction()));
			if(n.isInterface()) {
				throw new AssertionError("Invoke node "+n.getReferenced()+" without implementation in WCET analysis - did you preprocess virtual methods ?");
			}
			cost.addCost(RecursiveAnalysis.this.recursiveWCET.recursiveWCET(RecursiveAnalysis.this, n, ctx));
		}
	}

	public static class LocalIPETStrategy implements RecursiveWCETStrategy<StaticCacheApproximation> {
		public WcetCost recursiveWCET(
				RecursiveAnalysis<StaticCacheApproximation> stagedAnalysis,
				InvokeNode n, StaticCacheApproximation cacheMode) {
			if(cacheMode.needsInterProcIPET()) {
				throw new AssertionError("Ups. Cache Mode "+cacheMode+" not supported using local IPET strategy");
			}
			Project project = stagedAnalysis.project;
			MethodInfo invoker = n.getBasicBlock().getMethodInfo(); 
			MethodInfo invoked = n.getImplementedMethod();
			ProcessorModel proc = project.getProcessorModel();
			MethodCache cache = proc.getMethodCache();
			long cacheCost;
			WcetCost recCost = stagedAnalysis.computeWCET(invoked, cacheMode);
			long nonLocalExecCost = recCost.getCost() - recCost.getCacheCost();
			long nonLocalCacheCost = recCost.getCacheCost();
			long invokeReturnCost = cache.getInvokeReturnMissCost(
					proc,
					project.getFlowGraph(invoker),
	                project.getFlowGraph(invoked));
			if(! proc.hasMethodCache() || cacheMode == StaticCacheApproximation.ALWAYS_HIT) {
				cacheCost = 0;
			} else if(project.getCallGraph().isLeafNode(invoked)) {
				cacheCost = invokeReturnCost + nonLocalCacheCost;
			} else if(cacheMode == StaticCacheApproximation.ALL_FIT_LOCAL && cache.allFit(invoked)) {
				long returnCost = cache.getMissOnReturnCost(proc, project.getFlowGraph(invoker));
				/* Maybe its better not to apply the all-fit heuristic ... */
				long noAllFitCost = recCost.getCost() + invokeReturnCost;
				/* Compute cost without method cache */
				long alwaysHitCost = stagedAnalysis.computeWCET(invoked, StaticCacheApproximation.ALWAYS_HIT).getCost();
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
			} else { /* ALWAYS MISS or doesn't fit */
				cacheCost = invokeReturnCost + nonLocalCacheCost;				
			}
			WcetCost cost = new WcetCost();
			cost.addNonLocalCost(nonLocalExecCost);
			cost.addCacheCost(cacheCost);
			logger.info("Recursive WCET computation: " + invoked.getMethod() +
					    ". cummulative cache cost: "+cacheCost+
					    " non local execution cost: "+nonLocalExecCost);
			return cost;
		}
		
	}

	public Project getProject() {
		return project;
	}
	public void recordCost(MethodInfo invoked, Context ctx, WcetCost cost) {
		recordCost(new WcetKey(invoked,ctx),cost);		
	}
	private void recordCost(WcetKey key, WcetCost cost) {
		wcetMap.put(key, cost); 		
	}
	public boolean isCached(MethodInfo invoked, Context ctx) {
		return wcetMap.containsKey(new WcetKey(invoked,ctx));
	}
	public WcetCost getCached(MethodInfo invoked, Context ctx) {
		return wcetMap.get(new WcetKey(invoked,ctx));
	}
		
}
