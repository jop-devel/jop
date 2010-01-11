package com.jopdesign.wcet.analysis;

import com.jopdesign.build.MethodInfo;
import com.jopdesign.dfa.framework.CallString;
import com.jopdesign.wcet.frontend.ControlFlowGraph.BasicBlockNode;
import com.jopdesign.wcet.frontend.ControlFlowGraph.InvokeNode;

/**
 * Context for WCET analysis of a method.
 * Subclasses may provide callstrings and other context information.
 *
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 *
 */
public class AnalysisContext {
	public CallString getCallString() {
		return CallString.EMPTY;
	}
	/* FIXME: One big problem in Java:
	 * We would like to FORCE subclasses to override equals
	 */
	@Override
	public boolean equals(Object other) {
		if (this == other) return true;
		if (other == null) return false;
		if (other.getClass() != getClass()) return false;
		return true;
	}
	public int hashCode() {
		return 1;
	}
	public ExecutionContext getExecutionContext(BasicBlockNode n) {
		return new ExecutionContext(n.getBasicBlock().getMethodInfo(),getCallString());
	}
}
