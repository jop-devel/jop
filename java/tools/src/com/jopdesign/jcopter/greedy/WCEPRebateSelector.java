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

import java.util.Collection;
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
    protected void sortMethodData(MethodInfo method, MethodData data) {
    }

    @Override
    public void sortCandidates() {
    }

    @Override
    public Collection<Candidate> selectNextCandidates() {

        // TODO go down all methods in the callgraph which are on the WCET path, find best candidate

        return null;
    }

    @Override
    public Set<MethodInfo> updateChangeSet(Set<MethodInfo> optimizedMethods, Set<MethodInfo> candidateChanges) {
        // No need to add anything else, as we search the whole graph anyway..
        return candidateChanges;
    }
}
