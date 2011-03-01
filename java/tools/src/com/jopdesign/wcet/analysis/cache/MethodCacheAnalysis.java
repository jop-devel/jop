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
import com.jopdesign.common.code.ExecutionContext;
import com.jopdesign.common.code.SuperGraph;
import com.jopdesign.common.graphutils.Pair;
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
        private boolean isInvoke;

        /**
         * @param e        the corresponding supergraph edge
         * @param isInvoke whether the edge is an invoke edge
         */
        public MethodCacheSplitEdge(SuperGraph.SuperGraphEdge e, boolean isInvoke) {
            this.interProcEdge = e;
            this.isInvoke = isInvoke;
        }

        /* (non-Javadoc)
                  * @see java.lang.Object#hashCode()
                  */

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (interProcEdge.hashCode());
            result = prime * result + (isInvoke ? 1231 : 1237);
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
            if (isInvoke != other.isInvoke) return false;
            return true;
        }

        /* (non-Javadoc)
                  * @see java.lang.Object#toString()
                  */

        @Override
        public String toString() {
            return "MethodCacheSplitEdge [edge=" + interProcEdge
                    + (isInvoke ? "invoke" : "return") + "]";
        }
    }

    /**
     * Number of blocks needed to store the instructions of the given scope
     */
    private Map<ExecutionContext, Long> blocksNeeded;
    /**
     * The project analyzed
     */
    private WCETTool project;

    public MethodCacheAnalysis(WCETTool p) {
        this.project = p;
    }

    /**
     * Analyze the number of blocks needed by each scope.
     * <h2>Technique</h2>
     * <p>Traverse the scope graph, create a local ILP, and find maximum number of blocks</p>
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
    public void analyzeBlockUsage() {
        /* Get Method Cache */
        if (!project.getWCETProcessorModel().hasMethodCache()) {
            throw new AssertionError(String.format("MethodCacheAnalysis: Processor %s has no method cache",
                    project.getWCETProcessorModel().getName()));
        }

        MethodCache methodCache = project.getWCETProcessorModel().getMethodCache();
        IPETConfig ipetConfig = new IPETConfig(project.getConfig());

        /* initialize result data */
        blocksNeeded = new HashMap<ExecutionContext, Long>();

        /* iterate top down the scope graph (currently: the call graph) */
        TopologicalOrderIterator<ExecutionContext, ContextEdge> iter =
                project.getCallGraph().topDownIterator();

        while (iter.hasNext()) {
            ExecutionContext scope = iter.next();

            /* Create a supergraph */
            SuperGraph sg = getScopeSuperGraph(scope);

            /* create an ILP graph for all reachable methods */
            String key = String.format("method_cache_analysis:%s", scope.toString());

            /* create an global IPET problem for the supergraph */
            IPETSolver ipetSolver = GlobalAnalysis.buildIpetProblem(project, key, sg, ipetConfig);
            IPETBuilder<SuperGraph.CallContext> ipetBuilder = new IPETBuilder<SuperGraph.CallContext>(project, null);

            /* Add decision variables for all invoked methods, cost (blocks) and constraints */
            Map<MethodInfo, List<Pair<SuperGraph.SuperInvokeEdge, SuperGraph.SuperReturnEdge>>> callSites = sg.getAllCallSites();

            callSites.remove(scope.getMethodInfo());

            for (MethodInfo mi : callSites.keySet()) {

                /* sum(load_edges) <= 1 */
                LinearConstraint<ExecutionEdge> lv = new LinearConstraint<ExecutionEdge>(ConstraintType.LessEqual);
                for (Pair<SuperGraph.SuperInvokeEdge, SuperGraph.SuperReturnEdge> callSite : callSites.get(mi)) {
                    SuperGraph.SuperInvokeEdge invokeEdge = callSite.first();
                    /* add load and use edges */
                    ipetBuilder.changeContext(invokeEdge.getCallContext());
                    ExecutionEdge parentEdge = ipetBuilder.newEdge(invokeEdge);
                    ExecutionEdge loadEdge = ipetBuilder.newEdge(MethodCacheAnalysis.splitEdge(invokeEdge, true));
                    ExecutionEdge useEdge = ipetBuilder.newEdge(MethodCacheAnalysis.splitEdge(invokeEdge, false));
                    ipetSolver.addConstraint(IPETUtils.lowLevelEdgeSplit(parentEdge, loadEdge, useEdge));
                    ipetSolver.addEdgeCost(loadEdge, methodCache.requiredNumberOfBlocks(mi));
                    lv.addLHS(loadEdge, 1);
                }
                lv.addRHS(1);
                ipetSolver.addConstraint(lv);
            }

            /* Return variables */
            Map<ExecutionEdge, Long> flowMap = new HashMap<ExecutionEdge, Long>();

            /* Solve */
            double lpCost;
            try {
                lpCost = ipetSolver.solve(flowMap);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("LP Solver failed: " + e, e);
            }
            long neededBlocks = (long) (lpCost + 0.5);
            neededBlocks += methodCache.requiredNumberOfBlocks(scope.getMethodInfo());
            Logger.getLogger(this.getClass()).info("Number of Blocks for " + scope.getMethodInfo() + " is " + neededBlocks);
            this.blocksNeeded.put(scope, neededBlocks);
        }
    }

    public Map<ExecutionContext, Long> getBlockUsage() {
        if (blocksNeeded == null) analyzeBlockUsage();
        return blocksNeeded;
    }


    private SuperGraph getScopeSuperGraph(ExecutionContext scope) {
        MethodInfo m = scope.getMethodInfo();
        return new SuperGraph(project.getAppInfo(), project.getFlowGraph(m), project.getProjectConfig().callstringLength());
    }

    /**
     * @return a CFGEdge representing the either the hit or miss edge of an invoke or return edge
     */
    public static MethodCacheSplitEdge splitEdge(SuperGraph.SuperGraphEdge e, boolean b) {
        return new MethodCacheSplitEdge(e, b);
    }


}
