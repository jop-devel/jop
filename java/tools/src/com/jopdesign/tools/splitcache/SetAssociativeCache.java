package com.jopdesign.tools.splitcache;


/**
 * A generalized set-associative cache, with configurable number of ways (N),
 * number of cache lines per way, cache block size. Concrete subclasses implement
 * the cache replacement strategy.
 * Each parameter should be a power of 2.
 */
abstract class SetAssociativeCache implements Cache {
	public static class CacheLookupResult {
		int datum;
		boolean isHit;
		public CacheLookupResult(int datum, boolean isHit) {
			this.isHit = isHit;
			this.datum = datum;
		}
	}
	
	protected int ways;
	protected int lines;
	protected int blockSize;

	protected CacheBlock[][] cacheData;
	protected CacheStats stats;
	protected com.jopdesign.tools.splitcache.Cache nextLevelCache;
	private int blockBits;

	public int getSize() {
		return ways*lines*blockSize;
	}
	
	/**
	 * Build a new set associative cache
	 * @param ways Associativity
	 * @param rows Number of cache lines
	 * @param shift of address bits for line determination.
	 */
	public SetAssociativeCache(int ways, int linesPerWay, int wordsPerBlock, Cache nextLevelCache) {
		
		this.ways = ways;
		this.lines = linesPerWay;
		this.blockSize = wordsPerBlock;
		
		blockBits = 0;
		for(int w = blockSize - 1; w > 0; w>>=1) {
			blockBits++;
		}
		
		cacheData = new CacheBlock[ways][lines];
		for(int i = 0; i < ways; i++)
			for(int j = 0; j < lines; j++)
				cacheData[i][j] = new CacheBlock(blockSize);
		
		this.nextLevelCache = nextLevelCache;
		stats = new CacheStats();
	}
			
	public void invalidate() {
		
		for(int i = 0; i < ways; i++)
			for(int j = 0; j < lines; j++)
				cacheData[i][j].invalidate();
		stats.invalidate();
	}
	
	
	/** Read data from the given address; the upper bits select
	 * the cache line, the lower ones the word within the cache block:<br/>
	 * {@code | line_1 .... line_n | word_1 ... word_m | }<br/>
	 * {@code | ______ TAG _______ | _____ WORD ______ | }<br/>
	 * That is, the address is assumed to in words.
	 */	
	public int read(int addr_word) {
		
		int tag = addr_word>>>blockBits;
		int line = (tag) % lines;
		SetAssociativeCache.CacheLookupResult t = readCacheBlock(addr_word, tag, line);
		stats.read(t.isHit);
		return t.datum;
	}
	
	public abstract SetAssociativeCache.CacheLookupResult readCacheBlock(int addr, int tag, int line);
	
	public int blockMask() {
		
		return blockSize - 1;
	}
	
	public boolean isValid(int way, int line) {		
		return cacheData[way][line].isValid();
	}
	
	public int getTag(int way, int line) {
		
		return cacheData[way][line].getTag();
	}
	
	public CacheBlock getCacheBlock(int way, int line) {
		
		return cacheData[way][line];
	}
	/**
	 * @param tag
	 * @param line
	 * @return
	 */
	protected int lookupTag(int tag, int line) {
		
		int way = 0;
		while(way<ways) {
			if(isValid(way,line) && getTag(way,line) == tag) {
				break;
			}
			++way;
		}
		return way;
	}
	
	protected CacheBlock getOrLoadCacheBlock(int tag, int line, int way) {
		
		CacheBlock cacheBlock;

		/* On miss, load data */
		if(! validWay(way)) {
			cacheBlock = new CacheBlock(blockSize);
			cacheBlock.load(tag, tag<<blockBits, nextLevelCache);
		} else {
			cacheBlock = getCacheBlock(way,line);
		}
		return cacheBlock;
	}

	protected boolean validWay(int way) {
		
		return way >= 0 && way < ways;
	}
	
}