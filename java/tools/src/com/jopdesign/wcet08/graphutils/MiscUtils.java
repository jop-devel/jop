package com.jopdesign.wcet08.graphutils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

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
	/**
	 * 
	 * @param strs A collection of objects to join
	 * @param sep A seperator string
	 * @return The concatenated sequence of the given strings, interspersed with the seperator.
	 */
	public static String joinStrings(Iterable<? extends Object> strs, String sep) {
		StringBuilder b = new StringBuilder("");
		Iterator<? extends Object> i = strs.iterator();
		if(! i.hasNext()) return "";
		b.append(i.next());
		while(i.hasNext()) { b.append(sep);b.append(i.next()); }
		return b.toString();
	}

	public static String joinStrings(Object[] entries, String sep) {
		return joinStrings(Arrays.asList(entries), sep);
	}

}
