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
package com.jopdesign.wcet.uppaal;
import java.io.File;

import com.jopdesign.wcet.config.BooleanOption;
import com.jopdesign.wcet.config.Config;
import com.jopdesign.wcet.config.Option;
import com.jopdesign.wcet.config.StringOption;
import com.jopdesign.wcet.jop.CacheConfig;
import com.jopdesign.wcet.jop.CacheConfig.DynCacheApproximation;

/** 
 * 
 * UppAal configuration
 * @author Benedikt Huber (benedikt.huber@gmail.com)
 *
 */
public class UppAalConfig {
	/* Currently not an option */
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
	// FIXME: Change, as soon as we have implemented safe approx
	public static final BooleanOption UPPAAL_EMPTY_INITIAL_CACHE =
		new BooleanOption("uppaal-empty-initial-cache",
			"assume the cache is initially empty (FIFO) - otherwise use 1/2 cache",
			true);
	public static final BooleanOption UPPAAL_TIGHT_BOUNDS =
		new BooleanOption("uppaal-tight-bounds",
				"assume all loop bounds are tight in simulation", 
				false);
	public static final BooleanOption UPPAAL_SUPERGRAPH_TEMPLATE =
		new BooleanOption("uppaal-supergraph",
				"use one template per process",
				false);
	public static final BooleanOption UPPAAL_PROGRESS_MEASURE =
		new BooleanOption("uppaal-progress-measure", "use a global progress measure", false);
	public static final Option<?>[] uppaalOptions = {
		UPPAAL_VERIFYTA_BINARY, UPPAAL_EMPTY_INITIAL_CACHE,
		UPPAAL_TIGHT_BOUNDS, UPPAAL_COLLAPSE_LEAVES, UPPAAL_CONVEX_HULL,
		UPPAAL_SUPERGRAPH_TEMPLATE, UPPAAL_PROGRESS_MEASURE,
	};

	public boolean isDynamicCacheSim() {
		DynCacheApproximation cs = configData.getOption(CacheConfig.DYNAMIC_CACHE_APPROX);
		return ! (cs.equals(DynCacheApproximation.ALWAYS_MISS));
	}
	public boolean hasVerifier() {
		return verifyBinary != null;
	}
	public String getVerifier() {
		return verifyBinary;
	}
	public File getOutFile(String filename) {
		return new File(outDir,filename);
	}
	private Config configData;
	public boolean collapseLeaves;
	public boolean convexHullApprox;
	public boolean emptyInitialCache;
	public boolean assumeTightBounds;
	public boolean superGraphTemplate;
	public boolean useProgressMeasure;
	public DynCacheApproximation cacheApprox;
	public DynCacheApproximation getCacheApproximation() {
		return this.cacheApprox;
	}
	public final boolean debug = false;
	public final File outDir;

	private String verifyBinary = null;
	public UppAalConfig(Config c, File outDir) {
		this.configData = c;
		this.collapseLeaves = c.getOption(UPPAAL_COLLAPSE_LEAVES);
		this.convexHullApprox = c.getOption(UPPAAL_CONVEX_HULL);
		this.emptyInitialCache = c.getOption(UPPAAL_EMPTY_INITIAL_CACHE);
		this.assumeTightBounds = c.getOption(UPPAAL_TIGHT_BOUNDS);
		this.superGraphTemplate = c.getOption(UPPAAL_SUPERGRAPH_TEMPLATE);
		this.useProgressMeasure = c.getOption(UPPAAL_PROGRESS_MEASURE);
		if(c.hasOption(UPPAAL_VERIFYTA_BINARY)) {
			this.verifyBinary = c.getOption(UPPAAL_VERIFYTA_BINARY);
		}
		this.cacheApprox = c.getOption(CacheConfig.DYNAMIC_CACHE_APPROX);
		this.outDir = outDir;
	}
}
