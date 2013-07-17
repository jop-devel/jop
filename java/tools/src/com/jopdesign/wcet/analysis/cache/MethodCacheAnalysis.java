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

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import lpsolve.LpSolveException;

import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.Type;
import org.apache.log4j.Logger;

import com.jopdesign.common.MethodCode;
import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.code.CallString;
import com.jopdesign.common.code.ControlFlowGraph;
import com.jopdesign.common.code.ControlFlowGraph.CFGNode;
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
import com.jopdesign.wcet.analysis.InvalidFlowFactException;
import com.jopdesign.wcet.ipet.IPETConfig.CacheCostCalculationMethod;
import com.jopdesign.wcet.ipet.IPETSolver;
import com.jopdesign.wcet.jop.MethodCache;

/**
 * <p>Cache persistence analysis for the variable block Method cache.</p>
 * @throws InvalidFlowFactException 
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 */
public class MethodCacheAnalysis extends CachePersistenceAnalysis {

    static final String KEY = "wcet.MethodCacheAnalysis";

    protected final WCETTool wcetTool;

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

	private MethodCache methodCache;

	public MethodCacheAnalysis(WCETTool wcetTool) {

    	this.wcetTool = wcetTool;
        this.methodCache = wcetTool.getWCETProcessorModel().getMethodCache();        
    }

	public MethodCache getMethodCache() {

		return methodCache;
	}

	@Override
	public Set<SuperGraphEdge> addCacheCost(Segment segment, IPETSolver<SuperGraphEdge> ipetSolver,
			CacheCostCalculationMethod cacheCostCalc) throws InvalidFlowFactException, LpSolveException {
		
		if(this.getMethodCache().getNumBlocks() == 0) return new HashSet<SuperGraphEdge>();
		return super.addCacheCost(segment, ipetSolver, cacheCostCalc);
	}
	
	/**
	 * Add always miss cost: for each access to the method cache, add cost of access
	 * @param segment
	 * @param ipetSolver
	 */
	@Override
	public Set<SuperGraphEdge> addMissAlwaysCost(
			Segment segment,			
			IPETSolver<SuperGraphEdge> ipetSolver) {
	
		Iterable<SuperGraphEdge> accessEdges = collectCacheAccesses(segment);
		for(SuperGraphEdge accessEdge: accessEdges) {
			if(! segment.includesEdge(accessEdge)) {
				throw new AssertionError("Subsegment edge not in segment!: "+accessEdge);
			}
		}
		return addFixedCostEdges(accessEdges, ipetSolver, EDGE_MISS_COST, KEY+"_am",0);
	}

	
	/**
	 * Add miss once cost: for each method cache persistence segment, add maximum miss cost to the segment entries
	 * @param segment
	 * @param ipetSolver
	 * @param peristenceChecks which checks to perform
	 * @throws LpSolveException 
	 * @throws InvalidFlowFactException 
	 */
	@Override
	public Set<SuperGraphEdge> addMissOnceCost(Segment segment,
			IPETSolver<SuperGraphEdge> ipetSolver, EnumSet<PersistenceCheck> checks)
					throws InvalidFlowFactException, LpSolveException {	
		
		Set<SuperGraphEdge> missEdges = new HashSet<SuperGraphEdge>();
		Map<SuperGraphEdge, Long> extraCost = new HashMap<SuperGraphEdge, Long>();
		Collection<Segment> cover = findPersistenceSegmentCover(segment, checks, true, extraCost);
		
		int tag = 0;
		for(Segment persistenceSegment : cover) {
	
			tag++;
			/* Collect all cache accesses */
			long cost = computeMissOnceCost(persistenceSegment, getCacheAccessesByTag(persistenceSegment).entrySet(), 
					EDGE_MISS_COST, true, KEY+".addMissOnceCost", wcetTool);
			F1<SuperGraphEdge, Long> costModel = MiscUtils.const1(cost);			
			Set<SuperGraphEdge> costEdges = addFixedCostEdges(persistenceSegment.getEntryEdges(), ipetSolver,
					costModel, KEY + "_miss_once", tag);
			missEdges.addAll(costEdges);
			
		}
		
		for(Entry<SuperGraphEdge, Long> entry : extraCost.entrySet()) {
			missEdges.add(fixedAdditionalCostEdge(entry.getKey(), KEY+"_am", 0, entry.getValue(), ipetSolver));
		}
		
		return missEdges;
	}
	
	/**
	 * Add miss once constraints for all subsegments in the persistence cover of the given segment
	 * @param segment
	 * @param ipetSolver
	 * @return
	 */
	@Override
	public Set<SuperGraphEdge> addMissOnceConstraints(Segment segment,
			IPETSolver<SuperGraphEdge> ipetSolver) {
	
		Set<SuperGraphEdge> missEdges = new HashSet<SuperGraphEdge>();
		Map<SuperGraphEdge, Long> extraCost = new HashMap<SuperGraphEdge, Long>();
		Collection<Segment> cover = findPersistenceSegmentCover(segment, EnumSet.allOf(PersistenceCheck.class), false, extraCost);
		int segmentCounter = 0;
		for(Segment persistenceSegment : cover) {
			/* we need to distinguish edges which are shared between persistence segments */
			String key = KEY +"_" + (++segmentCounter);
			missEdges.addAll(addPersistenceSegmentConstraints(
			    		persistenceSegment,
			    		getCacheAccessesByTag(persistenceSegment).entrySet(),
						ipetSolver,
						EDGE_MISS_COST,
						key));
		}
		for(Entry<SuperGraphEdge, Long> entry : extraCost.entrySet()) {
			missEdges.add(fixedAdditionalCostEdge(entry.getKey(), KEY+"_am", 0, entry.getValue(), ipetSolver));
		}
		
		return missEdges;
	}
	
	@Override
	public Set<SuperGraphEdge> addGlobalAllFitConstraints(Segment segment,
			IPETSolver<SuperGraphEdge> ipetSolver) {
		
		return addPersistenceSegmentConstraints(segment,
				getCacheAccessesByTag(segment).entrySet(),
				ipetSolver,
				EDGE_MISS_COST,
				KEY);
	}

	/** Find a segment cover (i.e., a set of segments covering all execution paths)
	 *  where each segment in the set is persistent (a cache persistence region (CPR))
	 *  
	 *  <h2>The simplest algorithm for a segment S (for acyclic callgraphs)</h2>
	 *   <ul><li/>Check whether S itself is CPR; if so, return S
	 *       <li/>Otherwise, create subsegments S' for each invoked method,
	 *       <li/>and single node segments for each access
	 *   </ul>
	 * @param segment the parent segment
	 * @param checks the strategy to use to determine whether a segment is a persistence region
	 * @param avoidOverlap whether overlapping segments should be avoided
	 * @param extraCostOut additional cost for edges which are considered as always-miss or not-cached
	 * @return
	 */
	protected Collection<Segment> findPersistenceSegmentCover(Segment segment, EnumSet<PersistenceCheck> checks, 
			boolean avoidOverlap, Map<SuperGraphEdge, Long> extraCostOut) {
		
		List<Segment> cover = new ArrayList<Segment>();
	
		/* We currently only support entries to one CFG */
		Set<ContextCFG> entryMethods = new HashSet<ContextCFG>();
		for(SuperGraphEdge entryEdge : segment.getEntryEdges()) {
			entryMethods.add(entryEdge.getTarget().getContextCFG());
		}
		
		if(entryMethods.size() != 1) {
			throw new AssertionError("findPersistenceSegmentCover: only supporting segments with unique entry method");
		}
		if(this.isPersistenceRegion(segment, checks)) {
			// System.err.println("Adding cover segment for: "+entryMethods);
			cover.add(segment);
		} else {
			for(Pair<SuperInvokeEdge, SuperReturnEdge> invocation : segment.getCallSitesFrom(entryMethods.iterator().next())) {
				ContextCFG callee = invocation.first().getCallee();
				// System.err.println("Recursively analyzing: "+callee);
				
				Segment subSegment = Segment.methodSegment(callee, segment.getSuperGraph());
				Collection<Segment> subRegions = findPersistenceSegmentCover(subSegment, checks, avoidOverlap, extraCostOut);
				cover.addAll(subRegions);
				SuperReturnEdge rEdge = invocation.second();
				MiscUtils.incrementBy(extraCostOut, rEdge, getMissCost(rEdge), 0);
			}
		}
		return cover;
	}

	public boolean isPersistenceRegion(WCETTool wcetTool, MethodInfo m, CallString cs, EnumSet<PersistenceCheck> tests) {

		/* fast path for the optimizer (segments are still slow) */
		if(tests == EnumSet.of(PersistenceCheck.CountTotal)) {
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

		int cacheWays = getNumberOfWays();
		long usedWays = cacheWays + 1;
		if(tests.contains(PersistenceCheck.CountTotal)) {
			usedWays = countDistinctBlocksUsed(segment);
			if(usedWays <= cacheWays) return true;			
		}
		if(tests.contains(PersistenceCheck.CountRelaxed)) {
			try {
				usedWays = countDistinctBlocksAccessed(segment, false);
				if(usedWays <= cacheWays) return true;
				if(usedWays >= cacheWays*2) return false;
			} catch(Exception ex) {
				WCETTool.logger.error("Count Distinct Cache Blocks (Relaxed LP) failed: "+ex);				
			}
		}
		if(tests.contains(PersistenceCheck.CountILP)) {
			try {
				usedWays = countDistinctBlocksAccessed(segment, true);
				if(usedWays <= cacheWays) return true;
			} catch(Exception ex) {
				WCETTool.logger.error("Count Distinct Cache Blocks (Relaxed LP) failed: "+ex);				
			}
		}
		return false;
	}

	protected int getNumberOfWays() {
		/* used by the isPersistenceRegion strategy */
		return methodCache.getNumBlocks();
	}
   	
	/**
	 * Calculate the total number of distinct method cache blocks possibly accessed in this
	 * segment (not restricted to one execution)
	 * @param segment The segment to consider
	 * @return
	 */
	public long countDistinctBlocksUsed(Segment segment) {
		
	    long blocks = 0;
		for (MethodInfo reachable : this.getCacheAccessesByTag(segment).keySet()) {
	    	blocks  += methodCache.requiredNumberOfBlocks(reachable);
	    }
	    return blocks;
	}

	/**
	 * Analyze the maximum number of distinct cache lines (ways) possibly accessed during
	 * one execution of the segment
	 * @param segment the segment to analyze
	 * @param use integer variables (more expensive, more accurate)
	 * @return
	 * @throws InvalidFlowFactException 
	 * @throws LpSolveException 
	 */
	public long countDistinctBlocksAccessed(Segment segment, boolean useILP) throws InvalidFlowFactException, LpSolveException {
		
		return computeMissOnceCost(segment, getCacheAccessesByTag(segment).entrySet(), 
					NUMBER_OF_BLOCKS, useILP, KEY+".countDistinctCacheBlocks", wcetTool);
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
	 * plus all entry edges from the CFG entry.
	 * @param segment
	 * @return
	 */
	private Iterable<SuperGraphEdge> collectCacheAccesses(final Segment segment) {
		
		return new Filter<SuperGraphEdge>() {

			@Override
			protected boolean include(SuperGraphEdge e) {
				if(e instanceof SuperEdge) {
					return true;
				} else if(segment.getEntryEdges().contains(e)) {
					return true;
				} else {
					return false;
				}
			}				
		}.filter(segment.getEdges()); 		
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
			InstructionHandle invokeIns = ((SuperInvokeEdge) accessEdge).getInvokeNode().getInvokeSite().getInstructionHandle();
			return methodCache.getMissPenaltyOnInvoke(cfg.getNumberOfWords(), invokeIns.getInstruction());			
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
			long iMiss = methodCache.getMissPenaltyOnInvoke(invokedCfg.getNumberOfWords(), invokeSite.getInstructionHandle().getInstruction());
			if(iMiss > iMissMax) iMissMax = iMiss;
		}
		return iMissMax + rMiss;
	}
	
    /**
     * Check that cache is big enough to hold any method possibly invoked
     * Return largest method
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
