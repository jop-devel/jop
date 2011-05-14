/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2008, Martin Schoeberl (martin@jopdesign.com)

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

package com.jopdesign.sys;

/**
 * JOP's implementation of scoped memory.
 * 
 * The backing store of a nested scope is allocated
 * within the backing store of the outer scope.
 * A nested scope starts after the size for object allocations
 * for the outer scope and takes all of the remaining
 * backing store.
 * 
 * Therefore, at the moment no support for multiple, parallel
 * scopes!
 * 
 * @author Martin Schoeberl (martin@jopdesign.com)
 * 
 */
public class Scope {

	// TODO should be set at some time, before the first
	// scope (e.g. mission memory) is created
	final static int IM_SIZE = 10000;

	// TODO would be easier to use end address instead of size
	int cnt;
	/** Start address of memory area */
	int startPtr;
	/** Allocation pointer */
	int allocPtr;
	/**
	 * End of area for local allocations.
	 * Points to the last usable word.
	 */
	int endLocalPtr;
	/**
	 * End of backing store.
	 * Points to the last usable word.
	 */
	int endBsPtr;
	// TODO: support for multiple parallel scopes
	// We need also an allocation pointer for the backing
	// store.
	/** Parent scope */
	Scope parent;
	

	/**
	 * Constructor for the first scope that represents immortal memory.
	 * 
	 * @param start
	 * @param end
	 */
	Scope(int start, int end) {
		startPtr = start;
		endBsPtr = end;
		endLocalPtr = startPtr + IM_SIZE - 1;
		// the Scope object itself is already allocated
		allocPtr = GC.allocationPointer;
		parent = null;
	}

	/**
	 * Standard constructor for current experiments
	 * 
	 * @param size
	 */
	public Scope(int size) {
		cnt = 0;
		if (RtThreadImpl.mission) {
			Scheduler s = Scheduler.sched[RtThreadImpl.sys.cpuId];
			parent = s.ref[s.active].currentArea;
		} else {
			parent = RtThreadImpl.initArea;

		}
		// new memory area is within parents backing store and
		// starts after the area for objects allocated in the parent
		startPtr = parent.endLocalPtr+1;
		// use all available backing store
		// needs to be adapted for multiple PM allocations in MM
		endBsPtr = parent.endBsPtr;
		allocPtr = startPtr;
		endLocalPtr = startPtr + size - 1;
	}

	// that's for our scratchpad memory
	// public Scope(int[] localMem) {
	// backingStore = localMem;
	// cnt = 0;
	// allocPtr = 0;
	// // clean it
	// for (int i=0; i<localMem.length; ++i) {
	// localMem[i] = 0;
	// }
	// }

	/*
	 * public int getSize() { //return backingStore.length*4; return size; }
	 */

	public void enter(Runnable logic) {
		synchronized (this) {
			++cnt;
		}
		if (cnt != 1) {
			throw new Error("No cyclic enter and no sharing between threads");
		}
		// activate the memory area
		RtThreadImpl rtt = null;
		Scope outer = null;
		if (RtThreadImpl.mission) {
			// This method is only used by scopes within PrivateMemory so we
			// are certain that threads are running.
			Scheduler s = Scheduler.sched[RtThreadImpl.sys.cpuId];
			int nr = s.active;
			rtt = s.ref[nr];
			outer = rtt.currentArea;
			rtt.currentArea = this;
			logic.run();
			// exit the area
			rtt.currentArea = outer;
		} else {
			// without RtThreads running, main thread
			outer = RtThreadImpl.initArea;
			RtThreadImpl.initArea = this;
			logic.run();
			RtThreadImpl.initArea = outer;
		}
		cnt = 0;
		// clean the scope memory
		for (int i = startPtr; i < allocPtr; ++i) {
			Native.wrMem(0, i);
		}
		allocPtr = startPtr;
	}
}
