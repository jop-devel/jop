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
import com.jopdesign.common.code.CallString;
import com.jopdesign.common.code.ControlFlowGraph.InvokeNode;
import com.jopdesign.common.code.ExecutionContext;
import com.jopdesign.common.code.InvokeSite;
import com.jopdesign.common.graphutils.DFSTraverser;
import com.jopdesign.common.graphutils.DFSTraverser.DFSVisitor;
import com.jopdesign.common.graphutils.DFSTraverser.EmptyDFSVisitor;
import com.jopdesign.common.graphutils.GraphUtils;
import com.jopdesign.common.misc.MiscUtils;
import com.jopdesign.jcopter.JCopter;
import com.jopdesign.wcet.WCETProcessorModel;
import com.jopdesign.wcet.WCETTool;
import com.jopdesign.wcet.analysis.AnalysisContextLocal;
import com.jopdesign.wcet.analysis.LocalAnalysis;
import com.jopdesign.wcet.analysis.RecursiveAnalysis;
import com.jopdesign.wcet.analysis.RecursiveAnalysis.RecursiveStrategy;
import com.jopdesign.wcet.analysis.WcetCost;
import com.jopdesign.wcet.ipet.IPETConfig;
import com.jopdesign.wcet.ipet.IPETConfig.StaticCacheApproximation;
import com.jopdesign.wcet.jop.MethodCache;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.log4j.Logger;
import org.jgrapht.alg.TransitiveClosure;
import org.jgrapht.graph.SimpleDirectedGraph;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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

    private final MethodCache cache;
    private final CallGraph callGraph;
    private final Map<ExecutionContext,Integer> cacheBlocks;
    private final Map<ExecutionContext, Set<MethodInfo>> reachableMethods;
    private final AnalysisManager analyses;
    private final AnalysisType analysisType;

    private final Set<MethodInfo> classifyChanges;
    private final Set<MethodInfo> countChanges;

    ///////////////////////////////////////////////////////////////////////////////////
    // Constructors, initialization, standard getter
    ///////////////////////////////////////////////////////////////////////////////////

    public MethodCacheAnalysis(AnalysisManager analyses, AnalysisType analysisType) {
        this(analyses, analysisType, analyses.getTargetCallGraph());
    }

    public MethodCacheAnalysis(AnalysisManager analyses, AnalysisType analysisType, CallGraph callGraph) {
        this.analyses = analyses;
        this.analysisType = analysisType;
        this.cache = analyses.getJCopter().getMethodCache();
        this.callGraph = callGraph;

        cacheBlocks = new HashMap<ExecutionContext, Integer>(callGraph.getNodes().size());
        reachableMethods = new HashMap<ExecutionContext, Set<MethodInfo>>(callGraph.getNodes().size());
        classifyChanges = new HashSet<MethodInfo>();
        countChanges = new HashSet<MethodInfo>();
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

    public long getInvokeMissCount(InvokeSite invokeSite) {
        return getMissCount(invokeSite.getInvoker(), invokeSite.getInstructionHandle());
    }

    public long getInvokeMissCount(CallString context) {
        return getMissCount(context.getExecutionContext(), context.top().getInstructionHandle());
    }

    public long getReturnMissCount(InvokeSite invokeSite) {
        if (analysisType == AnalysisType.ALWAYS_HIT) return 0;
        if (analysisType == AnalysisType.ALWAYS_MISS || !allFit(invokeSite.getInvoker())) {
            return analyses.getExecCountAnalysis().getExecCount(invokeSite);
        }
        if (analysisType == AnalysisType.ALWAYS_MISS_OR_HIT || cache.isLRU()) {
            return 0;
        }
        // TODO for FIFO caches, return might cause cache misses in all-fit
        return 0;
    }

    public long getReturnMissCount(CallString callString) {
        if (analysisType == AnalysisType.ALWAYS_HIT) return 0;
        if (analysisType == AnalysisType.ALWAYS_MISS || !allFit(callString.top().getInvoker())) {
            return analyses.getExecCountAnalysis().getExecCount(
                    callString.getExecutionContext(), callString.top().getInstructionHandle());
        }
        if (analysisType == AnalysisType.ALWAYS_MISS_OR_HIT || cache.isLRU()) {
            return 0;
        }
        // TODO for FIFO caches, return might cause cache misses in all-fit
        return 0;
    }

    public long getTotalInvokeReturnMissCosts(InvokeSite invokeSite) {
        return getTotalInvokeReturnMissCosts(new CallString(invokeSite));
    }

    public long getTotalInvokeReturnMissCosts(CallString callString) {
        if (analysisType == AnalysisType.ALWAYS_HIT) return 0;

        AppInfo appInfo = AppInfo.getSingleton();
        int size = 0;
        for (MethodInfo method : appInfo.findImplementations(callString)) {
            size = Math.max(size, method.getCode().getNumberOfWords());
        }

        WCETProcessorModel pm = analyses.getJCopter().getWCETProcessorModel();
        int sizeInvoker = callString.top().getInvoker().getCode().getNumberOfWords();

        return getInvokeMissCount(callString) * pm.getMethodCacheMissPenalty(size, true)
             - getReturnMissCount(callString) * pm.getMethodCacheMissPenalty(sizeInvoker, false);
    }

    /**
     * @param invokeSite the invokesite causing the cache misses
     * @param invokee the invoked method
     * @return the number of cycles for a cache miss for a single invoke, assuming an unknown cache state before the invoke.
     */
    public long getInvokeReturnMissCosts(InvokeSite invokeSite, MethodInfo invokee) {
        if (analysisType == AnalysisType.ALWAYS_HIT) return 0;

        WCETProcessorModel pm = analyses.getJCopter().getWCETProcessorModel();
        int size = invokee.getCode().getNumberOfWords();

        // we do not know if the method is in the cache, so for a single invoke always assume a miss
        long cycles = pm.getMethodCacheMissPenalty(size, true);

        // for all-fit, the return is always cached for LRU caches
        if (analysisType == AnalysisType.ALWAYS_MISS ||
            !allFit(invokeSite.getInvoker()) ||
            (analysisType == AnalysisType.ALL_FIT_REGIONS && !cache.isLRU()))
        {
            int sizeInvoker = invokeSite.getInvoker().getCode().getNumberOfWords();
            cycles += pm.getMethodCacheMissPenalty(sizeInvoker, false);
        }

        return cycles;
    }

    /**
     * @param node an execution context containing the instruction
     * @param entry the instruction to check
     * @return number of expected executions of the instruction where not all methods reachable from
     *         the invoker (including the invoker) are in the cache.
     */
    public long getMissCount(ExecutionContext node, InstructionHandle entry) {
        if (analysisType == AnalysisType.ALWAYS_HIT) return 0;

        if (analysisType == AnalysisType.ALWAYS_MISS || !allFit(node)) {
            return analyses.getExecCountAnalysis().getExecCount(node, entry);
        }
        if (analysisType == AnalysisType.ALWAYS_MISS_OR_HIT) {
            return 0;
        }

        // TODO we would want the total number of invokes of the top-most all-fit methods in the callgraph which can reach
        //      this method

        return 0;
    }

    /**
     * @param method method containing the instruction
     * @param entry the instruction to check
     * @return number of expected executions of the instruction where not all methods reachable from
     *         the invoker (including the invoker) are in the cache.
     */
    public long getMissCount(MethodInfo method, InstructionHandle entry) {
        return getMissCount(new ExecutionContext(method), entry);
    }

    /**
     * @param modification the modifications which will be done
     * @return the number of cache miss cycles due to the code size change
     */
    public long getDeltaCacheMissCosts(CodeModification modification) {
        int deltaBytes = modification.getDeltaLocalCodesize();
        MethodInfo method = modification.getMethod();

        if (deltaBytes == 0) return 0;
        if (analysisType == AnalysisType.ALWAYS_HIT) return 0;

        WCETProcessorModel pm = analyses.getJCopter().getWCETProcessorModel();
        int size = getMethodSize(method);
        int oldWords = MiscUtils.bytesToWords(size);
        int newWords = MiscUtils.bytesToWords(size+deltaBytes);

        int deltaBlocks = cache.requiredNumberOfBlocks(newWords) - cache.requiredNumberOfBlocks(oldWords);
        int newBlocks = getRequiredBlocks(method) + deltaBlocks;

        long costs = 0;

        // if the method is not all-fit, we have a cache miss cost delta due to the codesize
        // we do not worry about changing cache classifications here, those costs are added later
        if (analysisType == AnalysisType.ALWAYS_MISS || !cache.allFit(newBlocks)) {
            long oldCycles = pm.getMethodCacheMissPenalty(oldWords, true);
            long newCycles = pm.getMethodCacheMissPenalty(newWords, true);

            // costs for invoke of the modified method
            costs = analyses.getExecCountAnalysis().getExecCount(method) * (newCycles - oldCycles);

            // costs for all returns to this method
            oldCycles = pm.getMethodCacheMissPenalty(oldWords, false);
            newCycles = pm.getMethodCacheMissPenalty(newWords, false);
            int startPos = modification.getStart().getPosition();
            int endPos = modification.getEnd().getPosition();

            for (InvokeSite invokeSite : method.getCode().getInvokeSites()) {
                // skip invokesites within the modified code, since we do not know what the new code will be..
                // must be handled by the optimizer itself.
                int pos = invokeSite.getInstructionHandle().getPosition();
                if (pos >= startPos && pos <= endPos) continue;

                costs += analyses.getExecCountAnalysis().getExecCount(invokeSite) * (newCycles - oldCycles);
            }
        }

        if (analysisType == AnalysisType.ALWAYS_MISS) {
            return costs;
        }

        // for ALWAYS_MISS_HIT oder MOST_ONCE we need to find out what has changed for all-fit
        Set<ExecutionContext> changes = findClassificationChanges(method, deltaBytes,
                                                                  modification.getRemovedInvokees(), false);

        // In all nodes where we have changes, we need to sum up the new costs
        AppInfo appInfo = AppInfo.getSingleton();
        long deltaCosts = 0;
        for (ExecutionContext node : changes) {
            // we do not need to count the invokes of the method itself
            // but all invokes in the method are now no longer always-hit/-miss
            for (InvokeSite invokeSite : node.getMethodInfo().getCode().getInvokeSites()) {
                // find max invokee size
                int sizeWords = 0;
                for (MethodInfo invokee : appInfo.findImplementations(node.getCallString().push(invokeSite))) {
                    sizeWords = Math.max(sizeWords, invokee.getCode().getNumberOfWords());
                }

                long count = analyses.getExecCountAnalysis().getExecCount(node, invokeSite.getInstructionHandle());
                // every invoke is now/was before always-miss both on invoke and return
                deltaCosts += count * pm.getMethodCacheMissPenalty(sizeWords, true);
                deltaCosts += count * pm.getMethodCacheMissPenalty(oldWords, false);
            }
        }
        // if the code increased, the classification changed from always-hit to always-miss ..
        if (deltaBytes > 0) {
            costs += deltaCosts;
        } else {
            costs -= deltaCosts;
        }

        if (analysisType == AnalysisType.ALL_FIT_REGIONS) {

            // TODO we need to add changed cache costs of all methods reachable from changeset since the
            //      number of cache misses (i.e number of invokes of top-most all-fit methods) changed

        }

        return costs;
    }

    ///////////////////////////////////////////////////////////////////////////////////
    // Notify of callgraph updates, query change sets
    ///////////////////////////////////////////////////////////////////////////////////

    public void clearChangeSet() {
        classifyChanges.clear();
        countChanges.clear();
    }

    /**
     * @return all methods containing invokeSites for which the cache analysis changed
     */
    public Collection<MethodInfo> getClassificationChangeSet() {
        return classifyChanges;
    }

    public Collection<MethodInfo> getMissCountChangeSet() {
        return countChanges;
    }

    public void inline(CodeModification modification, InvokeSite invokeSite, MethodInfo invokee) {
        Set<ExecutionContext> nodes = new HashSet<ExecutionContext>();

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

        onExecCountUpdate();
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

    public void onExecCountUpdate() {
        if (analysisType == AnalysisType.ALWAYS_HIT) return;

        // we check the exec analysis for changed exec counts,
        // need to update change sets since cache miss counts changed for cache-misses
        Set<MethodInfo> methods = analyses.getExecCountAnalysis().getChangeSet();
        if (analysisType == AnalysisType.ALWAYS_MISS) {
            countChanges.addAll(methods);
            return;
        }

        for (MethodInfo method : methods) {
            if (!allFit(method)) {
                countChanges.add(method);
            }
            // TODO if MOST_ONCE_MISS and not all invokers of this method are all-fit, we need to add
            //      all reachable methods to the changeset too!
        }

    }

    ///////////////////////////////////////////////////////////////////////////////////
    // WCA Interface
    ///////////////////////////////////////////////////////////////////////////////////

    public RecursiveStrategy<AnalysisContextLocal,WcetCost>
           createRecursiveStrategy(WCETTool tool, IPETConfig ipetConfig)
    {
        return new LocalAnalysis(tool, ipetConfig) {
            @Override
            public WcetCost recursiveCost(RecursiveAnalysis<AnalysisContextLocal, WcetCost> stagedAnalysis,
                                          InvokeNode n, AnalysisContextLocal ctx) {
                AnalysisContextLocal newCtx = ctx;

                if (analysisType == AnalysisType.ALWAYS_MISS_OR_HIT &&
                        allFit(cache, n.getInvokeSite().getInvoker(), ctx.getCallString()))
                {
                    newCtx = ctx.withCacheApprox(StaticCacheApproximation.ALWAYS_HIT);
                }
                return super.recursiveCost(stagedAnalysis, n, newCtx);
            }

            @Override
            protected boolean allFit(MethodCache cache, MethodInfo method, CallString callString) {
                return MethodCacheAnalysis.this.allFit(new ExecutionContext(method, callString));
            }
        };
    }

    public AnalysisContextLocal getRootContext() {
        return getAnalysisContext(CallString.EMPTY);
    }

    public AnalysisContextLocal getAnalysisContext(CallString callString) {
        if (analysisType == AnalysisType.ALWAYS_HIT) {
            return new AnalysisContextLocal(StaticCacheApproximation.ALWAYS_HIT, callString);
        }
        if (analysisType == AnalysisType.ALWAYS_MISS) {
            return new AnalysisContextLocal(StaticCacheApproximation.ALWAYS_MISS, callString);
        }
        if (analysisType == AnalysisType.ALWAYS_MISS_OR_HIT) {
            return new AnalysisContextLocal(StaticCacheApproximation.ALL_FIT_SIMPLE, callString);
        }
        return new AnalysisContextLocal(StaticCacheApproximation.ALL_FIT_SIMPLE, callString);
    }

    ///////////////////////////////////////////////////////////////////////////////////
    // Private stuff
    ///////////////////////////////////////////////////////////////////////////////////

    private int getMethodSize(MethodInfo method) {
        // TODO should we use Java size to make things faster?
        return method.getCode().getNumberOfBytes();
    }

    private int getRequiredBlocks(MethodInfo method) {
        int blocks = 0;
        for (ExecutionContext node : callGraph.getNodes(method)) {
            blocks = Math.max(blocks, cacheBlocks.get(node));
        }
        return blocks;
    }

    private void updateNodes(SimpleDirectedGraph<ExecutionContext,ContextEdge> closure,
                             Set<ExecutionContext> nodes, boolean reuseResults)
    {

        for (ExecutionContext node : nodes) {
            if (node.getMethodInfo().isNative()) continue;

            // We could make this more memory efficient, because in many cases we do not need a
            // separate set for each node, but this would be more complicated to calculate
            Set<MethodInfo> reachable = new HashSet<MethodInfo>();

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

        MethodCache cache = analyses.getJCopter().getMethodCache();

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

    private Set<ExecutionContext> findClassificationChanges(MethodInfo method, final int deltaBlocks,
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
        final Map<MethodInfo,Integer> blocks = new HashMap<MethodInfo, Integer>(removed.size());
        for (MethodInfo m : removed) {
            int size = MiscUtils.bytesToWords(getMethodSize(m));
            blocks.put(m, cache.requiredNumberOfBlocks(size));
        }

        // finally, go up all invokers, sum up reachable method set changes and deltaBlocks per node, check all-fit
        final Set<ExecutionContext> changeSet = new HashSet<ExecutionContext>();

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
                    changeSet.add(node);
                    if (update) {
                        classifyChanges.add(node.getMethodInfo());
                        countChanges.add(node.getMethodInfo());
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
        Map<ExecutionContext,Set<MethodInfo>> removeMethods = new HashMap<ExecutionContext, Set<MethodInfo>>();
        HashSet<ExecutionContext> queue = new HashSet<ExecutionContext>(roots);

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
                set = new HashSet<MethodInfo>(removed.size());
                removeMethods.put(node, set);
                for (MethodInfo m : removed) {
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
                    if (child.getMethodInfo().isNative()) continue;

                    // skip childs which will be removed
                    if (isRoot && removed.contains(child.getMethodInfo())) continue;

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

}
