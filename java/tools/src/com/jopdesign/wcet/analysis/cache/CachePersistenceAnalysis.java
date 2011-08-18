/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2011, Benedikt Huber (benedikt@vmars.tuwien.ac.at)

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.jopdesign.wcet.analysis.cache;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import lpsolve.LpSolveException;

import com.jopdesign.common.code.Segment;
import com.jopdesign.common.code.SuperGraph.SuperGraphEdge;
import com.jopdesign.common.misc.Iterators;
import com.jopdesign.common.misc.MiscUtils.F1;
import com.jopdesign.wcet.WCETTool;
import com.jopdesign.wcet.analysis.GlobalAnalysis;
import com.jopdesign.wcet.analysis.InvalidFlowFactException;
import com.jopdesign.wcet.ipet.IPETConfig;
import com.jopdesign.wcet.ipet.IPETSolver;
import com.jopdesign.wcet.ipet.IPETUtils;

/**
 * Purpose: This is the common interface for cache persistence analyses,
 * either using persistence region constraints (ALL_FIT_REGION) or
 * persistence region costs (ALL_FIT_SIMPLE). 
 * 
 * <p> TODO: Currently, this classes is
 * only used as a utility class, until the design for the object and method
 * cache analyses are fletched out.</p>
 * 
 * @param <T> type of the cache tags
 *
 * <h1>Persistence Analysis</h1>
 * Analyze the number of distinct cache tags accessed in each scope
 * <h2>Technique</h2>
 * <p>Traverse the analyzed segment, and calculate the maximum number of distinct cache tags
 * mapping to one set accessed when executing the segment. Approximations for this problem 
 * can be found (with decreasing precision) by solving an ILP problem, the relaxed LP problem,
 * or by simply counting the number of distinct methods accessed.
 * </p>
 * <p>If the maximum number of distinct tags accessed is less than or equal to the
 * associativity of the set, all cache blocks for the set are persistent within the
 * segment. That is, they are missed at most once (LRU: first miss, FIFO: one miss)
 * whenever the segment is executed.</p>
 * <p>In this case, we (a) add the maximum cache cost to all entry edges of the segment
 * (ALL_FIT_SIMPLE) (b) introduce one miss-cost variable for each edge accessing a certain
 * tag, and add a constraint that at most one of these variables is not zero (ALL_FIT_REGION)
 * <h2>Explanation</h2>
 * <p>Short Proof: Assume that at most {@code N} blocks used, and those {@code N} blocks
 * correspond to cache tags {@code T_1} through {@code T_n}. Then there is a path, s.t.
 * for each tag {@code T_k} the frequency of one edge accessing the tag
 * is greater than zero. Conversely, if for all edges accessing the tag the
 * frequency is 0, the corresponding data is never loaded into the cache.
 */
public abstract class CachePersistenceAnalysis<T> {

	/**
	 * @param persistenceSegment
	 * @param ipetSolver
	 * @param missEdgesOut
	 */
	public static <T> Set<SuperGraphEdge> addPersistenceSegmentConstraints(
			Segment persistenceSegment,
			Iterable<Entry<T, List<SuperGraphEdge>>> partition,
			IPETSolver<SuperGraphEdge> ipetSolver,
			F1<SuperGraphEdge,Long> costModel,
			Object analysisKey) {
		
		HashSet<SuperGraphEdge> missEdges = new HashSet<SuperGraphEdge>();
		
		for(Entry<T, List<SuperGraphEdge>> accessed : partition) {
			
			List<SuperGraphEdge> missOnceEdges = new ArrayList<SuperGraphEdge>();
			for(SuperGraphEdge accessEdge : accessed.getValue()) {

				long cost = costModel.apply(accessEdge);
				SuperGraphEdge missEdge = SuperGraphSplitEdge.generateSplitEdges(accessEdge, analysisKey, 1).iterator().next();
				ipetSolver.addConstraint(IPETUtils.relativeBound(Iterators.singleton(missEdge), Iterators.singleton(accessEdge), 1));	
				ipetSolver.addEdgeCost(missEdge, cost);
				missOnceEdges.add(missEdge);	
			}
			ipetSolver.addConstraint(IPETUtils.flowBound(missOnceEdges,1));
			missEdges.addAll(missOnceEdges);
		}
		return missEdges;
	}

	/**
	 * Analyze the cost for loading each distinct tag in the given segment at most once.
	 * This can also be used to count the number of distinct tags (given each tag the cost of 1),
	 * or the number of cache blocks for the variable block method cache (given each tag a cost
	 * equal to the number of cache blocks it needs)
	 * @param segment the segment to analyze
	 * @param accessEdges cache access edges partitioned by tag
	 * @param costModel
	 * @param useILP use integer variables (more expensive, more accurate)
	 * @param analysisKey
	 * @param wcetTool
	 * @return
	 * @throws InvalidFlowFactException 
	 * @throws LpSolveException 
	 */
	public static<T> long computeMissOnceCost(
			Segment segment, 
			Iterable<Entry<T, List<SuperGraphEdge>>> partition,
			F1<SuperGraphEdge,Long> costModel,
			boolean useILP,
			String analysisKey,
			WCETTool wcetTool)
			throws InvalidFlowFactException, LpSolveException {
		
        IPETConfig ipetConfig = new IPETConfig(wcetTool.getConfig());

		/* create an global IPET problem for the supergraph */
		IPETSolver<SuperGraphEdge> ipetSolver = GlobalAnalysis.buildIpetProblem(wcetTool, analysisKey, segment, ipetConfig);
		
		/* add persistence constraints */
		addPersistenceSegmentConstraints(segment, partition, ipetSolver, costModel, analysisKey);

		/* Solve */
		double lpCost = ipetSolver.solve(null, useILP);		
		long maxCacheCost = (long) (lpCost + 0.5);
		return maxCacheCost;
	}


}
