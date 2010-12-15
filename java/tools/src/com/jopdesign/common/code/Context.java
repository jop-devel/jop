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

package com.jopdesign.common.code;

import org.apache.bcel.generic.ConstantPoolGen;

public class Context {

	public int stackPtr;
	public int syncLevel;
	public boolean threaded;
	public ConstantPoolGen constPool;
	public String method;
	public CallString callString;	
	
	public Context() {
		stackPtr = -1;
		syncLevel = -1;
		threaded = _threaded;
		constPool = new ConstantPoolGen();
		method = null;
		callString = CallString.EMPTY;
	}

	public Context(Context c) {
		stackPtr = c.stackPtr;
		syncLevel = c.syncLevel;
		threaded = c.threaded;
		constPool = c.constPool;
		method = c.method;
		callString = c.callString;
	}
	
	private static boolean _threaded = false;
	public void createThread() {
		_threaded = true;
		threaded = true;
	}
	
	public static boolean isThreaded() {
		return _threaded;
	}

	public int hashCode() {
		return 1+31*stackPtr;
	}
	
	public boolean equals(Object o) {
		final Context c = (Context)o;
		if ((stackPtr == c.stackPtr || stackPtr < 0 || c.stackPtr < 0)) {
			return true;
		}		
		return false;
	}
	
}
