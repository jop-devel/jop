package com.jopdesign.wcet.uppaal;

import com.jopdesign.dfa.framework.CallString;
import com.jopdesign.wcet.analysis.AnalysisContext;
import com.jopdesign.wcet.analysis.ExecutionContext;
import com.jopdesign.wcet.frontend.ControlFlowGraph.CFGNode;
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

}
