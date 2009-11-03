package com.jopdesign.wcet.analysis;

import com.jopdesign.build.MethodInfo;

/**
 * An execution context consists of the method where the
 * instruction/block/etc. is executed, and an optional call string.
 *
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 *
 */
public class ExecutionContext {
	private MethodInfo method;
	private CallString callString;

	public ExecutionContext(MethodInfo method, CallString callString) {
		assert (method != null);
		assert (callString != null);

		this.method = method;
		this.callString = callString;
	}

	public ExecutionContext(MethodInfo method) {
		assert (method != null);

		this.method = method;
		this.callString = CallString.EMPTY;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + callString.hashCode();
		result = prime * result + method.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		ExecutionContext other = (ExecutionContext) obj;
		if (!method.equals(other.method)) return false;
		return callString.equals(other.callString);
	}

	public MethodInfo getMethodInfo() {
		return method;
	}

	public CallString getCallString() {
		return callString;
	}

}
