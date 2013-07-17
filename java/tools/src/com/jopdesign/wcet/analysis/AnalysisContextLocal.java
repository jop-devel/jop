/*
 * This file is part of JOP, the Java Optimized Processor
 * see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2010, Benedikt Huber (benedikt.huber@gmail.com)
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
package com.jopdesign.wcet.analysis;

import com.jopdesign.common.code.CallString;
import com.jopdesign.common.code.ControlFlowGraph.CFGNode;
import com.jopdesign.common.code.ExecutionContext;
import com.jopdesign.wcet.ipet.IPETConfig.CacheCostCalculationMethod;

public class AnalysisContextLocal implements AnalysisContext {

	protected final CallString callString;
	protected final CacheCostCalculationMethod mode;

	public AnalysisContextLocal(CacheCostCalculationMethod mode) {
		 this(mode, CallString.EMPTY);
	}
	
	public AnalysisContextLocal(CacheCostCalculationMethod mode, CallString callString) {
		this.mode = mode;
		this.callString = callString;
	}

	@Override
	public CallString getCallString()
	{
		return callString;
	}
	
	public CacheCostCalculationMethod getCacheApproxMode() {
		return mode;
	}

	public AnalysisContextLocal withCacheApprox(CacheCostCalculationMethod mode) {
		return new AnalysisContextLocal(mode, this.callString);
	}
	
	public AnalysisContextLocal withCallString(CallString cs)
	{
		return new AnalysisContextLocal(this.mode, cs);
	}

	@Override
	public ExecutionContext getExecutionContext(CFGNode n) {
		return new ExecutionContext(n.getControlFlowGraph().getMethodInfo(), callString);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + callString.hashCode();
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (obj.getClass() != getClass()) return false;
		AnalysisContextLocal other = (AnalysisContextLocal) obj;
		if (!mode.equals(other.mode)) return false;
		if (!callString.equals(other.callString)) return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "AnalysisContextLocal [callstring=" + callString + ", mode=" + mode + "]";
	}

    @Override
    public String getKey() {
        return callString+","+mode;
    }
}
