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
import com.jopdesign.jcopter.analysis.ExecFrequencyProvider;
import com.jopdesign.jcopter.analysis.StacksizeAnalysis;
import org.apache.bcel.generic.InstructionHandle;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Interface for a candidate selector implementation. The selector gets sets of optimization candidates
 * and needs to return a subset of candidates to optimize next.
 *
 * <p>The selector is also responsible for checking codesize constraints, both global and per method.</p>
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public interface CandidateSelector {

    void initialize(GreedyConfig config, boolean dumpStats);

    void printStatistics();

    void addCandidates(MethodInfo method, Collection<Candidate> candidates);

    void removeCandidates(MethodInfo method);

    void removeCandidates(MethodInfo method, InstructionHandle start, InstructionHandle end);

    void sortCandidates(ExecFrequencyProvider ecp);

    void sortCandidates(ExecFrequencyProvider ecp, Set<MethodInfo> changedMethods);

    Collection<Candidate> selectNextCandidates(ExecFrequencyProvider ecp);

    Collection<Candidate> getCandidates(MethodInfo method);

    void onSuccessfulOptimize(Candidate optimized, List<Candidate> newCandidates);

    void updateCandidates(MethodInfo method, ExecFrequencyProvider ecp, StacksizeAnalysis stacksizeAnalysis);

    void clear();

    /**
     *
     * @param ecp
     * @param optimizedMethods methods which have been optimized
     * @param candidateChanges methods for which the candidates will be recalculated.
     * @return a set of methods for which the candidates need to be sorted again.
     */
    Set<MethodInfo> updateChangeSet(ExecFrequencyProvider ecp, Set<MethodInfo> optimizedMethods, Set<MethodInfo> candidateChanges);
}
