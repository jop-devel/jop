/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2011, Benedikt Huber (benedikt@vmars.tuwien.ac.at)

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.jopdesign.common.code;

import java.util.Collection;

import com.jopdesign.common.code.ControlFlowGraph.CFGEdge;

/**
 * Purpose: Objects providing the InfeasibleEdgeDetector interface are used
 * to filter control flow graph edges which are infeasible in a certain (call) context
 *
 */
public interface InfeasibleEdgeDetector {
    public Collection<CFGEdge> getInfeasibleEdges(ControlFlowGraph cfg, CallString cs);
}
