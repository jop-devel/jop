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
import com.jopdesign.wcet08.config.BooleanOption;
import com.jopdesign.wcet08.config.Config;
import com.jopdesign.wcet08.config.EnumOption;
import com.jopdesign.wcet08.config.IntegerOption;
import com.jopdesign.wcet08.config.Option;
import com.jopdesign.wcet08.config.StringOption;
import com.jopdesign.wcet08.jop.CacheConfig;
import com.jopdesign.wcet08.jop.CacheConfig.DynCacheApproximation;

/** 
 * 
 * UppAal configuration
 * @author Benedikt Huber (benedikt.huber@gmail.com)
 *
 */
public class UppAalConfig {
	/* Currently not an option */
	public static final BooleanOption UPPAAL_EMPTY_INITIAL_CACHE =
		new BooleanOption("uppaal-empty-initial-cache",
			"assume the cache is initially empty (FIFO)",
			true);
	public static final BooleanOption UPPAAL_ONE_CHANNEL_PER_METHOD = 
		new BooleanOption("uppaal-one-chan-per-method",
				"use one sync channel per method",
				true);
	public static final BooleanOption UPPAAL_TIGHT_BOUNDS =
		new BooleanOption("uppaal-tight-bounds",
				"assume all loop bounds are tight in simulation", 
				false);
	public static final BooleanOption UPPAAL_COLLAPSE_LEAVES =
		new BooleanOption("uppaal-collapse-leaves",
				"collapse leaf methods to speed up simulation", 
				false);
	public static final StringOption UPPAAL_VERIFYTA_BINARY =
		new StringOption("uppaal-verifier",
			"binary of the uppaal model-checker (verifyta)",
			true);		
	public static final Option<Boolean> UPPAAL_CONVEX_HULL = 
		new BooleanOption("uppaal-convex-hull",
			"use UPPAAL's convex hull approximation",
			false);

	public static final Option<?>[] uppaalOptions = {
		UPPAAL_VERIFYTA_BINARY,
		UPPAAL_TIGHT_BOUNDS, UPPAAL_COLLAPSE_LEAVES, 
		UPPAAL_ONE_CHANNEL_PER_METHOD, UPPAAL_CONVEX_HULL
	};

	public static boolean isDynamicCacheSim(Config c) {
		DynCacheApproximation cs = c.getOption(CacheConfig.DYNAMIC_CACHE_APPROX);
		return ! (cs.equals(DynCacheApproximation.ALWAYS_HIT) ||
				  cs.equals(DynCacheApproximation.ALWAYS_MISS));
	}
	public static boolean hasVerifier(Config c) {
		return (c.hasOption(UPPAAL_VERIFYTA_BINARY));
	}
}
