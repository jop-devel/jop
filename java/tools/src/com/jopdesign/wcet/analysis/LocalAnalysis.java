/*
 * This file is part of JOP, the Java Optimized Processor
 * see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2010, Benedikt Huber (benedikt.huber@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jopdesign.wcet.analysis;

import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.code.ControlFlowGraph;
import com.jopdesign.wcet.ProcessorModel;
import com.jopdesign.wcet.Project;
import com.jopdesign.wcet.analysis.RecursiveAnalysis.RecursiveStrategy;
import com.jopdesign.wcet.ipet.IPETConfig;
import com.jopdesign.wcet.ipet.IPETConfig.StaticCacheApproximation;
import com.jopdesign.wcet.jop.MethodCache;

public class LocalAnalysis 
implements RecursiveStrategy<AnalysisContextLocal,WcetCost> {
	private boolean assumeMissOnceOnInvoke;
	private int maxCallstringLength;

	public LocalAnalysis(Project p, IPETConfig ipetConfig) {
		this.assumeMissOnceOnInvoke = ipetConfig.assumeMissOnceOnInvoke;
		this.maxCallstringLength = (int)p.getProjectConfig().callstringLength();
	}
	public LocalAnalysis() {
		this.assumeMissOnceOnInvoke = false;
	}
	public WcetCost recursiveCost(
			RecursiveAnalysis<AnalysisContextLocal,WcetCost> stagedAnalysis,
			ControlFlowGraph.InvokeNode n,
			AnalysisContextLocal ctx) {
		StaticCacheApproximation cacheMode = ctx.getCacheApproxMode();
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
		} else if(project.getCallGraph().isLeafMethod(invoked)) {
			cacheCost = invokeReturnCost + nonLocalCacheCost;
		} else if(cacheMode == StaticCacheApproximation.ALL_FIT_SIMPLE && cache.allFit(invoked,ctx.getCallString())) {
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
		RecursiveWcetAnalysis.logger.info("Recursive WCET computation: " + invoked +
				". invoke return cache cost: " + invokeReturnCost+
				". non-local cache cost: "    + nonLocalCacheCost+
				". cummulative cache cost: "+cacheCost+
				" non local execution cost: "+nonLocalExecCost);
		return cost;
	}

}
