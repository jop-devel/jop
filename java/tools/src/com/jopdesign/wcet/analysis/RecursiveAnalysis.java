package com.jopdesign.wcet.analysis;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import com.jopdesign.build.MethodInfo;
import com.jopdesign.wcet.Project;
import com.jopdesign.wcet.frontend.ControlFlowGraph;
import com.jopdesign.wcet.frontend.ControlFlowGraph.BasicBlockNode;
import com.jopdesign.wcet.frontend.ControlFlowGraph.CFGEdge;
import com.jopdesign.wcet.frontend.ControlFlowGraph.CFGNode;
import com.jopdesign.wcet.frontend.ControlFlowGraph.InvokeNode;
import com.jopdesign.wcet.frontend.ControlFlowGraph.SummaryNode;
import com.jopdesign.wcet.ipet.ILPModelBuilder;
import com.jopdesign.wcet.ipet.IpetConfig;
import com.jopdesign.wcet.ipet.MaxCostFlow;
import com.jopdesign.wcet.ipet.ILPModelBuilder.CostProvider;

/**
 * Class for recursive maximization problems.
 * Generalizes some concepts also useful outside the actual WCET analysis.
 *
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 *
 * @param <Context> Different Contexts may lead to different results.
 *                  Recomputation with the same context is cached.
 * @param <Rval>       Type of the thing being computed (e.g., WcetCost, long) etc.
 */
public abstract class RecursiveAnalysis<Context extends AnalysisContext, Rval> {

	/** Used for configuring recursive WCET caluclation */
	public interface RecursiveStrategy<Context extends AnalysisContext, Rval> {

		public Rval recursiveCost(RecursiveAnalysis<Context,Rval> stagedAnalysis,
				InvokeNode invocation,
				Context ctx);
	}
	
	/** Key for caching recursive calculations */
	protected class CacheKey {
		MethodInfo m;
		Context ctx;
		public CacheKey(MethodInfo m, Context mode) {
			this.m = m; this.ctx = mode;
		}

		public int hashCode() {
			final int prime = 31;
			int result = prime * m.getFQMethodName().hashCode();
			result = prime * result + ctx.hashCode();
			return result;
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			RecursiveAnalysis.CacheKey other = (RecursiveAnalysis.CacheKey) obj;
			if (!ctx.equals(other.ctx)) return false;
			if (!m.equals(other.m)) return false;
			return true;
		}

		@Override
		public String toString() {
			return this.m.getFQMethodName()+"["+this.ctx.hashCode()+"]";
		}
	}

	private Project project;
	private Hashtable<CacheKey, Rval> costMap;
	private ILPModelBuilder modelBuilder;

	public Project getProject() {
		return project;
	}

	public RecursiveAnalysis(Project p, IpetConfig ipetConfig) {
		this.project = p;
		this.costMap = new Hashtable<CacheKey,Rval>();
		this.modelBuilder = new ILPModelBuilder(ipetConfig);
	}

	public Rval computeCost(MethodInfo m, Context ctx) {
		/* use memoization to speed up analysis */
		CacheKey key = new CacheKey(m,ctx);
		if(isCached(key)) return getCached(key);

		/* compute solution */
		Rval rval = computeCostUncached(key.toString(), project.getFlowGraph(m), ctx);
		recordCost(key, rval);
		return rval;
	}

	public Rval computeCostUncached(String ilpName, ControlFlowGraph cfg, Context ctx) {
		Map<CFGNode, Rval> nodeCosts = buildNodeCostMap(cfg,ctx);
		CostProvider<CFGNode> costProvider = getCostProvider(nodeCosts);

		Map<CFGEdge, Long> edgeFlowOut = new HashMap<CFGEdge, Long>();
		long maxCost = runLocalComputation(ilpName, cfg, ctx, costProvider, edgeFlowOut);
		return extractSolution(cfg, nodeCosts, maxCost, edgeFlowOut);
	}

	protected abstract Rval extractSolution(
			ControlFlowGraph cfg,
			Map<CFGNode, Rval> nodeCosts,
			long maxCost,
			Map<CFGEdge, Long> edgeFlowOut);

	protected abstract CostProvider<CFGNode> getCostProvider(Map<CFGNode, Rval> nodeCosts);

	protected abstract Rval computeCostOfNode(CFGNode n, Context ctx);

	/**
	 * Compute the cost of the given control flow graph, using a local ILP
	 * @param name name for the ILP problem
	 * @param cfg the control flow graph
	 * @param ctx the context to use
	 * @return the cost for the given CFG
	 */
	public long runLocalComputation(
			String name, 
			ControlFlowGraph cfg, 
			Context ctx,
			CostProvider<CFGNode> costProvider,
			Map<CFGEdge, Long> edgeFlowOut) {
		MaxCostFlow<CFGNode,CFGEdge> problem =
			modelBuilder.buildLocalILPModel(name,cfg, costProvider);
		/* solve ILP */
		/* extract node flow, local cost, cache cost, cummulative cost */
		long maxCost = 0;
		try {
			maxCost = Math.round(problem.solve(edgeFlowOut));
		} catch (Exception e) {
			throw new Error("Failed to solve LP problem: "+e,e);
		}
		return maxCost;
	}

	/**
	 * map flowgraph nodes to costs
	 * If the node is a invoke, we need to compute the cost for the invoked method
	 * otherwise, just take the basic block cost
	 * @param cfg the target flowgraph
	 * @param context the cost computation context
	 * @return
	 */
	public Map<CFGNode, Rval>
		buildNodeCostMap(ControlFlowGraph fg,Context ctx) {

		HashMap<CFGNode, Rval> nodeCost = new HashMap<CFGNode,Rval>();
		for(CFGNode n : fg.getGraph().vertexSet()) {
			nodeCost.put(n, computeCostOfNode(n, ctx));
		}
		return nodeCost;
	}

	public void recordCost(MethodInfo invoked, Context ctx, Rval cost) {
		recordCost(new CacheKey(invoked,ctx),cost);
	}
	protected void recordCost(CacheKey key, Rval cost) {
		costMap.put(key, cost);
	}
	protected boolean isCached(CacheKey key) {
		return costMap.containsKey(key);
	}
	public boolean isCached(MethodInfo invoked, Context ctx) {
		return isCached(new CacheKey(invoked,ctx));
	}
	protected Rval getCached(CacheKey cacheKey) {
		return costMap.get(cacheKey);
	}
	public Rval getCached(MethodInfo invoked, Context ctx) {
		return getCached(new CacheKey(invoked,ctx));
	}

}
