package com.jopdesign.sys;

public class Lock {
	
	volatile int entry_count = 0;
	volatile int current_owner = 0; // use int to avoid putfield_ref
	volatile int queue_front = 0;
	volatile int queue_back = 0;
	
	// keep locks alive during GC
	private static Lock[] lifeline = new Lock[Const.CAM_SIZE];
	static int[] locks;
	
	// fill object pool
	static {
		locks = new int[Const.CAM_SIZE];
		for (int i = 0; i < Const.CAM_SIZE; i++) {
			Lock l = new Lock();
			lifeline[i] = l;
			locks[i] = Native.toInt(l);
		}
	}
}
