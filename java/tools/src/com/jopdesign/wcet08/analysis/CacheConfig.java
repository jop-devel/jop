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

import com.jopdesign.wcet08.Option;
import com.jopdesign.wcet08.Option.EnumOption;
import com.jopdesign.wcet08.Option.IntegerOption;

public class CacheConfig {
	/**
	 * Supported cache approximations: Assume all method cache accesses are miss 
	 * (<code>ALWAYS_MISS</code>), analyse the set of reachable methods
	 * (<code>ANALYSE_REACHABLE</code) or
	 * assume (unsafe !) all are hit (<code>ALWAYS_HIT</code>),
	 *
	 */
	public static final EnumOption<CacheApproximation> CACHE_APPROX =
		new EnumOption<CacheApproximation>(
				"cache-approx",
				"cache approximation for var block cache", 
				CacheApproximation.ANALYSE_REACHABLE);

	public static final IntegerOption CACHE_BLOCKS =
		new IntegerOption("cache-blocks","number of cache blocks",16);

	public static final IntegerOption BLOCK_SIZE_WORDS =
		new Option.IntegerOption("cache-block-size-words",
								 "size of cache blocks in bytes",
								 64);
		
	public static final Option<?>[] cacheOptions = {
		CACHE_APPROX, CACHE_BLOCKS, BLOCK_SIZE_WORDS
	};

	public enum CacheApproximation { ALWAYS_HIT, ALWAYS_MISS, ANALYSE_REACHABLE};
}
