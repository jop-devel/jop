package com.jopdesign.tools.splitcache;

import java.io.PrintStream;
import java.util.Vector;

import com.jopdesign.tools.DataMemory;
import com.jopdesign.tools.Cache.ReplacementStrategy;
import com.jopdesign.tools.splitcache.ObjectCache.FieldIndexMode;

/**
/**
 * This is the interface for split data cache simulations.
 * It's purpose is to collect statistics and simulate different
 * data cache architectures, providing a simple interface for JopSim.
 * 
 * One part of the interface allows the simulator to start, pause,
 * resume and stop the cache simulations and dump the collected
 * statistics. The other part is the implementation of the DataMemory
 * interface, which forwards the read/write/invalidate requests to
 * cache simulations and statistics collectors.
 * 
 * @author Benedikt Huber (benedikt@vmars.tuwien.ac.at)
 *
 */
public class SplitCacheSim implements DataMemory {
	private DataMemory backingMem;
	private Vector<DataMemory> caches;

	private SplitCache splitAllStats;
	private SplitCacheStats splitNoneStats;

	public SplitCacheSim(DataMemory backingMem) {
		this.backingMem = backingMem;
		caches = new Vector<DataMemory>();
		caches.add(backingMem);
		splitAllStats = SplitCacheStats.splitAllStatistics(backingMem);
		splitNoneStats = new SplitCacheStats(backingMem, "D$ (noinval)",Access.values(), false);
		caches.add(splitAllStats);
		caches.add(splitNoneStats);
	}
	
	public void addSimulatedCache(DataMemory cache) {
		caches.add(cache);
	}
	
	@Override
	public void invalidateData() {
		for(DataMemory cache : caches)
			cache.invalidateData();
	}

	@Override
	public void invalidateHandles() {
		for(DataMemory cache : caches)
			cache.invalidateHandles();
	}

	@Override
	public int read(int addr, Access type) {
		Integer v = null, prev = null;
		for(DataMemory cache : caches) {
			v = cache.read(addr, type);
			if(prev != null) checkRead(prev,v,cache, " / read " + addr + " @" + type);
			prev = v;
		}
		return v;
	}

	@Override
	public int readIndirect(int handle, int offset, Access type) {
		Integer v = null, prev = null;
		for(DataMemory cache : caches) {
			v = cache.readIndirect(handle, offset, type);
			if(prev != null) checkRead(prev, v,cache,"indirect: "+handle+" + "+offset+ " ~ " + backingMem.read(handle, Access.HANDLE)+ " @"+type);
			prev = v;
		}
		return v;
	}

	private void checkRead(int ref, int actual, DataMemory actualCache, String msg) {
		if(actual != ref) { 
			throw new AssertionError("SplitCacheSim: Wrong value in cache " + actualCache.getName() +
				      ": "+ "expected " + ref + " but found " + actual + " / " + msg);
		}		
	}

	@Override
	public void write(int addr, int value, Access type) {
		for(DataMemory cache : caches) {
			cache.write(addr, value, type);
		}
	}

	@Override
	public void writeIndirect(int handle, int offset, int value, Access type) {
		for(DataMemory cache : caches) {
			cache.writeIndirect(handle, offset, value, type);
		}
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
		String s = "=             Split Cache Simulations                =";
		int l = s.length();
		System.out.println(repeat('=', l));
		System.out.println(s);
		System.out.println(repeat('=', l));
		for(DataMemory cache : caches) {		
			System.out.println();
			cache.dumpStats();
		}
	}

	@Override
	public String getName() {
		return "SplitCacheSim{ "+ getName() + " }";
	}

//	public static DataMemory createIdealSplitCache(DataMemory nextLevelMem, int maxMemSize) {
//		SplitCache splitCache = new SplitCache("ideal",nextLevelMem);
//		for(Access ty : Access.values()) {
//			// Even an 'ideal' cache has to invalidate data on synchronization
//			splitCache.addCache(new SetAssociativeCache(1,maxMemSize,1,ReplacementStrategy.LRU,
//					            ty.isMutableData(), false, nextLevelMem), ty);
//		}
//		return splitCache;
//	}

	public void addDefaultCaches() {

		// Add a 8x8x4 object cache
		Access handled[] = { Access.FIELD };
		SplitCache ocSplitCache = new SplitCache("object$-8-4-4 + RAM", backingMem);
		ObjectCache objectCache = new ObjectCache(8,4,4,FieldIndexMode.Bypass,ReplacementStrategy.FIFO,
				false, backingMem, backingMem);
		ocSplitCache.addCache(objectCache, handled);
		this.caches.add(ocSplitCache);
		
	}


	private static StringBuffer repeat(char c, int k) {
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < k; i++) sb.append(c);
		return sb;
	}

	public static void printHeader(PrintStream out, String string) {
		out.println(repeat('-',string.length()));
		out.println(string);
		out.println(repeat('-',string.length()));
	}
	

}
