package com.jopdesign.wcet.ipet;

import java.util.Map;

/**
 * Implementors provide cost measure for objects of type T
 * @param <T> The type of object the measure is provided for
 */
public interface CostProvider<T> {
	public long getCost(T obj);

	/**
	 * A map based implementation of the {@link CostProvider} interface
	 * @param <T> The type of object the measure is provided for
	 */
	public static class MapCostProvider<T> implements CostProvider<T> {
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