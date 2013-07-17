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
 * for the outer scope.
 * 
 * @author Martin Schoeberl (martin@jopdesign.com)
 * 
 */
public class Memory {

	// TODO should be set at some time, before the first
	// scope (e.g. mission memory) is created
	final static int IM_SIZE = 32*1024;

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
	Memory parent;
	/** Nesting level */
	public int level;
	
	/**
	 * A reference for an inner memory that shall be reused
	 * for enterPrivateMemory.
	 */
	Memory inner;

	/**
	 * The singleton reference for the immortal memory.
	 */
	public static Memory immortal;

	
	Memory() {
	}
	
	/**
	 * Create a Scope object that represents immortal memory.
	 * Should only be called by GC on JVM startup.
	 * @return
	 */
	static Memory getImmortal(int start, int end) {
		if (immortal==null) {
			immortal = new Memory();
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

	public Memory(int size, int bsSize) {
		cnt = 0;
		if (RtThreadImpl.mission) {
			// should be atomic? probably not
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
	public Memory(int size) {
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
		// Anders thinks cnt is useless here
		synchronized (this) {
			++cnt;
		}
		if (cnt != 1) {
			throw new Error("No cyclic enter and no sharing between threads");
		}
		// clean the scope memory
		// would be more efficient to clean after exit...
//		for (int i = startPtr; i <= endLocalPtr; ++i) {
//			Native.wrMem(0, i);
//		}

		// activate the memory area
		RtThreadImpl rtt = null;
		Memory outer = null;
		if (RtThreadImpl.mission) {
			// This method is only used by scopes within PrivateMemory so we
			// are certain that threads are running.
			Scheduler s = Scheduler.sched[RtThreadImpl.sys.cpuId];
			int nr = s.active;
			rtt = s.ref[nr];
			outer = rtt.currentArea;
			// TODO: Illegal reference when used as part of enterPrivateMemory
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
		inner = null;
		allocPtr = startPtr;
	}
	
	// just a enter without pointer reset (and cleanup)
	// need to be checked that the area is actually owned
	// by a thread when it is private memory
	public void executeInArea(Runnable logic) {
		// activate the memory area
		RtThreadImpl rtt = null;
		Memory outer = null;
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
			
			// TODO: Illegal reference when executeInArea() is called from
			// a nested private memory.
			rtt.currentArea = outer;
		} else {
			// without RtThreads running, main thread
			outer = RtThreadImpl.initArea;
			RtThreadImpl.initArea = this;
			logic.run();
			RtThreadImpl.initArea = outer;
		}
	}
	/**
	 * Return the memory region which we are currently in.
	 * @return
	 */
	public static Memory getCurrentMemory() {
		Memory m;
		if (RtThreadImpl.mission) {
			Scheduler s = Scheduler.sched[RtThreadImpl.sys.cpuId];
			m = s.ref[s.active].currentArea;
		} else {
			m = RtThreadImpl.initArea;
		}
		return m;
	}
	
	/**
	 * This is SCJ style inner scopes for private memory.
	 * 
	 * At the moment we will just create a local Memory
	 * object for each enter. However, this is a memory
	 * leak and one instance shall be reused.
	 * 
	 * @param size
	 * @param logic
	 */
	public void enterPrivateMemory(int size, Runnable logic) {
		// TODO: is this assignment allowed?
		// The scope object lives in an outer one,
		// so it is not!
		// That is actually an example where we need allocateInArea().
		if (inner==null) {
			// TODO: Illegal reference
			inner = new Memory();
		}
		// Now set all fields for inner and adapt this
		inner.parent = this;
		// check if remaining BS is at least size
		if (this.endBsPtr-this.allocBsPtr+1<size) {
			throw GC.OOMError;
		}
		inner.startPtr = this.allocBsPtr;
		// use bsSize backing store
		// Take all backing store
		inner.endBsPtr = this.endBsPtr;
		// save this BS allocation pointer
		int bsPtr = this.allocBsPtr;
		// adjust parents BS allocation pointer
		this.allocBsPtr = inner.endBsPtr+1;
		inner.allocPtr = inner.startPtr;
		inner.endLocalPtr = inner.startPtr+size-1;
		// Own offered BS for nested scopes starts after
		// the local area
		inner.allocBsPtr = inner.endLocalPtr+1;
		inner.level = this.level+1;
		inner.enter(logic);
		// Reset BS allocation pointer
		this.allocBsPtr = bsPtr;
	}
	
	// executeInArea -- don't forget to synchronize new
	
	public static Memory getMemoryArea(Object object){
		
		// Debug stuff
//		int i = Native.toInt(object);
//		System.out.println("Object reference: "+i);
//				
//		int j = Native.rdMem(i+GC.OFF_MEM);
//		System.out.println("Memory object reference: "+j);

		Memory m = (Memory)Native.toObject(
			    Native.rdMem(Native.toInt(object)+ GC.OFF_MEM));
		
		return m;
	}
	
	public int size(){
		return endLocalPtr - startPtr + 1;
	}
	
	public int memoryConsumed(){
		return allocPtr - startPtr + 1;
	}
	
	public int memoryRemaining(){
		return endLocalPtr - allocPtr;
	}
	
	public int bStoreRemaining(){
		return endBsPtr - allocBsPtr;
	}
	
}
