/*
 * This file is part of JOP, the Java Optimized Processor
 *   see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2011, Stefan Hepp (stefan@stefant.org).
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

package com.jopdesign.jcopter.analysis;

import com.jopdesign.common.AppInfo;
import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.code.CallGraph;
import com.jopdesign.common.code.CallGraph.ContextEdge;
import com.jopdesign.common.code.ControlFlowGraph.InvokeNode;
import com.jopdesign.common.code.ExecutionContext;
import com.jopdesign.common.code.InvokeSite;
import com.jopdesign.common.graphutils.DFSTraverser;
import com.jopdesign.common.graphutils.DFSTraverser.DFSEdgeType;
import com.jopdesign.common.graphutils.DFSTraverser.DFSVisitor;
import com.jopdesign.common.graphutils.DFSTraverser.EmptyDFSVisitor;
import com.jopdesign.common.graphutils.GraphUtils;
import com.jopdesign.common.misc.AppInfoError;
import com.jopdesign.common.misc.MiscUtils;
import com.jopdesign.jcopter.JCopter;
import com.jopdesign.wcet.WCETTool;
import com.jopdesign.wcet.analysis.AnalysisContextLocal;
import com.jopdesign.wcet.analysis.GlobalAnalysis;
import com.jopdesign.wcet.analysis.LocalAnalysis;
import com.jopdesign.wcet.analysis.RecursiveAnalysis;
import com.jopdesign.wcet.analysis.RecursiveAnalysis.RecursiveStrategy;
import com.jopdesign.wcet.analysis.WcetCost;
import com.jopdesign.wcet.ipet.IPETConfig;
import com.jopdesign.wcet.ipet.IPETConfig.CacheCostCalculationMethod;
import com.jopdesign.wcet.jop.MethodCache;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.log4j.Logger;
import org.jgrapht.alg.TransitiveClosure;
import org.jgrapht.graph.SimpleDirectedGraph;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * This analysis keeps track of the number of bytes and blocks of code reachable from a method and
 * provides estimations of cache miss numbers.
 *
 * TODO create subclasses for various analysis types instead of enum
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class MethodCacheAnalysis {

    public enum AnalysisType { ALWAYS_MISS, ALWAYS_HIT, ALWAYS_MISS_OR_HIT, ALL_FIT_REGIONS }

    private static final Logger logger = Logger.getLogger(JCopter.LOG_ANALYSIS+".MethodCacheAnalysis");

    private final JCopter jcopter;
    private final MethodCache cache;
    private final CallGraph callGraph;
    private final Map<ExecutionContext,Integer> cacheBlocks;
    private final Map<ExecutionContext, Set<MethodInfo>> reachableMethods;
    private final AnalysisType analysisType;

    private final Set<MethodInfo> classifyChanges;

    ///////////////////////////////////////////////////////////////////////////////////
    // Constructors, initialization, standard getter
    ///////////////////////////////////////////////////////////////////////////////////

    public MethodCacheAnalysis(JCopter jcopter, AnalysisType analysisType, CallGraph callGraph) {
        this.jcopter = jcopter;
        this.analysisType = analysisType;
        this.cache = jcopter.getMethodCache();
        this.callGraph = callGraph;

        cacheBlocks = new LinkedHashMap<ExecutionContext, Integer>(callGraph.getNodes().size());
        reachableMethods = new LinkedHashMap<ExecutionContext, Set<MethodInfo>>(callGraph.getNodes().size());
        classifyChanges = new LinkedHashSet<MethodInfo>();
    }

    public CallGraph getCallGraph() {
        return callGraph;
    }

    public void initialize() {

        cacheBlocks.clear();
        reachableMethods.clear();

        if (analysisType == AnalysisType.ALWAYS_HIT || analysisType == AnalysisType.ALWAYS_MISS) {
            // we do not use the number of blocks for those analyses
            return;
        }

        // we add the method to the reachable set anyway, so we can ignore self-loops
        SimpleDirectedGraph<ExecutionContext,ContextEdge> closure = GraphUtils.createSimpleGraph(callGraph.getGraph());
        TransitiveClosure.INSTANCE.closeSimpleDirectedGraph(closure);

        updateNodes(closure, closure.vertexSet(), false);
    }

    ///////////////////////////////////////////////////////////////////////////////////
    // Query analysis results
    ///////////////////////////////////////////////////////////////////////////////////

    public boolean allFit(MethodInfo method) {
        for (ExecutionContext node : callGraph.getNodes(method)) {
            if (!allFit(node)) return false;
        }
        return true;
    }

    public boolean allFit(ExecutionContext node) {
        Integer blocks = cacheBlocks.get(node);
        if (blocks == null) {
            // not a node in the graph .. over-approximate simply by checking all nodes
            // do not call allFit(MethodInfo) or we might get endless recursion if for some reason the analysis
            // has not been updated for this method
            for (ExecutionContext n : callGraph.getNodes(node.getMethodInfo())) {
                blocks = cacheBlocks.get(n);
                if (blocks == null) {
                    logger.warn("No analysis results for method "+node.getMethodInfo());
                    return false;
                }
                if (!cache.allFit(blocks)) return false;
            }
            return true;
        }
        return cache.allFit(blocks);
    }

    public long getInvokeReturnCacheCosts(ExecFrequencyProvider ecp, InvokeSite invokeSite) {
        if (analysisType == AnalysisType.ALWAYS_HIT) return 0;

        AppInfo appInfo = AppInfo.getSingleton();
        int size = 0;
        for (MethodInfo method : appInfo.findImplementations(invokeSite)) {
            size = Math.max(size, getMethodSize(method));
        }
        size = MiscUtils.bytesToWords(size);

        int sizeInvoker = getMethodSize(invokeSite.getInvoker());
        sizeInvoker = MiscUtils.bytesToWords(sizeInvoker);

        long invokeCosts = cache.getMissPenaltyOnInvoke(size, invokeSite.getInvokeInstruction());
        long returnCosts = cache.getMissPenaltyOnReturn(sizeInvoker,invokeSite.getInvokeeRef().getDescriptor().getType());

        return getInvokeReturnCacheCosts(ecp, invokeSite, invokeCosts, returnCosts);
    }

    /**
     * @param ecp exec count provider
     * @param invokeSite the invoke site to check
     * @param invokeCacheCosts additional cycles for a single cache miss on invoke
     * @param returnCacheCosts additional cycles for a single cache miss on return
     * @return the number of cache miss cycles for all executions of the invoke
     */
    public long getInvokeReturnCacheCosts(ExecFrequencyProvider ecp, InvokeSite invokeSite,
                                         long invokeCacheCosts, long returnCacheCosts)
    {
        if (analysisType == AnalysisType.ALWAYS_HIT) return 0;
        if (analysisType == AnalysisType.ALWAYS_MISS || !allFit(invokeSite.getInvoker())) {
            // outside all-fit, every invoke is a miss
            long count = ecp.getExecCount(invokeSite);
            return count * (invokeCacheCosts + returnCacheCosts);
        }
        if (analysisType == AnalysisType.ALWAYS_MISS_OR_HIT) {
            return 0;
        }
        // invoke cache misses
        long misses = getPersistentMisses(ecp, callGraph.getNodes(invokeSite.getInvoker()));
        if (cache.isLRU()) {
            // we only have cache costs at the invoke, we can assume the return is always a hit
            return misses * invokeCacheCosts;
        }
        // we may have return cache misses too, over-approximate
        return misses * (invokeCacheCosts + returnCacheCosts);
    }

    public long getInvokeMissCount(ExecFrequencyProvider ecp, InvokeSite invokeSite, MethodInfo invokee) {
        if (analysisType == AnalysisType.ALWAYS_HIT) return 0;
        if (analysisType == AnalysisType.ALWAYS_MISS || !allFit(invokeSite.getInvoker())) {
            // outside all-fit, every invoke is a miss
            return ecp.getExecCount(invokeSite, invokee);
        }
        if (analysisType == AnalysisType.ALWAYS_MISS_OR_HIT || cache.isLRU()) {
            return 0;
        }
        return getPersistentMisses(ecp, callGraph.getNodes(invokeSite.getInvoker()));
    }

    public long getInvokeMissCount(ExecFrequencyProvider ecp, MethodInfo invokee) {
        long count = 0;

        // get all invoke sites of the invokee
        for (InvokeSite invokeSite : callGraph.getInvokeSites(invokee)) {
            count += getInvokeMissCount(ecp, invokeSite, invokee);
        }

        return count;
    }

    /**
     * @param ecp exec count provider
     * @param context the method to calculate return cache misses for, and a context in which the method is invoked.
     *        The cache missese are calculated only for the executions with the given context.
     * @return the total cache misses for all returns to the given method
     */
    public long getReturnMissCount(ExecFrequencyProvider ecp, ExecutionContext context) {
        if (analysisType == AnalysisType.ALWAYS_HIT) return 0;
        if (analysisType == AnalysisType.ALWAYS_MISS || !allFit(context.getMethodInfo())) {
            // outside all-fit, every invoke returns with a miss
            long retCount = 0;
            for (InvokeSite invokeSite : context.getMethodInfo().getCode().getInvokeSites()) {
                retCount += ecp.getExecFrequency(invokeSite);
            }
            return ecp.getExecCount(context) * retCount;
        }
        if (analysisType == AnalysisType.ALWAYS_MISS_OR_HIT || cache.isLRU()) {
            return 0;
        }
        // we have at most one return miss of invokes in this method
        return getPersistentMisses(ecp, callGraph.getNodes(context));
    }

    public long getReturnMissCount(ExecFrequencyProvider ecp, CodeModification modification) {
        if (analysisType == AnalysisType.ALWAYS_HIT) return 0;
        if (analysisType == AnalysisType.ALWAYS_MISS || !allFit(modification.getMethod())) {
            // outside all-fit, every invoke returns with a miss
            long retCount = 0;
            for (InvokeSite invokeSite : modification.getMethod().getCode().getInvokeSites()) {
                InstructionHandle ih = invokeSite.getInstructionHandle();
                if (modification.getStart().getPosition() <= ih.getPosition() &&
                    modification.getEnd().getPosition() >= ih.getPosition())
                {
                    continue;
                }
                retCount += ecp.getExecFrequency(invokeSite);
            }
            return ecp.getExecCount(modification.getMethod()) * retCount;
        }
        if (analysisType == AnalysisType.ALWAYS_MISS_OR_HIT || cache.isLRU()) {
            return 0;
        }
        // we have at most one return miss of invokes in this method
        return getPersistentMisses(ecp, callGraph.getNodes(modification.getMethod()));
    }

    /**
     * @param ecp execution counts to use
     * @param modification the modifications which will be done
     * @return the number of cache miss cycles due to the code size change, excluding the effects on the modified code
     */
    public long getDeltaCacheMissCosts(ExecFrequencyProvider ecp, CodeModification modification) {
        if (analysisType == AnalysisType.ALWAYS_HIT) return 0;

        int deltaBytes = modification.getDeltaLocalCodesize();
        if (deltaBytes == 0) return 0;

        MethodInfo method = modification.getMethod();

        int size = getMethodSize(method);
        int oldWords = MiscUtils.bytesToWords(size);
        int newWords = MiscUtils.bytesToWords(size+deltaBytes);

        int deltaBlocks = cache.requiredNumberOfBlocks(newWords) - cache.requiredNumberOfBlocks(oldWords);
        //int newBlocks = getRequiredBlocks(method) + deltaBlocks;

        // calc various cache miss cost deltas
        long deltaInvokeCacheMissCosts = cache.getMissPenalty(newWords, true) -
                                         cache.getMissPenalty(oldWords, true);
        long deltaReturnCacheMissCosts = cache.getMissPenalty(newWords, false) -
                                         cache.getMissPenalty(oldWords, false);

        long costs = 0;

        // we have cache costs due to invokes of the modified method
        costs += getInvokeMissCount(ecp, modification.getMethod()) * deltaInvokeCacheMissCosts;
        // .. and due to returns from invokees to the modified method
        costs += getReturnMissCount(ecp, modification) * deltaReturnCacheMissCosts;
        // .. and because other methods may not fit into the cache anymore
        costs += getAllFitChangeCosts(ecp, modification, deltaBlocks);

        return costs;
    }

    private long getAllFitChangeCosts(ExecFrequencyProvider ecp, CodeModification modification, int deltaBlocks)
    {
        if (analysisType == AnalysisType.ALWAYS_HIT || analysisType == AnalysisType.ALWAYS_MISS) {
            return 0;
        }

        int deltaBytes = modification.getDeltaLocalCodesize();
        MethodInfo method = modification.getMethod();

        // for ALWAYS_MISS_HIT oder MOST_ONCE we need to find out what has changed for all-fit
        Set<MethodInfo> changes = findClassificationChanges(method, deltaBlocks,
                                                            modification.getRemovedInvokees(), false);

        AppInfo appInfo = AppInfo.getSingleton();

        // In all nodes where we have changes, we need to sum up the new costs
        long deltaCosts = 0;
        for (MethodInfo node : changes) {
            // we do not need to count the invokes of the method itself
            // but all invokes in the method are now no longer always-hit/-miss
            for (InvokeSite invokeSite : node.getCode().getInvokeSites()) {

                // Note: this is very similar to getInvokeReturnCacheCosts(invokeSite), but we cannot use
                //       this here, because that method uses allFit and does not honor our 'virtual' codesize change
                int size = 0;
                for (MethodInfo impl : appInfo.findImplementations(invokeSite)) {
                    size = Math.max(size, getMethodSize(impl));
                }
                size = MiscUtils.bytesToWords(size);

                int sizeInvoker = getMethodSize(invokeSite.getInvoker());
                sizeInvoker = MiscUtils.bytesToWords(sizeInvoker);

                long invokeCosts = cache.getMissPenaltyOnInvoke(size, invokeSite.getInvokeInstruction());
                long returnCosts = cache.getMissPenaltyOnReturn(sizeInvoker,invokeSite.getInvokeeRef().getDescriptor().getType());

                long count = ecp.getExecCount(invokeSite);
                if (analysisType == AnalysisType.ALL_FIT_REGIONS) {
                    // for this analysis we already have one miss in the original cost estimation
                    count--;
                }
                deltaCosts += count * (invokeCosts + returnCosts);
            }
        }

        // if the code increased, the classification changed from always-hit to always-miss ..
        long costs = deltaBytes > 0 ? deltaCosts : -deltaCosts;

        if (analysisType == AnalysisType.ALL_FIT_REGIONS) {

            // find out how many additional persistent cache misses we have
            // find out border of new all-fit region
            Map<MethodInfo,Integer> deltaExec = new LinkedHashMap<MethodInfo, Integer>();
            int deltaCount = 0;
            Set<ExecutionContext> border = new LinkedHashSet<ExecutionContext>();

            if (deltaBlocks < 0) {
                throw new AppInfoError("Not implemented");
            } else {

                for (MethodInfo miss : changes) {
                    for (ExecutionContext context : callGraph.getNodes(miss)) {
                        for (ExecutionContext invokee : callGraph.getChildren(context)) {
                            // not all-fit if in changeset
                            if (changes.contains(invokee.getMethodInfo())) continue;
                            // we ignore native stuff
                            if (invokee.getMethodInfo().isNative()) continue;
                            // invokee is all-fit
                            if (border.add(invokee)) {
                                deltaCount += ecp.getExecCount(invokee);
                            }
                        }
                    }
                }

                // remove old miss count
                deltaCount -= getPersistentMisses(ecp, border);
            }

            // TODO this is not quite correct: instead of joining the reachable sets and multiplying
            //  with the delta count for the whole region, we should:
            //  - for every node in the reachable sets of the new border, sum up exec-counts of border nodes
            //    which contain that node in the reachable set
            //  - for every node in the reachable sets of the old border, subtract the exec counts of those border nodes
            //  - sum up invoke miss costs times calculates delta counts per node

            // find out cache miss costs of new all-fit region
            int regionCosts = 0;
            Set<MethodInfo> visited = new LinkedHashSet<MethodInfo>();
            for (ExecutionContext context : border) {
                for (MethodInfo reachable : reachableMethods.get(context)) {
                    if (visited.add(reachable)) {
                        regionCosts += cache.getMissPenalty(reachable.getCode().getNumberOfWords(), cache.isLRU());
                    }
                }
            }

            costs += deltaCount * regionCosts;
        }

        return costs;
    }

    ///////////////////////////////////////////////////////////////////////////////////
    // Notify of callgraph updates, query change sets
    ///////////////////////////////////////////////////////////////////////////////////

    public void clearChangeSet() {
        classifyChanges.clear();
    }

    /**
     * @return all methods containing invokeSites for which the cache analysis changed
     */
    public Set<MethodInfo> getClassificationChangeSet() {
        return classifyChanges;
    }

    /**
     * @param ecp exec count provider
     * @return all methods for which the cache costs of the contained invoke sites changed, either due to classification
     *         changes or due to execution count changes.
     */
    public Set<MethodInfo> getMissCountChangeSet(ExecFrequencyProvider ecp) {
        if (analysisType == AnalysisType.ALWAYS_HIT) return Collections.emptySet();

        Set<MethodInfo> countChanges = new LinkedHashSet<MethodInfo>(classifyChanges);

        // we check the exec analysis for changed exec counts,
        // need to update change sets since cache miss counts changed for cache-misses
        Set<MethodInfo> methods = ecp.getChangeSet();
        if (analysisType == AnalysisType.ALWAYS_MISS) {
            countChanges.addAll(methods);
            return countChanges;
        }

        for (MethodInfo method : methods) {
            if (!allFit(method)) {
                countChanges.add(method);
            }
        }

        if (analysisType == AnalysisType.ALL_FIT_REGIONS) {
            for (MethodInfo method : ecp.getChangeSet()) {
                if (!classifyChanges.contains(method)) {
                    continue;
                }
                // all methods for which the classification changed and for which the exe count changed..
                for (ExecutionContext context : callGraph.getNodes(method)) {
                    // add all reachable methods
                    countChanges.addAll(reachableMethods.get(context));
                }
            }
        }

        return countChanges;
    }

    public void inline(CodeModification modification, InvokeSite invokeSite, MethodInfo invokee) {
        if (analysisType == AnalysisType.ALWAYS_HIT || analysisType == AnalysisType.ALWAYS_MISS) return;

        Set<ExecutionContext> nodes = new LinkedHashSet<ExecutionContext>();

        // We need to go down first, find all new nodes
        MethodInfo invoker = invokeSite.getInvoker();
        LinkedList<ExecutionContext> queue = new LinkedList<ExecutionContext>(callGraph.getNodes(invoker));

        // TODO if the callgraph is compressed, we need to go down all callstring-length long paths
        while (!queue.isEmpty()) {
            ExecutionContext node = queue.remove();
            for (ExecutionContext child : callGraph.getChildren(node)) {
                if (!cacheBlocks.containsKey(child) && !nodes.contains(child)) {
                    nodes.add(child);
                    queue.add(child);
                }
            }
        }

        // update reachable set and codesize for all new nodes
        updateNewNodes(nodes);

        // To get the old size, use any execution node ..
        ExecutionContext node = callGraph.getNodes(invoker).iterator().next();

        // this includes all reachable methods, so we need to subtract them
        int oldBlocks = cacheBlocks.get(node);
        for (MethodInfo m : reachableMethods.get(node)) {
            int size = MiscUtils.bytesToWords(getMethodSize(m));
            oldBlocks -= cache.requiredNumberOfBlocks(size);
        }

        int size = MiscUtils.bytesToWords(getMethodSize(invoker));
        int newBlocks = cache.requiredNumberOfBlocks(size);

        // not using CodeModification codesize delta because it might be an estimation
        int deltaBlocks = newBlocks - oldBlocks;

        // now go up from the modified method, remove invokee from reachable sets if last invoke was inlined
        // and update block counts
        findClassificationChanges(invoker, deltaBlocks, modification.getRemovedInvokees(), true);
    }

    /**
     * Recalculate reachable sets and block counts for the given nodes. Other nodes are *not* updated.
     * @param nodes the nodes to recalculate
     */
    private void updateNewNodes(Set<ExecutionContext> nodes) {

        if (analysisType == AnalysisType.ALWAYS_HIT || analysisType == AnalysisType.ALWAYS_MISS || nodes.isEmpty()) {
            return;
        }

        // create closure for new nodes and their childs
        SimpleDirectedGraph<ExecutionContext,ContextEdge> closure =
                GraphUtils.createSimpleGraph(callGraph.getGraph(), nodes, true);
        TransitiveClosure.INSTANCE.closeSimpleDirectedGraph(closure);

        updateNodes(closure, nodes, true);
    }

    ///////////////////////////////////////////////////////////////////////////////////
    // WCA Interface
    ///////////////////////////////////////////////////////////////////////////////////

    public RecursiveStrategy<AnalysisContextLocal,WcetCost>
           createRecursiveStrategy(WCETTool tool, IPETConfig ipetConfig, CacheCostCalculationMethod cacheApprox)
    {
        if (cacheApprox.needsInterProcIPET()) {
            // TODO use method-cache for all-fit
            return new GlobalAnalysis.GlobalIPETStrategy(ipetConfig);
        }

        return new LocalAnalysis(tool, ipetConfig) {
            @Override
            public WcetCost recursiveCost(RecursiveAnalysis<AnalysisContextLocal, WcetCost> stagedAnalysis,
                                          InvokeNode n, AnalysisContextLocal ctx) {

            	AnalysisContextLocal newCtx = ctx;
                if (analysisType == AnalysisType.ALWAYS_MISS_OR_HIT &&
                        allFit(stagedAnalysis.getWCETTool(), n.getInvokeSite().getInvoker(), ctx.getCallString()))
                {
                    newCtx = ctx.withCacheApprox(CacheCostCalculationMethod.ALWAYS_HIT);
                }
                return super.recursiveCost(stagedAnalysis, n, newCtx);
            }
        };
    }

    ///////////////////////////////////////////////////////////////////////////////////
    // Private stuff
    ///////////////////////////////////////////////////////////////////////////////////

    private int getMethodSize(MethodInfo method) {
        // TODO should we use Java size to make things faster?
        if (method.isNative()) return 0;
        return method.getCode().getNumberOfBytes();
    }

    private void updateNodes(SimpleDirectedGraph<ExecutionContext,ContextEdge> closure,
                             Set<ExecutionContext> nodes, boolean reuseResults)
    {

        for (ExecutionContext node : nodes) {
            if (node.getMethodInfo().isNative()) continue;

            // We could make this more memory efficient, because in many cases we do not need a
            // separate set for each node, but this would be more complicated to calculate
            Set<MethodInfo> reachable = new LinkedHashSet<MethodInfo>();

            reachable.add(node.getMethodInfo());
            // we only need to add all children to the set, no need to go down the graph
            for (ContextEdge edge : closure.outgoingEdgesOf(node)) {
                ExecutionContext target = edge.getTarget();
                if (target.getMethodInfo().isNative()) continue;

                if (reuseResults && !nodes.contains(target)) {
                    reachable.addAll(reachableMethods.get(target));
                } else {
                    reachable.add(target.getMethodInfo());
                }
            }

            reachableMethods.put(node, reachable);
        }

        MethodCache cache = jcopter.getMethodCache();

        // now we can sum up the cache blocks for all nodes in the graph
        for (ExecutionContext node : nodes) {
            if (node.getMethodInfo().isNative()) continue;

            Set<MethodInfo> reachable = reachableMethods.get(node);

            int blocks = 0;
            for (MethodInfo method : reachable) {
                int size = MiscUtils.bytesToWords(getMethodSize(method));
                blocks += cache.requiredNumberOfBlocks(size);
            }

            cacheBlocks.put(node, blocks);
        }
    }

    private Set<MethodInfo> findClassificationChanges(MethodInfo method, final int deltaBlocks,
                                                      Collection<MethodInfo> removed, final boolean update)
    {
        if (analysisType == AnalysisType.ALWAYS_HIT || analysisType == AnalysisType.ALWAYS_MISS ||
                (deltaBlocks == 0 && removed.isEmpty()) )
        {
            return Collections.emptySet();
        }

        Set<ExecutionContext> roots = callGraph.getNodes(method);

        // First, go up and find all nodes where one or more methods need to be removed from the reachable methods set
        final Map<ExecutionContext,Set<MethodInfo>> removeMethods = findRemovedMethods(roots, removed);

        // next, calculate blocks of removed methods
        final Map<MethodInfo,Integer> blocks = new LinkedHashMap<MethodInfo, Integer>(removed.size());
        for (MethodInfo m : removed) {
            int size = MiscUtils.bytesToWords(getMethodSize(m));
            blocks.put(m, cache.requiredNumberOfBlocks(size));
        }

        // finally, go up all invokers, sum up reachable method set changes and deltaBlocks per node, check all-fit
        final Set<MethodInfo> changeSet = new LinkedHashSet<MethodInfo>();

        DFSVisitor<ExecutionContext,ContextEdge> visitor = new EmptyDFSVisitor<ExecutionContext, ContextEdge>() {
            @Override
            public void preorder(ExecutionContext node) {
                Set<MethodInfo> remove = removeMethods.get(node);
                int oldBlocks = cacheBlocks.get(node);
                int newBlocks = oldBlocks;

                if (remove != null) {
                    if (update) {
                        reachableMethods.get(node).removeAll(remove);
                    }
                    for (MethodInfo r : remove) {
                        newBlocks -= blocks.get(r);
                    }
                }

                newBlocks += deltaBlocks;
                if (update) {
                    cacheBlocks.put(node, newBlocks);
                }

                boolean oldFit = cache.allFit(oldBlocks);
                boolean newFit = cache.allFit(newBlocks);

                if (oldFit != newFit) {
                    changeSet.add(node.getMethodInfo());
                    if (update) {
                        classifyChanges.add(node.getMethodInfo());
                    }
                }
            }
        };

        DFSTraverser<ExecutionContext,ContextEdge> traverser =
                new DFSTraverser<ExecutionContext, ContextEdge>(visitor);
        traverser.traverse(callGraph.getReversedGraph(), roots);

        return changeSet;
    }

    private Map<ExecutionContext,Set<MethodInfo>> findRemovedMethods(Set<ExecutionContext> roots,
                                                                     Collection<MethodInfo> removed)
    {
        Map<ExecutionContext,Set<MethodInfo>> removeMethods = new LinkedHashMap<ExecutionContext, Set<MethodInfo>>();
        LinkedHashSet<ExecutionContext> queue = new LinkedHashSet<ExecutionContext>(roots);

        while (!queue.isEmpty()) {
            ExecutionContext node = queue.iterator().next();
            queue.remove(node);

            boolean changed = false;
            boolean isRoot = roots.contains(node);
            boolean isNew = false;

            // we initialize (lazily) by assuming that all removed methods are no longer reachable in any node,
            // and then removing entries from the set if they are found to be still reachable.
            // This ensures that the size of the sets only decreases and we eventually reach a fixpoint
            Set<MethodInfo> set = removeMethods.get(node);
            if (set == null) {
                set = new LinkedHashSet<MethodInfo>(removed.size());
                removeMethods.put(node, set);
                for (MethodInfo m : removed) {
                    // initially add method to remove set if it is reachable from this node
                    if (reachableMethods.get(node).contains(m)) {
                        set.add(m);
                    }
                }
                changed = true;
                isNew = true;
            }

            // check if any of the methods to remove have been removed from *all* childs and can therefore
            // be removed from this node
            for (MethodInfo r : removed) {
                // already removed
                if (!set.contains(r)) continue;

                for (ExecutionContext child : callGraph.getChildren(node)) {
                    // we ignore native methods in the cache analysis
                    if (child.getMethodInfo().isNative()) continue;

                    // skip childs which will be removed
                    if (isRoot && removed.contains(child.getMethodInfo())) continue;

                    // TODO this is incorrect for cyclic call graphs.. need to fix this!
                    if (reachableMethods.get(child).contains(r)) {
                        set.remove(r);
                        changed = true;
                    }
                }
            }

            if (isNew && set.isEmpty()) {
                // we did not remove anything here and we did not visit the parents yet, so nothing changes
                changed = false;
            }

            if (changed) {
                // we have found more methods, need to update parents
                queue.addAll(callGraph.getParents(node));
            }
        }

        return removeMethods;
    }

    private Set<ExecutionContext> findAllFitBorder(Collection<ExecutionContext> nodes) {
        final Set<ExecutionContext> border = new LinkedHashSet<ExecutionContext>();

        DFSVisitor<ExecutionContext,ContextEdge> visitor = new EmptyDFSVisitor<ExecutionContext, ContextEdge>() {
            @Override
            public boolean visitNode(ExecutionContext parent, ContextEdge edge, ExecutionContext node, DFSEdgeType type, Collection<ContextEdge> outEdges, int depth) {
                if (type == DFSEdgeType.ROOT || type == DFSEdgeType.BACK_EDGE) {
                    // skip the root, ignore back edges
                    return true;
                }
                if (!allFit(node)) {
                    // we do not go up from always-miss nodes, so the 'parent' must be all-fit
                    border.add(parent);
                    return false;
                }
                return true;
            }
        };

        DFSTraverser<ExecutionContext,ContextEdge> traverser = new DFSTraverser<ExecutionContext, ContextEdge>(visitor);
        traverser.traverse(callGraph.getReversedGraph(), nodes);

        return border;
    }

    private long getPersistentMisses(ExecFrequencyProvider ecp, Collection<ExecutionContext> nodes) {
        long count = 0;

        for (ExecutionContext border : findAllFitBorder(nodes)) {
            count += ecp.getExecCount(border);
        }

        return count;
    }

}
