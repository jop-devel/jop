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
import com.jopdesign.common.code.CallString;
import com.jopdesign.common.code.ExecutionContext;
import com.jopdesign.common.code.InvokeSite;
import com.jopdesign.common.misc.MiscUtils;
import com.jopdesign.wcet.WCETProcessorModel;
import com.jopdesign.wcet.jop.MethodCache;
import org.apache.bcel.generic.InstructionHandle;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * This analysis keeps track of the number of bytes and blocks of code reachable from a method and
 * provides estimations of cache miss numbers.
 *
 * TODO create subclasses for various analysis types instead of enum
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class MethodCacheAnalysis {

    public enum AnalysisType { ALWAYS_MISS, ALWAYS_HIT, ALWAYS_MISS_HIT, MOST_ONCE_HIT }

    private final MethodCache cache;
    private final CallGraph callGraph;
    private final Map<ExecutionContext,Integer> cacheBlocks;
    private final AnalysisManager analyses;
    private final AnalysisType analysisType;

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
        cacheBlocks = new HashMap<ExecutionContext, Integer>();
    }

    public CallGraph getCallGraph() {
        return callGraph;
    }

    public void initialize() {

        cacheBlocks.clear();

        // We do not use cache.allFit() here because this uses the wrong callgraph
        if (callGraph.isAcyclic()) {
            // This is easy ..
            for (ExecutionContext node : callGraph.reverseTopologicalOrder()) {

                int blocks = cache.requiredNumberOfBlocks(node.getMethodInfo());

                for (ExecutionContext child : callGraph.getChildren(node)) {
                    blocks += cacheBlocks.get(child);
                }

                cacheBlocks.put(node, blocks);
            }
        } else {
            // This is slow .. could be implemented faster, somehow.
            for (ExecutionContext node : callGraph.getNodes()) {

                int blocks = 0;

                for (MethodInfo method : callGraph.getReachableImplementationsSet(node)) {
                    blocks += cache.requiredNumberOfBlocks(method);
                }

                cacheBlocks.put(node, blocks);
            }
        }

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
        int blocks = cacheBlocks.get(node);
        return cache.allFit(blocks);
    }

    public long getInvokeMissCount(InvokeSite invokeSite) {
        return getMissCount(invokeSite.getInvoker(), invokeSite.getInstructionHandle());
    }

    public long getReturnMissCount(InvokeSite invokeSite) {
        if (analysisType == AnalysisType.ALWAYS_HIT) return 0;
        if (analysisType == AnalysisType.ALWAYS_MISS || !allFit(invokeSite.getInvoker())) {
            return analyses.getExecCountAnalysis().getExecCount(invokeSite);
        }
        // for all-fit, a return never causes a cache miss (this is different to getInvokeMissCount)
        return 0;
    }

    public long getTotalInvokeReturnMissCosts(InvokeSite invokeSite) {
        return getTotalInvokeReturnMissCosts(new CallString(invokeSite));
    }

    public long getTotalInvokeReturnMissCosts(CallString callString) {

        int size = 0;
        for (MethodInfo method : AppInfo.getSingleton().findImplementations(callString)) {
            size = Math.max(size, method.getCode().getNumberOfWords());
        }

        WCETProcessorModel pm = analyses.getJCopter().getWCETProcessorModel();
        int sizeInvoker = callString.top().getInvoker().getCode().getNumberOfWords();

        return getInvokeMissCount(callString.top()) * pm.getMethodCacheMissPenalty(size, true)
             - getReturnMissCount(callString.top()) * pm.getMethodCacheMissPenalty(sizeInvoker, false);
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

        // for all-fit, the return is always cached
        if (analysisType == AnalysisType.ALWAYS_MISS || !allFit(invokeSite.getInvoker())) {
            int sizeInvoker = invokeSite.getInvoker().getCode().getNumberOfWords();
            cycles += pm.getMethodCacheMissPenalty(sizeInvoker, false);
        }

        return cycles;
    }

    /**
     * @param method method containing the instruction
     * @param entry the instruction to check
     * @return number of expected executions of the instruction where not all methods reachable from
     *         the invoker (including the invoker) are in the cache.
     */
    public long getMissCount(MethodInfo method, InstructionHandle entry) {
        if (analysisType == AnalysisType.ALWAYS_HIT) return 0;

        if (analysisType == AnalysisType.ALWAYS_MISS || !allFit(method)) {
            return analyses.getExecCountAnalysis().getExecCount(method, entry);
        }
        if (analysisType == AnalysisType.ALWAYS_MISS_HIT) {
            return 0;
        }

        // TODO we would want the total number of invokes of the top-most all-fit methods in the callgraph which can reach
        //      this method

        return 0;
    }

    public long getDeltaCacheMissCosts(MethodInfo method, int deltaBytes) {
        if (deltaBytes == 0) return 0;
        if (analysisType == AnalysisType.ALWAYS_HIT) return 0;

        WCETProcessorModel pm = analyses.getJCopter().getWCETProcessorModel();
        int size = method.getCode().getNumberOfBytes();
        int oldWords = MiscUtils.bytesToWords(size);
        int newWords = MiscUtils.bytesToWords(size+deltaBytes);

        // TODO if delta is negative, an not-all-fit method may become all-fit, need to handle this properly if we ever
        //      actually have a decreasing codesize..
        if (deltaBytes < 0) {
            throw new AssertionError("Negative codesize change is not yet supported.");
        }

        if (analysisType == AnalysisType.ALWAYS_MISS || !allFit(method)) {
            long oldCycles = pm.getMethodCacheMissPenalty(oldWords, true);
            long newCycles = pm.getMethodCacheMissPenalty(newWords, true);

            // costs for invoke of the modified method
            long costs = analyses.getExecCountAnalysis().getExecCount(method) * (newCycles - oldCycles);

            // costs for all returns to this method
            oldCycles = pm.getMethodCacheMissPenalty(oldWords, false);
            newCycles = pm.getMethodCacheMissPenalty(newWords, false);
            for (InvokeSite invokeSite : method.getCode().getInvokeSites()) {
                costs += analyses.getExecCountAnalysis().getExecCount(invokeSite) * (newCycles - oldCycles);
            }

            return costs;
        }

        if (analysisType == AnalysisType.ALWAYS_MISS_HIT) {
            // How many blokes does it take to store all reachable code?
            int blocks = getRequiredBlocks(method);
            // .. same as before, only add additional blocks for increased method
            blocks += cache.requiredNumberOfBlocks(newWords) - cache.requiredNumberOfBlocks(oldWords);

            if (cache.allFit(blocks)) {
                // hu, still fits, so nothing changes (assuming always-hit for all-fit)
                return 0;
            }

            // Now this delta just got expensive: not always-hit anymore, this is always-miss now, including all invokers

            // TODO need to find all invokers which previously were always-hit and are now always-miss
            // for all those methods we now need to add up cache costs for all invoke sites


            return 0;
        }

        // TODO this is actually quite tricky.. need to find out which methods are now no longer all-fit,
        //      multiply the delta codesize with the number of cache misses of the method (number of invokes
        //      of all top-most all-fit methods which can reach this method), and add cache costs due to
        //      increasing the number of invokes of the top-most all-fit methods for the whole graph (since
        //      the top-most methods may have changed)

        return 0;
    }

    ///////////////////////////////////////////////////////////////////////////////////
    // Notify of callgraph updates, query change sets
    ///////////////////////////////////////////////////////////////////////////////////

    public void clearChangeSet() {

    }

    /**
     * @return all methods containing invokeSites for which the cache analysis changed
     */
    public Collection<MethodInfo> getClassificationChangeSet() {
        return null;
    }

    public void inline(InvokeSite invokeSite, MethodInfo invokee) {
        // Nothing else to do .. yet (if allFit, we might want to update cache miss counts)
        updateCodesize(invokeSite.getInvoker());
    }

    public void updateCodesize(MethodInfo method) {

    }

    ///////////////////////////////////////////////////////////////////////////////////
    // Private stuff
    ///////////////////////////////////////////////////////////////////////////////////

    private int getRequiredBlocks(MethodInfo method) {
        int blocks = 0;
        for (ExecutionContext node : callGraph.getNodes(method)) {
            blocks = Math.max(blocks, cacheBlocks.get(node));
        }
        return blocks;
    }

}
