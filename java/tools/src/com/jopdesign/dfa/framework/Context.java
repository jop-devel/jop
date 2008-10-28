package com.jopdesign.dfa.framework;

import java.util.LinkedList;

import org.apache.bcel.generic.ConstantPoolGen;

import com.jopdesign.dfa.analyses.HashedString;

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
