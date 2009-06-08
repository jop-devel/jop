package com.jopdesign.wcet.jop;

import com.jopdesign.build.MethodInfo;
import com.jopdesign.wcet.Project;
import com.jopdesign.wcet.config.Config;
import com.jopdesign.wcet.jop.JOPConfig.CacheImplementation;

public class BlockCache extends MethodCache {

	private boolean isLRU;
	private int blockCount;
	private int blockSize;

	public BlockCache(Project p, boolean isLRU, int cacheSizeWords, int blockCount) {
		super(p,cacheSizeWords);
		this.isLRU = isLRU;
		this.blockCount = blockCount;
		if(cacheSizeWords % blockCount != 0) {
			throw new AssertionError("Bad Cache Size / Block Count: "+cacheSizeWords+" / "+blockCount);
		}
		this.blockSize = cacheSizeWords / blockCount;
	}
	public int getBlockSize() {
		return blockSize;
	}
	public static MethodCache fromConfig(Project p, boolean isLRU) {
		Config c = p.getConfig();
		return new BlockCache(p,isLRU,
							  c.getOption(JOPConfig.CACHE_SIZE_WORDS).intValue(),
				              c.getOption(JOPConfig.CACHE_BLOCKS).intValue());
	}

	@Override
	public boolean allFit(MethodInfo m) {
		return super.getAllFitCacheBlocks(m) <= this.blockCount;
	}

	@Override
	public boolean isLRU() {
		return this.isLRU;
	}
	@Override
	public boolean fitsInCache(int sizeInWords) {
		return sizeInWords <= this.blockSize;
	}
	@Override
	public int requiredNumberOfBlocks(int sizeInWords) {
		if(! fitsInCache(sizeInWords)) throw new AssertionError("Method to large: "+sizeInWords);
		return 1;
	}
	@Override
	public CacheImplementation getName() {
		if(this.isLRU) return CacheImplementation.LRU_CACHE;
		else return CacheImplementation.FIFO_CACHE;
	}
	public int getNumBlocks() {
		return this.blockCount;
	}

}
