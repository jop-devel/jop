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

import com.jopdesign.common.config.BooleanOption;
import com.jopdesign.common.config.Config;
import com.jopdesign.common.config.EnumOption;
import com.jopdesign.common.config.IntegerOption;
import com.jopdesign.common.config.Option;
import com.jopdesign.common.config.StringOption;

import java.io.File;

/**
 * 
 * UppAal configuration
 * @author Benedikt Huber (benedikt.huber@gmail.com)
 *
 */
public class UppAalConfig {

	/**
	 * Dynamic cache approximations
	 * <ul>
	 *   <li/> CACHE_SIM
	 *   <li/> ALWAYS_MISS
	 * </ul>
	 * 
	 */
	public enum UppaalCacheApproximation {
		ALWAYS_MISS,
		CACHE_SIM
	}

	public static final EnumOption<UppaalCacheApproximation> UPPAAL_CACHE_APPROX =
		new EnumOption<UppaalCacheApproximation>(
				"uppaal-cache-approx",
				"dynamic cache approximation (uppaal)", 
				UppaalCacheApproximation.ALWAYS_MISS);

	public static final StringOption UPPAAL_VERIFYTA_BINARY =
		new StringOption("uppaal-verifier",
			"binary of the uppaal model-checker (verifyta)",
			"verifyta");		

	public static final BooleanOption UPPAAL_COLLAPSE_LEAVES =
		new BooleanOption("uppaal-collapse-leaves",
				"collapse leaf methods to speed up simulation", 
				false);
	
	public static final Option<Boolean> UPPAAL_CONVEX_HULL =
		new BooleanOption("uppaal-convex-hull",
			"use UPPAAL's convex hull approximation",
			false);

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
		new BooleanOption("uppaal-progress-measure", 
				          "use a global progress measure", 
				          true);
	
	public static final IntegerOption UPPAAL_COMPLEXITY_TRESHOLD =
		new IntegerOption("uppaal-treshold","limit UPPAAL to methods below the given expanded cyclomatic complexity",true);
	public static final Option<?>[] uppaalOptions = {
		UPPAAL_CACHE_APPROX, UPPAAL_COMPLEXITY_TRESHOLD,
		UPPAAL_VERIFYTA_BINARY, UPPAAL_EMPTY_INITIAL_CACHE,
		UPPAAL_TIGHT_BOUNDS, UPPAAL_COLLAPSE_LEAVES, UPPAAL_CONVEX_HULL,
		UPPAAL_SUPERGRAPH_TEMPLATE, UPPAAL_PROGRESS_MEASURE,
	};

	public boolean isDynamicCacheSim() {
		return ! (cacheApprox.equals(UppaalCacheApproximation.ALWAYS_MISS));
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
	public boolean collapseLeaves;
	public boolean convexHullApprox;
	public boolean emptyInitialCache;
	public boolean assumeTightBounds;
	public boolean superGraphTemplate;
	public boolean useProgressMeasure;
	public UppaalCacheApproximation cacheApprox;
	public UppaalCacheApproximation getCacheApproximation() {
		return this.cacheApprox;
	}
	public final boolean debug = false;
	public final File outDir;

	private String verifyBinary = null;
	private Long complexityTreshold;

	public UppAalConfig(Config c, File outDir) {
		this.collapseLeaves = c.getOption(UPPAAL_COLLAPSE_LEAVES);
		this.convexHullApprox = c.getOption(UPPAAL_CONVEX_HULL);
		this.emptyInitialCache = c.getOption(UPPAAL_EMPTY_INITIAL_CACHE);
		this.assumeTightBounds = c.getOption(UPPAAL_TIGHT_BOUNDS);
		this.superGraphTemplate = c.getOption(UPPAAL_SUPERGRAPH_TEMPLATE);
		this.useProgressMeasure = c.getOption(UPPAAL_PROGRESS_MEASURE);
		if(c.hasOption(UPPAAL_VERIFYTA_BINARY)) {
			this.verifyBinary = c.getOption(UPPAAL_VERIFYTA_BINARY);
		}
		this.cacheApprox = c.getOption(UPPAAL_CACHE_APPROX);
		if(c.hasOption(UPPAAL_COMPLEXITY_TRESHOLD)) {
			this.complexityTreshold = c.getOption(UPPAAL_COMPLEXITY_TRESHOLD);
		} else {
			this.complexityTreshold = null;
		}
		this.outDir = outDir;
	}
	public boolean hasComplexityTreshold() {
		return this.complexityTreshold != null;
	}
	public long getComplexityTreshold() {
		if(this.complexityTreshold == null) return (-1);
		else return this.complexityTreshold;
	}
}
