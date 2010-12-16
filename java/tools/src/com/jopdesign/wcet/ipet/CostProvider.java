/*
 * This file is part of JOP, the Java Optimized Processor
 * see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2010, Benedikt Huber (benedikt.huber@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jopdesign.wcet.ipet;

import java.util.Map;

/**
 * Implementors provide cost measure for objects of type T
 * @param <T> The type of object the measure is provided for
 */
public interface CostProvider<T> {
	long getCost(T obj);

	/**
	 * A map based implementation of the {@link CostProvider} interface
	 * @param <T> The type of object the measure is provided for
	 */
    class MapCostProvider<T> implements CostProvider<T> {
		private Map<T, Long> costMap;
		private long defCost;
		public MapCostProvider(Map<T,Long> costMap, long defCost) {
			this.costMap = costMap;
			this.defCost = defCost;
		}
		public long getCost(T obj) {
			Long cost = costMap.get(obj);
			if(cost == null) return defCost;
			else             return cost;
		}		
   }
}