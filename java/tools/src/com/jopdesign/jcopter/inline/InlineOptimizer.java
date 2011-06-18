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

package com.jopdesign.jcopter.inline;

import com.jopdesign.common.MethodInfo;
import com.jopdesign.jcopter.analysis.AnalysisManager;
import com.jopdesign.jcopter.analysis.StacksizeAnalysis;
import com.jopdesign.jcopter.greedy.Candidate;
import com.jopdesign.jcopter.greedy.CodeOptimizer;
import org.apache.bcel.generic.InstructionHandle;

import java.util.Collection;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class InlineOptimizer implements CodeOptimizer {

    @Override
    public Collection<Candidate> findCandidates(MethodInfo method, AnalysisManager analyses, StacksizeAnalysis stacksize, int maxLocals) {
        return null;
    }

    @Override
    public Collection<Candidate> findCandidates(MethodInfo method, AnalysisManager analyses, StacksizeAnalysis stacksize, int maxLocals, InstructionHandle start, InstructionHandle end) {
        return null;
    }
}
