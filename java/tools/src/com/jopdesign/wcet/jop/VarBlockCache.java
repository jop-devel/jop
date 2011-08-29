/*
 * This file is part of JOP, the Java Optimized Processor
 * see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2010, Benedikt Huber (benedikt.huber@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jopdesign.wcet.jop;

import com.jopdesign.common.config.OptionGroup;
import com.jopdesign.common.processormodel.JOPConfig;
import com.jopdesign.common.processormodel.JOPConfig.CacheImplementation;
import com.jopdesign.timing.MethodCacheTiming;
import com.jopdesign.wcet.WCETTool;

public class VarBlockCache extends MethodCacheImplementation {

	private int blockCount;
	private int blockSize;
	private boolean isLRU;

	public VarBlockCache(WCETTool p, MethodCacheTiming timing, int blockCount, int cacheSizeInWords, boolean isLRU) {

		super(p,timing,cacheSizeInWords);
		this.blockCount = blockCount;
		if(cacheSizeWords % blockCount != 0) {
			throw new AssertionError("Bad cache size / blockCount: " + cacheSizeWords + " / " + blockCount);
		}
		this.blockSize = cacheSizeWords / blockCount;
		this.isLRU = isLRU;
	}

	public static MethodCache fromConfig(WCETTool p, MethodCacheTiming timing, boolean isLRU) {
		OptionGroup o = JOPConfig.getOptions(p.getConfig());
		return new VarBlockCache(p, timing,
				                 o.getOption(JOPConfig.CACHE_BLOCKS).intValue(),
								 o.getOption(JOPConfig.CACHE_SIZE_WORDS).intValue(),
								 isLRU);
	}
	/** Return the number of blocks needed for the a method of size {@code words}.
	 */
	@Override
	public int requiredNumberOfBlocks(int sizeInWords) {
		return ((sizeInWords+blockSize-1) / blockSize);
	}

    @Override
    public boolean allFit(long blocks) {
        return blocks <= blockCount;
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
	
	@Override
	public String toString() {
		if(isLRU()) {
			return "m$-LRU-" + blockCount + "x" + blockSize;
		} else {
			return "m$-" + blockCount + "x" + blockSize;
		}
	}
}
