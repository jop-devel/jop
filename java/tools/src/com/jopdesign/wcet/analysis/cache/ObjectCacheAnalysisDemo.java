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
package com.jopdesign.wcet.analysis.cache;

import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.code.CallString;
import com.jopdesign.common.code.ControlFlowGraph;
import com.jopdesign.common.code.ControlFlowGraph.BasicBlockNode;
import com.jopdesign.common.code.ControlFlowGraph.CFGNode;
import com.jopdesign.common.code.ControlFlowGraph.CfgVisitor;
import com.jopdesign.common.code.ExecutionContext;
import com.jopdesign.common.processormodel.JOPConfig;
import com.jopdesign.wcet.WCETTool;
import com.jopdesign.wcet.analysis.AnalysisContext;
import com.jopdesign.wcet.analysis.AnalysisContextCallString;
import com.jopdesign.wcet.analysis.InvalidFlowFactException;
import com.jopdesign.wcet.analysis.RecursiveAnalysis;
import com.jopdesign.wcet.analysis.RecursiveAnalysis.RecursiveStrategy;
import com.jopdesign.wcet.analysis.RecursiveWcetAnalysis;
import com.jopdesign.wcet.ipet.CostProvider;
import com.jopdesign.wcet.ipet.IPETBuilder;
import com.jopdesign.wcet.ipet.CostProvider.MapCostProvider;
import com.jopdesign.wcet.ipet.IPETConfig;
import com.jopdesign.wcet.jop.ObjectCache;

import org.apache.bcel.generic.InstructionHandle;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import lpsolve.LpSolveException;

/** A demonstration of the persistence analysis for the object cache
 *  <p>
 *  As we have not yet implemented unsharing (this is not as trivial as it sounds),
 *  we use, once again, a recursive analysis 
 *  </p><p>
 *  We compute a WCET problem, with the following cost model:
 *  A object handle access has cost 1, everything else is cost 0.
 *  Solve the problem once with and once without persistence analysis, and compare costs.
 *  </p>
 *  
 */
public class ObjectCacheAnalysisDemo {
	public static final int DEFAULT_SET_SIZE = 64;

	public class RecursiveOCacheAnalysis extends
			RecursiveAnalysis<AnalysisContext, ObjectCache.ObjectCacheCost> {

		private RecursiveStrategy<AnalysisContext, ObjectCache.ObjectCacheCost> recursiveStrategy;

		public RecursiveOCacheAnalysis(WCETTool p, IPETConfig ipetConfig,
				RecursiveStrategy<AnalysisContext, ObjectCache.ObjectCacheCost> recursiveStrategy) {
			super(p, ipetConfig);
			this.recursiveStrategy = recursiveStrategy;
		}
		@Override
		protected ObjectCache.ObjectCacheCost computeCostOfNode(CFGNode n, AnalysisContext ctx) {
			return new OCacheVisitor(this.getWCETTool(), this, recursiveStrategy, ctx).computeCost(n);
		}

		@Override
		protected CostProvider<CFGNode> getCostProvider(
				Map<CFGNode, ObjectCache.ObjectCacheCost> nodeCosts) {
			HashMap<CFGNode, Long> costMap = new HashMap<CFGNode, Long>();
			for(Entry<CFGNode, ObjectCache.ObjectCacheCost> entry : nodeCosts.entrySet()) {
				costMap.put(entry.getKey(),entry.getValue().getCost());
			}
			return new MapCostProvider<CFGNode>(costMap, 1000);
		}

		@Override
		protected ObjectCache.ObjectCacheCost extractSolution(ControlFlowGraph cfg,
				Map<CFGNode, ObjectCache.ObjectCacheCost> nodeCosts,
				long maxCost,
				Map<IPETBuilder.ExecutionEdge, Long> executionEdgeFlow) {
			Map <ControlFlowGraph.CFGEdge, Long> edgeFlow  = RecursiveWcetAnalysis.executionToProgramFlow(cfg.getGraph(), executionEdgeFlow);
			Map <CFGNode, Long> nodeFlow = RecursiveWcetAnalysis.edgeToNodeFlow(cfg.getGraph(),edgeFlow);
			ObjectCache.ObjectCacheCost ocCost = new ObjectCache.ObjectCacheCost();
			for(Entry<CFGNode, Long> entry : nodeFlow.entrySet()) {
				ocCost.addCost(nodeCosts.get(entry.getKey()).times(entry.getValue()));
			}
			if(maxCost != ocCost.getCost()) {
				throw new AssertionError(
						String.format("Object Cache Cost: Cost of lp solver (%d) and reconstructed cost (%d) do not coincide",maxCost,ocCost.getCost()));
			}
			return ocCost;
		}


	} 

	/** Visitor for computing the WCET of CFG nodes */
	private class OCacheVisitor implements CfgVisitor {
		private ObjectCache.ObjectCacheCost cost;
		private RecursiveAnalysis<AnalysisContext, ObjectCache.ObjectCacheCost> recursiveAnalysis;
		private RecursiveStrategy<AnalysisContext, ObjectCache.ObjectCacheCost> recursiveStrategy;
		private AnalysisContext context;
		private WCETTool project;

		public OCacheVisitor(
				WCETTool p,
				RecursiveAnalysis<AnalysisContext, ObjectCache.ObjectCacheCost> recursiveAnalysis,
				RecursiveStrategy<AnalysisContext, ObjectCache.ObjectCacheCost> recursiveStrategy, 
				AnalysisContext ctx
				) {
			this.project = p; 
			this.recursiveAnalysis = recursiveAnalysis;
			this.recursiveStrategy = recursiveStrategy;
			this.context = ctx;
		}
		// Cost ~ number of cache misses
		// TODO: A basic block is a scope too!
		public void visitBasicBlockNode(BasicBlockNode n) {
			long worstCaseMissCost = objectCache.getLoadBlockCycles();
			for(InstructionHandle ih : n.getBasicBlock().getInstructions()) {
				if(ObjectCacheAnalysis.getHandleType(project, n.getControlFlowGraph(), ih) == null) continue;
				if(! ObjectCacheAnalysis.isFieldCached(project, n.getControlFlowGraph(), ih, objectCache.getMaxCachedFieldIndex())) {
					cost.addBypassCost(worstCaseMissCost,1);
				} else {
					cost.addMissCost(worstCaseMissCost,1);
					cost.addAccessToCachedField(1); 
				}
			}
		} 

		public void visitInvokeNode(ControlFlowGraph.InvokeNode n) {
			visitBasicBlockNode(n);
			if(n.isVirtual()) {
				throw new AssertionError("Invoke node "+n.getReferenced()+" without implementation in WCET analysis - did you preprocess virtual methods ?");
			}
			cost.addCost(recursiveStrategy.recursiveCost(recursiveAnalysis, n, context));
		}

		public void visitVirtualNode(ControlFlowGraph.VirtualNode n) {
		}
		
		public void visitReturnNode(ControlFlowGraph.ReturnNode n) {			
		}

		public void visitSummaryNode(ControlFlowGraph.SummaryNode n) {
			ControlFlowGraph subCfg = n.getControlFlowGraph();
			cost.addCost(recursiveAnalysis.computeCostUncached(n.toString(), subCfg, this.context));
		}
		public ObjectCache.ObjectCacheCost computeCost(CFGNode n) {
			this.cost = new ObjectCache.ObjectCacheCost();
			n.accept(this);
			return cost;
		}
	}

	// Ok, a few notes what is probably incorrect at the moment:
	//  a) Cannot handle java implemented methods (I think)
	//  b) invokevirtual also accesses the object, this is not considered
	private class RecursiveWCETOCache
	implements RecursiveStrategy<AnalysisContext,ObjectCache.ObjectCacheCost> {
		public ObjectCache.ObjectCacheCost recursiveCost(
				RecursiveAnalysis<AnalysisContext,ObjectCache.ObjectCacheCost> stagedAnalysis,
				ControlFlowGraph.InvokeNode invocation,
				AnalysisContext ctx) {
			MethodInfo invoked = invocation.getImplementingMethod();
			ObjectCache.ObjectCacheCost cost;
			try {
				if(allPersistent(invoked, ctx.getCallString())) {
					cost  = getAllFitCost(invoked, ctx.getCallString());
				}
				//System.out.println("Cost for: "+invocation.getImplementedMethod()+" [all fit]: "+cost);
				else {
					// FIXME: callstring missing
					// AnalysisContext recCtx = ctx.withCallString(ctx.getCallString().push(invocation,project.getProjectConfig().callstringLength()));
					cost = stagedAnalysis.computeCost(invoked, ctx);
					//System.out.println("Cost for: "+invocation.getImplementedMethod()+" [recursive]: "+cost);
				}
			} catch (InvalidFlowFactException e) {
				throw new RuntimeException(e);
			} catch (LpSolveException e) {
				throw new RuntimeException(e);
			}
			return cost;
		}

	}

	private WCETTool project;
	private ObjectCacheAnalysis objRefAnalysis;
	private boolean assumeAllMiss;
	private ObjectCache objectCache;

	public ObjectCacheAnalysisDemo(WCETTool p, ObjectCache objectCache) {
		this.project = p;
		this.objectCache = objectCache;
		this.objRefAnalysis = new ObjectCacheAnalysis(project, objectCache);
	}
	
	public void setAssumeAlwaysMiss() {
		this.assumeAllMiss = true;
	}
	
	public ObjectCache.ObjectCacheCost computeCost() {
		/* Cache Analysis */
		RecursiveAnalysis<AnalysisContext, ObjectCache.ObjectCacheCost> recAna =
			new RecursiveOCacheAnalysis(project, new IPETConfig(project.getConfig()),
					new RecursiveWCETOCache());
		
		return recAna.computeCost(project.getTargetMethod(), new AnalysisContextCallString(CallString.EMPTY));
	}

	public long getMaxAccessedTags(MethodInfo invoked, CallString context) throws InvalidFlowFactException, LpSolveException {
		if(! context.isEmpty()) {
			throw new AssertionError("Callstrings are not yet supported for object cache analysis");
		}
		return objRefAnalysis.countDistinctCachedTagsAccessed(new ExecutionContext(invoked, context));
	}
 
	private ObjectCache.ObjectCacheCost getAllFitCost(MethodInfo invoked, CallString context) throws InvalidFlowFactException, LpSolveException {

		if(! context.isEmpty()) {
			throw new AssertionError("Callstrings are not yet supported for object cache analysis");
		}
		return objRefAnalysis.getMaxCacheCost(new ExecutionContext(invoked, context), objectCache.getCostModel());
	}


	private boolean allPersistent(MethodInfo invoked, CallString context) throws InvalidFlowFactException, LpSolveException {

		if(assumeAllMiss) return false;
		return getMaxAccessedTags(invoked, context) <= objectCache.getAssociativity();
	}		
	
}
