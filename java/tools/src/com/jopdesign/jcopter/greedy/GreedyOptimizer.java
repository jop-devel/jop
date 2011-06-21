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
import com.jopdesign.jcopter.JCopter;
import com.jopdesign.jcopter.analysis.AnalysisManager;
import com.jopdesign.jcopter.analysis.StacksizeAnalysis;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
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
    private final GreedyConfig config;
    private final List<CodeOptimizer> optimizers;

    private static final Logger logger = Logger.getLogger(JCopter.LOG_OPTIMIZER+".GreedyOptimizer");

    public GreedyOptimizer(GreedyConfig config) {
        this.config = config;
        this.appInfo = config.getAppInfo();
        this.optimizers = new ArrayList<CodeOptimizer>();
    }

    public void addOptimizer(CodeOptimizer optimizer) {
        this.optimizers.add(optimizer);
    }

    public void optimize() {

        Set<MethodInfo> rootMethods = config.getRootMethods();

        AnalysisManager analyses = initializeAnalyses();

        for (CodeOptimizer opt : optimizers) {
            opt.initialize(analyses, rootMethods);
        }

        CandidateSelector selector = new WCETRebateSelector(analyses, config.getMaxCodesize());

        selector.initialize();



    }

    private AnalysisManager initializeAnalyses() {


        return null;
    }


    private void optimizeMethods(AnalysisManager analyses, CandidateSelector selector, Set<MethodInfo> methods) {

        Map<MethodInfo,MethodData> methodData = new HashMap<MethodInfo, MethodData>(methods.size());

        selector.clear();

        // first find and initialize all candidates
        for (MethodInfo method : methods) {

            // to update maxLocals
            method.getCode().compile();

            StacksizeAnalysis stacksize = analyses.getStacksizeAnalysis(method);

            int locals = method.getCode().getMaxLocals();

            for (CodeOptimizer optimizer : optimizers) {
                Collection<Candidate> found;
                found = optimizer.findCandidates(method, analyses, stacksize, locals);
                selector.addCandidates(method, found);
            }

            methodData.put(method, new MethodData(locals));
        }

        // now use the RebateSelector to order the candidates
        selector.sortCandidates();

        Set<MethodInfo> optimizedMethods = new HashSet<MethodInfo>();
        Set<MethodInfo> changedMethods = new HashSet<MethodInfo>();
        Set<MethodInfo> wcaChangeset = new HashSet<MethodInfo>();

        Collection<Candidate> candidates = selector.selectNextCandidates();
        while (candidates != null) {

            optimizedMethods.clear();
            changedMethods.clear();

            // perform optimization
            for (Candidate c : candidates) {
                MethodInfo method = c.getMethod();
                StacksizeAnalysis stacksize = analyses.getStacksizeAnalysis(method);

                if (!c.optimize(analyses, stacksize)) continue;

                // to update maxStack and positions
                method.getCode().compile();

                // Update analyses with new codesize
                // TODO


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

                // TODO maybe merge those next three selector calls into one update call?

                // Notify selector that the candidate has been optimized successfully
                selector.wasSuccessful(c);

                // need to remove all candidates in this method which overlap the optimized region first
                selector.removeCandidates(method, c.getStart(), c.getEnd());

                for (CodeOptimizer optimizer : optimizers) {
                    Collection<Candidate> found;
                    found = optimizer.findCandidates(method, analyses, stacksize, locals, c.getStart(), c.getEnd());
                    selector.addCandidates(method, found);
                }

                optimizedMethods.add(method);
            }

            // First, find out for which *methods* the *cache-miss-counts* changed (because this is used to calculate
            // the gain), needed later for the selector.


            // now we need to find out for which *invokeSites* the *cache-miss-counts* (invoke and return) of the
            // cache analysis changed, and add those methods to the change-set (because candidates may use this data).
            // Actually we would only need to update candidates whose range includes the affected invokeSites, but well..



            // then we add all optimized methods as well as their direct callers, because the *cache-miss-costs*
            // changed for this methods, might be used by the candidates.

            changedMethods.addAll(optimizedMethods);

            for (MethodInfo method : optimizedMethods) {
                changedMethods.addAll( appInfo.getCallGraph().getDirectInvokers(method) );
            }

            // for those methods with invokesites with cache-cost changes, as well as all their callers
            // (but only for the methods reachable from the WCA-targets) we need to recalculate the WCA if used

            if (config.useWCA()) {
                wcaChangeset.clear();



            }

            // for those methods with invokesites with cache-cost changes, recalculate the candidate-gains
            // (assuming that the candidates do not use the WCA results, else we would recalculate in wcaChangeset too)
            // but only for methods which we optimize.

            // small shortcut if we optimize one method at a time. In this case we only have one method to update
            for (MethodInfo method : (methods.size() == 1 ? methods : changedMethods)) {
                MethodData data = methodData.get(method);
                if (data == null) continue;
                selector.updateCandidates(method, analyses.getStacksizeAnalysis(method));
            }

            // Finally use the set of methods with changed cache-miss-counts, add the set of methods where the
            // candidates changed, add the WCA-changeset, and recalculate the rebate for all methods which have candidates.

            if (methods.size() == 1) {
                selector.sortCandidates(methods);
            } else {



                selector.sortCandidates(changedMethods);
            }

            // Finally, select the next candidates
            candidates = selector.selectNextCandidates();
        }

    }


}
