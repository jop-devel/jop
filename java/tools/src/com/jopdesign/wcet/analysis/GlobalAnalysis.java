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
import com.jopdesign.common.code.Segment;
import com.jopdesign.common.code.ControlFlowGraph.BasicBlockNode;
import com.jopdesign.common.code.ControlFlowGraph.CFGNode;
import com.jopdesign.common.code.ControlFlowGraph.ReturnNode;
import com.jopdesign.common.code.SuperGraph.ContextCFG;
import com.jopdesign.common.code.SuperGraph.SuperGraphEdge;
import com.jopdesign.common.code.SuperGraph.SuperGraphNode;
import com.jopdesign.common.code.SuperGraph;
import com.jopdesign.common.graphutils.Pair;
import com.jopdesign.wcet.WCETProcessorModel;
import com.jopdesign.wcet.WCETTool;
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
 *
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 */
public class GlobalAnalysis {

    private WCETTool project;

    private IPETConfig ipetConfig;

    public GlobalAnalysis(WCETTool p, IPETConfig ipetConfig) {
        this.ipetConfig = ipetConfig;
        this.project = p;
    }

    /**
     * Compute WCET using global IPET, and either ALWAYS_MISS or GLOBAL_ALL_FIT
     * TODO: Refactor and generalize cache analysis
     */
    public WcetCost computeWCET(MethodInfo m, AnalysisContextLocal ctx) throws Exception {

        StaticCacheApproximation cacheMode = ctx.getCacheApproxMode();
        if (cacheMode != StaticCacheApproximation.ALWAYS_MISS &&
                cacheMode != StaticCacheApproximation.GLOBAL_ALL_FIT) {
            throw new Exception("Global IPET: only ALWAYS_MISS and GLOBAL_ALL_FIT are supported" +
                    " as cache approximation strategies");
        }
        
        String key = m.getFQMethodName() + "_global_" + cacheMode;
        return computeWCET(key, Segment.methodSegment(project,m,ctx.getCallString()), cacheMode);
    }

    /**
     * Compute WCET for a segment, using global IPET, and cache analysis results
     */
    public WcetCost computeWCET(String key, Segment segment, StaticCacheApproximation cacheMode) throws Exception {

        /* create an IPET problem for the segment */
        IPETSolver<SuperGraphEdge> ipetSolver = buildIpetProblem(project, key, segment, ipetConfig);

        /* compute cost */
        setExecutionCost(segment, cacheMode, ipetSolver);

        /* Add constraints for method cache */
        Set<ExecutionEdge> missEdges = new HashSet<ExecutionEdge>();
        if(project.getWCETProcessorModel().hasMethodCache()) {
        	switch(cacheMode) {
        	case ALWAYS_HIT:      break; /* no additional costs */
        	case ALWAYS_MISS:     methodCacheAnalysis.addMissAlwaysCost(ipetSolver, segment); break;
        	case ALL_FIT_SIMPLE:  methodCacheAnalysis.addMissOnceCost(ipetSolver, segment); break;
        	case ALL_FIT_REGIONS: missEdges = methodCacheAnalysis.addMissOnceConstraints(ipetSolver, segment); break;
        	}        	
        }

        /* Return variables */
        Map<SuperGraphEdge, Long> flowMap = new HashMap<SuperGraphEdge, Long>();

        /* Solve */
        double lpCost = ipetSolver.solve(flowMap);

        /* Cost extraction */
        WcetCost cost = new WcetCost();
        //System.err.println("=== Cost Summary ===");
        for (Entry<SuperGraphEdge, Long> flowEntry : flowMap.entrySet()) {
        	SuperGraphEdge edge = flowEntry.getKey();
            long edgeCost = ipetSolver.getEdgeCost(edge);
            long flowCost = edgeCost * flowEntry.getValue();
            if (missEdges.contains(edge)) {
            	if(WCETTool.logger.isTraceEnabled() && flowEntry.getValue() > 0) {
            		WCETTool.logger.trace("Execution Cost [cache]: "+ edge + " = " + flowCost + " ( " + flowEntry.getValue() + " * " + edgeCost + " )");
            	}
                cost.addCacheCost(flowCost);
            } else {
            	if(WCETTool.logger.isTraceEnabled() && flowEntry.getValue() > 0) {
            		WCETTool.logger.trace("Execution Cost [flow]: "+ edge + " = " + flowCost);
            	}
                cost.addNonLocalCost(flowCost);
            }
        }

        /* Sanity Check, and Return */
        long objValue = (long) (lpCost + 0.5);
        if (cost.getCost() != objValue) {
            throw new AssertionError("[GlobalAnalysis] Inconsistency: lpValue vs. extracted value: " + objValue + " / " + cost.getCost());
        }
        return cost;
    }
    
    /**
     * Create an interprocedural max-cost max-flow problem for the given segment<br/>
     * Notes:<ul>
     * <li/> super graph edges always have the callstring of the invoking method
     * </ul>
     *
     * @param wcetTool    A reference to the WCETTool
     * @param problemName A unique identifier for the problem (for reporting)
     * @param segment     The segment to build the ILP for
     * @param ipetConfig  Cost of nodes (or {@code null} if no cost is associated with nodes)
     * @return The max-cost maxflow problem
     */
    public static IPETSolver<SuperGraphEdge> buildIpetProblem(WCETTool wcetTool, String problemName, Segment segment, IPETConfig ipetConfig) {

        IPETSolver<SuperGraphEdge> ipetSolver = new IPETSolver<SuperGraphEdge>(problemName, ipetConfig);

        /* In- and Outflow */
    	ipetSolver.addConstraint(IPETUtils.constantFlow(segment.getEntryEdges(), 1));
        ipetSolver.addConstraint(IPETUtils.constantFlow(segment.getExitEdges(), 1));

        /* Structural flow constraints */
        for (SuperGraphNode node: segment.getNodes()) {
        	ipetSolver.addConstraints(IPETUtils.flowPreservation(segment.incomingEdgesOf(node), segment.outgoingEdgesOf(node)));
        }

        /* Program Flow Constraints */
        for (ContextCFG n : segment.getCallGraphNodes()) {
            ipetSolver.addConstraints(IPETUtils.loopBoundConstraints(n.getCfg(), ipetBuilder));
            ipetSolver.addConstraints(IPETUtils.infeasibleEdgeConstraints(n.getCfg(), ipetBuilder));
        }

        return ipetSolver;
    }


    /**
     * Compute the execution time of each edge in in the supergraph
     *
     * @param segment       the supergraph, whose vertices are considered
     * @param ipetInst the IPET instance
     * @return the cost map
     */
    private Map<ExecutionEdge, WcetCost> setExecutionCost(Segment segment, StaticCacheApproximation approx, IPETSolver ipetInst) {

        HashMap<ExecutionEdge, WcetCost> edgeCost = new HashMap<ExecutionEdge, WcetCost>();
        boolean alwaysMiss = (approx == StaticCacheApproximation.ALWAYS_MISS);

        for (ContextCFG n : segment.getCallGraphNodes()) {

            // FIXME: There is a discrepancy but also overlap between analysis contexts and execution contexts
            AnalysisContextLocal aCtx = new AnalysisContextLocal(approx, n.getCallString());
            WcetVisitor visitor = new LocalCostVisitor(project, aCtx);

            /* For each CFG instance, consider CFG nodes
                          * Currently there is no need to attribute cost to callsites */
            for (CFGNode cfgNode : n.getCfg().vertexSet()) {
                WcetCost cost = visitor.computeCost(cfgNode);
                for (ControlFlowGraph.CFGEdge edge : n.getCfg().outgoingEdgesOf(cfgNode)) {
                    edgeCost.put(segment.liftEdge(edge), cost);
                    ipetInst.addEdgeCost(sEdge, cost.getCost());
                }
            }
        }
        return edgeCost;
    }

    /* add cost for missing each method once (ALL FIT) */

    private Set<ExecutionEdge> addMissOnceCost(SuperGraph sg, IPETSolver ipetSolver) {
        /* collect access sites */

        Map<MethodInfo, List<SuperGraph.SuperEdge>> accessEdges = getMethodSwitchEdges(sg);
        MethodCache cache = project.getWCETProcessorModel().getMethodCache();

        Set<ExecutionEdge> missEdges = new HashSet<ExecutionEdge>();
        /* For each  MethodInfo, create a binary decision variable */
        for (Entry<MethodInfo, List<SuperGraph.SuperEdge>> entry : accessEdges.entrySet()) {
            LinearConstraint<ExecutionEdge> lv = new LinearConstraint<ExecutionEdge>(ConstraintType.LessEqual);
            /* sum(miss_edges) <= 1 */
            for (SuperGraph.SuperEdge e : entry.getValue()) {
                /* add hit and miss edges */
                IPETBuilder<SuperGraph.CallContext> c = new IPETBuilder<SuperGraph.CallContext>(project, e.getCaller().getContext());
                IPETBuilder.ExecutionEdge parentEdge = c.newEdge(e);
                IPETBuilder.ExecutionEdge hitEdge = c.newEdge(MethodCacheAnalysis.splitEdge(e, true));
                IPETBuilder.ExecutionEdge missEdge = c.newEdge(MethodCacheAnalysis.splitEdge(e, false));
                ipetSolver.addConstraint(IPETUtils.lowLevelEdgeSplit(parentEdge, hitEdge, missEdge));
                missEdges.add(missEdge);
                ipetSolver.addEdgeCost(missEdge, cache.missOnceCost(entry.getKey(), ipetConfig.doAssumeMissOnceOnInvoke()));
                lv.addLHS(missEdge, 1);
            }
            lv.addRHS(1);
            ipetSolver.addConstraint(lv);
        }
        return missEdges;
    }


    /**
     * For each method, get all supergraph edges which switch the context to that method
     *
     * @param superGraph
     * @return
     */
    private Map<MethodInfo, List<SuperGraph.SuperEdge>> getMethodSwitchEdges(SuperGraph superGraph) {

        Map<MethodInfo, List<SuperGraph.SuperEdge>> iMap =
                new HashMap<MethodInfo, List<SuperGraph.SuperEdge>>();
        for (SuperGraph.SuperEdge edge : superGraph.getCallGraphEdges()) {
            MethodInfo targetMethod = edge.getCallee().getCfg().getMethodInfo();
            List<SuperGraph.SuperEdge> edges = iMap.get(targetMethod);
            if (edges == null) edges = new ArrayList<SuperGraph.SuperEdge>();
            edges.add(edge);
            iMap.put(targetMethod, edges);
        }
        return iMap;
    }


    public static class GlobalIPETStrategy
            implements RecursiveStrategy<AnalysisContextLocal, WcetCost> {
        private IPETConfig ipetConfig;

        public GlobalIPETStrategy(IPETConfig ipetConfig) {
            this.ipetConfig = ipetConfig;
        }
        // TODO: [cache-analysis] Generalize/Refactor Cache Analysis
        // FIXME: Proper Call Context Support!

        public WcetCost recursiveCost(
                RecursiveAnalysis<AnalysisContextLocal, WcetCost> stagedAnalysis,
                ControlFlowGraph.InvokeNode n,
                AnalysisContextLocal ctx) {
            if (ctx.getCacheApproxMode() != StaticCacheApproximation.ALL_FIT_REGIONS) {
                throw new AssertionError("Cache Mode " + ctx.getCacheApproxMode() + " not supported using" +
                        " _mixed_ local/global IPET strategy");
            }
            WCETTool project = stagedAnalysis.getWCETTool();
            int callStringLength = project.getProjectConfig().callstringLength();
            MethodInfo invoker = n.getBasicBlock().getMethodInfo();
            MethodInfo invoked = n.getImplementedMethod();
            WCETProcessorModel proc = project.getWCETProcessorModel();
            MethodCache cache = proc.getMethodCache();
            long returnCost = cache.getMissOnReturnCost(proc, project.getFlowGraph(invoker));
            long invokeReturnCost = cache.getInvokeReturnMissCost(
                    proc,
                    project.getFlowGraph(invoker),
                    project.getFlowGraph(invoked));
            WcetCost cost = new WcetCost();

            AnalysisContextLocal recCtx = ctx.withCallString(ctx.getCallString().push(n, callStringLength));
            if (cache.allFit(invoked, recCtx.getCallString()) && !project.getCallGraph().isLeafMethod(invoked)) {

                /* Perform a GLOBAL-ALL-FIT analysis */
                GlobalAnalysis ga = new GlobalAnalysis(project, ipetConfig);
                WcetCost allFitCost = null;
                try {
                    allFitCost = ga.computeWCET(invoked, recCtx.withCacheApprox(StaticCacheApproximation.GLOBAL_ALL_FIT));
                }
                catch (Exception e) {
                    throw new AssertionError(e);
                }
                cost.addCacheCost(returnCost + allFitCost.getCacheCost());
                cost.addNonLocalCost(allFitCost.getNonCacheCost());
                cost.addPotentialCacheFlushes(1);
                //System.err.println("Potential cache flush: "+invoked+" from "+invoker);
            } else {
                WcetCost recCost = stagedAnalysis.computeCost(invoked, recCtx);
                cost.addCacheCost(recCost.getCacheCost() + invokeReturnCost);
                cost.addNonLocalCost(recCost.getCost() - recCost.getCacheCost());
            }
            WCETTool.logger.debug("Recursive WCET computation [GLOBAL IPET]: " + invoked +
                    ". cummulative cache cost: " + cost.getCacheCost() +
                    ", execution cost: " + cost.getNonCacheCost());
            return cost;
        }

    }

    public static class LocalCostVisitor extends WcetVisitor {
        private boolean addAlwaysMissCost;
        private AnalysisContextLocal ctx;

        public LocalCostVisitor(WCETTool p, AnalysisContextLocal ctx) {
            super(p);
            this.ctx = ctx;
        }

        public void visitInvokeNode(ControlFlowGraph.InvokeNode n) {
            visitBasicBlockNode(n);
        }

		@Override
		public void visitReturnNode(ReturnNode n) {}

		@Override
        public void visitBasicBlockNode(BasicBlockNode n) {
            cost.addLocalCost(project.getWCETProcessorModel().basicBlockWCET(ctx.getExecutionContext(n), n.getBasicBlock()));
        }
    }
}
