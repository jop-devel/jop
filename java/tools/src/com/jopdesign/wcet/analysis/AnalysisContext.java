/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2010, Benedikt Huber (benedikt@vmars.tuwien.ac.at)

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

package com.jopdesign.wcet.analysis;


import com.jopdesign.common.code.CallString;
import com.jopdesign.common.code.ControlFlowGraph.CFGNode;
import com.jopdesign.common.code.ExecutionContext;

/**
 * Purpose:
 * @author Benedikt Huber (benedikt@vmars.tuwien.ac.at)
 *
 */
public interface AnalysisContext {
	CallString getCallString();
	ExecutionContext getExecutionContext(CFGNode n);

    /**
     * @return a short key for this context used for filename generation. The name must not be too long, else
     *    we run into problems with dot on windows.
     */
    String getKey();
}
