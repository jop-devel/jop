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
import java.util.List;

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

            float codesize = getDeltaGlobalCodesize(candidate);

            RebateRatio ratio = new RebateRatio(candidate, candidate.getHeuristicFactor() * gain / codesize);
            ratios.add(ratio);
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

        // TODO check WCA tool if candidate is on WC path

        return true;
    }
}
