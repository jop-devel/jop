package com.jopdesign.tools;

public class ObjectCacheSim {

	
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
		public int missCount = 0, accessCount = 0;
		public void reset() {
			missCount = 0;
			accessCount = 0;
		}
	}
	
	private int assoc;
	private int lineSize;
	private boolean fillLine;
	private boolean useFieldsAsTag;
	private boolean useLRU;
	
	private ObjectCacheStat stats;
	private CacheEntry cacheLines[];
	
	public ObjectCacheSim(int assoc, int lineSize, boolean fillLine, boolean useFieldsAsTag, boolean useLRU) {
		this.assoc = assoc;
		this.lineSize = lineSize;
		this.fillLine = fillLine;
		this.useFieldsAsTag = useFieldsAsTag;
		this.useLRU = useLRU;
		
		this.stats = new ObjectCacheStat();
		this.cacheLines = new CacheEntry[assoc];
		for(int i = 0; i < assoc; i++) cacheLines[i] = CacheEntry.invalidEntry(lineSize);
	}

	public void accessField(int ref, int off) {
		int addr;
		if(useFieldsAsTag) addr = ref+off;
		else                   addr = ref;
		this.stats.accessCount++;
		if(! useFieldsAsTag && off > lineSize) {
			stats.missCount++;
			return;
		}
		CacheLookup lookup;
		if(useLRU) lookup = insertLineLRU(addr);
		else       lookup = insertLineFIFO(addr);
		
		if(! useFieldsAsTag) {
			int line = lookup.cacheLine;
			boolean isFieldCached = cacheLines[line].contents[off] >= 0;
			if(! isFieldCached) {
				lookup.wasHit = false;
				if(fillLine) {
					for(int i = 0; i < lineSize; i++) {
						cacheLines[line].contents[i] = i;
					}
				} else {
					cacheLines[line].contents[off] = off;
				}
			}
		}
		if(! lookup.wasHit) {
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
		int ac = stats.accessCount;
		int mc = stats.missCount;
		System.out.println(
				String.format("Object Cache: Assoc: %d, Access: %d, Miss: %d, Ration: %.2f %%",
						assoc, ac,mc,(double)(ac-mc)/(double)(ac)*100.0));
	}
}
