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

import java.io.PrintStream;
import java.util.Stack;

import com.jopdesign.tools.splitcache.DataCacheStats;
import com.jopdesign.tools.splitcache.SplitCacheSim;


/**
 * 
 * Simulation of data memory backed by a flat array
 * @author Benedikt Huber (benedikt@vmars.tuwien.ac.at)
 *
 */
public class MainDataMemory extends DataMemory {
	
	private int[] mem;
	private DataCacheStats stats;

	private Stack<DataCacheStats> recordedStats = new Stack<DataCacheStats>();
	@Override
	public void resetStats() {
		this.stats.reset();
	}
	@Override 
	public void recordStats() {
		recordedStats.push(stats.clone());
	}


	/** create a new simple, flat data memory
	 * @param mem the backing storage for the flat memory
	 */
	public MainDataMemory(int mem[]) {
		this.mem = mem;
		stats = new DataCacheStats(getName());
	}
	

	@Override
	public int read(int addr, Access type) {
		stats.read(true);
		return mem[addr];
	}

	@Override
	public void write(int addr, int value, Access type) {
		stats.write();
		mem[addr] = value; 
	}

	@Override public String getName() {
		return "Main Memory (RAM)";
	}
	
	@Override public void dump(PrintStream out) {
		SplitCacheSim.printHeader(out, "   Main Memory   ");
		new DataCacheStats(getName()).addAverage(recordedStats).dump(out);
	}

	@Override public void invalidateData() {}
	@Override public void invalidateHandles() {}

}