/**
 *
 */
package com.jopdesign.wcet.analysis;

import com.jopdesign.common.code.CallString;
import com.jopdesign.common.code.ControlFlowGraph;
import com.jopdesign.wcet.ipet.IPETConfig.StaticCacheApproximation;

public class AnalysisContextLocal implements AnalysisContext {

	protected final CallString callString;
	protected final StaticCacheApproximation mode;

	public AnalysisContextLocal(StaticCacheApproximation mode) {
		 this(mode, CallString.EMPTY);
	}
	
	public AnalysisContextLocal(StaticCacheApproximation mode, CallString callString) {
		this.mode = mode;
		this.callString = callString;
	}

	@Override
	public CallString getCallString()
	{
		return callString;
	}
	
	public StaticCacheApproximation getCacheApproxMode() {
		return mode;
	}

	public AnalysisContextLocal withCacheApprox(StaticCacheApproximation mode) {
		return new AnalysisContextLocal(mode, this.callString);
	}
	
	public AnalysisContextLocal withCallString(CallString cs)
	{
		return new AnalysisContextLocal(this.mode, cs);
	}

	@Override
	public ExecutionContext getExecutionContext(ControlFlowGraph.CFGNode n) {
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
		return "AnalysisContextLocal [callString=" + callString + ", mode="
				+ mode + "]";
	}

	
}
