/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2009, Peter Hilber (peter@hilber.name)

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

package rttm;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

public class Diagnostics {
	/**
	 * To be called for every CPU for which {@link #stat(int)} will be called.
	 */
	public static void saveStatistics() {
		int cpuId = Native.rd(Const.IO_CPU_ID);
		
		statistics[cpuId].counters[RETRIES] = Native.rd(Const.MEM_TM_RETRIES);
		statistics[cpuId].counters[COMMITS] = Native.rd(Const.MEM_TM_COMMITS);
		statistics[cpuId].counters[EARLY_COMMITS] = 
			Native.rd(Const.MEM_TM_EARLY_COMMITS);
		statistics[cpuId].counters[READ_SET] = 
			Native.rd(Const.MEM_TM_READ_SET);
		statistics[cpuId].counters[WRITE_SET] = 
			Native.rd(Const.MEM_TM_WRITE_SET);
		statistics[cpuId].counters[READ_OR_WRITE_SET] = 
			Native.rd(Const.MEM_TM_READ_OR_WRITE_SET);
		
		statistics[cpuId].saved = true;
	}
	
	protected static final int RETRIES = 0;
	protected static final int COMMITS = 1;
	protected static final int EARLY_COMMITS = 2;
	protected static final int READ_SET = 3;
	protected static final int WRITE_SET = 4;
	protected static final int READ_OR_WRITE_SET = 5;

	protected static final String[] descriptions = {
		"Retries: ", "Commits: ", "Early commits: ", "Read set: ", 
		"Write set: ", "Read or write set: " 
		};
	
	protected static final int COUNTERS_CNT = 6;
	
	
	public static class Statistics {
		public volatile boolean saved = false;
		public volatile int[] counters = new int[COUNTERS_CNT];
	}
	
	public static Statistics[] statistics = 
		new Statistics[Native.rd(Const.IO_CPUCNT)];
	
	protected static Statistics sumStatistics = new Statistics();
	
	static {
		for (int i = 0; i < Native.rd(Const.IO_CPUCNT); i++) {
			statistics[i] = new Statistics();
		}
	}
	
	public static void stat() {
		for (int i = 0; i < Native.rd(Const.IO_CPUCNT); i++) {
			stat(i);
		}
		
		for (int i = 0; i < COUNTERS_CNT; i++) {
			sumStatistics.counters[i] = 0;
		}
		
		for (int j = 0; j < Native.rd(Const.IO_CPUCNT); j++) {
			sumStatistics.counters[RETRIES] += 
				statistics[j].counters[RETRIES];
			sumStatistics.counters[COMMITS] += 
				statistics[j].counters[COMMITS];
			sumStatistics.counters[EARLY_COMMITS] += 
				statistics[j].counters[EARLY_COMMITS];
			sumStatistics.counters[READ_SET] = Math.max(
				statistics[j].counters[READ_SET],
				sumStatistics.counters[READ_SET]);
			sumStatistics.counters[WRITE_SET] = Math.max(
				statistics[j].counters[WRITE_SET],
				sumStatistics.counters[WRITE_SET]);
			sumStatistics.counters[READ_OR_WRITE_SET] = Math.max(
				statistics[j].counters[READ_OR_WRITE_SET],
				sumStatistics.counters[READ_OR_WRITE_SET]);
		}
		sumStatistics.saved = true;
		
		stat(sumStatistics, "SUM/MAX");
	}

	public static void stat(int cpuId) {
		while (!statistics[cpuId].saved);
		
		System.out.println();
		System.out.print("CPU ");
		System.out.println(cpuId);
		
		for (int i = 0; i < COUNTERS_CNT; i++) {
			System.out.print(descriptions[i]);
			System.out.println(statistics[cpuId].counters[i]);
		}
	}
	
	public static void stat(Statistics statistics, String description) {
		while (!statistics.saved);
		
		System.out.println();
		System.out.println(description);
		
		for (int i = 0; i < COUNTERS_CNT; i++) {
			System.out.print(descriptions[i]);
			System.out.println(statistics.counters[i]);
		}
	}
}
