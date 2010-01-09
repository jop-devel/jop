/**
 *
 */
package com.jopdesign.wcet.analysis;

import com.jopdesign.build.MethodInfo;
import com.jopdesign.wcet.ProcessorModel;
import com.jopdesign.wcet.Project;
import com.jopdesign.wcet.analysis.RecursiveAnalysis.RecursiveStrategy;
import com.jopdesign.wcet.frontend.ControlFlowGraph.InvokeNode;
import com.jopdesign.wcet.ipet.IpetConfig;
import com.jopdesign.wcet.ipet.IpetConfig.StaticCacheApproximation;
import com.jopdesign.wcet.jop.MethodCache;

public class LocalAnalysis 
implements RecursiveStrategy<AnalysisContextLocal,WcetCost> {
	private boolean assumeMissOnceOnInvoke;
	private int maxCallstringLength;

	public LocalAnalysis(Project p, IpetConfig ipetConfig) {
		this.assumeMissOnceOnInvoke = ipetConfig.assumeMissOnceOnInvoke;
		this.maxCallstringLength = p.getProjectConfig().callstringLength();
	}
	public LocalAnalysis() {
		this.assumeMissOnceOnInvoke = false;
	}
	public WcetCost recursiveCost(
			RecursiveAnalysis<AnalysisContextLocal,WcetCost> stagedAnalysis,
			InvokeNode n,
			AnalysisContextLocal ctx) {
		StaticCacheApproximation cacheMode = ctx.cacheApprox;
		if(cacheMode.needsInterProcIPET()) {
			throw new AssertionError("Error: Cache Mode "+cacheMode+" not supported using local IPET strategy - " +
					"it needs an interprocedural IPET analysis");
		}
		Project project    = stagedAnalysis.getProject();
		MethodInfo invoker = n.getBasicBlock().getMethodInfo();
		MethodInfo invoked = n.getImplementedMethod();
		ProcessorModel proc = project.getProcessorModel();
		MethodCache cache = proc.getMethodCache();
		long cacheCost;
		AnalysisContextLocal recCtx = ctx.withCallString(ctx.getCallString().push(n,maxCallstringLength));
		WcetCost recCost = stagedAnalysis.computeCost(invoked, recCtx);
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
		} else if(cacheMode == StaticCacheApproximation.ALL_FIT_SIMPLE && cache.allFit(invoked)) {
			long returnCost = cache.getMissOnReturnCost(proc, project.getFlowGraph(invoker));
			/* Maybe its better not to apply the all-fit heuristic ... */
			long noAllFitCost = recCost.getCost() + invokeReturnCost;
			/* Compute cost without method cache */
			AnalysisContextLocal ahCtx = recCtx.withCacheApprox(StaticCacheApproximation.ALWAYS_HIT);
			long alwaysHitCost = stagedAnalysis.computeCost(invoked, ahCtx).getCost();
			/* Compute penalty for loading each method exactly once */
			long allFitPenalty = cache.getMissOnceCummulativeCacheCost(invoked,assumeMissOnceOnInvoke);
			long allFitCacheCost = allFitPenalty  + returnCost;
			/* Cost All-Fit: recursive + penalty for loading once + return to caller */
			long allFitCost = alwaysHitCost + allFitCacheCost;

			// System.out.println(String.format("Method: %s, alwaysHitCost: %d, allFitCost: %d, noAllFitCost: %d(%d+%d)",
			//		invoked.getFQMethodName(), alwaysHitCost, allFitCost, noAllFitCost,recCost.getCost(),invokeReturnCost));

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
		RecursiveWcetAnalysis.logger.info("Recursive WCET computation: " + invoked.getMethod() +
				". invoke return cache cost: " + invokeReturnCost+
				". non-local cache cost: "    + nonLocalCacheCost+
				". cummulative cache cost: "+cacheCost+
				" non local execution cost: "+nonLocalExecCost);
		return cost;
	}

}
