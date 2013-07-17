package com.jopdesign.wcet.jop;

import com.jopdesign.common.config.Config;
import com.jopdesign.common.processormodel.JOPConfig;
import com.jopdesign.common.processormodel.JOPConfig.CacheImplementation;
import com.jopdesign.timing.MethodCacheTiming;
import com.jopdesign.wcet.WCETTool;

public class BlockCache extends MethodCacheImplementation {

	private boolean isLRU;
	private int blockCount;
	private int blockSize;

	public BlockCache(WCETTool p, MethodCacheTiming timing, boolean isLRU, int cacheSizeWords, int blockCount) {
		super(p,timing,cacheSizeWords);
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
	public static MethodCache fromConfig(WCETTool p, MethodCacheTiming timing, boolean isLRU) {
		Config c = p.getConfig();
		return new BlockCache(p,timing,isLRU,
							  c.getOption(JOPConfig.CACHE_SIZE_WORDS).intValue(),
				              c.getOption(JOPConfig.CACHE_BLOCKS).intValue());
	}

    @Override
    public boolean allFit(long blocks) {
        return blocks <= this.blockCount;
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

	@Override
	public int getNumBlocks() {
		return this.blockCount;
	}

}
