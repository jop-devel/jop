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
package com.jopdesign.wcet08.uppaal.translator;
import com.jopdesign.wcet08.Config;
import com.jopdesign.wcet08.Option;

/** 
 * 
 * UppAal configuration
 * @author Benedikt Huber (benedikt.huber@gmail.com)
 *
 */
public class UppAalConfig {
	private Config config;
	
	public static final String UPPAAL_ALWAYS_HIT= "uppaal-always-hit-cache";
	public static final String UPPAAL_TIGHT_BOUNDS = "uppaal-tight-bounds";
	public static final Option<?>[] uppaalOptions = {
		new Option.BooleanOption(UPPAAL_ALWAYS_HIT,
				"assume always hit in uppaal simulation", 
				true),
		new Option.BooleanOption(UPPAAL_TIGHT_BOUNDS,
				"assume all loop bounds are tight in simulation", 
				false)

	};
	public UppAalConfig(Config c) {
		this.config = c;
	}
	public boolean alwaysHitCache() {
		return config.getBooleanOption(UPPAAL_ALWAYS_HIT, true);
	}
	public boolean assumeTightBounds() {
		return config.getBooleanOption(UPPAAL_TIGHT_BOUNDS, false);
	}
	
}
