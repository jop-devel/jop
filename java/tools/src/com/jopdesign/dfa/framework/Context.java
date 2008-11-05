/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2008, Wolfgang Puffitsch

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.jopdesign.dfa.framework;

import java.util.LinkedList;

import org.apache.bcel.generic.ConstantPoolGen;


public class Context {

	public int stackPtr;
	public int syncLevel;
	public boolean threaded;
	public ConstantPoolGen constPool;
	public String method;
	public LinkedList<HashedString> callString;	
	
	public Context() {
		stackPtr = -1;
		syncLevel = -1;
		threaded = _threaded;
		constPool = new ConstantPoolGen();
		method = null;
		callString = new LinkedList<HashedString>();
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

	public boolean equals(Context c) {
		if ((stackPtr == c.stackPtr || stackPtr < 0 || c.stackPtr < 0)
				//&& callString.equals(c.callString)
				) {
			return true;
		}		
		return false;
	}
	
}
