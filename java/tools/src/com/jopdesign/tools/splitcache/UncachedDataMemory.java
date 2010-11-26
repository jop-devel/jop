package com.jopdesign.tools.splitcache;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import com.jopdesign.tools.DataMemory;

public class UncachedDataMemory extends DataMemory {

	private DataMemory backingMem;
	private Stack<DataCacheStats> recordedStats = new Stack<DataCacheStats>();
	private DataCacheStats stats;
	private Set<Access> handledAccessTypes;

	public UncachedDataMemory(DataMemory backingMem, Access[] handled) {
		this.backingMem = backingMem;
		this.stats = new DataCacheStats(getName());
		this.handledAccessTypes = new HashSet<Access>(Arrays.asList(handled));
	}

	@Override
	public void dump(PrintStream out) {
		SplitCacheSim.printHeader(out, "Uncached Memory "+handledAccessTypes.toString());
		new DataCacheStats(getName()).addAverage(recordedStats).dump(out);
	}

	@Override
	public String getName() {
		return "uncached-memory";
	}

	@Override
	public void invalidateCache() {}
	@Override
	public void invalidateData() {}
	@Override
	public void invalidateHandles() {}

	@Override
	public int read(int addr, Access type) {
		stats.readBypassed();
		return backingMem.read(addr, type);
	}

	@Override
	public void recordStats() {
		this.recordedStats.push(stats.clone());
	}

	@Override
	public void resetStats() {
		this.stats.reset();
	}

	@Override
	public void write(int addr, int value, Access type) {
		this.stats.write();
		backingMem.write(addr,value,type);
	}
}
