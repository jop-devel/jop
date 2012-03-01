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

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class GainCalculator {

    private final AnalysisManager analyses;

    public GainCalculator(AnalysisManager analyses) {
        this.analyses = analyses;
    }

    public long calculateGain(ExecFrequencyProvider ecp, Candidate candidate) {

        long gain = ecp.getExecCount(candidate.getMethod(), candidate.getEntry())
                    * candidate.getLocalGain();

        gain -= candidate.getDeltaCacheMissCosts(analyses, ecp);

        gain -= getCodesizeCacheCosts(ecp, candidate);

        return gain;
    }

    public long improveGain(ExecFrequencyProvider ecp, Candidate candidate, long gain) {
        // TODO depending on config, use WCA to calculate local or global WC gain

        // TODO else add heuristic factor to prefer candidates outside IF-constructs

        return (long) (candidate.getHeuristicFactor() * gain);
    }

    private long getCodesizeCacheCosts(ExecFrequencyProvider ecp, Candidate candidate) {
        return analyses.getMethodCacheAnalysis().getDeltaCacheMissCosts(ecp, candidate);
    }

}
