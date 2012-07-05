/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2008-2011, Martin Schoeberl (martin@jopdesign.com)

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

package scopeuse.ex3;

import com.jopdesign.sys.Memory;

public class Worker implements Runnable{

	RetObject rObj;
	
	Memory m;
	
	Worker(Memory m){
		this.m = m;
	}

	@Override
	public void run() {
		
		// do some work...
		
		//TODO: This example should use newInstance()
		//TODO: ManagedMemory.getCurrentManagedMemory()
		//TODO: MemoryArea.getMemoryArea(this), there is some issue
		// using offsets 6 and 7 in the object handle. For now, we pass
		// a reference to the memory area where we want the object to be 
		// saved
		
		m.executeInArea(new Runnable() {
			
			@Override
			public void run() {

				rObj = new RetObject();
			}
		});
	}
}

class RetObject {
	
	long a = System.currentTimeMillis();
}