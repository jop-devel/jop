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
	public enum CacheApproximation { ALWAYS_HIT, ALWAYS_MISS, ANALYSE_REACHABLE };
	public final int INVOKE_STATIC_HIDE_LOAD_CYCLES = 37;
	public final int MIN_RETURN_HIDE_LOAD_CYCLES = 9;
	public final int MIN_HIDE_LOAD_CYCLES = 9;
	private Config config;

	public CacheConfig(Config c) { 
		this.config = c; 
	}	
	public static final String CACHE_APPROX = "cache-approx";	
	public static final String CACHE_BLOCKS = "cache-blocks";	
	public static final String BLOCK_SIZE = "cache-block-size";

	public static final Option[] cacheOptions = {
		new Option.EnumOption<CacheApproximation>(CACHE_APPROX,"cache approximation for var block cache", CacheApproximation.ANALYSE_REACHABLE),
		new Option.IntegerOption(CACHE_BLOCKS,"number of cache blocks",16),
		new Option.IntegerOption(BLOCK_SIZE,"size of cache blocks in bytes",256)
	};
	
	public int cacheBlocks() {
		return config.getIntOption(CACHE_BLOCKS,16);
	}
	public int blockSize() {
		return config.getIntOption(BLOCK_SIZE,256);
	}
}
