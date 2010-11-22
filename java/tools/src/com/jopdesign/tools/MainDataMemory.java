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

package com.jopdesign.tools;

import java.util.Stack;


/**
 * 
 * Simulation of data memory backed by a flat array
 * @author Benedikt Huber (benedikt@vmars.tuwien.ac.at)
 *
 */
public class MainDataMemory implements DataMemory {
	public static class MainDataMemoryStats {
		long readCount;
		long writeCount;		
	}
	private int[] mem;
	private MainDataMemoryStats stats;
	private Stack<MainDataMemoryStats> statsStack = new Stack<MainDataMemoryStats>();
	
	/** create a new simple, flat data memory
	 * @param mem the backing storage for the flat memory
	 */
	public MainDataMemory(int mem[]) {
		this.mem = mem;
		resetStats();
	}
	

	@Override
	public int read(int addr, Access type) {
		stats.readCount++;
		return mem[addr];
	}

	@Override
	public void write(int addr, int value, Access type) {
		mem[addr] = value; 
		stats.writeCount++;
	}

	@Override
	public int readIndirect(int handle, int offset, Access type) {
		stats.readCount+=2;
		int ref = mem[handle]; 
		return mem[ref+offset];
	}

	@Override
	public void writeIndirect(int handle, int offset, int value, Access type) {
		stats.readCount++;
		int ref = mem[handle];
		stats.writeCount++;
		mem[ref+offset] = value; 
	}		

	@Override public void invalidateData() {}

	@Override public void invalidateHandles() {}

	@Override public String getName() {
		return "Main Memory (RAM)";
	}
	
	@Override
	public void resetStats() {
		stats = new MainDataMemoryStats();
	}
	
	@Override
	public void recordStats() {
		statsStack.push(stats);
	}

	@Override public void dumpStats() {
		System.out.println(String.format("%8s & %8s & %8s \\\\","","readcnt","writecnt"));
		System.out.println(String.format("%8s & %8s & %8s \\\\","RAM",stats.readCount,stats.writeCount));
	}
}