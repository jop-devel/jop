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
import org.apache.bcel.generic.InstructionHandle;

import java.util.Collection;

/**
 * This is the main optimizer, which uses CodeOptimizers to generate candidates and uses a greedy
 * selection algorithm to select the candidates to optimize.
 *
 * TODO can we reuse Candidate and CodeOptimizer for different optimization strategies as well? Either move them
 *   to a different package or rename them to something more specific to avoid naming conflicts.
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class RebateOptimizer {

    private static class RebateRatio {

    }



}
