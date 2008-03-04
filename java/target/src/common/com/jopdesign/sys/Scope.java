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
	int[] backingStore;
	int allocPtr;
	Scope outer;

	public Scope(long size) {
		backingStore = new int[((int) size+3)>>2];
		cnt = 0;
		allocPtr = 0;
	}

	public Scope(int[] localMem) {
		backingStore = localMem;
		cnt = 0;
		allocPtr = 0;
	}
	
	public int getSize() {
		return backingStore.length*4;
	}

	public void enter(Runnable logic) {
		synchronized (this) {
			++cnt;
		}
		if (cnt!=1) {
			throw new Error("No cyclic enter and no sharing between threads");
		}
		// activate the memory area
		outer = GC.getCurrentArea();
		GC.setCurrentArea(this);
		// super.enter(logic); nothing to do in MemoryArea
		logic.run();
		// deactivate the area
		GC.setCurrentArea(outer);

		synchronized (this) {
			cnt--;
			if (cnt==0) {
				for (int i=0; i<allocPtr; ++i) {
					backingStore[i] = 0;
				}
				allocPtr = 0;
			}
		}
	}

}
