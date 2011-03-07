package com.jopdesign.sys;

import rtlib.Buffer;

class Lock {

	volatile int level;
	volatile int holder; // use int to avoid putfield_ref
	volatile int queue;

	// Object pool for lock objects
	// May be used only when HW-lock is used!
	static Buffer lockPool = new Buffer(Native.rdMem(Const.IO_CPUCNT)*24);
	// keep locks alive during GC
	private static Lock[] lifeline = new Lock[Native.rdMem(Const.IO_CPUCNT)*24];

	// fill object pool
	static {
		int i = 0;
		while (!lockPool.full()) {
			Lock l = new Lock();
			lockPool.write(Native.toInt(l));
			lifeline[i++] = l;
		}
	}

}