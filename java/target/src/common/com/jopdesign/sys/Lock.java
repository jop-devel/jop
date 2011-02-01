package com.jopdesign.sys;

import rtlib.Buffer;

class Lock {

	volatile int level;
	volatile int holder; // use int to avoid putfield_ref
	volatile int queue;

	// Object pool for lock objects
	// May be used only when HW-lock is used!
	static Buffer lockPool =
		new Buffer(Native.rdMem(Const.IO_CPUCNT)*16);
	// fill object pool
	static {
		while (!lockPool.full()) {
			lockPool.write(Native.toInt(new Lock()));
		}
	}

}