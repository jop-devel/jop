/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 208, Benedikt Huber (benedikt@vmars.tuwien.ac.at)

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

import java.io.PrintStream;
import java.util.Collection;

import com.jopdesign.tools.DataMemory.DataMemoryStats;

public class DataCacheStats implements DataMemoryStats {	
	
	public enum StatTy { ReadCount, HitCount, BypassCount, WriteCount, InvalidateCount };
	
	private long stats[] = new long[StatTy.values().length];
	private String name;

	public DataCacheStats(String name) {			
		this.name = name;
	}

	public long get(StatTy ty) { 
		return stats[ty.ordinal()];
	}
	public long getMissCount() {
		return get(StatTy.ReadCount) - get(StatTy.HitCount) - get(StatTy.BypassCount);
	}
	public double getHitRate() {
		return (double)get(StatTy.HitCount) / (double)get(StatTy.ReadCount);
	}
	public double getMissRate() {
		return (double)getMissCount() / (double)get(StatTy.ReadCount);
	}
	public double getBypassRate() {
		return (double)get(StatTy.BypassCount) / (double)get(StatTy.ReadCount);
	}

	private void incr(StatTy ty) {
		stats[ty.ordinal()]++;		
	}
	
	public void read(boolean hit) {
		incr(StatTy.ReadCount);
		if(hit) incr(StatTy.HitCount);
	}
	public void readBypassed() {
		incr(StatTy.ReadCount);
		incr(StatTy.BypassCount);		
	}
	public void write() {
		incr(StatTy.WriteCount);
	}
	public void invalidate() {
		incr(StatTy.InvalidateCount);
	}
	@Override
	public void reset() {
		this.stats = new long[stats.length];
	}

	@Override
	public void dump(PrintStream out) {
		out.println(getHeader());
		out.println(toString());
	}
	public String getHeader() {
		return String.format("%20s & %8s & %8s & %8s & %8s & %8s & %6s & %6s & %6s \\\\",
							 "name", "readcnt", "hitcnt", "bypass", "wrcnt", "invalcnt",
							 "hit%", "miss%", "byp%");
	}
	@Override
	public String toString() {
		return String.format("%20s & %8d & %8d & %8d & %8d & %8d & %2.4f & %2.4f & %2.4f \\\\",
				             name, get(StatTy.ReadCount), get(StatTy.HitCount), get(StatTy.BypassCount),
				             get(StatTy.WriteCount), get(StatTy.InvalidateCount),
				             100*getHitRate(), 100*getMissRate(), 100*getBypassRate());
	}
	
	@Override
	public DataCacheStats addAverage(Collection<? extends DataMemoryStats> stats) {
		int n = stats.size();
		if(n == 0) return this;
		for(DataMemoryStats gcs : stats) {
			DataCacheStats cs = (DataCacheStats) gcs;
			for(StatTy ty : StatTy.values()) {
				this.stats[ty.ordinal()] += cs.get(ty);
			}
		}
		for(StatTy ty : StatTy.values()) {
			this.stats[ty.ordinal()] /= n;
		}
		return this;
	}

	@Override
	public DataCacheStats clone() {
		DataCacheStats dcs = new DataCacheStats(this.name);
		System.arraycopy(this.stats, 0, dcs.stats, 0, stats.length);
		return dcs;
	}
	
}