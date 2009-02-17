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
package com.jopdesign.wcet08.jop;

import com.jopdesign.wcet08.config.BooleanOption;
import com.jopdesign.wcet08.config.EnumOption;
import com.jopdesign.wcet08.config.IntegerOption;
import com.jopdesign.wcet08.config.Option;

public class CacheConfig {
	public static final int BYTES_PER_WORD = 4;
	/**
	 * Supported method cache implementations:
	 * <ul>
	 *  <li/> LRU_CACHE: N - Block LRU cache
	 *  <li/> FIFO_VARBLOCK_CACHE: Variable Block cache
	 *  <li/> NO_METHOD_CACHE: Assume there are no method cache misses
	 * </ul>
	 */
	public enum CacheImplementation {
		LRU_CACHE, FIFO_CACHE,
		FIFO_VARBLOCK_CACHE,
		NO_METHOD_CACHE, 
	}
	public static final EnumOption<CacheImplementation> CACHE_IMPL =
		new EnumOption<CacheImplementation>(
				"cache-impl",
				"method cache implementation",
				CacheImplementation.FIFO_VARBLOCK_CACHE);
	public static final IntegerOption CACHE_BLOCKS =
		new IntegerOption("cache-blocks","number of cache blocks",16);

	public static final IntegerOption CACHE_SIZE_WORDS =
		new IntegerOption("cache-size-words",
						  "size of the cache in words",
						  256);
	
   /** Static cache approximations:
	 * <ul>
	 *   <li/> ALL_FIT_MISS_ONCE
	 *     <ul>
	 *     <li/> FIFO_VARBLOCK_CACHE: If all fit, assume miss (at most) once on return
	 *     <li/> LRU_CACHE: If all fit, assume miss (at most) once on invoke
	 *     <li/> otherwise: Not applicable
	 *     </ul>
	 *   <li/> ALWAYS_MISS (all acceses are cache misses)
	 *   <li/> ALWAYS_HIT (all accesses are hits)
	 * </ul>
	 */
	public enum StaticCacheApproximation { 
		ALWAYS_HIT, ALWAYS_MISS,
		ALL_FIT_LOCAL, ALL_FIT
    };
	public static final EnumOption<StaticCacheApproximation> STATIC_CACHE_APPROX =
		new EnumOption<StaticCacheApproximation>(
				"cache-approx",
				"static cache approximation", 
				StaticCacheApproximation.ALL_FIT);
	public static final BooleanOption ASSUME_MISS_ONCE_ON_INVOKE =
		new BooleanOption("assume-miss-once-on-invoke",
						  "assume method cache loads in miss-once areas always happen on invoke (unsafe)",
						  false);
   /**
	 * Dynamic cache approximations
	 * <ul>
	 *   <li/> CACHE_SIM
	 *   <li/> ALWAYS_MISS
	 *   <li/> ALWAYS_HIT
	 * </ul>
	 * 
	 */
	public enum DynCacheApproximation {
		ALWAYS_HIT, ALWAYS_MISS,
		CACHE_SIM
	}
	public static final EnumOption<DynCacheApproximation> DYNAMIC_CACHE_APPROX =
		new EnumOption<DynCacheApproximation>(
				"dyn-cache-approx",
				"dynamic cache approximation (uppaal)", 
				DynCacheApproximation.CACHE_SIM);
		
	public static final Option<?>[] cacheOptions = {
		CACHE_IMPL, CACHE_BLOCKS, CACHE_SIZE_WORDS,
		STATIC_CACHE_APPROX, ASSUME_MISS_ONCE_ON_INVOKE,
		DYNAMIC_CACHE_APPROX
	};
}
