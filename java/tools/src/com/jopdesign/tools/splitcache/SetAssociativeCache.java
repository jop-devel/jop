package com.jopdesign.tools.splitcache;

import java.util.Stack;

import com.jopdesign.tools.DataMemory;
import com.jopdesign.tools.Cache.ReplacementStrategy;

/**
 * A generalized set-associative cache, with configurable number of ways (N),
 * number of cache lines per way, cache block size. Concrete subclasses implement
 * the cache replacement strategy.
 * Each parameter should be a power of 2.
 */
public class SetAssociativeCache implements DataMemory {
	
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
	protected DataMemory nextLevelMemory;
	private int blockBits;
	private ReplacementStrategy replacementStrategy;
	private boolean needsDataInvalidation;
	private boolean needsHandleInvalidation;

	protected CacheStats stats;
	private Stack<CacheStats> statsStack = new Stack<CacheStats>();

	public int getSizeInWords() {
		return ways*lines*blockSize;
	}
	public int getSizeInBytes() {
		return getSizeInWords() << 2;
	}
	
	/**
	 * Build a new set associative cache
	 * @param ways Associativity
	 * @param rows Number of cache lines
	 * @param shift of address bits for line determination.
	 */
	public SetAssociativeCache(int ways, int linesPerWay, int wordsPerBlock, ReplacementStrategy replacementStrategy,
			                   boolean needsDataInvalidation, boolean needsHandleInvalidation,
			                   DataMemory nextLevelCache) {
		
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
		
		this.replacementStrategy = replacementStrategy;
		this.nextLevelMemory = nextLevelCache;
		this.needsDataInvalidation = needsDataInvalidation;
		this.needsHandleInvalidation = needsHandleInvalidation;
		resetStats();
	}
		
	public void invalidateCache() {
		
		for(int i = 0; i < ways; i++)
			for(int j = 0; j < lines; j++)
				cacheData[i][j].invalidate();
		stats.invalidate();
	}
	
	@Override
	public void invalidateData() {
		if(this.needsDataInvalidation) {
			this.invalidateCache();
		}
	}

	@Override
	public void invalidateHandles() {
		if(this.needsHandleInvalidation) {
			this.invalidateCache();
		}
	}
	
	/** Read data from the given address; the upper bits select
	 * the cache line, the lower ones the word within the cache block:<br/>
	 * {@code | line_1 .... line_n | word_1 ... word_m | 0 0     }<br/>
	 * {@code | ______ TAG _______ | _____ WORD ______ | aligned }<br/>
	 * That is, the address is assumed to be word-aligned
	 */	
	@Override
	public int read(int addr, Access type) {
		CacheBlock cacheBlock = readCacheBlock(addr, tagOfAddress(addr), lineOfAddress(addr));

		/* return cache lookup result */
		int datum = cacheBlock.getData(wordOfAddress(addr));
		if(datum != nextLevelMemory.read(addr,type)) {
			throw new AssertionError("Bad Cache implementation: Read "+datum+" at "+addr+" but next level has "+nextLevelMemory.read(addr,type));
		}
		return datum;
	}


	@Override
	public void write(int addr, int value, Access type) {
		int way = lookupTag(tagOfAddress(addr), lineOfAddress(addr));
		if(validWay(way)) {
			CacheBlock cb = getCacheBlock(way, lineOfAddress(addr));
			cb.modifyData(wordOfAddress(addr), value);
		}
		nextLevelMemory.write(addr, value, type);
	}
	
	public int readIndirect(int handle, int offset, Access type) {
		int addr = read(handle, Access.HANDLE);
		return read(addr+offset, type);
	}
	public void writeIndirect(int handle, int offset, int value, Access type) {
		int addr = read(handle, Access.HANDLE);
		write(addr+offset, value, type);		
	}

	private CacheBlock readCacheBlock(int addr, int tag, int line) {
		int way = lookupTag(tag, line);

		CacheBlock cacheBlock = getOrLoadCacheBlock(tag, line, way);

		switch(replacementStrategy) {
		case LRU:  		updateLRU(cacheBlock, way, cacheData, line);
		case FIFO:      updateFIFO(cacheBlock, way, cacheData, line);
		}

		stats.read(validWay(way));
		return cacheBlock;
	}	
	
	
	private int blockMask() {		
		return blockSize - 1;
	}
	

	private int lookupTag(int tag, int line) {
		
		int way = 0;
		while(way<ways) {
			if(isValid(way,line) && getTag(way,line) == tag) {
				break;
			}
			++way;
		}
		return way;
	}
	
	private CacheBlock getOrLoadCacheBlock(int tag, int line, int way) {
		
		CacheBlock cacheBlock;

		/* On miss, load data */
		if(! validWay(way)) {
			cacheBlock = new CacheBlock(blockSize);
			cacheBlock.load(tag, addressOfTag(tag), nextLevelMemory);
		} else {
			cacheBlock = getCacheBlock(way,line);
		}
		return cacheBlock;
	}

	public int wordOfAddress(int addr) {
		return addr & blockMask();		
	}
	
	public int tagOfAddress(int addr) {
		return addr >>> blockBits;
	}
	
	private int addressOfTag(int tag) {
		return tag << blockBits;
	}

	public int lineOfAddress(int addr) {
		return tagOfAddress(addr) % lines;		
	}

	protected boolean validWay(int way) {		
		return way >= 0 && way < ways;
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
	
	@Override
	public String getName() {
		return String.format("Object Cache{ ways=%d, lines=%d, blocksize=%d, replacement=%s }",
				             this.ways, this.lines, this.blockSize,
				             this.replacementStrategy.toString());
	}
	@Override
	public void resetStats() {
		stats = new CacheStats();
	}
	@Override
	public void recordStats() {
		statsStack.push(stats);
	}

	@Override
	public void dumpStats() {
		System.out.println(this.stats.toString());
	}

	public static<BlockType> void updateLRU(BlockType accessedElement, int oldBlockPos, BlockType[][] cacheData, int line) {
		/* LRU move A B C X D -> X A B C D*/
		BlockType movedBlock = accessedElement;
		for(int w = 0; w <= oldBlockPos && w < cacheData.length; w++) {
			BlockType activeBlock = cacheData[w][line];
			cacheData[w][line] = movedBlock;
			movedBlock = activeBlock;
		}		
	}

	public static<BlockType> void updateFIFO(BlockType accessedElement, int oldBlockPos, BlockType[][] cacheData, int line) {
		/* FIFO move A B C D -> X A B C */
		if(oldBlockPos >= 0 && oldBlockPos < cacheData.length) {
			BlockType movedBlock = accessedElement;
			for(int w = 0; w < cacheData.length; w++) {
				BlockType activeBlock = cacheData[w][line];
				cacheData[w][line] = movedBlock;
				movedBlock = activeBlock;
			}				
		}
	}

}