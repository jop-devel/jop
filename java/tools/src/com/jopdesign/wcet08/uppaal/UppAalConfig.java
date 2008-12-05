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
import java.io.File;

import com.jopdesign.wcet08.Config;
import com.jopdesign.wcet08.Option;
import com.jopdesign.wcet08.Option.BooleanOption;
import com.jopdesign.wcet08.Option.EnumOption;
import com.jopdesign.wcet08.Option.StringOption;

/** 
 * 
 * UppAal configuration
 * @author Benedikt Huber (benedikt.huber@gmail.com)
 *
 */
public class UppAalConfig {
	private Config config;
	public enum CacheSim { ALWAYS_HIT, ALWAYS_MISS, TWO_BLOCK };
	
	public static final EnumOption<CacheSim> UPPAAL_CACHE_SIM =
		new Option.EnumOption<CacheSim>("uppaal-cache-sim",
				"which cache simulation to use in UppAal", 
				CacheSim.ALWAYS_HIT);
	public static final BooleanOption UPPAAL_ONE_CHANNEL_PER_METHOD = 
		new Option.BooleanOption("uppaal-one-chan-per-method",
				"use one sync channel per method",
				true);
	public static final BooleanOption UPPAAL_TIGHT_BOUNDS =
		new Option.BooleanOption("uppaal-tight-bounds",
				"assume all loop bounds are tight in simulation", 
				false);
	public static final StringOption UPPAAL_VERIFYTA_BINARY =
		new Option.StringOption("uppaal-verifier",
			"binary of the uppaal model-checker (verifyta)",
			true);		

	public static final Option<?>[] uppaalOptions = {
		UPPAAL_CACHE_SIM, UPPAAL_TIGHT_BOUNDS, UPPAAL_TIGHT_BOUNDS, UPPAAL_VERIFYTA_BINARY 
	};
	public UppAalConfig(Config c) {
		this.config = c;
	}
	public boolean assumeTightBounds() {
		return config.getBooleanOption(UPPAAL_TIGHT_BOUNDS);
	}
	public boolean useOneChannelPerMethod() {
		return config.getBooleanOption(UPPAAL_ONE_CHANNEL_PER_METHOD);
	}
	public File getUppaalBinary() {
		File f = config.getFileOption(UPPAAL_VERIFYTA_BINARY);
		if(! f.exists() || ! f.isFile()) {
			throw new AssertionError("UppAal binary does not exist: "+f);
		}
		return f;
	}
	public CacheSim getCacheSim() {
		return config.getEnumOption(UPPAAL_CACHE_SIM);
	}
	public boolean isDynamicCacheSim() {
		return this.getCacheSim().equals(CacheSim.TWO_BLOCK);
	}
	public static boolean hasVerifier() {
		return (Config.instance().getOptions().containsKey(UPPAAL_VERIFYTA_BINARY));
	}
}
