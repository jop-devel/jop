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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.Type;
import org.apache.log4j.Logger;

import lpsolve.LpSolveException;

import com.jopdesign.common.MethodCode;
import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.code.CallString;
import com.jopdesign.common.code.ControlFlowGraph;
import com.jopdesign.common.code.InvokeSite;
import com.jopdesign.common.code.Segment;
import com.jopdesign.common.code.SuperGraph.ContextCFG;
import com.jopdesign.common.code.SuperGraph.SuperEdge;
import com.jopdesign.common.code.SuperGraph.SuperGraphEdge;
import com.jopdesign.common.code.SuperGraph.SuperGraphNode;
import com.jopdesign.common.code.SuperGraph.SuperInvokeEdge;
import com.jopdesign.common.code.SuperGraph.SuperReturnEdge;
import com.jopdesign.common.graphutils.Pair;
import com.jopdesign.common.misc.AppInfoException;
import com.jopdesign.common.misc.Filter;
import com.jopdesign.common.misc.MiscUtils;
import com.jopdesign.common.misc.MiscUtils.F1;
import com.jopdesign.wcet.WCETTool;
import com.jopdesign.wcet.analysis.GlobalAnalysis;
import com.jopdesign.wcet.analysis.InvalidFlowFactException;
import com.jopdesign.wcet.ipet.IPETConfig;
import com.jopdesign.wcet.ipet.IPETSolver;
import com.jopdesign.wcet.ipet.IPETConfig.StaticCacheApproximation;
import com.jopdesign.wcet.ipet.LinearConstraint;
import com.jopdesign.wcet.ipet.LinearConstraint.ConstraintType;
import com.jopdesign.wcet.jop.MethodCache;

/**
 * <p>Cache persistence analysis for the variable block Method cache.</p>
 * @throws InvalidFlowFactException 
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 */
public class MethodCacheAnalysis extends CachePersistenceAnalysis<MethodInfo> {

    private static final String KEY = MethodCacheAnalysis.class.getCanonicalName();

    public static enum PersistenceCheck {
        /** check persistence by counting the total number of distinct methods (cheap) */
        CountTotal,
        
        /** check persistence by maximizing the number of distinct blocks accessed (LP relaxation)  */
        CountRelaxed,
        
        /** check persistence by maximizing the number of distinct blocks accessed (LP relaxation)  */
        CountILP 
    	
    };
    
    public static final EnumSet<PersistenceCheck> CHECK_COUNT_FAST    = EnumSet.of(PersistenceCheck.CountTotal);
    public static final EnumSet<PersistenceCheck> CHECK_COUNT_PRECISE = EnumSet.allOf(PersistenceCheck.class);
        
    protected final WCETTool wcetTool;
	private MethodCache methodCache;

	private final F1<SuperGraphEdge, Long> NUMBER_OF_BLOCKS = 
			new F1<SuperGraphEdge,Long>() {
				@Override
				public Long apply(SuperGraphEdge e) {
					MethodInfo mi = e.getTarget().getCfg().getMethodInfo();
					return (long) methodCache.requiredNumberOfBlocks(mi);
				}			
	};
	
    private final F1<SuperGraphEdge, Long> EDGE_MISS_COST = new F1<SuperGraphEdge,Long>() {
		@Override
		public Long apply(SuperGraphEdge accessEdge) {
			return getMissCost(accessEdge);			
		}			
	};

	public MethodCacheAnalysis(WCETTool wcetTool) {

    	this.wcetTool = wcetTool;
        this.methodCache = wcetTool.getWCETProcessorModel().getMethodCache();        
    }

	public MethodCache getMethodCache() {

		return methodCache;
	}

	/**
	 * Add method cache cost to ipet problem
	 * @param segment
	 * @param ipetSolver
	 * @return
	 * @throws LpSolveException 
	 * @throws InvalidFlowFactException 
	 */
	public Set<SuperGraphEdge> addCacheCost(Segment segment, IPETSolver<SuperGraphEdge> ipetSolver,
			StaticCacheApproximation cacheCalculation) throws InvalidFlowFactException, LpSolveException {
		
		Set<SuperGraphEdge> missEdges;
		switch(cacheCalculation) {
		case ALWAYS_HIT:      missEdges = new HashSet<SuperGraphEdge>(); break; /* no additional costs */
		case ALWAYS_MISS:     missEdges = addMissAlwaysCost(segment, ipetSolver); break;
		case ALL_FIT_SIMPLE:  missEdges = addMissOnceCost(segment, ipetSolver); break;
		case ALL_FIT_REGIONS: missEdges = addMissOnceConstraints(segment, ipetSolver); break;
		case GLOBAL_ALL_FIT:  missEdges = addGlobalAllFitConstraints(segment, ipetSolver); break;
		default: throw new RuntimeException("addCacheCost(): Unexpected cache approximation mode "+cacheCalculation);
		}        	
		return missEdges;
	}
	
	public boolean isPersistenceRegion(WCETTool wcetTool, MethodInfo m, CallString cs, EnumSet<PersistenceCheck> tests) {

		/* fast path for the optimizer (segments are still slow) */
		if(tests == CHECK_COUNT_FAST) {
			List<MethodInfo> methods = wcetTool.getCallGraph().getReachableImplementations(m, cs);
	        long blocks = 0;
			for (MethodInfo reachable : methods) {
	        	blocks  += methodCache.requiredNumberOfBlocks(reachable);
	        }
	        return methodCache.allFit(blocks);
		}
		Segment segment = Segment.methodSegment(m, cs, wcetTool, wcetTool.getCallstringLength(), wcetTool);
		return isPersistenceRegion(segment, tests);
	}

	/**
	 * Perform a heuristic check whether in the given segment all methods are persistent
	 * @param segment
	 * @param tests which checks to perform
	 * @return true if all methods are persistent in the method cache, false if not sure
	 * @throws LpSolveException 
	 * @throws InvalidFlowFactException 
	 */
	public boolean isPersistenceRegion(Segment segment,  EnumSet<PersistenceCheck> tests)  {

		int cacheBlocks = methodCache.getNumBlocks();
		long usedBlocks = cacheBlocks + 1;
		if(tests.contains(PersistenceCheck.CountTotal)) {
			usedBlocks = countTotalCacheBlocks(segment);
			if(usedBlocks <= cacheBlocks) return true;			
		}
		if(tests.contains(PersistenceCheck.CountRelaxed)) {
			try {
				usedBlocks = countDistinctCacheBlocks(segment, false);
				if(usedBlocks <= cacheBlocks) return true;
				if(usedBlocks >= cacheBlocks*2) return false;
			} catch(Exception ex) {
				WCETTool.logger.error("Count Distinct Cache Blocks (Relaxed LP) failed: "+ex);				
			}
		}
		if(tests.contains(PersistenceCheck.CountILP)) {
			try {
				usedBlocks = countDistinctCacheBlocks(segment, true);
				if(usedBlocks <= cacheBlocks) return true;
			} catch(Exception ex) {
				WCETTool.logger.error("Count Distinct Cache Blocks (Relaxed LP) failed: "+ex);				
			}
		}
		return false;
	}
   	
	/**
	 * Analyze the number of cache lines (ways) needed to guarantee that all methods
	 * are persistent
	 * @param segment the segment to analyze
	 * @param use integer variables (more expensive, more accurate)
	 * @return
	 * @throws InvalidFlowFactException 
	 * @throws LpSolveException 
	 */
	public long countDistinctCacheBlocks(Segment segment, boolean useILP) throws InvalidFlowFactException, LpSolveException {
		
		return computeMissOnceCost(segment, getCacheAccessesByTag(segment).entrySet(), 
					NUMBER_OF_BLOCKS, useILP, KEY, wcetTool);
	}

	protected Map<MethodInfo, List<SuperGraphEdge>> getCacheAccessesByTag(Segment segment) {

		/* Collect all method cache accesses */
		Iterable<SuperGraphEdge> cacheAccessEdges = collectCacheAccesses(segment);
		return groupAccessEdges(cacheAccessEdges);
	}

	/**
	 * @param cacheAccessEdges
	 * @return
	 */
	private Map<MethodInfo, List<SuperGraphEdge>> groupAccessEdges(
			Iterable<SuperGraphEdge> cacheAccessEdges) {

		/* Group method cache access by method */
		F1<SuperGraphEdge, MethodInfo> getMethodInfo = new F1<SuperGraphEdge, MethodInfo>() {
			public MethodInfo apply(SuperGraphEdge v) { return v.getTarget().getContextCFG().getCfg().getMethodInfo(); }
		};
		return MiscUtils.group(getMethodInfo, null, cacheAccessEdges);
	}

	/**
	 * Collect all method cache accesses in the segment: These are all supergraph edges,
	 * plus all entry edges.
	 * @param segment
	 * @return
	 */
	private Iterable<SuperGraphEdge> collectCacheAccesses(final Segment segment) {
		
		return new Filter<SuperGraphEdge>() {
			@Override
			protected boolean include(SuperGraphEdge e) {
				return (e instanceof SuperEdge || segment.getEntryEdges().contains(e));
			}				
		}.filter(segment.getEdges()); 		
	}

	/**
	 * Get the maximum number of distinct method cache blocks possibly accessed in the
	 * given execution context
	 * @param segment The segment to consider
	 * @return
	 */
	public long countTotalCacheBlocks(Segment segment) {
		
        long blocks = 0;
		for (MethodInfo reachable : this.getCacheAccessesByTag(segment).keySet()) {
        	blocks  += methodCache.requiredNumberOfBlocks(reachable);
        }
        return blocks;
	}

    /** Find a segment cover (i.e., a set of segments covering all execution paths)
     *  where each segment in the set is persistent (a cache persistence region (CPR))
     *  
     *  <h2>The simplest algorithm for a segment S (for acyclic callgraphs)</h2>
     *   <ul><li/>Check whether S itself is CPR; if so, return S
     *       <li/>Otherwise, create subsegments S' for each invoked method,
     *       <li/>and single node segments for each access
     *   </ul>
     *  FIXME: currently returns always-miss segments
     * @param segment the parent segment
     * @param avoidOverlap 
     * @return
     */
	private Collection<Segment> findPersistenceSegmentCover(Segment segment, boolean avoidOverlap) {
		
		List<Segment> cover = new ArrayList<Segment>();

		/* We currently only support entries to one CFG */
		Set<ContextCFG> entryMethods = new HashSet<ContextCFG>();
		for(SuperGraphEdge entryEdge : segment.getEntryEdges()) {
			entryMethods.add(entryEdge.getTarget().getContextCFG());
		}
		
		if(entryMethods.size() != 1) {
			throw new AssertionError("findPersistenceSegmentCover: only supporting segments with unique entry method");
		}
		if(this.isPersistenceRegion(segment, CHECK_COUNT_PRECISE)) {
			// System.err.println("Adding cover segment for: "+entryMethods);
			cover.add(segment);
		} else {
			for(Pair<SuperInvokeEdge, SuperReturnEdge> invocation : segment.getCallSitesFrom(entryMethods.iterator().next())) {
				ContextCFG callee = invocation.first().getCallee();
				// System.err.println("Recursively analyzing: "+callee);
				
				cover.addAll(findPersistenceSegmentCover(Segment.methodSegment(callee, segment.getSuperGraph()), avoidOverlap)); 
				// System.err.println("Adding return segment for: "+entryMethods);
				cover.add(Segment.nodeSegment(invocation.second().getTarget(), segment.getSuperGraph()));
			}
		}
		return cover;
	}
	/* Going further: In the top-level CFG, consider all loops, and check whether they are part of the segment
	 * (this is the case if all edges of the loop (intraprocedural) are part of the segment)
	 * If so, run the loop segment check (which is simpler, because in loops it is sensible to
	 * assume that total = distinct)
	 *      *  <h2>A  quite simple algorithm for a segment S (for acyclic callgraphs)</h2>
     *   <ul><li/>Check whether S itself is CPR; if so, return S
     *       <li/>Otherwise, create segments for each direct subsegment S' for
     *     <ul><li/>Loops (not nested inside other loops, in the same method)
     *         <li/>Invoke nodes (not contained in loops, in the same method)
     *              *   </ul>
	 */

	/**
	 * Add always miss cost: for each access to the method cache, add cost of access
	 * @param segment
	 * @param ipetSolver
	 */
	public Set<SuperGraphEdge> addMissAlwaysCost(
			Segment segment,			
			IPETSolver<SuperGraphEdge> ipetSolver) {

		Iterable<SuperGraphEdge> accessEdges = collectCacheAccesses(segment);
		for(SuperGraphEdge accessEdge: accessEdges) {
			if(! segment.includesEdge(accessEdge)) {
				throw new AssertionError("Subsegment edge not in segment!: "+accessEdge);
			}
		}
		return CachePersistenceAnalysis.addFixedCostEdges(accessEdges, ipetSolver, EDGE_MISS_COST, KEY+"_am",0);
	}

	/**
	 * Add miss once cost: for each method cache persistence segment, add maximum miss cost to the segment entries
	 * @param segment
	 * @param ipetSolver
	 * @throws LpSolveException 
	 * @throws InvalidFlowFactException 
	 */
	public Set<SuperGraphEdge> addMissOnceCost(Segment segment,
			IPETSolver<SuperGraphEdge> ipetSolver) throws InvalidFlowFactException, LpSolveException {

		Set<SuperGraphEdge> missEdges = new HashSet<SuperGraphEdge>();
		int tag = 0;
		
		for(Segment persistenceSegment : findPersistenceSegmentCover(segment, true)) {

			tag++;
			/* Collect all cache accesses */
			long cost = computeMissOnceCost(persistenceSegment, getCacheAccessesByTag(persistenceSegment).entrySet(), 
					EDGE_MISS_COST, true, KEY, wcetTool);
			WCETTool.logger.debug("miss once cost for segment: "+persistenceSegment+": "+cost);

			F1<SuperGraphEdge, Long> costModel = MiscUtils.const1(cost);			
			Set<SuperGraphEdge> costEdges = CachePersistenceAnalysis.addFixedCostEdges(persistenceSegment.getEntryEdges(), ipetSolver,
					costModel, KEY + "_miss_once", tag);
			missEdges.addAll(costEdges);
			
		}
		return missEdges;
	}

	/**
	 * Add miss once constraints for all subsegments in the persistence cover of the given segment
	 * @param segment
	 * @param ipetSolver
	 * @return
	 */
	public Set<SuperGraphEdge> addMissOnceConstraints(Segment segment,
			IPETSolver<SuperGraphEdge> ipetSolver) {

		Set<SuperGraphEdge> missEdges = new HashSet<SuperGraphEdge>();
		for(Segment persistenceSegment : findPersistenceSegmentCover(segment, false)) {
			
			missEdges.addAll(
			    addPersistenceSegmentConstraints(
			    		persistenceSegment,
			    		getCacheAccessesByTag(persistenceSegment).entrySet(),
						ipetSolver,
						EDGE_MISS_COST,
						KEY));
		}
		return missEdges;
	}


	public Set<SuperGraphEdge> addGlobalAllFitConstraints(Segment segment,
			IPETSolver<SuperGraphEdge> ipetSolver) {
		
		return addPersistenceSegmentConstraints(segment,
				getCacheAccessesByTag(segment).entrySet(),
				ipetSolver,
				EDGE_MISS_COST,
				KEY);
	}


	/**
	 * Get miss cost for an edge accessing the method cache
	 * @param accessEdge either a SuperInvoke or SuperReturn edge, or an entry edge of the segment analyzed
	 * @return maximum miss penalty (in cycles)
	 */
	private long getMissCost(SuperGraphEdge accessEdge) {
		
		SuperGraphNode accessed = accessEdge.getTarget();
		ControlFlowGraph cfg = accessed.getCfg();
		if(accessEdge instanceof SuperReturnEdge) {
			/* return edge: return cost */
			Type returnType = accessEdge.getSource().getCfg().getMethodInfo().getType();
			return methodCache.getMissPenaltyOnReturn(cfg.getNumberOfWords(), returnType);
		} else if(accessEdge instanceof SuperInvokeEdge) {
			InvokeInstruction invokeIns = ((SuperInvokeEdge) accessEdge).getInvokeNode().getInvokeSite().getInvokeInstruction();
			return methodCache.getMissPenaltyOnInvoke(cfg.getNumberOfWords(), invokeIns);			
		} else {
			/* entry edge of the segment: can be invoke or return cost */
			return methodCache.getMissPenalty(cfg.getNumberOfWords(), false);
		}
	}

	/* utility functions */

	/**
	 * Get the maximum method cache miss penalty for invoking {@code invoked} and returning to {@code invoker}.<br/>
	 * Also works with virtual invokes (taking the maximum cost)
	 * @param invokeSite the invoke site
	 * @param context call context
	 * @return miss penalty in cycles
	 */
	public long getInvokeReturnMissCost(InvokeSite invokeSite, CallString context) {

		ControlFlowGraph invokerCfg = wcetTool.getFlowGraph(invokeSite.getInvoker());
		long rMiss = methodCache.getMissPenaltyOnReturn(invokerCfg.getNumberOfWords(), invokeSite.getInvokeeRef().getDescriptor().getType());
		long iMissMax = 0;
		for(MethodInfo target : wcetTool.findImplementations(invokeSite.getInvoker(), invokeSite.getInstructionHandle(), context)) {
			ControlFlowGraph invokedCfg = wcetTool.getFlowGraph(target);
			long iMiss = methodCache.getMissPenaltyOnInvoke(invokedCfg.getNumberOfWords(), invokeSite.getInvokeInstruction());
			if(iMiss > iMissMax) iMissMax = iMiss;
		}
		return iMissMax + rMiss;
	}
	
    /**
     * Check that cache is big enough to hold any method possibly invoked
     * Return largest method
     * TODO: move to method cache analysis
     */
    public static MethodInfo checkCache(WCETTool wcetTool, Iterable<MethodInfo> methods) throws AppInfoException {
		MethodCache methodCache = wcetTool.getWCETProcessorModel().getMethodCache();
        int maxWords = 0;
        MethodInfo largestMethod = null;
        // It is inconvenient for testing to take all methods into account
        // for (ClassInfo ci : project.getAppInfo().getClassInfos()) {
        for (MethodInfo mi : methods) {
            MethodCode code = mi.getCode();
            if (code == null) continue;
            // FIXME: using getNumberOfBytes(false) here to be compatible to old behaviour.
            //        should probably be getNumberOfBytes()
            int size = code.getNumberOfBytes(false);
            int words = MiscUtils.bytesToWords(size);
            if (!methodCache.fitsInCache(words)) {
                throw new AppInfoException("Cache to small for target method: " + mi.getFQMethodName() + " / " + words + " words");
            }
            if (words >= maxWords) {
                largestMethod = mi;
                maxWords = words;
            }
        }

        return largestMethod;
    }

    /**
     * Compute the maximal total cache-miss penalty for <strong>invoking and executing</strong>
     * m.
     * <p>
     * Precondition: The set of all methods reachable from <code>m</code> fit into the cache
     * </p><p>
     * Algorithm: If all methods reachable from <code>m</code> (including <code>m</code>) fit
     * into the cache, we can compute the WCET of <m> using the {@code ALWAYS_HIT} cache
     * approximation, and then add the sum of cache miss penalties for every reachable method.
     * </p><p>
     * Note that when using this approximation, we attribute the
     * total cache miss cost to the invocation of that method.
     * </p><p>
     * Explanation: We know that there is only one cache miss per method, but for FIFO caches we
     * do not know when the cache miss will occur (on return or invoke), except for leaf methods.
     * Let <code>h</code> be the number of cycles hidden by <strong>any</strong> return or
     * invoke instructions. Then the cache miss penalty is bounded by <code>(b-h)</code> per
     * method.
     * </p>
     * @param m The method invoked
     * @return the cache miss penalty
     * @deprecated ported from the old method cache analysis framework
     */
    @Deprecated
    public long getMissOnceCummulativeCacheCost(MethodInfo m, boolean assumeOnInvoke) {
        long miss = 0;
        for (MethodInfo reachable : wcetTool.getCallGraph().getReachableImplementationsSet(m)) {
            miss += getMissOnceCost(reachable, assumeOnInvoke);
        }
        Logger.getLogger(this.getClass()).debug("getMissOnceCummulativeCacheCost for " + m + "/" + (assumeOnInvoke ? "invoke" : "return") + ":" + miss);
        return miss;
    }

	/**
	 * @param mi the method info invoked
	 * @param assumeOnInvoke whether we may assume the miss happens on invoke
	 * @return miss penalty in cycles
	 */
	public long getMissOnceCost(MethodInfo mi, boolean assumeOnInvoke) {

		int words = wcetTool.getFlowGraph(mi).getNumberOfWords();
		long missCycles = methodCache.getMissPenalty(words, true);		
		if(! assumeOnInvoke) {
			long rMissCycles = methodCache.getMissPenalty(words, false);
			missCycles = Math.max( missCycles, rMissCycles );
		}
		return missCycles;
	}

}
