package com.jopdesign.tools.splitcache;

import java.io.PrintStream;
import java.util.Vector;

import com.jopdesign.tools.DataMemory;
import com.jopdesign.tools.Cache.ReplacementStrategy;
import com.jopdesign.tools.DataMemory.Access;
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
public class SplitCacheSim extends DataMemory {
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
	public void invalidateCache() {
		for(DataMemory cache : caches)
			cache.invalidateCache();
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
	public int readField(int handle, int offset, Access type) {
		Integer v = null, prev = null;
		for(DataMemory cache : caches) {
			v = cache.readField(handle, offset, type);
			if(prev != null) checkRead(prev,v,cache,"indirect: "+handle+" + "+offset+ " ~ " + backingMem.read(handle, Access.HANDLE)+ " @"+type);
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
	public void writeField(int handle, int offset, int value, Access type) {
		for(DataMemory cache : caches) {
			cache.writeField(handle, offset, value, type);
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
	public void dump(PrintStream out) {
		printHeader(out, "            Split Cache Simulations            ", '=');
		for(DataMemory cache : caches) {		
			out.println();
			cache.dump(out);
		}
		printHeader(out, "            End of Split Cache Simulation      ", '*');
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
		// Add a few (pure) object caches
		addObjectCacheSims();
		// Add standard data caches
		addStdDataCacheSims();
		// Add split caches
		addSplitCacheSims();
	}

	private void addStdDataCacheSims() {
		SetAssociativeCache dCache;
		dCache = new SetAssociativeCache(4,128,1,ReplacementStrategy.LRU,
                true,false,this.backingMem, Access.values());
		this.caches.add(dCache);	
		dCache = new SetAssociativeCache(4,32,4,ReplacementStrategy.LRU,
                true,false,this.backingMem, Access.values());
		this.caches.add(dCache);	
	}

	private void addSplitCacheSims() {
		Access handledConst[] = { Access.CLINFO, Access.CONST, Access.IFTAB, Access.MTAB };
		Access handledStatic[] = { Access.STATIC };

		Access handledFullyAssoc[] = { Access.FIELD, Access.MVB, Access.HANDLE };
		Access handledArrayBypass[] = { Access.INTERN, Access.ARRAY, Access.ALEN };

		Access handledOCache[] = { Access.FIELD, Access.MVB, Access.HANDLE };
		Access handledMem[] = { Access.INTERN, Access.ARRAY, Access.ALEN };

		SplitCache splitCache;
		SetAssociativeCache constCache;
		SetAssociativeCache staticCache;
		ObjectCache objectCache;

		// Currently implemented split cache (source: wolfgang)
		splitCache = new SplitCache("const-1-256-1 + static-1-256-1 + fullassoc-16-1-1 + RAM", backingMem);
		constCache = new SetAssociativeCache(1,256,1,ReplacementStrategy.LRU,false,false,backingMem,handledConst);
		splitCache.addCache(constCache, handledConst);
		staticCache = new SetAssociativeCache(1,256,1,ReplacementStrategy.LRU,true,false,backingMem,handledStatic);
		splitCache.addCache(staticCache, handledStatic);
		staticCache = new SetAssociativeCache(16,1,1,ReplacementStrategy.LRU,true,false,backingMem,handledFullyAssoc);
		splitCache.addCache(staticCache, handledFullyAssoc);
		splitCache.addCache(new UncachedDataMemory(backingMem, handledArrayBypass), handledArrayBypass);
		this.caches.add(splitCache);

		// Split cache as above, but with 4-word blocks
		splitCache = new SplitCache("const-1-64-4 + static-1-64-4 + fullassoc-16-1-4 + RAM", backingMem);
		constCache = new SetAssociativeCache(1,64,4,ReplacementStrategy.LRU,false,false,backingMem,handledConst);
		splitCache.addCache(constCache, handledConst);
		staticCache = new SetAssociativeCache(1,64,4,ReplacementStrategy.LRU,true,false,backingMem,handledStatic);
		splitCache.addCache(staticCache, handledStatic);
		staticCache = new SetAssociativeCache(16,1,4,ReplacementStrategy.LRU,true,false,backingMem,handledFullyAssoc);
		splitCache.addCache(staticCache, handledFullyAssoc);
		splitCache.addCache(new UncachedDataMemory(backingMem, handledArrayBypass), handledArrayBypass);
		this.caches.add(splitCache);

		// evaluation for minor revision ofCC:P&E paper: standard 2Kb Cache
		SetAssociativeCache stdCache;
		Access handledStd[] = { Access.CLINFO, Access.CONST, Access.IFTAB, Access.MTAB, Access.STATIC, Access.FIELD, Access.MVB, Access.HANDLE };
		splitCache = new SplitCache("cc-p&e std 2Kb: 4 x 32 x 4 const,static+object", backingMem);
		stdCache = new SetAssociativeCache(4,32,4,ReplacementStrategy.LRU,false,false,backingMem,handledStd);
		splitCache.addCache(stdCache, handledStd);
		splitCache.addCache(new UncachedDataMemory(backingMem, handledMem), handledMem);
		this.caches.add(splitCache);
		
		// evaluation for minor revision of CC:P&E paper: split cache with object cache, 2Kb
		splitCache = new SplitCache("cc-p&e o$ 2Kb: const-1-64-4 + static-1-32-4 + object$-8-4-4-LRU + RAM", backingMem);
		constCache = new SetAssociativeCache(1,64,4,ReplacementStrategy.LRU,false,false,backingMem,handledConst);
		splitCache.addCache(constCache, handledConst);
		staticCache = new SetAssociativeCache(1,32,4,ReplacementStrategy.LRU,true,false,backingMem,handledStatic);
		splitCache.addCache(staticCache, handledStatic);
		objectCache = new ObjectCache(8,4,4,FieldIndexMode.Bypass,ReplacementStrategy.LRU, false, backingMem, backingMem, handledOCache);
		splitCache.addCache(objectCache, handledOCache);
		splitCache.addCache(new UncachedDataMemory(backingMem, handledMem), handledMem);
		this.caches.add(splitCache);

		// verification of results for minor revision of CC:P&E paper
		// ==> splitcache with standard cache (not O$).
		splitCache = new SplitCache("cc-p&e split3 2Kb: const-1-64-4 + static-1-32-4 + static-1-32-4 + RAM", backingMem);
		constCache = new SetAssociativeCache(1,64,4,ReplacementStrategy.LRU,false,false,backingMem,handledConst);
		splitCache.addCache(constCache, handledConst);
		staticCache = new SetAssociativeCache(1,32,4,ReplacementStrategy.LRU,true,false,backingMem,handledStatic);
		splitCache.addCache(staticCache, handledStatic);
		{ 
			SetAssociativeCache cacheForObjects = new SetAssociativeCache(1,32,4,ReplacementStrategy.LRU,true,false,backingMem,handledOCache);
			splitCache.addCache(cacheForObjects, handledOCache);
		}
		splitCache.addCache(new UncachedDataMemory(backingMem, handledMem), handledMem);
		this.caches.add(splitCache);

		// verification of results for minor revision of CC:P&E paper
		// ==> splitcache with 4 standard caches
		splitCache = new SplitCache("cc-p&e split4 2Kb: const-1-32-4 + static-1-32-4 + handle/mvb-1-32-4 + field-1-32-4 + RAM", backingMem);
		constCache = new SetAssociativeCache(1,64,4,ReplacementStrategy.LRU,false,false,backingMem,handledConst);
		splitCache.addCache(constCache, handledConst);
		staticCache = new SetAssociativeCache(1,32,4,ReplacementStrategy.LRU,true,false,backingMem,handledStatic);
		splitCache.addCache(staticCache, handledStatic);
		{ 
			Access[] handledHandleCache = { Access.HANDLE, Access.MVB };
			SetAssociativeCache cacheForHandles = new SetAssociativeCache(1,64,2,ReplacementStrategy.LRU,true,false,backingMem,handledHandleCache);
			splitCache.addCache(cacheForHandles, handledHandleCache);
			Access[] handledFieldCache = { Access.FIELD };
			SetAssociativeCache cacheForObjects = new SetAssociativeCache(1,32,4,ReplacementStrategy.LRU,true,false,backingMem,handledFieldCache);
			splitCache.addCache(cacheForObjects, handledFieldCache);
		}
		splitCache.addCache(new UncachedDataMemory(backingMem, handledMem), handledMem);
		this.caches.add(splitCache);

		// verification of results for minor revision of CC:P&E paper
		// ==> splitcache with 5 standard caches
		splitCache = new SplitCache("cc-p&e split5 2.5Kb: const-1-32-4 + static-1-32-4 + handle-1-32-4 + mvb-132-4 + field-1-32-4 + RAM", backingMem);
		constCache = new SetAssociativeCache(1,64,4,ReplacementStrategy.LRU,false,false,backingMem,handledConst);
		splitCache.addCache(constCache, handledConst);
		staticCache = new SetAssociativeCache(1,32,4,ReplacementStrategy.LRU,true,false,backingMem,handledStatic);
		splitCache.addCache(staticCache, handledStatic);
		{ 
			Access[] handledHandleCache = { Access.HANDLE };
			SetAssociativeCache cacheForHandles = new SetAssociativeCache(1,64,2,ReplacementStrategy.LRU,true,false,backingMem,handledHandleCache);
			splitCache.addCache(cacheForHandles, handledHandleCache);
			Access[] handledMvbCache = { Access.MVB};
			SetAssociativeCache cacheForMVBs = new SetAssociativeCache(1,64,2,ReplacementStrategy.LRU,true,false,backingMem,handledMvbCache);
			splitCache.addCache(cacheForMVBs, handledMvbCache);
			Access[] handledFieldCache = { Access.FIELD };
			SetAssociativeCache cacheForObjects = new SetAssociativeCache(1,32,4,ReplacementStrategy.LRU,true,false,backingMem,handledFieldCache);
			splitCache.addCache(cacheForObjects, handledFieldCache);
		}
		splitCache.addCache(new UncachedDataMemory(backingMem, handledMem), handledMem);
		this.caches.add(splitCache);

		// // One sample split cache configuration
//		for (int ways = 1; ways <= 32; ways *= 2) { 
//			for (int bpo = 1; bpo <= 32; bpo *= 2) {
//
//				splitCache = new SplitCache("const-1-256-1 + static-1-256-1 + object$-"+ways+"-"+bpo+"-1-LRU + RAM", backingMem);
//				constCache = new SetAssociativeCache(1,256,1,ReplacementStrategy.LRU,false,false,backingMem,handledConst);
//				splitCache.addCache(constCache, handledConst);
//				staticCache = new SetAssociativeCache(1,256,1,ReplacementStrategy.LRU,true,false,backingMem,handledStatic);
//				splitCache.addCache(staticCache, handledStatic);
//				objectCache = new ObjectCache(ways,bpo,1,FieldIndexMode.Bypass,ReplacementStrategy.LRU, false, backingMem, backingMem, handledOCache);
//				splitCache.addCache(objectCache, handledOCache);
//				splitCache.addCache(new UncachedDataMemory(backingMem, handledMem), handledMem);
//				this.caches.add(splitCache);
//
//				splitCache = new SplitCache("const-1-64-4 + static-1-64-4 + object$-"+ways+"-"+bpo+"-4-LRU + RAM", backingMem);
//				constCache = new SetAssociativeCache(1,64,4,ReplacementStrategy.LRU,false,false,backingMem,handledConst);
//				splitCache.addCache(constCache, handledConst);
//				staticCache = new SetAssociativeCache(1,64,4,ReplacementStrategy.LRU,true,false,backingMem,handledStatic);
//				splitCache.addCache(staticCache, handledStatic);
//				objectCache = new ObjectCache(ways,bpo,4,FieldIndexMode.Bypass,ReplacementStrategy.LRU, false, backingMem, backingMem, handledOCache);
//				splitCache.addCache(objectCache, handledOCache);
//				splitCache.addCache(new UncachedDataMemory(backingMem, handledMem), handledMem);
//				this.caches.add(splitCache);
//
//				splitCache = new SplitCache("const-1-64-4 + static-1-64-4 + object$-"+ways+"-1-"+bpo+"-LRU + RAM", backingMem);
//				constCache = new SetAssociativeCache(1,64,4,ReplacementStrategy.LRU,false,false,backingMem,handledConst);
//				splitCache.addCache(constCache, handledConst);
//				staticCache = new SetAssociativeCache(1,64,4,ReplacementStrategy.LRU,true,false,backingMem,handledStatic);
//				splitCache.addCache(staticCache, handledStatic);
//				objectCache = new ObjectCache(ways,1,bpo,FieldIndexMode.Bypass,ReplacementStrategy.LRU, false, backingMem, backingMem, handledOCache);
//				splitCache.addCache(objectCache, handledOCache);
//				splitCache.addCache(new UncachedDataMemory(backingMem, handledMem), handledMem);
//				this.caches.add(splitCache);
//			}
//		}
	}


	private void addObjectCacheSims() {
		// // Add a 8x8x4 object cache
		// Access handledAccessTypes[] = { Access.FIELD };
		// SplitCache ocSplitCache;
		// ObjectCache objectCache;

		// objectCache = new ObjectCache(16,8,4,FieldIndexMode.Bypass,ReplacementStrategy.FIFO, 
		// 		                      false, backingMem, backingMem, handledAccessTypes);
		// ocSplitCache = new SplitCache(objectCache.getName() + " + RAM", backingMem);
		// ocSplitCache.addCache(objectCache, handledAccessTypes);
		// this.caches.add(ocSplitCache);

		// objectCache = new ObjectCache(8,4,4,FieldIndexMode.Bypass,ReplacementStrategy.LRU,
		// 		                      false, backingMem, backingMem, handledAccessTypes);
		// ocSplitCache = new SplitCache(objectCache.getName() + " + RAM", backingMem);
		// ocSplitCache.addCache(objectCache, handledAccessTypes);
		// this.caches.add(ocSplitCache);

		// objectCache = new ObjectCache(8,16,1,FieldIndexMode.Bypass,ReplacementStrategy.LRU, 
		// 		                     false, backingMem, backingMem, handledAccessTypes);
		// ocSplitCache = new SplitCache(objectCache.getName() + " + RAM", backingMem);
		// ocSplitCache.addCache(objectCache, handledAccessTypes);
		// this.caches.add(ocSplitCache);

		// objectCache = new ObjectCache(4,4,2,FieldIndexMode.Bypass,ReplacementStrategy.FIFO,
		// 		                      false, backingMem, backingMem, handledAccessTypes);
		// ocSplitCache = new SplitCache(objectCache.getName() + " + RAM", backingMem);
		// ocSplitCache.addCache(objectCache, handledAccessTypes);
		// this.caches.add(ocSplitCache);
	}

	private static StringBuffer repeat(char c, int k) {
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < k; i++) sb.append(c);
		return sb;
	}

	public static void printHeader(PrintStream out, String string) {
		printHeader(out, string, '-');
	}
	
	static void printHeader(PrintStream out, String string, char c) {
		out.println(repeat(c,string.length()));
		out.println(string);
		out.println(repeat(c,string.length()));
	}

}
