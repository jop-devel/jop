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

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class WCETRebateSelector extends QueueSelector {

    public WCETRebateSelector(AnalysisManager analyses, GainCalculator gc, int maxGlobalSize) {
        super(analyses, gc, maxGlobalSize);
    }

    @Override
    protected boolean skipCandidate(Candidate candidate) {
        return !checkWCPath(candidate);
    }

    private boolean checkWCPath(Candidate candidate) {

        // if the method is not in the WCA callgraph, do not check the WCET path
        if (!analyses.getWCAInvoker().isWCAMethod(candidate.getMethod())) return true;

        return analyses.getWCAInvoker().isOnLocalWCETPath(candidate.getMethod(), candidate.getEntry());
    }

    @Override
    public Set<MethodInfo> updateChangeSet(ExecFrequencyProvider ecp, Set<MethodInfo> optimizedMethods, Set<MethodInfo> candidateChanges) {
        Set<MethodInfo> changeSet = new LinkedHashSet<MethodInfo>(candidateChanges);
        changeSet.addAll( ecp.getChangeSet() );
        changeSet.addAll( analyses.getMethodCacheAnalysis().getMissCountChangeSet(ecp) );

        // WCA invoker checks analysis changesets itself
        changeSet.addAll(analyses.getWCAInvoker().updateWCA(optimizedMethods));

        return changeSet;
    }
}
