package com.jopdesign.tools.splitcache;
// TODO: remove
/**
 * Object Cache Simulator
 * To enable object cache simulation, you should add DUMP_CACHE statements to your test driver.
 * Additionally, use the environment variables defined below to control the object cache
 *
 * 
 *
 * @author Benedikt Huber (benedikt@vmars.tuwien.ac.at)
 *
 */
public class ObjectCacheSim {
	private static final String OCACHE_ASSOC = "OCACHE_ASSOC";
	private static final String OCACHE_WORDS_PER_LINE = "OCACHE_WORDS_PER_LINE";
	private static final String OCACHE_BURST_WORDS = "OCACHE_BURST_WORDS";
	private static final String OCACHE_SINGLE_FIELD = "OCACHE_SINGLE_FIELD";
	private static final String OCACHE_REPLACEMENT = "OCACHE_REPLACEMENT";
	private static final String OCACHE_ACCESS_COST = "OCACHE_ACCESS_COST";
	private static final String OCACHE_LOAD_FIELD_COST = "OCACHE_LOAD_FIELD_COST";
	private static final String OCACHE_LOAD_BURST_COST = "OCACHE_LOAD_BURST_COST";

	private static String stringFromEnv(String key, String def) {
		String val = System.getenv(key);
		if(val == null) return def;
		return val;		
	}
	private static int intFromEnv(String key, int def) {
		return Integer.parseInt(stringFromEnv(key,""+def));
	}
	private static boolean boolFromEnv(String key) {
		return System.getenv(key) != null;
	}
	
	public static ObjectCacheSim configureFromEnv() {
		int assoc = intFromEnv(OCACHE_ASSOC, 16);
		int wordsPerLine = intFromEnv(OCACHE_WORDS_PER_LINE, 16);
		int wordsPerBurst = intFromEnv(OCACHE_BURST_WORDS,1);
		boolean fieldAsTag = boolFromEnv(OCACHE_SINGLE_FIELD);
		String replacement = stringFromEnv(OCACHE_REPLACEMENT,"lru");
		
		int accessCost = intFromEnv(OCACHE_ACCESS_COST, 0);
		int loadFieldCost = intFromEnv(OCACHE_LOAD_FIELD_COST, 2);
		int loadBurstCost = intFromEnv(OCACHE_LOAD_BURST_COST, 2 * wordsPerBurst);
		
		ObjectCacheSim ocs = new ObjectCacheSim(assoc,wordsPerLine,wordsPerBurst,fieldAsTag,replacement.equals("lru"));
		ocs.setCost(accessCost, loadFieldCost, loadBurstCost);
		return ocs;
	}

	private int accessCost;
	private int loadBurstCost;
	private int loadFieldCost;
	
	private void setCost(int accessCost, int loadFieldCost, int loadBurstCost) {
		this.accessCost = accessCost;
		this.loadFieldCost = loadFieldCost;
		this.loadBurstCost = loadBurstCost;
	}


	static class CacheEntry {
		int tag;
		int contents[];
		public static CacheEntry invalidEntry(int contentSize) {
			CacheEntry ce = new CacheEntry();
			ce.tag = -1;
			ce.contents = new int[contentSize];
			return ce;
			
		}
		public CacheEntry reuse(int ref, int initialContent) {
			for(int i = 0; i < contents.length; i++) contents[i] = initialContent;
			tag = ref;
			return this;
		}
		public void invalidate(int initialContent) {
			for(int i = 0; i < contents.length; i++) contents[i] = initialContent;
			tag = -1;
		}
	}
	
	static class CacheLookup {
		public CacheLookup(boolean hit, int i) {
			wasHit = hit;
			cacheLine = i;
		}
		boolean wasHit;
		int cacheLine;
	}
	
	public static class ObjectCacheStat {
		public int loadCycles = 0, missCount = 0, accessCount = 0;
		public void reset() {
			loadCycles = 0;
			missCount = 0;
			accessCount = 0;
		}
	}
	
	private int assoc;
	private int lineSize;
	private boolean useFieldsAsTag;
	private boolean useLRU;
	
	private ObjectCacheStat stats;
	private CacheEntry cacheLines[];
	private int wordsPerBurst;
	private int burstMask;
	
	private ObjectCacheSim(int assoc, int lineSize, int wordsPerBurst, boolean useFieldsAsTag, boolean useLRU) {
		this.assoc = assoc;
		this.lineSize = lineSize;
		this.wordsPerBurst = wordsPerBurst;
		/* e.g. ~11 for 4 word burst */
		this.burstMask = ~ (wordsPerBurst-1);
		this.useFieldsAsTag = useFieldsAsTag;
		this.useLRU = useLRU;
		
		this.stats = new ObjectCacheStat();
		this.cacheLines = new CacheEntry[assoc];
		for(int i = 0; i < assoc; i++) cacheLines[i] = CacheEntry.invalidEntry(lineSize);
	}

	public void accessField(int ref, int off) {
		int addr;
		stats.loadCycles += this.accessCost; 
		if(useFieldsAsTag) addr = ref+off;
		else               addr = ref;
		this.stats.accessCount++;
		if(! useFieldsAsTag && off > lineSize) {
			stats.missCount++;
			stats.loadCycles += this.loadFieldCost;
			return;
		}
		CacheLookup lookup;
		if(useLRU) lookup = insertLineLRU(addr);
		else       lookup = insertLineFIFO(addr);
		
		if(! useFieldsAsTag) {
			int line = lookup.cacheLine;
			boolean isFieldCached = cacheLines[line].contents[off] >= 0;
			if(! isFieldCached) {
				//System.out.println("Field not cached for "+addr+" + "+off+" -- fillLine = "+fillLine);
				lookup.wasHit = false;
				int startOff = off & burstMask;
				for(int i = startOff; i < startOff + wordsPerBurst; i++) {
					cacheLines[line].contents[i] = i;
				}
			} else if(! lookup.wasHit) {
				throw new AssertionError("Cache is incoherent: not a hit but field is cached: "+addr+" + "+off);
			}
		}
		if(! lookup.wasHit) {
			stats.loadCycles += this.loadBurstCost;
			stats.missCount++;			
		}
	}

	
	
	// 0 is first entry
	private CacheLookup insertLineLRU(int ref) {
		if(cacheLines[0].tag == ref) return new CacheLookup(true,0);
		CacheEntry floating = cacheLines[0];
		// Invariant: 'floating' needs to be stored somewhere
		int i;
		for(i = 1; i < assoc; i++)
		{
			if(cacheLines[i].tag == ref) break;
			CacheEntry tmp = cacheLines[i];
			cacheLines[i] = floating;
			floating = tmp;			
		}
		if(i < assoc) { // tags[i] = ref
			// was in cache
			cacheLines[0] = cacheLines[i];
			cacheLines[i] = floating; // was in cache
			return new CacheLookup(true,0);
		} else {
			// was not in cache
			cacheLines[0] = cacheLines[assoc-1].reuse(ref, -1);
			return new CacheLookup(false,0);
		}
	}

	private CacheLookup insertLineFIFO(int ref) {
		int line = cacheLineOf(ref);
		if(line >= 0) return new CacheLookup(true,line);
		CacheEntry last = cacheLines[assoc-1];
		for(int i = assoc-1; i > 0; --i) {
			cacheLines[i] = cacheLines[i-1];
		}
		cacheLines[0] = last.reuse(ref, -1);
		return new CacheLookup(false,0);
	}
	
	private int cacheLineOf(int ref) {
		for(int i = 0; i < assoc; i++) {
			if(cacheLines[i].tag == ref) return i;
		}
		return -1;
	}
	
	public void resetStats() {
		stats.reset();
	}
	
	public ObjectCacheStat getStats() {
		return stats;
	}

	public void flushCache() {
		resetStats();
		for(int i = 0; i < assoc; i++) cacheLines[i].invalidate(-1);
	}

	public void dumpStats() {
		double cpa = (double)stats.loadCycles / (double)stats.accessCount;
		int ac = stats.accessCount;
		int mc = stats.missCount;
		System.out.println(
				String.format("Object Cache (%s): Assoc: %d, words per line: %d, burst: %d, Cycles/Access: %.2f, Load Cycles: %d,Access: %d, Miss: %d, Ratio: %.2f %%",
						useLRU?"LRU":"FIFO", assoc, lineSize, wordsPerBurst, cpa, stats.loadCycles,
						ac,mc,(double)(ac-mc)/(double)(ac)*100.0));
	}

}
