/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2008, Benedikt Huber (benedikt.huber@gmail.com)

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.jopdesign.wcet08.analysis;

import com.jopdesign.wcet08.Config;
import com.jopdesign.wcet08.Option;

public class CacheConfig {
	/**
	 * Supported cache approximations: Assume all method cache accesses are miss 
	 * (<code>ALWAYS_MISS</code>), analyse the set of reachable methods
	 * (<code>ANALYSE_REACHABLE</code) or
	 * assume (unsafe !) all are hit (<code>ALWAYS_HIT</code>),
	 *
	 */
	public enum CacheApproximation { ALWAYS_HIT, ALWAYS_MISS, ANALYSE_REACHABLE};
	private Config config;

	public CacheConfig(Config c) { 
		this.config = c; 
	}	
	public static final String CACHE_APPROX = "cache-approx";	
	public static final String CACHE_BLOCKS = "cache-blocks";
	private static final int DEFAULT_NUM_CACHE_BLOCKS = 16;
	public static final String BLOCK_SIZE_WORDS = "cache-block-size-words";
	private static final int DEFAULT_BLOCK_WORDS = 64;

	public static final Option[] cacheOptions = {
		new Option.EnumOption<CacheApproximation>(CACHE_APPROX,"cache approximation for var block cache", CacheApproximation.ANALYSE_REACHABLE),
		new Option.IntegerOption(CACHE_BLOCKS,"number of cache blocks",DEFAULT_NUM_CACHE_BLOCKS),
		new Option.IntegerOption(BLOCK_SIZE_WORDS,"size of cache blocks in bytes",DEFAULT_BLOCK_WORDS)
	};
	
	public int numCacheBlocks() {
		return config.getIntOption(CACHE_BLOCKS,DEFAULT_NUM_CACHE_BLOCKS);
	}
	public int blockSizeInWords() {
		return config.getIntOption(BLOCK_SIZE_WORDS,DEFAULT_BLOCK_WORDS);
	}
}
