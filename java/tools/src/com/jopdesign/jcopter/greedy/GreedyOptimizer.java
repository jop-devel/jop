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

import com.jopdesign.common.MethodInfo;
import com.jopdesign.jcopter.JCopter;
import com.jopdesign.jcopter.analysis.AnalysisManager;
import com.jopdesign.jcopter.analysis.StacksizeAnalysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

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

        private StacksizeAnalysis stacksizeAnalysis;
        private int maxLocals;

        private MethodData(int maxLocals, StacksizeAnalysis stacksizeAnalysis) {
            this.maxLocals = maxLocals;
            this.stacksizeAnalysis = stacksizeAnalysis;
        }

        public int getMaxLocals() {
            return maxLocals;
        }

        public void setMaxLocals(int maxLocals) {
            this.maxLocals = maxLocals;
        }

        public StacksizeAnalysis getStacksizeAnalysis() {
            return stacksizeAnalysis;
        }
    }

    private final GreedyConfig config;
    private final List<CodeOptimizer> optimizers;

    private static final Logger logger = Logger.getLogger(JCopter.LOG_OPTIMIZER+".GreedyOptimizer");

    public GreedyOptimizer(GreedyConfig config) {
        this.config = config;
        this.optimizers = new ArrayList<CodeOptimizer>();
    }

    public void addOptimizer(CodeOptimizer optimizer) {
        this.optimizers.add(optimizer);
    }

    public void optimize() {

        Set<MethodInfo> rootMethods = config.getRootMethods();

        //AnalysisManager analyses = initializeAnalyses();


    }



    private void optimizeMethods(AnalysisManager analyses, Set<MethodInfo> methods) {

        Map<MethodInfo,MethodData> methodData = new HashMap<MethodInfo, MethodData>(methods.size());

        CandidateSelector selector = new RebateSelector(analyses);

        selector.initialize();

        // first find and initialize all candidates
        for (MethodInfo method : methods) {

            // to update maxLocals
            method.getCode().compile();

            StacksizeAnalysis stacksize = new StacksizeAnalysis(method);
            stacksize.analyze();

            int locals = method.getCode().getMaxLocals();

            for (CodeOptimizer optimizer : optimizers) {
                Collection<Candidate> found;
                found = optimizer.findCandidates(method, analyses, stacksize, locals);
                selector.addCandidates(method, found);
            }

            methodData.put(method, new MethodData(locals, stacksize));
        }

        // now use the RebateSelector to order the candidates
        selector.sortCandidates();

        Set<MethodInfo> optimizedMethods = new HashSet<MethodInfo>();
        Set<MethodInfo> changedMethods = new HashSet<MethodInfo>();

        Collection<Candidate> candidates = selector.selectNextCandidates();
        while (candidates != null) {

            optimizedMethods.clear();
            changedMethods.clear();

            // perform optimization
            for (Candidate c : candidates) {
                MethodInfo method = c.getMethod();
                StacksizeAnalysis stacksize = methodData.get(method).getStacksizeAnalysis();

                if (!c.optimize(analyses, stacksize)) continue;

                // to update maxStack and positions
                method.getCode().compile();

                // Now we need to update the stackAnalysis and find new candidates in the optimized code
                stacksize.analyze(c.getStart(), c.getEnd());

                int locals = c.getMaxLocalsInRegion();

                // We need to remove candidates from methods which are no longer reachable
                Collection<MethodInfo> unreachable = c.getUnreachableMethods();
                if (unreachable != null && !unreachable.isEmpty()) {
                    for (MethodInfo m : unreachable) {
                        selector.removeCandidates(m);
                    }
                }

                // need to remove all candidates in this method which overlap the optimized region first
                selector.removeCandidates(method, c.getStart(), c.getEnd());

                for (CodeOptimizer optimizer : optimizers) {
                    Collection<Candidate> found;
                    found = optimizer.findCandidates(method, analyses, stacksize, locals, c.getStart(), c.getEnd());
                    selector.addCandidates(method, found);
                }

                optimizedMethods.add(method);
            }

            // now we need to find out for which _invokeSites_ the cache-miss-counts of the
            // cache analysis changed, add them to the change-set

            // then we add all optimized methods as well as their direct callers, because the cache-miss-costs changed

            // for those methods with invokesites with cache-cost changes, as well as all their callers,
            // but only for the methods reachable from the WCA-targets, we need to recalculate the WCA

            // for those methods with invokesites with cache-cost changes, recalculate the candidate-gains
            // (assuming that they do not use the WCA results, else we would recalculate wca-changeset too)

            // Now, find out for which _methods_ the cache-miss-counts changed, add them to the set of methods where the
            // candidates changed, add the WCA-changeset, and recalculate the rebate for all those methods


            // Finally, select the next candidates
            candidates = selector.selectNextCandidates();
        }

    }


}
