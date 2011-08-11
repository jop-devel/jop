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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.jgrapht.traverse.TopologicalOrderIterator;

import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.code.ExecutionContext;
import com.jopdesign.common.code.Segment;
import com.jopdesign.common.code.CallGraph.ContextEdge;
import com.jopdesign.common.code.SuperGraph.SuperEdge;
import com.jopdesign.common.code.SuperGraph.SuperGraphEdge;
import com.jopdesign.common.code.SuperGraph.SuperGraphNode;
import com.jopdesign.common.code.SuperGraph.SuperReturnEdge;
import com.jopdesign.common.misc.Filter;
import com.jopdesign.common.misc.Iterators;
import com.jopdesign.common.misc.MiscUtils;
import com.jopdesign.common.misc.MiscUtils.F1;
import com.jopdesign.wcet.WCETTool;
import com.jopdesign.wcet.analysis.GlobalAnalysis;
import com.jopdesign.wcet.analysis.InvalidFlowFactException;
import com.jopdesign.wcet.ipet.IPETConfig;
import com.jopdesign.wcet.ipet.IPETSolver;
import com.jopdesign.wcet.ipet.IPETUtils;
import com.jopdesign.wcet.ipet.LinearConstraint;
import com.jopdesign.wcet.ipet.LinearConstraint.ConstraintType;
import com.jopdesign.wcet.jop.MethodCache;

/**
 * Analysis of the variable block Method cache.
 * Goal: Detect persistence scopes.
 * This is not really important, but a good demonstration of the technique.
 * <p/>
 * TODO: [cache-analysis] Use a scopegraph instead of a callgraph
 *
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 */
public class MethodCacheAnalysis {

    /**
     * Number of blocks needed to store the instructions of the given scope
     */
    private Map<ExecutionContext, Long> blocksNeeded;
    /**
     * The project analyzed
     */
    private WCETTool wcetTool;
	private MethodCache methodCache;

    public MethodCacheAnalysis(WCETTool p) {
        this.wcetTool = p;
        this.methodCache = wcetTool.getWCETProcessorModel().getMethodCache();        
    }

    /**
     * Analyze the number of distinct cache tags accessed in each scope
     * <h2>Technique</h2>
     * <p>Traverse the call graph, and solve the IPET problem determining the maximum number of
     * distinct blocks accessed when executing the method
     * <ol>
     * <li/> Create an IPET-problem for this scope (structural constraints, flow constraints)
     * <li/> Add Block Usage Constraints.
     * <ol>
     * <li/> Split each invoke edge {@code inv(c,m)} for method {@code m} at callsite {@code c}
     * into {@code invload(c,m))} and {@code invothers(c,m)}, with
     * {@code inv(c,m) = invload(c,m) + invothers(c,m)}
     * <li/> For all invoke edges for method {@code m}, {@code sum invload(c_i, m) &lt;= 1}
     * </ol>
     * <li/> The cost for each {@code invload(c_i,m)} variable is the number of blocks used by {@code m}.
     * </ol>
     * <h2>Explanation</h2>
     * <p>Short Proof: Assume that at most {@code N} blocks used, and those {@code N} blocks
     * correspond to methods {@code M_1} through {@code M_n}. Then there is a path, s.t.
     * for each method {@code M_k} the frequency of one invoke block {@code b_i = invoke M_k}
     * is greater than zero. Conversely, if for all invoke blocks {@code b_i = invoke M_k} the
     * frequency is 0, the method is never loaded. The method at the root of the scope graph is
     * always loaded.
     * @throws InvalidFlowFactException 
     */
    public void countDistinctCacheBlocks() throws InvalidFlowFactException {
        /* Get Method Cache */
        if (!wcetTool.getWCETProcessorModel().hasMethodCache()) {
            throw new AssertionError(String.format("MethodCacheAnalysis: Processor %s has no method cache",
                    wcetTool.getWCETProcessorModel().getName()));
        }


        /* initialize result data */
        blocksNeeded = new HashMap<ExecutionContext, Long>();

        /* iterate top down the scope graph (currently: the call graph) */
        TopologicalOrderIterator<ExecutionContext, ContextEdge> iter =
                wcetTool.getCallGraph().topDownIterator();

        while (iter.hasNext()) {
            ExecutionContext scope = iter.next();

            long neededBlocks = countDistinctCacheBlocks(scope, true);
    		Logger.getLogger(this.getClass()).info("Maximum number of distinct cache tags for " +
    				scope.getMethodInfo() + " is " + neededBlocks);
    		this.blocksNeeded.put(scope, neededBlocks);
        }
    }

    
	/**
	 * Analyze the number of cache lines (ways) needed to guarantee that all methods
	 * are persistent
	 * @param scope the scope to analyze
	 * @param use integer variables (more expensive, more accurate)
	 * @return
	 * @throws InvalidFlowFactException 
	 */
	public long countDistinctCacheBlocks(ExecutionContext scope, boolean useILP) throws InvalidFlowFactException {
		
        Segment segment = Segment.methodSegment(wcetTool, scope.getMethodInfo(), scope.getCallString(),
        		                                wcetTool.getProjectConfig().callstringLength());
		return countDistinctCacheBlocks(segment, useILP);
	}
	
	/**
	 * Analyze the number of cache lines (ways) needed to guarantee that all methods
	 * are persistent
	 * @param segment the segment to analyze
	 * @param use integer variables (more expensive, more accurate)
	 * @return
	 * @throws InvalidFlowFactException 
	 */
	public long countDistinctCacheBlocks(Segment segment, boolean useILP) throws InvalidFlowFactException {
		
        IPETConfig ipetConfig = new IPETConfig(wcetTool.getConfig());

		/* create an ILP graph for all reachable methods */
		String key = String.format("method_cache_analysis:%s", segment.toString());

		/* create an global IPET problem for the supergraph */
		IPETSolver<SuperGraphEdge> ipetSolver = GlobalAnalysis.buildIpetProblem(wcetTool, key, segment, ipetConfig);

		/* Collect all method cache accesses */
		Iterable<SuperGraphEdge> cacheAccessEdges = collectCacheAccesses(segment);

		Iterable<Entry<MethodInfo, List<SuperGraphEdge>>> partition = groupAccessEdges(cacheAccessEdges);
		
		/* Add decision variables for all invoked methods, cost (blocks) and constraints */
	    for (Entry<MethodInfo, List<SuperGraphEdge>> entry : partition) {
	    		    		    	
	    	MethodInfo mi = entry.getKey();
	    	List<SuperGraphEdge> accesses = entry.getValue();

	    	/* sum(miss_edges) <= 1 */
		    LinearConstraint<SuperGraphEdge> lv = new LinearConstraint<SuperGraphEdge>(ConstraintType.LessEqual);

		    for (SuperGraphEdge access : accesses) {
		    	List<SuperGraphEdge> cacheEdges = SuperGraphSplitEdge.generateSplitEdges(access, this.getClass(), 2); 
		    	SuperGraphEdge missEdge = cacheEdges.get(0);
		    	SuperGraphEdge hitEdge  = cacheEdges.get(1);
		    	ipetSolver.addConstraint(IPETUtils.lowLevelEdgeSplit(access, missEdge, hitEdge));
		    	ipetSolver.addEdgeCost(missEdge, methodCache.requiredNumberOfBlocks(mi));
		    	lv.addLHS(missEdge, 1);		    		
		    }
		    lv.addRHS(1);
		    ipetSolver.addConstraint(lv);
		}

		/* Solve */
		double lpCost;
		try {
		    lpCost = ipetSolver.solve(null, useILP);
		} catch (Exception e) {
		    e.printStackTrace();
		    throw new RuntimeException("LP Solver failed: " + e, e);
		}
		long neededBlocks = (long) (lpCost + 0.5);
		/* Not needed any more, we take the target method of all entry edges into account
  		 * neededBlocks += methodCache.requiredNumberOfBlocks(scope.getMethodInfo());
  		 */
		return neededBlocks;
	}

	/**
	 * @param cacheAccessEdges
	 * @return
	 */
	private Iterable<Entry<MethodInfo, List<SuperGraphEdge>>> groupAccessEdges(
			Iterable<SuperGraphEdge> cacheAccessEdges) {
		/* Group method cache access by method */
		F1<SuperGraphEdge, MethodInfo> getMethodInfo = new F1<SuperGraphEdge, MethodInfo>() {
			public MethodInfo apply(SuperGraphEdge v) { return v.getTarget().getContextCFG().getCfg().getMethodInfo(); }
		};
		Iterable<Entry<MethodInfo, List<SuperGraphEdge>>> partition = MiscUtils.group(getMethodInfo, null, cacheAccessEdges).entrySet();
		return partition;
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
	 * @param scope the scope to analyze
	 * @return
	 */
	public long countTotalCacheBlocks(ExecutionContext scope) {
        long blocks = 0;
		for (MethodInfo reachable : wcetTool.getCallGraph().getReachableImplementations(scope)) {
        	blocks  += methodCache.requiredNumberOfBlocks(reachable);
        }
        return blocks;
	}

    public Map<ExecutionContext, Long> getBlockUsage() throws InvalidFlowFactException {
    	
        if (blocksNeeded == null) countDistinctCacheBlocks();
        return blocksNeeded;
    }


    /** Find a segment cover (i.e., a set of segments covering all execution paths)
     *  where each segment in the set is persistent
     *  FIXME: not yet implemented; which just return the segment itself
     * @param segment the parent segment
     * @param avoidOverlap 
     * @return
     */
	private Iterable<Segment> findPersistenceSegmentCover(Segment segment, boolean avoidOverlap) {
		List<Segment> cover = new ArrayList<Segment>();
		cover.add(segment); /* optimal cache */
		return cover;
	}

	/**
	 * Add always miss cost: for each access to the method cache, add cost of access
	 * @param segment
	 * @param ipetSolver
	 */
	public Set<SuperGraphEdge> addMissAlwaysCost(
			Segment segment,			
			IPETSolver<SuperGraphEdge> ipetSolver) {
		
		Set<SuperGraphEdge> missEdges = new HashSet<SuperGraphEdge>();
		for(SuperGraphEdge accessEdge: collectCacheAccesses(segment)) {
			missEdges.add(fixedAdditionalCostEdge(accessEdge, getMissCost(accessEdge), ipetSolver));
		}
		return missEdges;
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

	/**
	 * Add miss once cost: for each method cache persistence segment, add miss cost for the segment entries
	 * @param segment
	 * @param ipetSolver
	 */
	public Set<SuperGraphEdge> addMissOnceCost(Segment segment,
			IPETSolver<SuperGraphEdge> ipetSolver) {

		Set<SuperGraphEdge> missEdges = new HashSet<SuperGraphEdge>();
		for(Segment persistenceSegment : findPersistenceSegmentCover(segment, true)) {
			long cost = 0;
			for(MethodInfo mi : persistenceSegment.getMethods()) {
				cost += methodCache.getMaxMissCost(wcetTool.getFlowGraph(mi));
			}
			for(SuperGraphEdge entryEdge : persistenceSegment.getEntryEdges()) {
				missEdges.add(fixedAdditionalCostEdge(entryEdge, cost, ipetSolver));				
			}
		}
		return missEdges;
	}

	private SuperGraphEdge fixedAdditionalCostEdge(SuperGraphEdge accessEdge,
			long cost, IPETSolver<SuperGraphEdge> ipetSolver) {
		
		SuperGraphEdge missEdge = SuperGraphSplitEdge.generateSplitEdges(accessEdge, this.getClass(), 1).iterator().next();
		ipetSolver.addConstraint(IPETUtils.flowPreservation(Iterators.singleton(accessEdge), Iterators.singleton(missEdge)));	
		ipetSolver.addEdgeCost(missEdge, cost);
		return missEdge;
	}

	/**
	 * Add miss once constraints for all persistency regions in the segment
	 * @param segment
	 * @param ipetSolver
	 * @return
	 */
	public Set<SuperGraphEdge> addMissOnceConstraints(Segment segment,
			IPETSolver<SuperGraphEdge> ipetSolver) {

		Set<SuperGraphEdge> missEdges = new HashSet<SuperGraphEdge>();
		for(Segment persistenceSegment : findPersistenceSegmentCover(segment, false)) {
			
			for(Entry<MethodInfo, List<SuperGraphEdge>> accessed : groupAccessEdges(collectCacheAccesses(persistenceSegment))) {
				
				List<SuperGraphEdge> missOnceEdges = new ArrayList<SuperGraphEdge>();
				for(SuperGraphEdge accessEdge : accessed.getValue()) {

					long cost = getMissCost(accessEdge);
					SuperGraphEdge missEdge = SuperGraphSplitEdge.generateSplitEdges(accessEdge, this.getClass(), 1).iterator().next();
					ipetSolver.addConstraint(IPETUtils.relativeBound(Iterators.singleton(missEdge), Iterators.singleton(accessEdge), 1));	
					ipetSolver.addEdgeCost(missEdge, cost);
					missOnceEdges.add(missEdge);
				}
				ipetSolver.addConstraint(IPETUtils.flowBound(missOnceEdges,1));
				missEdges.addAll(missOnceEdges);
			}
		}
		return missEdges;
	}
}
