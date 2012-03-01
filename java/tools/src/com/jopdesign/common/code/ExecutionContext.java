/*
 * This file is part of JOP, the Java Optimized Processor
 *   see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2008, Benedikt Huber (benedikt.huber@gmail.com)
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

package com.jopdesign.common.code;

import com.jopdesign.common.MethodInfo;

/**
 * An execution context consists of the method where the
 * instruction/block/etc. is executed, and an optional call string.
 *
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 */
public class ExecutionContext implements MethodContainer {

    private final int hash;
    private final MethodInfo method;
    private final CallString callString;

    public ExecutionContext(MethodInfo method, CallString callString) {
        assert (method != null);
        assert (callString != null);

        this.method = method;
        this.callString = callString;
        this.hash = callString.hashCode() * 31 + method.hashCode();
    }

    public ExecutionContext(MethodInfo method) {
        assert (method != null);

        this.method = method;
        this.callString = CallString.EMPTY;
        this.hash = callString.hashCode() * 31 + method.hashCode();
    }

    public MethodInfo getMethodInfo() {
        return method;
    }

    public CallString getCallString() {
        return callString;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        ExecutionContext other = (ExecutionContext) obj;
        if (!method.equals(other.getMethodInfo())) return false;
        return callString.equals(other.getCallString());
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer("ExecutionContext(");
        sb.append(method.getFQMethodName());
        if (!callString.isEmpty()) {
            sb.append(",\n");
            sb.append(callString.toStringVerbose(true));
        }
        sb.append(")");
        return sb.toString();
    }
}
