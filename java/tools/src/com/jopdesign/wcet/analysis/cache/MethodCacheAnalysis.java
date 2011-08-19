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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lpsolve.LpSolveException;

import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.code.Segment;
import com.jopdesign.common.code.SuperGraph.ContextCFG;
import com.jopdesign.common.code.SuperGraph.SuperEdge;
import com.jopdesign.common.code.SuperGraph.SuperGraphEdge;
import com.jopdesign.common.code.SuperGraph.SuperGraphNode;
import com.jopdesign.common.code.SuperGraph.SuperInvokeEdge;
import com.jopdesign.common.code.SuperGraph.SuperReturnEdge;
import com.jopdesign.common.graphutils.Pair;
import com.jopdesign.common.misc.Filter;
import com.jopdesign.common.misc.MiscUtils;
import com.jopdesign.common.misc.MiscUtils.F1;
import com.jopdesign.wcet.WCETTool;
import com.jopdesign.wcet.analysis.InvalidFlowFactException;
import com.jopdesign.wcet.ipet.IPETSolver;
import com.jopdesign.wcet.jop.MethodCache;

/**
 * <p>Cache persistence analysis for the variable block Method cache.</p>
 * @throws InvalidFlowFactException 
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 */
public class MethodCacheAnalysis extends CachePersistenceAnalysis<MethodInfo> {

    private static final String KEY = MethodCacheAnalysis.class.getCanonicalName();

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

	/**
	 * Perform a heuristic check whether in the given segment all methods are persistent
	 * @param segment
	 * @return true if all methods are persistent in the method cache, false if not sure
	 * @throws LpSolveException 
	 * @throws InvalidFlowFactException 
	 */
	protected boolean isPersistenceRegionHeuristic(Segment segment)  {

		int cacheBlocks = methodCache.getNumBlocks();
		String technique = "count";
		long usedBlocks = countTotalCacheBlocks(segment);
		if(usedBlocks <= cacheBlocks) return true;
		try {
			technique = "Relaxed LP";
			// usedBlocks = countDistinctCacheBlocks(segment, false);
			if(usedBlocks <= cacheBlocks) return true;
			if(usedBlocks >= cacheBlocks*2) return false;
			technique = "ILP";
			return countDistinctCacheBlocks(segment, true) <= cacheBlocks;
		} catch (Exception e) {
			WCETTool.logger.error("Count Distinct Cache Blocks: "+technique+" failed: "+e);
			try {
				File dumpFile = new File(wcetTool.getOutDir("segments"),"segment_"+(keygen++)+".dot");
				WCETTool.logger.error("Count Distinct Cache Blocks failed, dumping segment to "+dumpFile+": "+e);
				segment.exportDOT(dumpFile);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return false;
		}
	}
    private static int keygen = 0;
   	
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
					NUMBER_OF_BLOCKS, true, KEY, wcetTool);
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
		if(this.isPersistenceRegionHeuristic(segment)) {
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
	 * @param accessEdge
	 * @return
	 */
	private long getMissCost(SuperGraphEdge accessEdge) {
		SuperGraphNode accessed = accessEdge.getTarget();
		if(accessEdge instanceof SuperReturnEdge) {
			/* return edge: return cost */
			return methodCache.getMissOnReturnCost(accessed.getCfg());
		} else {
			/* entry edge of the segment, or invoke edge: invoke cost */
			return methodCache.getMissOnInvokeCost(accessed.getCfg());
		}
	}

}
