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
package com.jopdesign.wcet08.uppaal;
import com.jopdesign.wcet08.Config;
import com.jopdesign.wcet08.Option;
import com.jopdesign.wcet08.Option.BooleanOption;
import com.jopdesign.wcet08.Option.EnumOption;
import com.jopdesign.wcet08.Option.IntegerOption;
import com.jopdesign.wcet08.Option.StringOption;

/** 
 * 
 * UppAal configuration
 * @author Benedikt Huber (benedikt.huber@gmail.com)
 *
 */
public class UppAalConfig {
	public enum CacheSim { ALWAYS_HIT, ALWAYS_MISS, LRU_BLOCK, FIFO_BLOCK, VARIABLE_BLOCK };
	
	public static final EnumOption<CacheSim> UPPAAL_CACHE_SIM =
		new Option.EnumOption<CacheSim>("uppaal-cache-sim",
				"which cache simulation to use in UppAal", 
				CacheSim.ALWAYS_HIT);
	public static final IntegerOption UPPAAL_CACHE_BLOCKS =
		new Option.IntegerOption("uppaal-cache-blocks",
				"number of cache blocks for UppAal cache simulation", 
				2);
	public static final IntegerOption UPPAAL_CACHE_BLOCK_WORDS =
		new Option.IntegerOption("uppaal-cache-block-words",
				"size of cache blocks (in words) for UppAal cache simulation", 
				64);
	/* Currently not an option */
	public static final BooleanOption UPPAAL_EMPTY_INITIAL_CACHE =
		new Option.BooleanOption("uppaal-empty-initial-cache",
			"assume the cache is initially empty (FIFO)",
			true);
	public static final BooleanOption UPPAAL_ONE_CHANNEL_PER_METHOD = 
		new Option.BooleanOption("uppaal-one-chan-per-method",
				"use one sync channel per method",
				true);
	public static final BooleanOption UPPAAL_TIGHT_BOUNDS =
		new Option.BooleanOption("uppaal-tight-bounds",
				"assume all loop bounds are tight in simulation", 
				false);
	public static final BooleanOption UPPAAL_COLLAPSE_LEAVES =
		new Option.BooleanOption("uppaal-collapse-leaves",
				"collapse leaf methods to speed up simulation", 
				false);
	public static final StringOption UPPAAL_VERIFYTA_BINARY =
		new Option.StringOption("uppaal-verifier",
			"binary of the uppaal model-checker (verifyta)",
			true);		

	public static final Option<?>[] uppaalOptions = {
		UPPAAL_CACHE_SIM, UPPAAL_CACHE_BLOCKS,
		UPPAAL_TIGHT_BOUNDS, UPPAAL_COLLAPSE_LEAVES, 
		UPPAAL_ONE_CHANNEL_PER_METHOD, UPPAAL_VERIFYTA_BINARY 
	};

	public static boolean isDynamicCacheSim() {
		CacheSim cs = Config.instance().getOption(UPPAAL_CACHE_SIM);
		return ! (cs.equals(CacheSim.ALWAYS_HIT) || cs.equals(CacheSim.ALWAYS_MISS));
	}
	public static boolean hasVerifier() {
		return (Config.instance().hasOption(UPPAAL_VERIFYTA_BINARY));
	}
}
