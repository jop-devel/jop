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

import com.jopdesign.jcopter.analysis.AnalysisManager;
import com.jopdesign.jcopter.analysis.ExecFrequencyProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public abstract class QueueSelector extends RebateSelector {

    private final TreeSet<RebateRatio> queue;
    private final GainCalculator gainCalculator;

    public QueueSelector(AnalysisManager analyses, GainCalculator gainCalculator, int maxGlobalSize) {
        super(analyses, maxGlobalSize);
        this.gainCalculator = gainCalculator;
        queue = new TreeSet<RebateRatio>();
    }

    @Override
    public void clear() {
        super.clear();
        queue.clear();
    }

    @Override
    public void sortCandidates(ExecFrequencyProvider ecp) {
        queue.clear();
        sortCandidates(ecp, methodData.keySet());
    }

    @Override
    public Collection<Candidate> selectNextCandidates(ExecFrequencyProvider ecp) {
        while (true) {
            RebateRatio next = queue.pollLast();
            if (next == null) return null;

            if (!checkConstraints(next.getCandidate())) {
                continue;
            }

            logSelection(ecp, next);
            return next.getCandidates();
        }
    }

    @Override
    protected void onRemoveMethodData(MethodData data) {
        queue.removeAll(data.getRatios());
    }

    @Override
    protected void sortMethodData(ExecFrequencyProvider ecp, MethodData data) {
        queue.removeAll(data.getRatios());
        data.getRatios().clear();

        Collection<RebateRatio> ratios = calculateRatios(ecp, data.getCandidates());

        data.getRatios().addAll(ratios);
        queue.addAll(ratios);
    }

    protected Collection<RebateRatio> calculateRatios(ExecFrequencyProvider ecp, List<Candidate> candidates) {
        List<RebateRatio> ratios = new ArrayList<RebateRatio>(candidates.size());

        for (Candidate candidate : candidates) {

            if (skipCandidate(candidate)) continue;

            long gain = gainCalculator.calculateGain(ecp, candidate);
            if (gain <= 0) continue;

            ratios.add(createRatio(gainCalculator, ecp, candidate, gain));
        }

        return ratios;
    }

    protected abstract boolean skipCandidate(Candidate candidate);
}
