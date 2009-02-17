package com.jopdesign.wcet08.graphutils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MiscUtils {
	public static<K,V> void addToSet(Map<K,Set<V>> map,K key, V val) {
		Set<V> set = map.get(key);
		if(set == null) {
			set = new HashSet<V>();
			map.put(key,set);
		}
		set.add(val);
	}

	public static int bytesToWords(int by) {
		return (by + 3) / 4;
	}

}
