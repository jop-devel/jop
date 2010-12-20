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
import com.jopdesign.common.code.CallString;
import com.jopdesign.common.code.ControlFlowGraph;
import com.jopdesign.common.code.ControlFlowGraph.BasicBlockNode;
import com.jopdesign.common.code.ControlFlowGraph.CFGNode;
import com.jopdesign.common.code.SuperGraph;
import com.jopdesign.common.code.SuperGraphNode;
import com.jopdesign.wcet.WCETProcessorModel;
import com.jopdesign.wcet.Project;
import com.jopdesign.wcet.analysis.RecursiveAnalysis.RecursiveStrategy;
import com.jopdesign.wcet.analysis.cache.MethodCacheAnalysis;
import com.jopdesign.wcet.ipet.IPETBuilder;
import com.jopdesign.wcet.ipet.IPETBuilder.ExecutionEdge;
import com.jopdesign.wcet.ipet.IPETConfig;
import com.jopdesign.wcet.ipet.IPETConfig.StaticCacheApproximation;
import com.jopdesign.wcet.ipet.IPETSolver;
import com.jopdesign.wcet.ipet.IPETUtils;
import com.jopdesign.wcet.ipet.LinearConstraint;
import com.jopdesign.wcet.ipet.LinearConstraint.ConstraintType;
import com.jopdesign.wcet.jop.MethodCache;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Global IPET-based analysis, supporting variable block caches (all fit region approximation).
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 *
 */
public class GlobalAnalysis {
		
	private Project project;

	private IPETConfig ipetConfig;
	
	public GlobalAnalysis(Project p, IPETConfig ipetConfig) {
		this.ipetConfig = ipetConfig;
		this.project = p;
	}
	
	/** Compute WCET using global IPET, and either ALWAYS_MISS or GLOBAL_ALL_FIT 
	 *  TODO: Refactor and generalize cache analysis
	 * */
	public WcetCost computeWCET(MethodInfo m, AnalysisContextLocal ctx) throws Exception {

		StaticCacheApproximation cacheMode = ctx.getCacheApproxMode();
		if(cacheMode != StaticCacheApproximation.ALWAYS_MISS &&
		   cacheMode != StaticCacheApproximation.GLOBAL_ALL_FIT) {
			throw new Exception("Global IPET: only ALWAYS_MISS and GLOBAL_ALL_FIT are supported"+
					            " as cache approximation strategies");
		}

		String key = m.getFQMethodName() + "_global_" + cacheMode;
		SuperGraph sg = new SuperGraph(project.getAppInfo(),
									   project.getFlowGraph(m),
									   project.getProjectConfig().callstringLength(),
									   ctx.getCallString());		

		/* Dump supergraph in debug mode */
		if(project.getProjectConfig().isDebugMode()) {
			try {
				FileWriter fw = new FileWriter(project.getProjectConfig().getOutFile("graphs", key+".dot"));
				sg.exportDOT(fw);
				fw.close();
			} catch(IOException ex) {
				ex.printStackTrace();
			}
		}
		
		/* create an IPET problem for all reachable methods */
		IPETSolver ipetSolver = buildIpetProblem(key, sg, ipetConfig);
		
		/* compute cost */
		setExecutionCost(sg, cacheMode, ipetSolver);
		
		/* Add constraints for cache */
		Set<ExecutionEdge> missEdges = new HashSet<ExecutionEdge>();
		if(cacheMode == StaticCacheApproximation.GLOBAL_ALL_FIT) {
			missEdges = addMissOnceCost(sg, ipetSolver);
		}

		/* Return variables */
		Map<ExecutionEdge, Long> flowMap = new HashMap<ExecutionEdge, Long>();
		
		/* Solve */
		double lpCost = ipetSolver.solve(flowMap);

		/* Cost extraction */
		WcetCost cost = new WcetCost();
		//System.err.println("=== Cost Summary ===");
		for(Entry<ExecutionEdge, Long> flowEntry : flowMap.entrySet()) {
			ExecutionEdge edge = flowEntry.getKey();
			long edgeCost = ipetSolver.getEdgeCost(edge);
			long flowCost = edgeCost * flowEntry.getValue();
			if(missEdges.contains(edge)) {
				//if(flowCost > 0) System.err.println("Cache Cost: "+ edge + " = " + flowCost);
				cost.addCacheCost(flowCost);
			} else {
				//if(flowCost > 0) System.err.println("Execution Cost: "+ edge + " = " + flowCost);
				cost.addNonLocalCost(flowCost);				
			}
		}
		
		/* Sanity Check, and Return */
		long objValue = (long) (lpCost+0.5);
		if(cost.getCost() != objValue) {
			throw new AssertionError("[GlobalAnalysis] Inconsistency: lpValue vs. extracted value: "+objValue+" / "+cost.getCost());
		}
		return cost;
	}

	/**
	 * Create an interprocedural max-cost max-flow problem for the given supergraph<br/>
	 * Notes:<ul>
	 * <li/> super graph edges always have the callstring of the invoking method
	 * </ul>
	 * 
	 * @param problemName A unique identifier for the problem (for reporting)
	 * @param sg       The supergraph to build the ILP for
	 * @param ipetConfig Cost of nodes (or {@code null} if no cost is associated with nodes)
	 * @return The max-cost maxflow problem
	 */
	public static IPETSolver buildIpetProblem(String problemName, SuperGraph sg, IPETConfig ipetConfig) {

		IPETSolver ipetSolver = new IPETSolver(problemName, ipetConfig);
		
		IPETBuilder<SuperGraph.CallContext> ipetBuilder = new IPETBuilder<SuperGraph.CallContext>(sg.getRootNode().getContext());

		for(SuperGraphNode n : sg.getSuperGraphNodes()) {

			ControlFlowGraph cfg = n.getCfg();
			ipetBuilder.changeContext(n.getContext());
			
			if(n.equals(sg.getRootNode())) {
				/* Root node : inflow(entry) = outflow(exit) = 1 */
				ipetSolver.addConstraints(IPETUtils.structuralFlowConstraintsRoot(cfg.getGraph(), ipetBuilder));
			} else {
				/* Inner node: inputEdges = outputEdges = flow(superReturnEdges which have super graph node as source) */
				List<ExecutionEdge> invokeEdges = new ArrayList<ExecutionEdge>();
				for(SuperGraph.SuperGraphEdge e: sg.incomingInvokeEdgesOf(n)) {
					invokeEdges.add(ipetBuilder.newEdgeInContext(e, e.getCallContext()));
				}
				ipetSolver.addConstraints(IPETUtils.structuralFlowConstraints(cfg.getGraph(), invokeEdges, invokeEdges, ipetBuilder));
			}
			/* Flow constraints */
			ipetSolver.addConstraints(IPETUtils.loopBoundConstraints(n.getCfg(), ipetBuilder));
			ipetSolver.addConstraints(IPETUtils.infeasibleEdgeConstraints(n.getCfg(), ipetBuilder));

		}

		/* Constraints for invoke/return super edge pairs */
		for(Entry<SuperGraph.SuperInvokeEdge,SuperGraph.SuperReturnEdge> superEdgePair : sg.getSuperEdgePairs().entrySet()) {
			ipetBuilder.changeContext(superEdgePair.getKey().getCallContext());
			ipetSolver.addConstraints(IPETUtils.invokeReturnConstraints(superEdgePair.getKey(), superEdgePair.getValue(), ipetBuilder));
		}
		
		return ipetSolver;
	}
	
	
	/**
	 * Compute the execution time of each edge in in the supergraph
	 * @param sg the supergraph, whose vertices are considered
	 * @param ipetInst the IPET instance
	 * @return the cost map
	 */
	private Map<ExecutionEdge, WcetCost> setExecutionCost(SuperGraph sg, StaticCacheApproximation approx, IPETSolver ipetInst) {

		HashMap<ExecutionEdge, WcetCost> edgeCost = new HashMap<ExecutionEdge,WcetCost>();
		boolean alwaysMiss = (approx == StaticCacheApproximation.ALWAYS_MISS);

		IPETBuilder<SuperGraph.CallContext> ipetBuilder = new IPETBuilder<SuperGraph.CallContext>(new SuperGraph.CallContext(CallString.EMPTY));

		for(SuperGraphNode n : sg.getGraph().vertexSet()) {
			ipetBuilder.changeContext(n.getContext());

			// FIXME: There is a discrepancy but also overlap between analysis contexts and execution contexts
			AnalysisContextLocal aCtx = new AnalysisContextLocal(approx, n.getCallString());
			GlobalVisitor visitor = new GlobalVisitor(project, aCtx, alwaysMiss);
			
			/* For each CFG instance, consider CFG nodes
			 * Currently there is no need to attribute cost to callsites */
			for(CFGNode cfgNode : n.getCfg().getGraph().vertexSet()) {
				WcetCost cost = visitor.computeCost(cfgNode);
				for(ControlFlowGraph.CFGEdge edge : n.getCfg().getGraph().outgoingEdgesOf(cfgNode)) {
					ExecutionEdge e = ipetBuilder.newEdge(edge);
					edgeCost.put(e, cost);
					ipetInst.addEdgeCost(e, cost.getCost());
				}
			}
		}
		return edgeCost;
	}
	
	/* add cost for missing each method once (ALL FIT) */
	private Set<ExecutionEdge> addMissOnceCost(SuperGraph sg, IPETSolver ipetSolver) {
		/* collect access sites */
		
		Map<MethodInfo, List<SuperGraph.SuperGraphEdge>> accessEdges = getMethodSwitchEdges(sg);
		MethodCache cache = project.getProcessorModel().getMethodCache();

		Set<ExecutionEdge> missEdges = new HashSet<ExecutionEdge>();
		/* For each  MethodInfo, create a binary decision variable */
		for(Entry<MethodInfo, List<SuperGraph.SuperGraphEdge>> entry : accessEdges.entrySet()) {
			LinearConstraint<ExecutionEdge> lv = new LinearConstraint<ExecutionEdge>(ConstraintType.LessEqual);
			/* sum(miss_edges) <= 1 */
			for(SuperGraph.SuperGraphEdge e : entry.getValue()) {
				/* add hit and miss edges */
				IPETBuilder<SuperGraph.CallContext> c = new IPETBuilder<SuperGraph.CallContext>(e.getCallContext());
				IPETBuilder.ExecutionEdge parentEdge = c.newEdge(e);
				IPETBuilder.ExecutionEdge hitEdge  = c.newEdge(MethodCacheAnalysis.splitEdge(e,true));
				IPETBuilder.ExecutionEdge missEdge = c.newEdge(MethodCacheAnalysis.splitEdge(e,false));
				ipetSolver.addConstraint(IPETUtils.lowLevelEdgeSplit(parentEdge, hitEdge, missEdge));
				missEdges .add(missEdge);
				ipetSolver.addEdgeCost(missEdge, cache.missOnceCost(entry.getKey(), ipetConfig.assumeMissOnceOnInvoke));
				lv.addLHS(missEdge,1);
			}
			lv.addRHS(1);
			ipetSolver.addConstraint(lv);
		}
		return missEdges;
	}
	
	
	/**
	 * For each method, get all supergraph edges which switch the context to that method
	 * @param superGraph
	 * @return
	 */
	private Map<MethodInfo, List<SuperGraph.SuperGraphEdge>> getMethodSwitchEdges(SuperGraph superGraph) {

		Map<MethodInfo, List<SuperGraph.SuperGraphEdge>> iMap =
			new HashMap<MethodInfo, List<SuperGraph.SuperGraphEdge>>();
		for(SuperGraph.SuperGraphEdge edge : superGraph.getSuperEdges()) {
			MethodInfo targetMethod = superGraph.getTargetNode(edge).getCfg().getMethodInfo();
			List<SuperGraph.SuperGraphEdge> edges = iMap.get(targetMethod);
			if(edges == null) edges = new ArrayList<SuperGraph.SuperGraphEdge>();
			edges.add(edge);
			iMap.put(targetMethod, edges);
		}
		return iMap;
	}


	public static class GlobalIPETStrategy 
	implements RecursiveStrategy<AnalysisContextLocal,WcetCost> {
		private IPETConfig ipetConfig;
		public GlobalIPETStrategy(IPETConfig ipetConfig) {
			this.ipetConfig = ipetConfig;
		}
		// TODO: [cache-analysis] Generalize/Refactor Cache Analysis
		// FIXME: Proper Call Context Support!
		public WcetCost recursiveCost(
				RecursiveAnalysis<AnalysisContextLocal,WcetCost> stagedAnalysis,
				ControlFlowGraph.InvokeNode n,
				AnalysisContextLocal ctx) {
			if(ctx.getCacheApproxMode() != StaticCacheApproximation.ALL_FIT_REGIONS) {
				throw new AssertionError("Cache Mode " + ctx.getCacheApproxMode() + " not supported using" +
						                " _mixed_ local/global IPET strategy");
			}
			Project project = stagedAnalysis.getWCETTool();
			int callStringLength = project.getProjectConfig().callstringLength();
			MethodInfo invoker = n.getBasicBlock().getMethodInfo();
			MethodInfo invoked = n.getImplementedMethod();
			WCETProcessorModel proc = project.getProcessorModel();
			MethodCache cache = proc.getMethodCache();
			long returnCost = cache.getMissOnReturnCost(proc, project.getFlowGraph(invoker));
			long invokeReturnCost = cache.getInvokeReturnMissCost(
					proc,
					project.getFlowGraph(invoker),
	                project.getFlowGraph(invoked));
			WcetCost cost = new WcetCost();

			AnalysisContextLocal recCtx = ctx.withCallString(ctx.getCallString().push(n, callStringLength));
			if(cache.allFit(invoked, recCtx.getCallString()) && ! project.getCallGraph().isLeafMethod(invoked)) {				
				
				/* Perform a GLOBAL-ALL-FIT analysis */
				GlobalAnalysis ga = new GlobalAnalysis(project, ipetConfig);
				WcetCost allFitCost = null;
				try { allFitCost= ga.computeWCET(invoked, recCtx.withCacheApprox(StaticCacheApproximation.GLOBAL_ALL_FIT)); }
				catch (Exception e) { throw new AssertionError(e); }
				cost.addCacheCost(returnCost + allFitCost.getCacheCost());
				cost.addNonLocalCost(allFitCost.getNonCacheCost());
				cost.addPotentialCacheFlushes(1);
				//System.err.println("Potential cache flush: "+invoked+" from "+invoker);
			} else {
				WcetCost recCost = stagedAnalysis.computeCost(invoked, recCtx);
				cost.addCacheCost(recCost.getCacheCost() + invokeReturnCost);
				cost.addNonLocalCost(recCost.getCost() - recCost.getCacheCost());
			}
			Project.logger.info("Recursive WCET computation [GLOBAL IPET]: " + invoked +
			        		    ". cummulative cache cost: "+ cost.getCacheCost()+
					            ", execution cost: "+ cost.getNonCacheCost());
			return cost;
		}

	}
	

	public static class GlobalVisitor extends WcetVisitor {
		private boolean addAlwaysMissCost;
		private AnalysisContextLocal ctx;
		public GlobalVisitor(Project p, AnalysisContextLocal ctx, boolean addAlwaysMissCost) {
			super(p);
			this.ctx = ctx;
			this.addAlwaysMissCost = addAlwaysMissCost;
		}
		public void visitInvokeNode(ControlFlowGraph.InvokeNode n) {
			visitBasicBlockNode(n);
			if(addAlwaysMissCost) {
				WCETProcessorModel proc = project.getProcessorModel();
				this.cost.addCacheCost(proc.getInvokeReturnMissCost(
						n.invokerFlowGraph(),
						n.receiverFlowGraph()));
			}
		}
		@Override
		public void visitBasicBlockNode(BasicBlockNode n) {
			cost.addLocalCost(project.getProcessorModel().basicBlockWCET(ctx.getExecutionContext(n),n.getBasicBlock()));
		}
	}
}
