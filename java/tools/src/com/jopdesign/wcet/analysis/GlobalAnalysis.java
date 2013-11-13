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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import lpsolve.LpSolveException;

import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.code.ControlFlowGraph;
import com.jopdesign.common.code.ControlFlowGraph.BasicBlockNode;
import com.jopdesign.common.code.ControlFlowGraph.CFGEdge;
import com.jopdesign.common.code.ControlFlowGraph.CFGNode;
import com.jopdesign.common.code.ControlFlowGraph.ReturnNode;
import com.jopdesign.common.code.ExecutionContext;
import com.jopdesign.common.code.LoopBound;
import com.jopdesign.common.code.Segment;
import com.jopdesign.common.code.SuperGraph.ContextCFG;
import com.jopdesign.common.code.SuperGraph.SuperGraphEdge;
import com.jopdesign.common.code.SuperGraph.SuperGraphNode;
import com.jopdesign.common.code.SuperGraph.SuperInvokeEdge;
import com.jopdesign.common.code.SuperGraph.SuperReturnEdge;
import com.jopdesign.common.code.SymbolicMarker;
import com.jopdesign.common.code.SymbolicMarker.SymbolicMarkerType;
import com.jopdesign.common.graphutils.LoopColoring;
import com.jopdesign.common.graphutils.Pair;
import com.jopdesign.common.misc.AppInfoError;
import com.jopdesign.common.misc.Iterators;
import com.jopdesign.common.misc.MiscUtils;
import com.jopdesign.wcet.WCETTool;
import com.jopdesign.wcet.analysis.RecursiveAnalysis.RecursiveStrategy;
import com.jopdesign.wcet.analysis.cache.CacheAnalysis;
import com.jopdesign.wcet.analysis.cache.CacheAnalysis.UnsupportedCacheModelException;
import com.jopdesign.wcet.analysis.cache.CachePersistenceAnalysis.PersistenceCheck;
import com.jopdesign.wcet.analysis.cache.MethodCacheAnalysis;
import com.jopdesign.wcet.analysis.cache.SuperGraphExtraCostEdge;
import com.jopdesign.wcet.annotations.LoopBoundExpr;
import com.jopdesign.wcet.ipet.IPETConfig;
import com.jopdesign.wcet.ipet.IPETConfig.CacheCostCalculationMethod;
import com.jopdesign.wcet.ipet.IPETSolver;
import com.jopdesign.wcet.ipet.IPETUtils;
import com.jopdesign.wcet.ipet.LinearConstraint;
import com.jopdesign.wcet.ipet.LinearConstraint.ConstraintType;
import com.jopdesign.wcet.jop.CacheModel;

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
     */
    public WcetCost computeWCET(MethodInfo m, AnalysisContextLocal ctx) throws Exception {

        CacheCostCalculationMethod cacheMode = ctx.getCacheApproxMode();        
        String key = "global" + "_" + cacheMode;
        Segment segment = Segment.methodSegment(m, ctx.getCallString(),project, project.getAppInfo().getCallstringLength(), project);
        return computeWCET(key, segment, cacheMode);
    }

    /**
     * Compute WCET for a segment, using global IPET, and cache analysis results
     * @throws InvalidFlowFactException 
     * @throws LpSolveException 
     * @throws UnsupportedCacheModelException 
     */
    public WcetCost computeWCET(String key, Segment segment, CacheCostCalculationMethod cacheMode) throws InvalidFlowFactException, LpSolveException, UnsupportedCacheModelException {

        /* create an IPET problem for the segment */
    	String problemId = formatProblemName(key, segment.getEntryCFGs().toString());
    	IPETSolver<SuperGraphEdge> ipetSolver = buildIpetProblem(project, problemId, segment, ipetConfig);

        /* compute cost */
        setExecutionCost(segment, ipetSolver);

        /* Add constraints for caches */
        HashMap<String, Set<SuperGraphEdge>> costMissEdges = new HashMap<String, Set<SuperGraphEdge>>();

        for(CacheModel cacheModel : project.getWCETProcessorModel().getCaches()) {
        	
        	CacheAnalysis cpa = CacheAnalysis.getCacheAnalysisFor(cacheModel, project);
        	Set<SuperGraphEdge> edges = cpa.addCacheCost(segment, ipetSolver, cacheMode);
        	costMissEdges.put(cacheModel.toString(), edges);
        }
        
        /* Add constraints for object cache */
    	// ObjectRefAnalysis objectCacheAnalysis = new ObjectRefAnalysis(project, false, 4, 8, 4);
        
        /* Return variables */
        Map<SuperGraphEdge, Long> flowMap = new HashMap<SuperGraphEdge, Long>();

        /* Solve */
        long _start = System.currentTimeMillis();
    	double relaxedCost = 0;
        try {
        	relaxedCost = ipetSolver.solve(null, false);
        } catch(LpSolveException ex) {
        	WCETTool.logger.error("Solving the relaxed problem failed - bug in lp solving lib?");
        }
        long _time_rlp = System.currentTimeMillis() - _start;
        double ilpCost = ipetSolver.solve(flowMap);
        long _time_ilp = System.currentTimeMillis() - _start;
        WCETTool.logger.info(String.format("LP (%d ms) %d | %d ILP (%d ms)",_time_rlp, Math.round(relaxedCost), Math.round(ilpCost), _time_ilp));

        /* Cost extraction */
        WcetCost cost;
        
        /* extract cost and generate a profile in 'profiles' */
    	cost = exportCostProfile(flowMap, costMissEdges, ipetSolver, problemId);

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
//        try {
//			segment.exportDOT(wcetTool.getProjectConfig().getOutFile(problemName+".dot"));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
        
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


    /**
	 * @param flowMap 
     * @param costMissEdges 
	 * @param ipetSolver 
	 * @param problemId
	 */
	private WcetCost exportCostProfile(Map<SuperGraphEdge, Long> flowMap, 
			HashMap<String, Set<SuperGraphEdge>> costMissEdges, 
			IPETSolver<SuperGraphEdge> ipetSolver, 
			String problemId) {
	
		File profileFile = new File(project.getOutDir("profiles"),problemId+".txt");
		WcetCost cost = new WcetCost();
		HashMap<SuperGraphEdge,Long> costProfile = new HashMap<SuperGraphEdge,Long>();
		HashMap<SuperGraphEdge,Long> cacheCostProfile = new HashMap<SuperGraphEdge,Long>();

		/* extra cost lookup map */
		HashMap<SuperGraphEdge, String> extraCostKeys = new HashMap<SuperGraphEdge, String>();
		for(Entry<String, Set<SuperGraphEdge>> cacheEntry: costMissEdges.entrySet()) {
			String key = cacheEntry.getKey();
			for(SuperGraphEdge costEdge : cacheEntry.getValue()) {
				extraCostKeys.put(costEdge, key);
			}
		}
		
		for (Entry<SuperGraphEdge, Long> flowEntry : flowMap.entrySet()) {
	    	SuperGraphEdge edge = flowEntry.getKey();
	        long edgeCost = ipetSolver.getEdgeCost(edge);
	        long flowCost = edgeCost * flowEntry.getValue();
	        if(extraCostKeys.containsKey(edge)) {
	        	cost.addCacheCost(extraCostKeys.get(edge), flowCost);
	        	MiscUtils.incrementBy(cacheCostProfile, edge, flowCost, 0);	        	
	        } else {
	        	cost.addLocalCost(flowCost);            	
	        }
	    	MiscUtils.incrementBy(costProfile, edge, flowCost, 0);
	    }
		/* export profile */
		try {
			ArrayList<Entry<SuperGraphEdge,Long>> profile = new ArrayList<Entry<SuperGraphEdge,Long>>(costProfile.entrySet());
			Collections.sort(profile, new Comparator<Entry<SuperGraphEdge,Long>>() {
				@Override
				public int compare(Entry<SuperGraphEdge, Long> o1, Entry<SuperGraphEdge, Long> o2) {
					return o2.getValue().compareTo(o1.getValue());
				}
			});
			FileWriter fw = new FileWriter(profileFile);
			fw.append("Profile\n");
			fw.append(problemId+"\n");
			fw.append("------------------------------------------------------\n");
			long totalCost = cost.getCost();
			for(Entry<SuperGraphEdge, Long> entry : profile) {
				Long flowCost = entry.getValue();
				double contribution = (100.0 * flowCost) / totalCost;
				fw.append(String.format("  %-50s %8d %.2f%%",entry.getKey(), flowCost,contribution));
				if(cacheCostProfile.containsKey(entry.getKey())) {
					fw.append(String.format(" cache{%d}",cacheCostProfile.get(entry.getKey())));
				}
				fw.append(String.format(" flow{%d}",flowMap.get(entry.getKey())));
				fw.append('\n');
			}
			fw.close();
		} catch (IOException ex) {
			
			WCETTool.logger.error("Generating profile file failed: "+ex);
		}
		return cost;
	}

    // FIXME: Remove? I think it is not needed any more
	@Deprecated
	public static class GlobalIPETStrategy
            implements RecursiveStrategy<AnalysisContextLocal, WcetCost> {
        private IPETConfig ipetConfig;

        public GlobalIPETStrategy(IPETConfig ipetConfig) {
            this.ipetConfig = ipetConfig;
        }

        public WcetCost recursiveCost(
                RecursiveAnalysis<AnalysisContextLocal, WcetCost> stagedAnalysis,
                ControlFlowGraph.InvokeNode n,
                AnalysisContextLocal ctx) {
        	
            if (ctx.getCacheApproxMode() != CacheCostCalculationMethod.ALL_FIT_REGIONS) {
                throw new AssertionError("Cache Mode " + ctx.getCacheApproxMode() + " not supported using" +
                        " _mixed_ local/global IPET strategy");
            }
            WCETTool project = stagedAnalysis.getWCETTool();
            MethodCacheAnalysis mca = new MethodCacheAnalysis(project);
            int callStringLength = project.getCallstringLength();

            MethodInfo invoker = n.getBasicBlock().getMethodInfo();
            MethodInfo invoked = n.getImplementingMethod();
            long returnCost       = mca.getMissOnceCost(invoker, false);
            long invokeReturnCost = mca.getInvokeReturnMissCost(n.getInvokeSite(), ctx.getCallString());
            WcetCost cost = new WcetCost();

            AnalysisContextLocal recCtx = ctx.withCallString(ctx.getCallString().push(n, callStringLength));
            Segment segment = Segment.methodSegment(invoked, recCtx.getCallString(), project, callStringLength, project);

            if (!project.getCallGraph().isLeafMethod(invoked) &&
            	mca.isPersistenceRegion(segment,EnumSet.allOf(PersistenceCheck.class))) {

                /* Perform a GLOBAL-ALL-FIT analysis */
                GlobalAnalysis ga = new GlobalAnalysis(project, ipetConfig);
                WcetCost allFitCost = null;
                try {
                    allFitCost = ga.computeWCET(invoked, recCtx.withCacheApprox(CacheCostCalculationMethod.GLOBAL_ALL_FIT));
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
        
        @Override
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

	/**
	 * Generate a suitable 'problem name' for reporting (unique, no longer than 80 characters)
	 * @param key The key of the problem generator (e.g. method-cache-analysis). <b>Has to have less than 60 characters</b>
	 * @param description A (potentially long) description
	 * @return A string {@code key + '_' + unique(key) + '_' + description}, truncated so has at most 80 characters (filenames!)
	 */
	public static String formatProblemName(String key, String description) {
		
		if(key.length() > 60) throw new AssertionError("formatProblemName: precondition violation: |key|>60");
		
		StringBuffer sb = new StringBuffer();
		sb.append(key);
		sb.append('_');
		sb.append(generateProblemId(key));
		sb.append('_');
		sb.append(description);
		return sb.substring(0, Math.min(80, sb.length()));
	}

    private static Map<String,Long> problemCounter = new HashMap<String,Long>();
	private static long generateProblemId(String key) {
		return MiscUtils.increment(problemCounter,key,0);
	}

}
