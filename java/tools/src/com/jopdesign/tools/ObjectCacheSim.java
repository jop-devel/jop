package com.jopdesign.tools;

public class ObjectCacheSim {
	private static final boolean CACHE_SINGLE_FIELDS = 
		System.getenv("WCET_CACHE_FIELDS_ONLY") != null;
	private int assoc;
	private ObjectCacheStat stats;
	private int osize;
	private int[] tags;
	public static class ObjectCacheStat {
		public int missCount = 0, accessCount = 0;
		public void reset() {
			missCount = 0;
			accessCount = 0;
		}
	}
	public ObjectCacheSim(int assoc, int osize) {
		this.assoc = assoc;
		this.osize = osize;
		this.stats = new ObjectCacheStat();
		this.tags = new int[assoc];
		for(int i = 0; i < assoc; i++) tags[i] = 0;
	}

	public void accessField(int ref, int off) {
		int addr;
		if(CACHE_SINGLE_FIELDS) addr = ref+off;
		else                    addr = ref;
		this.stats.accessCount++;
		if(! CACHE_SINGLE_FIELDS && off > osize) {
			stats.missCount++;
			return;
		}
		if(! isCached(addr)) {
			stats.missCount++;
		}
		loadObject(addr);
	}
	// 0 is first entry
	private void loadObject(int ref) {
		if(tags[0] == ref) return;
		int i = 0;
		int floating = ref;
		// Invariant: 'floating' needs to be stored somewhere
		do {
			int tmp = tags[i];
			tags[i] = floating;
			floating = tmp;
			i++;
		} while(i < assoc && tags[i] != ref);
		if(i < assoc) { // tags[i] = ref
			tags[i] = floating;
		}
	}

	private boolean isCached(int ref) {
		for(int i = 0; i < assoc; i++) {
			if(tags [i] == ref) return true;
		}
		return false;
	}
	public void resetStats() {
		stats.reset();
	}
	public ObjectCacheStat getStats() {
		return stats;
	}

	public void flushCache() {
		resetStats();
		for(int i = 0; i < assoc; i++) tags[i] = 0;
	}

	public void dumpStats() {
		int ac = stats.accessCount;
		int mc = stats.missCount;
		System.out.println(
				String.format("Object Cache: Assoc: %d, Access: %d, Miss: %d, Ration: %.2f %%",
						assoc, ac,mc,(double)(ac-mc)/(double)(ac)*100.0));
	}
}
