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
	/**
	 * Allocation pointer for the nested backing store.
	 */
	int allocBsPtr;
	// TODO: support for multiple parallel scopes
	// We need also an allocation pointer for the backing
	// store.
	/** Parent scope */
	Scope parent;
	/** Nesting level */
	int level;

	/**
	 * The singleton reference for the immortal memory.
	 */
	static Scope immortal;

	
	Scope() {
	}
	
	/**
	 * Create a Scope object that represents immortal memory.
	 * Should only be called by GC on JVM startup.
	 * @return
	 */
	static Scope getImmortal(int start, int end) {
		if (immortal==null) {
			immortal = new Scope();
			immortal.startPtr = start;
			immortal.endBsPtr = end;
			immortal.endLocalPtr = immortal.startPtr + IM_SIZE - 1;
			immortal.allocBsPtr = immortal.endLocalPtr+1;
			// the Scope object itself is already allocated
			immortal.allocPtr = GC.allocationPointer;
			immortal.parent = null;
			immortal.level = 0;
			
		}
		return immortal;
	}

	public Scope(int size, int bsSize) {
		cnt = 0;
		if (RtThreadImpl.mission) {
			Scheduler s = Scheduler.sched[RtThreadImpl.sys.cpuId];
			parent = s.ref[s.active].currentArea;
		} else {
			parent = RtThreadImpl.initArea;

		}
		// If backing store size is not set, take all
		if (bsSize==0) {
			bsSize = parent.endBsPtr-parent.allocBsPtr+1;			
		}

		// new memory area is within parents backing store and
		// starts after the area for objects allocated in the parent
		if (bsSize<size || parent.endBsPtr-parent.allocBsPtr+1 < bsSize) {
			throw GC.OOMError;
		}
		startPtr = parent.allocBsPtr;
		// use bsSize backing store
		endBsPtr = startPtr+bsSize-1;
		// adjust parents BS allocation pointer
		parent.allocBsPtr = endBsPtr+1;
		allocPtr = startPtr;
		endLocalPtr = startPtr+size-1;
		// Own offered BS for nested scopes starts after
		// the local area
		allocBsPtr = endLocalPtr+1;
		level = parent.level+1;
	}
	/**
	 * Create a scope and use all available backing store from the outer scope.
	 * 
	 * @param size
	 */
	public Scope(int size) {
		this(size, 0);
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
