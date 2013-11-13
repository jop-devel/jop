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
import com.jopdesign.jcopter.analysis.AnalysisManager;
import com.jopdesign.jcopter.analysis.ExecFrequencyProvider;
import com.jopdesign.jcopter.analysis.WCAInvoker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class WCEPRebateSelector extends RebateSelector {

    private final GainCalculator gainCalculator;

    public WCEPRebateSelector(AnalysisManager analyses, GainCalculator gainCalculator, int maxGlobalSize) {
        super(analyses, maxGlobalSize);
        this.gainCalculator = gainCalculator;
    }

    @Override
    protected void onRemoveMethodData(MethodData data) {
    }

    @Override
    protected void sortMethodData(ExecFrequencyProvider ecp, MethodData data) {
    }

    @Override
    public void sortCandidates(ExecFrequencyProvider ecp) {
    }

    @Override
    public Collection<Candidate> selectNextCandidates(ExecFrequencyProvider ecp) {

        WCAInvoker wcaInvoker = analyses.getWCAInvoker();

        Set<MethodInfo> visited = new LinkedHashSet<MethodInfo>();
        LinkedList<MethodInfo> queue = new LinkedList<MethodInfo>();

        // go down all methods in the callgraph which are on the WCET path, find best candidate
        RebateRatio next = null;

        queue.addAll(wcaInvoker.getWcaTargets());
        visited.addAll(queue);

        while (!queue.isEmpty()) {
            MethodInfo method = queue.removeFirst();

            MethodData data = methodData.get(method);
            if (data == null) continue;

            // skip methods not on the WCET path
            if (!wcaInvoker.isOnWCETPath(method)) {
                continue;
            }

            // select best candidate from method or use previous method
            next = selectCandidate(ecp, data, next);

            // add childs to queue if not already visited
            for (MethodInfo child : analyses.getTargetCallGraph().getInvokedMethods(method)) {
                if (visited.add(child)) {
                    queue.add(child);
                }
            }

        }

        logSelection(ecp, next);
        return next != null ? next.getCandidates() : null;
    }

    @Override
    public Set<MethodInfo> updateChangeSet(ExecFrequencyProvider ecp, Set<MethodInfo> optimizedMethods, Set<MethodInfo> candidateChanges) {
        // No need to add anything else, as we search the whole graph anyway..
        analyses.getWCAInvoker().updateWCA(optimizedMethods);

        return candidateChanges;
    }

    private RebateRatio selectCandidate(ExecFrequencyProvider ecp, MethodData data, RebateRatio next) {

        List<Candidate> remove = new ArrayList<Candidate>();

        for (Candidate candidate : data.getCandidates()) {

            if (!checkConstraints(candidate)) {
                remove.add(candidate);
                continue;
            }

            if (!analyses.getWCAInvoker().isOnLocalWCETPath(candidate.getMethod(), candidate.getEntry())) {
                continue;
            }

            long gain = gainCalculator.calculateGain(ecp, candidate);
            if (gain <= 0) continue;

            RebateRatio ratio = createRatio(gainCalculator, ecp, candidate, gain);
            if (next == null || ratio.getRatio() > next.getRatio()) {
                next = ratio;
            }
        }

        for (Candidate candidate : remove) {
            removeCandidate(candidate);
        }

        return next;
    }


}
