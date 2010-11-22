/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2010, Benedikt Huber (benedikt@vmars.tuwien.ac.at)

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

package com.jopdesign.tools.splitcache;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import com.jopdesign.tools.DataMemory;


/**
 * Split cache delegator
 * @author Benedikt Huber (benedikt@vmars.tuwien.ac.at)
 *
 */
public class SplitCache implements DataMemory {
	
	@SuppressWarnings("unused")
	private DataMemory defaultMemory;
	private Vector<DataMemory> caches;
	private DataMemory handlers[];
	private String name;
	private Map<DataMemory, Vector<Access>> handlerFor = new HashMap<DataMemory, Vector<Access>>();
	
	public SplitCache(String name, DataMemory defaultMemory) {
		this.name = name;
		this.defaultMemory = defaultMemory;
		this.caches = new Vector<DataMemory>();
		this.handlers = new DataMemory[Access.getMaxOrdinal() + 1];
		for(Access ty : Access.values()) {
			handlers[ty.ordinal()] = defaultMemory;
		}
	}
	

	/**
	 * Add a new cache
	 * @param cache the cache implementation
	 * @param handled the handled access types
	 * @return the id of the cache
	 */
	public int addCache(DataMemory cache, Access[] handled) {
		this.caches.add(cache);
		Vector<Access> hvec = new Vector<Access>();
		for(Access ty : handled) {
			hvec.add(ty);
			this.handlers[ty.ordinal()] = cache;
		}
		handlerFor .put(cache, hvec);
		return caches.size() -1;
	}

	public int addCache(DataMemory cache, Access handled) {
		Access h[] = new Access[1];
		h[0] = handled;
		return this.addCache(cache, h);
	}
	
	@Override
	public int read(int addr, Access type) {
		return handlers[type.ordinal()].read(addr, type);
	}

	@Override
	public int readIndirect(int handle, int offset, Access type) {
		try {
			return handlers[type.ordinal()].readIndirect(handle, offset, type);
		} catch(IndirectAccessUnsupported ex) {
			int addr = read(handle, Access.HANDLE);
			return read(addr+offset, type);			
		}
	}

	@Override
	public void write(int addr, int value, Access type) {
		handlers[type.ordinal()].write(addr, value, type);
	}

	@Override
	public void writeIndirect(int handle, int offset, int value, Access type) {
		try {
			handlers[type.ordinal()].writeIndirect(handle, offset, value, type);			
		} catch(IndirectAccessUnsupported ex) {
			int addr = read(handle, Access.HANDLE);
			write(addr+offset, value, type);			
		}
	}

	@Override
	public void invalidateData() {
		for(DataMemory cache : caches) {
			cache.invalidateData();
		}
	}

	@Override
	public void invalidateHandles() {
		for(DataMemory cache : caches) {
			cache.invalidateHandles();
		}
	}
	@Override
	public String getName() {
		StringBuffer sb = new StringBuffer("SplitCache{ ");
		for(DataMemory cache : caches) {
			sb.append(cache.getName());
			sb.append(", ");
		}
		sb.append(" }");
		return sb.toString();
	}
	
	@Override
	public void resetStats() {
		for(DataMemory mem : caches) {
			mem.resetStats();
		}
	}

	@Override
	public void recordStats() {
		for(DataMemory mem : caches) {
			mem.recordStats();
		}
	}

	@Override
	public void dumpStats() {		
		System.out.println("=== SplitCache " + name + " ===");
		for(DataMemory cache : caches) {
			cache.dumpStats();
			System.out.println();
		}
	}


}
