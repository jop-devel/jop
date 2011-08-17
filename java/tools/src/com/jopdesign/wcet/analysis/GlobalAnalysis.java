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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import lpsolve.LpSolveException;

import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.code.ControlFlowGraph;
import com.jopdesign.common.code.ExecutionContext;
import com.jopdesign.common.code.LoopBound;
import com.jopdesign.common.code.Segment;
import com.jopdesign.common.code.SymbolicMarker;
import com.jopdesign.common.code.ControlFlowGraph.BasicBlockNode;
import com.jopdesign.common.code.ControlFlowGraph.CFGEdge;
import com.jopdesign.common.code.ControlFlowGraph.CFGNode;
import com.jopdesign.common.code.ControlFlowGraph.ReturnNode;
import com.jopdesign.common.code.SuperGraph.ContextCFG;
import com.jopdesign.common.code.SuperGraph.SuperGraphEdge;
import com.jopdesign.common.code.SuperGraph.SuperGraphNode;
import com.jopdesign.common.code.SuperGraph.SuperInvokeEdge;
import com.jopdesign.common.code.SuperGraph.SuperReturnEdge;
import com.jopdesign.common.code.SymbolicMarker.SymbolicMarkerType;
import com.jopdesign.common.graphutils.LoopColoring;
import com.jopdesign.common.graphutils.Pair;
import com.jopdesign.common.misc.AppInfoError;
import com.jopdesign.common.misc.Iterators;
import com.jopdesign.wcet.WCETProcessorModel;
import com.jopdesign.wcet.WCETTool;
import com.jopdesign.wcet.analysis.RecursiveAnalysis.RecursiveStrategy;
import com.jopdesign.wcet.analysis.cache.MethodCacheAnalysis;
import com.jopdesign.wcet.annotations.LoopBoundExpr;
import com.jopdesign.wcet.ipet.IPETConfig;
import com.jopdesign.wcet.ipet.IPETSolver;
import com.jopdesign.wcet.ipet.IPETUtils;
import com.jopdesign.wcet.ipet.LinearConstraint;
import com.jopdesign.wcet.ipet.IPETConfig.StaticCacheApproximation;
import com.jopdesign.wcet.ipet.LinearConstraint.ConstraintType;
import com.jopdesign.wcet.jop.MethodCache;

/**
 * Global IPET-based analysis, supporting variable block caches (all fit region approximation).
 *
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 */
public class GlobalAnalysis {

	private WCETTool project;

    private IPETConfig ipetConfig;

	private MethodCacheAnalysis methodCacheAnalysis;

    public GlobalAnalysis(WCETTool p, IPETConfig ipetConfig) {
        this.ipetConfig = ipetConfig;
        this.project = p;
        if(p.getWCETProcessorModel().hasMethodCache()) {
            this.methodCacheAnalysis = new MethodCacheAnalysis(p);        	
        }
    }

    /**
     * Compute WCET using global IPET, and either ALWAYS_MISS or GLOBAL_ALL_FIT
     */
    public WcetCost computeWCET(MethodInfo m, AnalysisContextLocal ctx) throws Exception {

        StaticCacheApproximation cacheMode = ctx.getCacheApproxMode();
        if (cacheMode != StaticCacheApproximation.ALWAYS_MISS &&
                cacheMode != StaticCacheApproximation.GLOBAL_ALL_FIT) {
            throw new Exception("Global IPET: only ALWAYS_MISS and GLOBAL_ALL_FIT are supported" +
                    " as cache approximation strategies");
        }
        
        String key = m.getFQMethodName() + "_global_" + cacheMode;
        Segment segment = Segment.methodSegment(m, ctx.getCallString(),project, project.getAppInfo().getCallstringLength(), project);
        return computeWCET(key, segment, cacheMode);
    }

    /**
     * Compute WCET for a segment, using global IPET, and cache analysis results
     * @throws InvalidFlowFactException 
     * @throws LpSolveException 
     */
    public WcetCost computeWCET(String key, Segment segment, StaticCacheApproximation cacheMode) throws InvalidFlowFactException, LpSolveException {

        /* create an IPET problem for the segment */
        IPETSolver<SuperGraphEdge> ipetSolver = buildIpetProblem(project, key, segment, ipetConfig);

        /* compute cost */
        setExecutionCost(segment, ipetSolver);

        /* Add constraints for method cache */
        Set<SuperGraphEdge> missEdges = new HashSet<SuperGraphEdge>();
        if(project.getWCETProcessorModel().hasMethodCache()) {
        	switch(cacheMode) {
        	case ALWAYS_HIT:      break; /* no additional costs */
        	case ALWAYS_MISS:     missEdges = methodCacheAnalysis.addMissAlwaysCost(segment, ipetSolver); break;
        	case ALL_FIT_SIMPLE:  missEdges = methodCacheAnalysis.addMissOnceCost(segment, ipetSolver); break;
        	case ALL_FIT_REGIONS: missEdges = methodCacheAnalysis.addMissOnceConstraints(segment, ipetSolver); break;
        	case GLOBAL_ALL_FIT:  missEdges = methodCacheAnalysis.addGlobalAllFitConstraints(segment, ipetSolver); break;
        	}        	
        }

        /* Return variables */
        Map<SuperGraphEdge, Long> flowMap = new HashMap<SuperGraphEdge, Long>();

        /* Solve */
        long _start = System.currentTimeMillis();
        double relaxedCost = ipetSolver.solve(null, false);
        long _time_rlp = System.currentTimeMillis() - _start;
        double ilpCost = ipetSolver.solve(flowMap);
        long _time_ilp = System.currentTimeMillis() - _start;
        WCETTool.logger.info(String.format("LP (%d ms) %d | %d ILP (%d ms)",_time_rlp, Math.round(relaxedCost), Math.round(ilpCost), _time_ilp));

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
        if(Double.isInfinite(ilpCost)) {
            throw new AssertionError("[GlobalAnalysis] Unbounded (infinite lp cost)");        	
        }
        long objValue = (long) (ilpCost + 0.5);
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
     * @throws InvalidFlowFactException 
     */
    public static IPETSolver<SuperGraphEdge> buildIpetProblem(WCETTool wcetTool, String problemName, Segment segment, IPETConfig ipetConfig) throws InvalidFlowFactException {

        IPETSolver<SuperGraphEdge> ipetSolver = new IPETSolver<SuperGraphEdge>(problemName, ipetConfig);

        /* DEBUGGING: Render segment */
        try {
			segment.exportDOT(wcetTool.getProjectConfig().getOutFile(problemName+".dot"));
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        /* In- and Outflow */
    	ipetSolver.addConstraint(IPETUtils.constantFlow(segment.getEntryEdges(), 1));
        ipetSolver.addConstraint(IPETUtils.constantFlow(segment.getExitEdges(), 1));

        /* Structural flow constraints */
        for (SuperGraphNode node: segment.getNodes()) {
        	ipetSolver.addConstraint(IPETUtils.flowPreservation(segment.incomingEdgesOf(node), segment.outgoingEdgesOf(node)));
        }

        /* Supergraph constraints */
        for (Pair<SuperInvokeEdge, SuperReturnEdge> superEdgePair : segment.getCallSites()) {
        	Iterable<SuperGraphEdge> es1 = Iterators.<SuperGraphEdge>singleton(superEdgePair.first());
        	Iterable<SuperGraphEdge> es2 = Iterators.<SuperGraphEdge>singleton(superEdgePair.second());
        	ipetSolver.addConstraint(IPETUtils.flowPreservation(es1,es2));
        }
        
        /* Program Flow Constraints */
        for(LinearConstraint<SuperGraphEdge> flowFact : getFlowFacts(wcetTool, segment)) {
        	ipetSolver.addConstraint(flowFact);
        }
        return ipetSolver;
    }


    /**
     * Get all flow facts (e.g. loop bounds, infeasible edges) for the given segment
	 * @param segment
	 * @return
     * @throws InvalidFlowFactException 
	 */
	private  static Iterable<LinearConstraint<SuperGraphEdge>> getFlowFacts(
			WCETTool wcetTool, Segment segment) throws InvalidFlowFactException {
		return Iterators.concat(
				getLoopBounds(wcetTool, segment),
				getInfeasibleEdgeConstraints(wcetTool, segment));
	}


	/**
	 * <p>Get all loop bounds for the given segment.</p>
	 * <p>For each loop bound B for loop H relative to marker M:</p>
	 * <p>sum(M) * B &lt;= sum(continue-edges-of(H))</p>
     *
	 * @param segment
	 * @return
	 * @throws InvalidFlowFactException 
	 */
	private static Iterable<LinearConstraint<SuperGraphEdge>> getLoopBounds(
			WCETTool wcetTool, Segment segment) throws InvalidFlowFactException {

		List<LinearConstraint<SuperGraphEdge>> constraints =
			new ArrayList<LinearConstraint<SuperGraphEdge>>();

		// For all CFG instances
		for(ContextCFG ccfg : segment.getCallGraphNodes()) {

	    	ControlFlowGraph cfg = ccfg.getCfg();

	    	// for all loops in the method
	        LoopColoring<CFGNode, ControlFlowGraph.CFGEdge> loops = cfg.getLoopColoring();
	        for (CFGNode hol : loops.getHeadOfLoops()) {
	        	
	            LoopBound loopBound = wcetTool.getLoopBound(hol, ccfg.getContext().getCallString());

	            if (loopBound == null) {
	                throw new AppInfoError("No loop bound record for head of loop: " + hol + " : " + cfg.buildLoopBoundMap());
	            }
	            addLoopConstraints(constraints, segment, ccfg, hol, loops, loopBound);
	        }
		}
        return constraints;	    	
	}

	/**
	 * Add loop contraints
	 * @param constraints the new constraints are added to this collection
	 * @param segment
	 * @param ccfg
	 * @param headOfLoop
	 * @param loops
	 * @param loopBound
	 * @throws InvalidFlowFactException 
	 */
	private static void addLoopConstraints(
			List<LinearConstraint<SuperGraphEdge>> constraints,
			Segment segment,
			ContextCFG ccfg,
			CFGNode headOfLoop,
			LoopColoring<CFGNode, CFGEdge> loops,
			LoopBound loopBound) throws InvalidFlowFactException {
		
        /* marker loop constraints */
        for (Entry<SymbolicMarker, LoopBoundExpr> markerBound : loopBound.getLoopBounds()) {

            /* loop constraint */
            LinearConstraint<SuperGraphEdge> loopConstraint =
            	new LinearConstraint<SuperGraphEdge>(ConstraintType.GreaterEqual);
            /* rhs = sum(continue-edges(loop)) */
            Iterable<SuperGraphEdge> continueEdges = 
            	segment.liftCFGEdges(ccfg, loops.getBackEdgesTo(headOfLoop));
            loopConstraint.addRHS( continueEdges );
            
            /* Multiplicities */
            ExecutionContext executionContext = 
            	new ExecutionContext(ccfg.getCfg().getMethodInfo(), ccfg.getCallString());
			long lhsMultiplicity = markerBound.getValue().upperBound(executionContext);

            SymbolicMarker marker = markerBound.getKey();
            if (marker.getMarkerType() == SymbolicMarkerType.OUTER_LOOP_MARKER) {

                CFGNode outerLoopHol;
                outerLoopHol = loops.getLoopAncestor(headOfLoop, marker.getOuterLoopDistance());
                if (outerLoopHol == null) {
                	throw new InvalidFlowFactException("Bad outer loop annotation");
                }
                Iterable<SuperGraphEdge>  exitEdges =
                	segment.liftCFGEdges(ccfg, loops.getExitEdgesOf(outerLoopHol));
                for (SuperGraphEdge exitEdge : exitEdges) {
                    loopConstraint.addLHS(exitEdge, lhsMultiplicity);
                }
            } else {
                assert (marker.getMarkerType() == SymbolicMarkerType.METHOD_MARKER);
                throw new AssertionError("ILPModelBuilder: method markers not yet supported, sorry");
            }
            constraints.add(loopConstraint);
        }
	}

	/**
	 * For each infeasible edge, assert that the edge has flow 0
	 * @param wcetTool
	 * @param segment
	 * @return
	 */
	private static Iterable<LinearConstraint<SuperGraphEdge>> getInfeasibleEdgeConstraints(
			WCETTool wcetTool, Segment segment) {

		List<LinearConstraint<SuperGraphEdge>> constraints = new ArrayList<LinearConstraint<SuperGraphEdge>>();
		// - for each infeasible edge
		// -- edge = 0
		for(ContextCFG ccfg : segment.getCallGraphNodes()) {
			for (CFGEdge edge : wcetTool.getInfeasibleEdges(ccfg.getCfg(), ccfg.getCallString())) {
				LinearConstraint<SuperGraphEdge> infeasibleConstraint =
					new LinearConstraint<SuperGraphEdge>(ConstraintType.Equal);
				infeasibleConstraint.addLHS(segment.liftCFGEdges(ccfg, Iterators.singleton(edge)));
				infeasibleConstraint.addRHS(0);
				constraints.add(infeasibleConstraint);
			}
		}
		return constraints;
	}


	/**
     * Compute the execution time of each edge in in the supergraph
     * 
     * FIXME: There is both discrepancy and overlap between analysis contexts and execution contexts
     *
     * @param segment       the supergraph, whose vertices are considered
     * @param ipetInst      the IPET instance
     * @return the cost map
     */
    private Map<SuperGraphEdge, WcetCost> setExecutionCost(Segment segment, IPETSolver<SuperGraphEdge> ipetInstance) {

        HashMap<SuperGraphEdge, WcetCost> edgeCost = new HashMap<SuperGraphEdge, WcetCost>();

        /* Attribute edge cost to edge source */
        for (SuperGraphEdge e : segment.getEdges()) {
        	
        	/* ignore exit edges, because their target is per definitionem not part of the segment */
        	if(segment.isExitEdge(e)) continue;
        	
        	SuperGraphNode sg = e.getTarget();
        	
            WcetCost cost = calculateCost(sg);
            edgeCost.put(e, cost);
            ipetInstance.addEdgeCost(e, cost.getCost());        	
        }
        return edgeCost;
    }


    /* add cost for missing each method once (ALL FIT) */
//    private Set<ExecutionEdge> addMissOnceCost(SuperGraph sg, IPETSolver ipetSolver) {
//        /* collect access sites */
//
//        Map<MethodInfo, List<SuperGraph.SuperEdge>> accessEdges = getMethodSwitchEdges(sg);
//        MethodCache cache = project.getWCETProcessorModel().getMethodCache();
//
//        Set<ExecutionEdge> missEdges = new HashSet<ExecutionEdge>();
//        /* For each  MethodInfo, create a binary decision variable */
//        for (Entry<MethodInfo, List<SuperGraph.SuperEdge>> entry : accessEdges.entrySet()) {
//            LinearConstraint<ExecutionEdge> lv = new LinearConstraint<ExecutionEdge>(ConstraintType.LessEqual);
//            /* sum(miss_edges) <= 1 */
//            for (SuperGraph.SuperEdge e : entry.getValue()) {
//                /* add hit and miss edges */
//                IPETBuilder<SuperGraph.CallContext> c = new IPETBuilder<SuperGraph.CallContext>(project, e.getCaller().getContext());
//                IPETBuilder.ExecutionEdge parentEdge = c.newEdge(e);
//                IPETBuilder.ExecutionEdge hitEdge = c.newEdge(MethodCacheAnalysis.splitEdge(e, true));
//                IPETBuilder.ExecutionEdge missEdge = c.newEdge(MethodCacheAnalysis.splitEdge(e, false));
//                ipetSolver.addConstraint(IPETUtils.lowLevelEdgeSplit(parentEdge, hitEdge, missEdge));
//                missEdges.add(missEdge);
//                ipetSolver.addEdgeCost(missEdge, cache.missOnceCost(entry.getKey(), ipetConfig.doAssumeMissOnceOnInvoke()));
//                lv.addLHS(missEdge, 1);
//            }
//            lv.addRHS(1);
//            ipetSolver.addConstraint(lv);
//        }
//        return missEdges;
//    }

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
            MethodInfo invoked = n.getImplementingMethod();
            WCETProcessorModel proc = project.getWCETProcessorModel();
            MethodCache cache = proc.getMethodCache();
            long returnCost = cache.getMissOnReturnCost(project.getFlowGraph(invoker));
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
    
    /* cost calculation */
    private WcetCost calculateCost(SuperGraphNode sgn) {
        AnalysisContext ctx = new AnalysisContextCallString(sgn.getContextCFG().getCallString());
        WcetVisitor costCalculator = new BasicBlockCost(project, ctx);
        return costCalculator.computeCost(sgn.getCFGNode());    	
    }
    
    private static class BasicBlockCost extends WcetVisitor {
        private AnalysisContext ctx;

        public BasicBlockCost(WCETTool p, AnalysisContext ctx) {
            super(p);
            this.ctx = ctx;
        }

        public void visitInvokeNode(ControlFlowGraph.InvokeNode n) {
            visitBasicBlockNode(n);
        }

		@Override
		public void visitReturnNode(ReturnNode n) {
			
		}

		@Override
        public void visitBasicBlockNode(BasicBlockNode n) {
            cost.addLocalCost(project.getWCETProcessorModel().basicBlockWCET(ctx.getExecutionContext(n), n.getBasicBlock()));
        }
    }

}
