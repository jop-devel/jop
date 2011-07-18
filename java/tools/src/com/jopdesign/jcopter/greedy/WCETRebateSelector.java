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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class WCETRebateSelector extends RebateSelector {

    public WCETRebateSelector(AnalysisManager analyses, int maxGlobalSize) {
        super(analyses, maxGlobalSize);
    }

    @Override
    protected Collection<RebateRatio> calculateRatios(MethodInfo method, List<Candidate> candidates) {
        List<RebateRatio> ratios = new ArrayList<RebateRatio>(candidates.size());

        for (Candidate candidate : candidates) {

            if (!checkWCPath(candidate)) continue;

            float gain = calculateGain(candidate);
            if (gain < 0) continue;

            ratios.add(createRatio(candidate, gain));
        }

        return ratios;
    }

    private long calculateGain(Candidate candidate) {

        // TODO depending on config, use WCA to calculate local or global WC gain

        long gain = analyses.getExecCountAnalysis().getExecCount(candidate.getMethod(), candidate.getEntry())
                    * candidate.getLocalGain();

        gain -= analyses.getMethodCacheAnalysis().getMissCount(candidate.getMethod(), candidate.getEntry())
                * candidate.getDeltaCacheMissCosts();

        gain -= getCodesizeCacheCosts(candidate);

        return gain;
    }

    private boolean checkWCPath(Candidate candidate) {

        if (!analyses.getWCAInvoker().isWCAMethod(candidate.getMethod())) return true;

        return analyses.getWCAInvoker().isOnWCETPath(candidate.getMethod(), candidate.getEntry());
    }

    @Override
    public Set<MethodInfo> updateChangeSet(Set<MethodInfo> optimizedMethods, Set<MethodInfo> candidateChanges) {
        Set<MethodInfo> changeSet = new HashSet<MethodInfo>(candidateChanges);
        changeSet.addAll( analyses.getExecCountAnalysis().getChangeSet() );
        changeSet.addAll( analyses.getMethodCacheAnalysis().getMissCountChangeSet() );

        // WCA invoker checks analysis changesets itself
        analyses.getWCAInvoker().updateWCA(optimizedMethods);

        return changeSet;
    }
}
