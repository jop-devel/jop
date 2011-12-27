/*
 * This file is part of JOP, the Java Optimized Processor
 * see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2010, Benedikt Huber <benedikt.huber@gmail.com>
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

package com.jopdesign.dfa.framework;

import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.code.CallString;
import org.apache.bcel.generic.ConstantPoolGen;

public class Context {

    public int stackPtr;
    public int syncLevel;
    public boolean threaded;
    public CallString callString;

    private MethodInfo method;
    private ConstantPoolGen cpg;
    private String methodName;

    public Context() {
        stackPtr = -1;
        syncLevel = -1;
        threaded = _threaded;
        method = null;
        callString = CallString.EMPTY;
    }
    
    public Context(Context c) {
        stackPtr = c.stackPtr;
        syncLevel = c.syncLevel;
        threaded = c.threaded;
        callString = c.callString;
        setMethodInfo(c.getMethodInfo());
    }

    public void setMethodInfo(MethodInfo method) {
        this.method = method;
        cpg = method.getConstantPoolGen();
        methodName = method.toString();
    }

	public void setCallString(CallString cs) {
		this.callString = cs;
	}

    public MethodInfo getMethodInfo() {
        return method;
    }

    public ConstantPoolGen constPool() {
        return cpg;
    }

    public String method() {
        return methodName;
    }

    private static boolean _threaded = false;

    @SuppressWarnings({"AssignmentToStaticFieldFromInstanceMethod"})
    public void createThread() {
        _threaded = true;
        threaded = true;
    }

    public static boolean isThreaded() {
        return _threaded;
    }

    public int hashCode() {
        return 1 + 31 * stackPtr;
    }

    public boolean equals(Object o) {
        if (!(o instanceof Context)) return false;
        final Context c = (Context) o;
        if ((stackPtr == c.stackPtr || stackPtr < 0 || c.stackPtr < 0)) {
            return true;
        }
        return false;
    }


}
