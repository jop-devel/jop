package com.jopdesign.wcet.jop;

import com.jopdesign.build.MethodInfo;
import com.jopdesign.wcet.Project;
import com.jopdesign.wcet.config.Config;
import com.jopdesign.wcet.jop.CacheConfig.CacheImplementation;

public class VarBlockCache extends MethodCache {

	private int blockCount;
	private int blockSize;
	private boolean isLRU;

	public VarBlockCache(Project p, int blockCount, int cacheSizeInWords, boolean isLRU) {
		super(p,cacheSizeInWords);
		this.blockCount = blockCount;
		if(cacheSizeWords % blockCount != 0) {
			throw new AssertionError("Bad cache size / blockCount: "+
		                             cacheSizeWords+" / "+blockCount);
		}
		this.blockSize = cacheSizeWords / blockCount;
		this.isLRU = isLRU;
	}

	public static MethodCache fromConfig(Project p, boolean isLRU) {
		Config c = p.getConfig();
		return new VarBlockCache(p,
				                 c.getOption(CacheConfig.CACHE_BLOCKS).intValue(),
								 c.getOption(CacheConfig.CACHE_SIZE_WORDS).intValue(),
								 isLRU);
	}
	/** Return the number of blocks needed for the a method of size {@code words}.
	 */
	@Override
	public int requiredNumberOfBlocks(int sizeInWords) {
		return ((sizeInWords+blockSize-1) / blockSize);
	}

	@Override
	public boolean allFit(MethodInfo m) {
		return super.getAllFitCacheBlocks(m) <= this.blockCount;
	}

	@Override
	public boolean fitsInCache(int sizeInWords) {
		return (requiredNumberOfBlocks(sizeInWords) <= this.blockCount);
	}
	
	@Override
	public boolean isLRU() {
		return this.isLRU;
	}

	@Override
	public CacheImplementation getName() {
		if(isLRU()) return CacheImplementation.LRU_VARBLOCK_CACHE;
		else        return CacheImplementation.FIFO_VARBLOCK_CACHE;
	}

	public int getNumBlocks() {
		return this.blockCount;
	}
}
