/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2011, Benedikt Huber (benedikt@vmars.tuwien.ac.at)

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

package com.jopdesign.wcet.analysis.cache;

import java.util.Set;

import lpsolve.LpSolveException;

import com.jopdesign.common.code.Segment;
import com.jopdesign.common.code.SuperGraph.SuperGraphEdge;
import com.jopdesign.wcet.WCETTool;
import com.jopdesign.wcet.analysis.InvalidFlowFactException;
import com.jopdesign.wcet.ipet.IPETSolver;
import com.jopdesign.wcet.ipet.IPETConfig.CacheCostCalculationMethod;
import com.jopdesign.wcet.jop.CacheModel;
import com.jopdesign.wcet.jop.MethodCache;
import com.jopdesign.wcet.jop.ObjectCache;

/**
 * Purpose: The common base class for all cache analyses
 *
 */
public abstract class CacheAnalysis {

	public static class UnsupportedCacheModelException extends Exception {

		public UnsupportedCacheModelException(String msg) {
			super(msg);
		}
		public UnsupportedCacheModelException(String msg, Throwable cause) {
			super(msg, cause);
		}
	}

	/**
	 * Add cache cost to ipet problem
	 * @param segment
	 * @param ipetSolver
	 * @return
	 * @throws LpSolveException 
	 * @throws InvalidFlowFactException 
	 */
	public abstract Set<SuperGraphEdge> addCacheCost(Segment segment, IPETSolver<SuperGraphEdge> ipetSolver,
			CacheCostCalculationMethod cacheCostCalculation) throws InvalidFlowFactException, LpSolveException;

	/**
	 * Factory method to get the cache analysis
	 * @param cacheModel the model of a cache
	 * @return the cache analysis for the given cache
	 * @throws UnsupportedCacheModelException if there is no analysis for the cache model available
	 */
	public static CacheAnalysis getCacheAnalysisFor(CacheModel cacheModel, WCETTool wcetTool)
			throws UnsupportedCacheModelException {

		if(cacheModel instanceof MethodCache) {
			return new MethodCacheAnalysis(wcetTool);
		} else if(cacheModel instanceof ObjectCache) {
			return new ObjectCacheAnalysis(wcetTool, (ObjectCache) cacheModel);
		} else {
			throw new AssertionError("getCacheAnalysisFor: unknown cache:"+cacheModel);
		}
	}
}
