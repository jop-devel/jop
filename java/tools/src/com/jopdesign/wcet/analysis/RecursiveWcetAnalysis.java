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
package com.jopdesign.wcet.analysis;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.jgrapht.DirectedGraph;

import com.jopdesign.build.ClassInfo;
import com.jopdesign.build.MethodInfo;
import com.jopdesign.wcet.ProcessorModel;
import com.jopdesign.wcet.Project;
import com.jopdesign.wcet.frontend.BasicBlock;
import com.jopdesign.wcet.frontend.ControlFlowGraph;
import com.jopdesign.wcet.frontend.WcetAppInfo;
import com.jopdesign.wcet.frontend.ControlFlowGraph.BasicBlockNode;
import com.jopdesign.wcet.frontend.ControlFlowGraph.CFGEdge;
import com.jopdesign.wcet.frontend.ControlFlowGraph.CFGNode;
import com.jopdesign.wcet.frontend.ControlFlowGraph.InvokeNode;
import com.jopdesign.wcet.frontend.ControlFlowGraph.SummaryNode;
import com.jopdesign.wcet.ipet.IpetConfig;
import com.jopdesign.wcet.ipet.ILPModelBuilder.CostProvider;
import com.jopdesign.wcet.report.ClassReport;

/**
 * Simple and fast local analysis, with the possibility to use more expensive analysis
 * methods (global IPET for miss-once fit-all, UPPAAL) for parts of the program.
 *
 * @author Benedikt Huber (benedikt.huber@gmail.com)
 *
 */

public class RecursiveWcetAnalysis<Context extends AnalysisContext>
             extends RecursiveAnalysis<Context, WcetCost> {

	/** Visitor for computing the WCET of CFG nodes */
	private class LocalWcetVisitor extends WcetVisitor {
		Context ctx;
		public LocalWcetVisitor(Project project, Context ctx) {
			super(project);
			this.ctx = ctx;
		}
		@Override
		public void visitSummaryNode(SummaryNode n) {
			cost.addCost(runWCETComputation("summary",n.getSubGraph(),ctx).getCost());
		}
		@Override
		public void visitInvokeNode(InvokeNode n) {
			cost.addLocalCost(processor.getExecutionTime(ctx.getExecutionContext(n),n.getInstructionHandle()));
			if(n.isInterface()) {
				throw new AssertionError("Invoke node "+n.getReferenced()+" without implementation in WCET analysis - did you preprocess virtual methods ?");
			}
			cost.addCost(RecursiveWcetAnalysis.this.recursiveWCET.recursiveCost(RecursiveWcetAnalysis.this, n, ctx));
		}
		@Override
		public void visitBasicBlockNode(BasicBlockNode n) {
			cost.addLocalCost(project.getProcessorModel().basicBlockWCET(ctx.getExecutionContext(n),n.getBasicBlock()));
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
	/** Provide execution cost using a node->cost table
	 */
	public static class WcetCostProvider<T> implements CostProvider<T> {
		private Map<T, WcetCost> costMap;
		private WcetCost defCost;
		public WcetCostProvider(Map<T,WcetCost> costMap) {
			this.costMap = costMap;
			this.defCost = null;
		}
		public WcetCostProvider(Map<T,WcetCost> costMap, WcetCost defCost) {
			this.costMap = costMap;
			this.defCost = defCost;
		}
		public long getCost(T obj) {
			WcetCost cost = costMap.get(obj);
			if(cost == null) {
				if(defCost == null) {
					throw new AssertionError("Missing entry for "+obj+" in cost map");
				} else {
					return defCost.getCost();
				}
			} else {
				return cost.getCost();
			}
		}
	}

	static final Logger logger = Logger.getLogger(RecursiveWcetAnalysis.class);
	private WcetAppInfo appInfo;
	private ProcessorModel processor;
	private RecursiveAnalysis.RecursiveStrategy<Context, WcetCost> recursiveWCET;

	public RecursiveWcetAnalysis(Project project,
			RecursiveAnalysis.RecursiveStrategy<Context, WcetCost> recursiveStrategy) {
		this(project, new IpetConfig(project.getConfig()), recursiveStrategy);
	}
	public RecursiveWcetAnalysis(Project project,
			                 IpetConfig ipetConfig,
			                 RecursiveAnalysis.RecursiveStrategy<Context,WcetCost> recursiveStrategy) {
		super(project, ipetConfig);
		this.appInfo = project.getWcetAppInfo();
		this.processor = project.getWcetAppInfo().getProcessorModel();

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
	public WcetCost computeCost(MethodInfo m, Context ctx) {
		/* use memoization to speed up analysis */
		CacheKey key = new CacheKey(m,ctx);
		if(super.isCached(key)) return super.getCached(key);
		/* compute solution */
		LocalWCETSolution sol = runWCETComputation(key.toString(), appInfo.getFlowGraph(m), ctx);
		sol.checkConsistentency();
		recordCost(key, sol.getCost());
		/* Logging and Report */
		if(getProject().reportGenerationActive()) {
			logger.info("Report generation active: "+m+" in context "+ctx);
			updateReport(key, sol);
		}
		return sol.getTotalCost();
	}

	public LocalWCETSolution runWCETComputation(
			String key,
			ControlFlowGraph cfg, 
			Context ctx) {
		Map<CFGNode,WcetCost> nodeCosts = buildNodeCostMap(cfg,ctx);
		CostProvider<CFGNode> costProvider = getCostProvider(nodeCosts);

		Map<CFGEdge, Long> edgeFlowOut = new HashMap<CFGEdge, Long>();
		long maxCost = runLocalComputation(key, cfg, ctx, costProvider, edgeFlowOut );
		LocalWCETSolution sol = new LocalWCETSolution(cfg.getGraph(),nodeCosts);
		sol.setSolution(maxCost, edgeFlowOut);
		return sol;
	}
	
	// FIXME: [recursive-wet-analysis] Report generation is a big mess
	private void updateReport(CacheKey key, LocalWCETSolution sol) {
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
					TreeSet<Integer> lineRange = basicBlock.getSourceLineRange();
					if(lineRange.isEmpty()) {
						Project.logger.error("No source code lines associated with basic block ! ");
					}
					ClassInfo cli = basicBlock.getClassInfo();
					ClassReport cr = getProject().getReport().getClassReport(cli);
					Long oldCost = (Long) cr.getLineProperty(lineRange.first(), "cost");
					if(oldCost == null) oldCost = 0L;
					long newCost = sol.getNodeFlow(n)*nodeCosts.get(n).getCost();
					Project.logger.debug("Attaching cost "+oldCost + " + "+newCost+" to line "+lineRange.first());
					cr.addLineProperty(lineRange.first(), "cost", oldCost + newCost);
					for(int i : lineRange) {
						cr.addLineProperty(i, "color", "red");
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
		stats.put("all-methods-fit-in-cache",getProject().getProcessorModel().getMethodCache().allFit(m));
		getProject().getReport().addDetailedReport(m,"WCET_"+key.ctx.toString(),stats,nodeFlowCostDescrs,sol.getEdgeFlow());
	}
	@Override
	public WcetCost computeCostOfNode(CFGNode n ,Context ctx) {
		WcetVisitor wcetVisitor = new LocalWcetVisitor(getProject(), ctx);
		return wcetVisitor.computeCost(n);
	}

	@Override
	protected CostProvider<CFGNode> getCostProvider(
			Map<CFGNode, WcetCost> nodeCosts) {
		return new WcetCostProvider<CFGNode>(nodeCosts);
	}

	// currently unused
	@Override
	protected WcetCost extractSolution(ControlFlowGraph cfg,
			Map<CFGNode, WcetCost> nodeCosts,
			long maxCost,
			Map<CFGEdge, Long> edgeFlowOut) {
		LocalWCETSolution sol = new LocalWCETSolution(cfg.getGraph(),nodeCosts);
		sol.setSolution(maxCost, edgeFlowOut);
		return sol.getCost();
	}

}
