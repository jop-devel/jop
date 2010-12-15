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

import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.code.CallString;

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
