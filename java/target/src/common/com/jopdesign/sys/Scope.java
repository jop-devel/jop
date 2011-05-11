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
 * JOP's implementation of scoped memory
 * 
 * @author Martin Schoeberl (martin@jopdesign.com)
 *
 */
public class Scope {


	
	int cnt;
	int startPointer;
	int allocationPointer;
	long size;
	Scope parent;

	public Scope(long size) {
		cnt = 0;
		// Checks if the ImmortalMemory is currently being created
		if(RtThreadImpl.outerArea == null)
		{
			// This scope belongs to the ImmortalMemory currently being created
			startPointer = GC.allocationPointer;
			parent = null;
		}
		else
		{
			if (RtThreadImpl.mission) {
				Scheduler s = Scheduler.sched[RtThreadImpl.sys.cpuId];
				parent = s.ref[s.active].currentArea;
			}
			else
			{
				parent = RtThreadImpl.outerArea;
				
			}
			startPointer = parent.startPointer+(int)parent.size;
		}
		allocationPointer = startPointer;
		this.size = size;
	}

	/*// that's for our scratchpad memory
	public Scope(int[] localMem) {
		backingStore = localMem;
		cnt = 0;
		allocPtr = 0;
		// clean it
		for (int i=0; i<localMem.length; ++i) {
			localMem[i] = 0;
		}
	}*/
	
	/*public int getSize() {
		//return backingStore.length*4;
		return size;
	}*/

	public void enter(Runnable logic) {
		synchronized (this) {
			++cnt;
		}
		if (cnt!=1) {
			throw new Error("No cyclic enter and no sharing between threads");
		}
		// activate the memory area
		RtThreadImpl rtt = null;
		// This method is only used by scopes within PrivateMemory so we 
		// are certain that threads are running.
		Scheduler s = Scheduler.sched[RtThreadImpl.sys.cpuId];
		int nr = s.active;
		rtt = s.ref[nr];
		Scope parent = rtt.currentArea;
		rtt.currentArea = this;
		// super.enter(logic); nothing to do in MemoryArea
		logic.run();
		// deactivate the area
		rtt.currentArea = parent;

		// No reason to reset anything since the scope is "destroyed" after this method
		/*synchronized (this) {
			cnt--;
		}*/
	}

}
