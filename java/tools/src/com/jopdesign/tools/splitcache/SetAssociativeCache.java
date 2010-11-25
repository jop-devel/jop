package com.jopdesign.tools.splitcache;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Stack;

import com.jopdesign.tools.DataMemory;
import com.jopdesign.tools.Cache.ReplacementStrategy;

/**
 * A generalized set-associative cache, with configurable number of ways (N),
 * number of cache sets per way and cache block size. Concrete subclasses implement
 * the cache replacement strategy.
 * Each parameter should be a power of 2.
 */
public class SetAssociativeCache extends DataMemory {
	
	public static class CacheLookupResult {
		int datum;
		boolean isHit;
		public CacheLookupResult(int datum, boolean isHit) {
			this.isHit = isHit;
			this.datum = datum;
		}
	}
	
    protected int ways;
	protected int sets;
	protected int blockSize;

	protected CacheBlock[][] cacheData;
	protected DataMemory nextLevelMemory;
	private int blockBits;
	private ReplacementStrategy replacementStrategy;
	private boolean needsDataInvalidation;
	private boolean needsHandleInvalidation;

	protected DataCacheStats stats;
	private Access[] handledAccessTypes;

	public int getSizeInWords() {
		return ways*sets*blockSize;
	}
	public int getSizeInBytes() {
		return getSizeInWords() << 2;
	}
	
	/**
	 * Build a new set associative cache
	 * @param ways Associativity
	 * @param rows Number of cache sets
	 * @param shift of address bits for set determination.
	 */
	public SetAssociativeCache(int ways, int setsPerWay, int wordsPerBlock, ReplacementStrategy replacementStrategy,
			                   boolean needsDataInvalidation, boolean needsHandleInvalidation,
			                   DataMemory nextLevelCache, Access handled[]) {
		this.handledAccessTypes = handled;
		this.ways = ways;
		this.sets = setsPerWay;
		this.blockSize = wordsPerBlock;
		
		blockBits = 0;
		for(int w = blockSize - 1; w > 0; w>>=1) {
			blockBits++;
		}
		
		cacheData = new CacheBlock[ways][sets];
		for(int i = 0; i < ways; i++)
			for(int j = 0; j < sets; j++)
				cacheData[i][j] = new CacheBlock(blockSize);
		
		this.replacementStrategy = replacementStrategy;
		this.nextLevelMemory = nextLevelCache;
		this.needsDataInvalidation = needsDataInvalidation;
		this.needsHandleInvalidation = needsHandleInvalidation;
		this.stats = new DataCacheStats(getName());
	}
		
	public void invalidateCache() {
		
		for(int i = 0; i < ways; i++)
			for(int j = 0; j < sets; j++)
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
	 * the cache set, the lower ones the word within the cache block:<br/>
	 * {@code | set_1 .... set_n | word_1 ... word_m | 0 0     }<br/>
	 * {@code | ______ TAG _______ | _____ WORD ______ | aligned }<br/>
	 * That is, the address is assumed to be word-aligned
	 */	
	@Override
	public int read(int addr, Access type) {
		CacheBlock cacheBlock = readCacheBlock(addr, tagOfAddress(addr), setOfAddress(addr));

		/* return cache lookup result */
		int datum = cacheBlock.getData(wordOfAddress(addr));
		if(datum != nextLevelMemory.read(addr,type)) {
			throw new AssertionError("Bad Cache implementation: Read "+datum+" at "+addr+" but next level has "+nextLevelMemory.read(addr,type));
		}
		return datum;
	}

	private CacheBlock readCacheBlock(int addr, int tag, int set) {
		int way = lookupTag(tag, set);

		CacheBlock cacheBlock = getOrLoadCacheBlock(tag, set, way);

		switch(replacementStrategy) {
		case LRU:  		updateLRU(cacheBlock, way, cacheData, set);
		case FIFO:      updateFIFO(cacheBlock, way, cacheData, set);
		}

		stats.read(validWay(way));
		return cacheBlock;
	}	
	


	@Override
	public void write(int addr, int value, Access type) {
		int way = lookupTag(tagOfAddress(addr), setOfAddress(addr));
		if(validWay(way)) {
			CacheBlock cb = getCacheBlock(way, setOfAddress(addr));
			cb.modifyData(wordOfAddress(addr), value);
		}
		stats.write();
		nextLevelMemory.write(addr, value, type);
	}
	
	public int readField(int handle, int offset, Access type) {
		int addr = read(handle, Access.HANDLE);
		return read(addr+offset, type);
	}
	public void writeField(int handle, int offset, int value, Access type) {
		int addr = read(handle, Access.HANDLE);
		write(addr+offset, value, type);		
	}

	
	private int blockMask() {		
		return blockSize - 1;
	}
	

	private int lookupTag(int tag, int set) {
		
		int way = 0;
		while(way<ways) {
			if(isValid(way,set) && getTag(way,set) == tag) {
				break;
			}
			++way;
		}
		return way;
	}
	
	private CacheBlock getOrLoadCacheBlock(int tag, int set, int way) {
		
		CacheBlock cacheBlock;

		/* On miss, load data */
		if(! validWay(way)) {
			cacheBlock = new CacheBlock(blockSize);
			cacheBlock.load(tag, addressOfTag(tag), nextLevelMemory);
		} else {
			cacheBlock = getCacheBlock(way,set);
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

	public int setOfAddress(int addr) {
		return tagOfAddress(addr) % sets;		
	}

	protected boolean validWay(int way) {		
		return way >= 0 && way < ways;
	}

	public boolean isValid(int way, int set) {		
		return cacheData[way][set].isValid();
	}
	
	public int getTag(int way, int set) {
		
		return cacheData[way][set].getTag();
	}
	
	public CacheBlock getCacheBlock(int way, int set) {
		
		return cacheData[way][set];
	}

	// stats + debug
	private Stack<DataCacheStats> recordedStats = new Stack<DataCacheStats>();
	@Override
	public void resetStats() {
		this.stats.reset();
	}
	@Override 
	public void recordStats() {
		recordedStats.push(stats.clone());
	}
	public DataCacheStats getAverageStats() {
		return new DataCacheStats(getName()).addAverage(this.recordedStats);
	}
	
	@Override
	public String getName() {
		return String.format("D$-%d-%d-%d-%s",ways,sets,blockSize,this.replacementStrategy.toString());
	}

	@Override
	public String toString() {
		String handledStr = handledAccessTypes.length == Access.values().length ?
				            "all" :
				            Arrays.toString(handledAccessTypes);
		return String.format("Set Associative Cache{ ways=%d, sets=%d, blocksize=%d, replacement=%s, handled=%s }",
				             this.ways, this.sets, this.blockSize,
				             this.replacementStrategy.toString(), handledStr);
	}

	@Override
	public void dump(PrintStream out) {
		SplitCacheSim.printHeader(out,toString());
		this.getAverageStats().dump(out);
	}

	public static<BlockType> void updateLRU(BlockType accessedElement, int oldBlockPos, BlockType[][] cacheData, int set) {
		/* LRU move A B C X D -> X A B C D*/
		BlockType movedBlock = accessedElement;
		for(int w = 0; w <= oldBlockPos && w < cacheData.length; w++) {
			BlockType activeBlock = cacheData[w][set];
			cacheData[w][set] = movedBlock;
			movedBlock = activeBlock;
		}		
	}

	public static<BlockType> void updateFIFO(BlockType accessedElement, int oldBlockPos, BlockType[][] cacheData, int set) {
		/* FIFO move A B C D -> X A B C */
		if(oldBlockPos >= 0 && oldBlockPos < cacheData.length) {
			BlockType movedBlock = accessedElement;
			for(int w = 0; w < cacheData.length; w++) {
				BlockType activeBlock = cacheData[w][set];
				cacheData[w][set] = movedBlock;
				movedBlock = activeBlock;
			}				
		}
	}

}