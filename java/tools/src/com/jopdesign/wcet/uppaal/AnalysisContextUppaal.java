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
package com.jopdesign.wcet.uppaal;

import com.jopdesign.common.code.CallString;
import com.jopdesign.common.code.ControlFlowGraph.CFGNode;
import com.jopdesign.common.code.ExecutionContext;
import com.jopdesign.wcet.analysis.AnalysisContext;
import com.jopdesign.wcet.uppaal.UppAalConfig.UppaalCacheApproximation;

public class AnalysisContextUppaal implements AnalysisContext {

	private UppaalCacheApproximation cacheApprox;

	public AnalysisContextUppaal(UppaalCacheApproximation cacheApproximation) {
		assert(cacheApproximation != null);

		this.cacheApprox = cacheApproximation;
	}
	
	public CallString getCallString() {
		return CallString.EMPTY;
	}
	
	public ExecutionContext getExecutionContext(CFGNode n) {
		return new ExecutionContext(n.getControlFlowGraph().getMethodInfo());
	}

	public UppaalCacheApproximation getCacheApprox() {
		return cacheApprox;
	}

	public AnalysisContextUppaal withCacheApprox(
			UppaalCacheApproximation cacheApproximation) {
		return new AnalysisContextUppaal(cacheApproximation);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + cacheApprox.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		if (getClass() != obj.getClass()) return false;
		AnalysisContextUppaal other = (AnalysisContextUppaal) obj;
		if (!cacheApprox.equals(other.cacheApprox)) return false;
		return true;
	}

	@Override
	public String toString() {
		return "AnalysisContextUppaal [cacheApprox=" + cacheApprox + "]";
	}

    @Override
    public String getKey() {
        return toString();
    }
}
