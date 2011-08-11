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
import com.jopdesign.common.code.CallGraph.ContextEdge;
import com.jopdesign.common.code.SuperGraph.ContextCFG;
import com.jopdesign.common.code.SuperGraph.SuperEdge;
import com.jopdesign.common.code.SuperGraph.SuperGraphEdge;
import com.jopdesign.common.code.SuperGraph.SuperReturnEdge;
import com.jopdesign.common.code.ExecutionContext;
import com.jopdesign.common.code.Segment;
import com.jopdesign.common.code.SuperGraph;
import com.jopdesign.common.graphutils.Pair;
import com.jopdesign.common.misc.Filter;
import com.jopdesign.common.misc.MiscUtils;
import com.jopdesign.common.misc.MiscUtils.F1;
import com.jopdesign.wcet.WCETTool;
import com.jopdesign.wcet.analysis.GlobalAnalysis;
import com.jopdesign.wcet.ipet.IPETBuilder;
import com.jopdesign.wcet.ipet.IPETBuilder.ExecutionEdge;
import com.jopdesign.wcet.ipet.IPETConfig;
import com.jopdesign.wcet.ipet.IPETSolver;
import com.jopdesign.wcet.ipet.IPETUtils;
import com.jopdesign.wcet.ipet.LinearConstraint;
import com.jopdesign.wcet.ipet.LinearConstraint.ConstraintType;
import com.jopdesign.wcet.jop.MethodCache;
import org.apache.log4j.Logger;
import org.jgrapht.traverse.TopologicalOrderIterator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

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
     * Purpose: An <emph>model class</emph> for low-level method cache edges (used in the IPET calculations)
     */
    public static class MethodCacheSplitEdge {

        private SuperGraph.SuperGraphEdge interProcEdge;
        private boolean isHitEdge;

        /**
         * @param e        the corresponding supergraph edge
         * @param isHitEdge whether the edge is a miss edge
         */
        public MethodCacheSplitEdge(SuperGraph.SuperGraphEdge e, boolean isHitEdge) {
            this.interProcEdge = e;
            this.isHitEdge = isHitEdge;
        }

        /* (non-Javadoc)
                  * @see java.lang.Object#hashCode()
                  */

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (interProcEdge.hashCode());
            result = prime * result + (isHitEdge ? 1231 : 1237);
            return result;
        }

        /* (non-Javadoc)
                  * @see java.lang.Object#equals(java.lang.Object)
                  */

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            MethodCacheSplitEdge other = (MethodCacheSplitEdge) obj;
            if (!interProcEdge.equals(other.interProcEdge)) return false;
            if (isHitEdge != other.isHitEdge) return false;
            return true;
        }

        /* (non-Javadoc)
                  * @see java.lang.Object#toString()
                  */

        @Override
        public String toString() {
            return "MethodCacheSplitEdge ["
                    + (isHitEdge ? "hit! " : "miss! ")
                    + interProcEdge + "]";
        }
    }

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
     */
    public void countDistinctCacheBlocks() {
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
	 */
	public long countDistinctCacheBlocks(ExecutionContext scope, boolean useILP) {

        IPETConfig ipetConfig = new IPETConfig(wcetTool.getConfig());

		/* Create an analysis segment */
		Segment segment = getAnalysisSegment(scope);

		/* create an ILP graph for all reachable methods */
		String key = String.format("method_cache_analysis:%s", scope.toString());

		/* create an global IPET problem for the supergraph */
		IPETSolver<SuperGraphEdge> ipetSolver = GlobalAnalysis.buildIpetProblem(wcetTool, key, segment, ipetConfig);

		/* Collect all method cache accesses */
		Iterable<SuperGraphEdge> cacheAccessEdges = collectCacheAccesses(segment);
		
		/* Group method cache access by method */
		F1<SuperGraphEdge, MethodInfo> getMethodInfo = new F1<SuperGraphEdge, MethodInfo>() {
			public MethodInfo apply(SuperGraphEdge v) { return v.getTarget().getContextCFG().getCfg().getMethodInfo(); }
		};
		Map<MethodInfo, List<SuperGraphEdge>> partition = MiscUtils.group(getMethodInfo, null, cacheAccessEdges);
		
		/* Add decision variables for all invoked methods, cost (blocks) and constraints */
	    for (Entry<MethodInfo, List<SuperGraphEdge>> entry : partition.entrySet()) {
	    		    		    	
	    	MethodInfo mi = entry.getKey();
	    	List<SuperGraphEdge> accesses = entry.getValue();

	    	/* sum(miss_edges) <= 1 */
		    LinearConstraint<SuperGraphEdge> lv = new LinearConstraint<SuperGraphEdge>(ConstraintType.LessEqual);

		    for (SuperGraphEdge access : accesses) {
		    	List<SuperGraphEdge> cacheEdges = GlobalAnalysis.generateSplitEdges(access, this.getClass(), 2); 
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

    public Map<ExecutionContext, Long> getBlockUsage() {
        if (blocksNeeded == null) countDistinctCacheBlocks();
        return blocksNeeded;
    }


    private Segment getAnalysisSegment(ExecutionContext scope) {
        MethodInfo m = scope.getMethodInfo();
        return Segment.methodSegment(wcetTool, m, scope.getCallString(), wcetTool.getProjectConfig().callstringLength());
    }

    /**
     * @return a CFGEdge representing the either the hit or miss edge of an invoke or return edge
     */
    public static MethodCacheSplitEdge splitEdge(SuperGraph.SuperGraphEdge e, boolean isHitEdge) {
        return new MethodCacheSplitEdge(e, isHitEdge);
    }


}
