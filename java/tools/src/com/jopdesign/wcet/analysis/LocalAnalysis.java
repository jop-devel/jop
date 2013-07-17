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

import java.util.EnumSet;

import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.code.CallString;
import com.jopdesign.common.code.ControlFlowGraph;
import com.jopdesign.common.code.Segment;
import com.jopdesign.wcet.WCETProcessorModel;
import com.jopdesign.wcet.WCETTool;
import com.jopdesign.wcet.analysis.RecursiveAnalysis.RecursiveStrategy;
import com.jopdesign.wcet.analysis.cache.CachePersistenceAnalysis;
import com.jopdesign.wcet.analysis.cache.CachePersistenceAnalysis.PersistenceCheck;
import com.jopdesign.wcet.analysis.cache.MethodCacheAnalysis;
import com.jopdesign.wcet.ipet.IPETConfig;
import com.jopdesign.wcet.ipet.IPETConfig.CacheCostCalculationMethod;
import org.apache.log4j.Logger;

public class LocalAnalysis 
implements RecursiveStrategy<AnalysisContextLocal,WcetCost> {
	private boolean assumeMissOnceOnInvoke;
	private int maxCallstringLength;

    private static final Logger logger = Logger.getLogger(WCETTool.LOG_WCET_ANALYSIS+".LocalAnalysis");

	public LocalAnalysis(WCETTool p, IPETConfig ipetConfig) {
		
		this(ipetConfig.doAssumeMissOnceOnInvoke(), p.getCallstringLength());
	}
	
	public LocalAnalysis() {
		
		this(false, 0);
	}
	
	public LocalAnalysis(boolean doAssumeMissOnceOnInvoke, int callstringLength) {

		this.assumeMissOnceOnInvoke = doAssumeMissOnceOnInvoke;
		this.maxCallstringLength = callstringLength;
	}

	public WcetCost recursiveCost(
			RecursiveAnalysis<AnalysisContextLocal,WcetCost> stagedAnalysis,
			ControlFlowGraph.InvokeNode n,
			AnalysisContextLocal ctx) {
		
		CacheCostCalculationMethod cacheMode = ctx.getCacheApproxMode();
		if(cacheMode.needsInterProcIPET()) {
			throw new AssertionError("Error: Cache Mode "+cacheMode+" not supported using local IPET strategy - " +
					"it needs an interprocedural IPET analysis");
		}
		
		WCETTool project   = stagedAnalysis.getWCETTool();
		MethodInfo invoker = n.getBasicBlock().getMethodInfo();
		MethodInfo invoked = n.getImplementingMethod();
		WCETProcessorModel proc = project.getWCETProcessorModel();
		MethodCacheAnalysis mca = new MethodCacheAnalysis(project);
		
		long cacheCost;
		AnalysisContextLocal recCtx = ctx.withCallString(ctx.getCallString().push(n,maxCallstringLength));
		WcetCost recCost = stagedAnalysis.computeCost(invoked, recCtx);
		
		
		long nonLocalExecCost = recCost.getCost() - recCost.getCacheCost();
		long nonLocalCacheCost = recCost.getCacheCost();
		long invokeReturnCost = mca.getInvokeReturnMissCost(n.getInvokeSite(), ctx.getCallString());
						
		if(proc.getMethodCache().getNumBlocks() == 0 || cacheMode == CacheCostCalculationMethod.ALWAYS_HIT) {
			cacheCost = 0;
		}
		
		else if(project.getCallGraph().isLeafMethod(invoked)) {
			cacheCost = invokeReturnCost + nonLocalCacheCost;
		}
		
		else if(cacheMode == CacheCostCalculationMethod.ALL_FIT_SIMPLE && allFit(project, invoked,recCtx.getCallString())) {
			
			long returnCost = mca.getMissOnceCost(invoker, false);

			/* Maybe its better not to apply the all-fit heuristic ... */
			long noAllFitCost = recCost.getCost() + invokeReturnCost;
			
			/* Compute cost without method cache */
			AnalysisContextLocal ahCtx = recCtx.withCacheApprox(CacheCostCalculationMethod.ALWAYS_HIT);
			long alwaysHitCost = stagedAnalysis.computeCost(invoked, ahCtx).getCost();
			
			/* Compute penalty for loading each method exactly once */
			long allFitPenalty = mca.getMissOnceCummulativeCacheCost(invoked,assumeMissOnceOnInvoke);
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
		} 
		
		else { /* ALWAYS MISS or doesn't fit */
			cacheCost = invokeReturnCost + nonLocalCacheCost;
		}
		WcetCost cost = new WcetCost();
		cost.addNonLocalCost(nonLocalExecCost);
		cost.addCacheCost(cacheCost);
		logger.debug("Recursive WCET computation: " + invoked +
				". invoke return cache cost: " + invokeReturnCost+
				". non-local cache cost: "    + nonLocalCacheCost+
				". cummulative cache cost: "+cacheCost+
				" non local execution cost: "+nonLocalExecCost);
		return cost;
	}

	protected boolean allFit(WCETTool wcetTool, MethodInfo invoked,CallString callString) {

		return new MethodCacheAnalysis(wcetTool).
				isPersistenceRegion(wcetTool, invoked, callString, EnumSet.of(PersistenceCheck.CountTotal));
	}
}
