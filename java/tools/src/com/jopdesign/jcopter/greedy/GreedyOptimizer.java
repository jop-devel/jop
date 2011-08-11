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

package com.jopdesign.jcopter.greedy;

import com.jopdesign.common.AppInfo;
import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.code.CallGraph.InvokeEdge;
import com.jopdesign.common.code.CallGraph.MethodNode;
import com.jopdesign.common.misc.AppInfoError;
import com.jopdesign.jcopter.JCopter;
import com.jopdesign.jcopter.analysis.AnalysisManager;
import com.jopdesign.jcopter.analysis.ExecCountProvider;
import com.jopdesign.jcopter.analysis.StacksizeAnalysis;
import com.jopdesign.jcopter.greedy.GreedyConfig.GreedyOrder;
import org.apache.log4j.Logger;
import org.jgrapht.traverse.TopologicalOrderIterator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is the main optimizer, which uses CodeOptimizers to generate candidates and uses a greedy
 * selection algorithm to select the candidates to optimize.
 *
 * TODO can we reuse Candidate and CodeOptimizer for different optimization strategies as well? Either move them
 *   to a different package or rename them to something more specific to avoid naming conflicts.
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class GreedyOptimizer {

    private class MethodData {

        private int maxLocals;

        private MethodData(int maxLocals) {
            this.maxLocals = maxLocals;
        }

        public int getMaxLocals() {
            return maxLocals;
        }

        public void setMaxLocals(int maxLocals) {
            this.maxLocals = maxLocals;
        }
    }

    private final AppInfo appInfo;
    private final JCopter jcopter;
    private final GreedyConfig config;
    private final List<CodeOptimizer> optimizers;

    private int countCandidates;
    private int countOptimized;

    private static final Logger logger = Logger.getLogger(JCopter.LOG_OPTIMIZER+".GreedyOptimizer");

    public GreedyOptimizer(GreedyConfig config) {
        this.jcopter = config.getJCopter();
        this.config = config;
        this.appInfo = config.getAppInfo();
        this.optimizers = new ArrayList<CodeOptimizer>();
    }

    public void addOptimizer(CodeOptimizer optimizer) {
        this.optimizers.add(optimizer);
    }

    public void optimize() {

        List<MethodInfo> rootMethods = config.getTargetMethods();

        // initialization
        resetCounters();

        AnalysisManager analyses = initializeAnalyses(config.useWCEP());

        for (CodeOptimizer opt : optimizers) {
            opt.initialize(analyses, rootMethods);
        }

        CandidateSelector selector;
        if (config.useWCA()) {
            GainCalculator gc = new GainCalculator(analyses);
            if (config.useWCEP()) {
                selector = new WCEPRebateSelector(analyses, gc, config.getMaxCodesize());
            } else {
                selector = new WCETRebateSelector(analyses, gc, config.getMaxCodesize());
            }
        } else {
            selector = new ACETRebateSelector(analyses, new GainCalculator(analyses), config.getMaxCodesize());
        }

        selector.initialize();

        // iterate over regions in callgraph

        GreedyOrder order = config.getOrder();
        if (order == GreedyOrder.Global || (order == GreedyOrder.WCAFirst && !config.useWCA())) {

            optimizeMethods(analyses, analyses.getExecCountAnalysis(), selector,
                            analyses.getTargetCallGraph().getMethodInfos());

        } else if (order == GreedyOrder.Targets) {

            for (MethodInfo target : config.getTargetMethods()) {
                optimizeMethods(analyses, analyses.getExecCountAnalysis(), selector,
                                analyses.getTargetCallGraph().getReachableImplementationsSet(target));
            }

        } else if (order == GreedyOrder.WCAFirst) {

            Set<MethodInfo> wcaMethods = analyses.getWCAMethods();
            optimizeMethods(analyses, analyses.getWCAInvoker(), selector, wcaMethods);

            // We do not want to include the wca methods in the second pass because inlining there could have negative
            // effects on the WCET path due to the cache
            Set<MethodInfo> others = new HashSet<MethodInfo>(analyses.getTargetCallGraph().getMethodInfos());
            others.removeAll(wcaMethods);

            selector.printStatistics();

            //analyses.dumpTargetCallgraph("acet", true);

            selector = new ACETRebateSelector(analyses, new GainCalculator(analyses), config.getMaxCodesize());
            selector.initialize();

            optimizeMethods(analyses, analyses.getExecCountAnalysis(), selector, others);

        } else if (order == GreedyOrder.TopDown || order == GreedyOrder.BottomUp) {

            if (config.useWCA() && !analyses.hasWCATargetsOnly()) {
                // TODO iterate over WCA and then non-wca graph or something in this case..
                throw new AppInfoError("Order "+order+" currently only works with WCA if the target method is the WCA target");
            }

            TopologicalOrderIterator<MethodNode,InvokeEdge> topOrder =
                    new TopologicalOrderIterator<MethodNode, InvokeEdge>(
                            analyses.getTargetCallGraph().getAcyclicMergedGraph(order == GreedyOrder.BottomUp)
                    );

            ExecCountProvider ecp = config.useWCA() ? analyses.getWCAInvoker() : analyses.getExecCountAnalysis();

            while (topOrder.hasNext()) {
                MethodNode node = topOrder.next();

                optimizeMethods(analyses, ecp, selector, Collections.singleton(node.getMethodInfo()));
            }

        } else {
            throw new AppInfoError("Order "+order+" not yet implemented.");
        }

        selector.printStatistics();
        printStatistics();
    }

    private void resetCounters() {
        countCandidates = 0;
        countOptimized = 0;
    }

    private void printStatistics() {
        for (CodeOptimizer o : optimizers) {
            o.printStatistics();
        }
        logger.info("Candidates: "+countCandidates+", Optimized: "+countOptimized);
    }

    private AnalysisManager initializeAnalyses(boolean updateWCEP) {

        AnalysisManager analyses = new AnalysisManager(jcopter);

        analyses.initAnalyses(config.getTargetMethodSet(), config.getCacheAnalysisType(),
                              config.useWCA() ? config.getWCATargetSet() : null, updateWCEP);

        logger.info("Callgraph nodes: "+analyses.getTargetCallGraph().getNodes().size());

        return analyses;
    }


    private void optimizeMethods(AnalysisManager analyses, ExecCountProvider ecp,
                                 CandidateSelector selector, Set<MethodInfo> methods)
    {
        Map<MethodInfo,MethodData> methodData = new HashMap<MethodInfo, MethodData>(methods.size());

        selector.clear();

        // first find and initialize all candidates
        for (MethodInfo method : methods) {

            if (method.isNative()) continue;

            // to update maxLocals
            method.getCode().compile();

            StacksizeAnalysis stacksize = analyses.getStacksizeAnalysis(method);

            int locals = method.getCode().getMaxLocals();

            for (CodeOptimizer optimizer : optimizers) {
                Collection<Candidate> found;
                found = optimizer.findCandidates(method, analyses, stacksize, locals);
                selector.addCandidates(method, found);
                countCandidates += found .size();
            }

            methodData.put(method, new MethodData(locals));
        }

        // now use the RebateSelector to order the candidates
        selector.sortCandidates(ecp);

        Set<MethodInfo> optimizedMethods = new HashSet<MethodInfo>();
        Set<MethodInfo> candidateChanges = new HashSet<MethodInfo>();

        Collection<Candidate> candidates = selector.selectNextCandidates(ecp);
        while (candidates != null) {

            optimizedMethods.clear();
            candidateChanges.clear();

            analyses.clearChangeSets();

            // perform optimization
            for (Candidate c : candidates) {
                MethodInfo method = c.getMethod();
                StacksizeAnalysis stacksize = analyses.getStacksizeAnalysis(method);

                logger.info("Optimizing "+c.toString());
                if (!c.optimize(analyses, stacksize)) continue;
                countOptimized++;

                // to update maxStack and positions
                method.getCode().compile();

                // Now we need to update the stackAnalysis and find new candidates in the optimized code
                stacksize.analyze(c.getStart(), c.getEnd());

                int locals = c.getMaxLocalsInRegion();

                // find new candidates in optimized code
                List<Candidate> newCandidates = new ArrayList<Candidate>();
                for (CodeOptimizer optimizer : optimizers) {
                    Collection<Candidate> found;
                    found = optimizer.findCandidates(method, analyses, stacksize, locals, c.getStart(), c.getEnd());
                    newCandidates.addAll(found);
                }

                // Notify selector to update codesize, remove unreachable methods and to replace
                // old candidates with new ones
                selector.onSuccessfulOptimize(c, newCandidates);
                countCandidates += newCandidates.size();

                optimizedMethods.add(method);
            }

            // Now we need to find out for which methods we need to recalculate the candidates..

            // First we add all optimized methods since we added new candidates and changed the codesize
            candidateChanges.addAll(optimizedMethods);

            // small shortcut if we optimize one method at a time. In this case we only have one method to update anyway
            if (methods.size() > 1) {
                // For all direct callers the cache-miss-*costs* of invokes changed, so we add them too
                // Actually we would only need to update caller candidates whose range includes the affected
                // invokeSites, but well..
                for (MethodInfo method : optimizedMethods) {
                    candidateChanges.addAll(appInfo.getCallGraph().getDirectInvokers(method));
                }

                // No need to add exec count changes, since candidates calculate values only per single execution,
                // or is there? (cache miss count changes due to exec frequency changes are handled by the cache analysis)

                // We need to find out for which invokeSites the cache-miss-*counts* (as used by
                // Candidate#getDeltaCacheMissCosts()) of invoke and return changed, add to the changeset
                candidateChanges.addAll(analyses.getMethodCacheAnalysis().getClassificationChangeSet());
            }

            // Now let the selector update its analyses and find out which additional methods need sorting
            Set<MethodInfo> changeSet = selector.updateChangeSet(ecp, optimizedMethods, candidateChanges);

            // for those methods with invokesites with cache-cost changes, recalculate the candidate-gains
            // (assuming that the candidates do not use the WCA results, else we would recalculate in changeSet too)
            // but only for methods which we optimize.
            for (MethodInfo method : candidateChanges) {
                // skip methods in changeset which are not being optimized
                if (!methodData.containsKey(method)) continue;
                selector.updateCandidates(method, ecp, analyses.getStacksizeAnalysis(method));
            }

            // Finally use the set of methods for which something changed, and re-sort all candidates of those methods
            if (methods.size() == 1) {
                selector.sortCandidates(ecp, methods);
            } else {
                logger.info("Sort changes "+changeSet.size());
                selector.sortCandidates(ecp, changeSet);
            }

            // Finally, select the next candidates
            candidates = selector.selectNextCandidates(ecp);
        }

    }


}
