package com.jopdesign.wcet.graphutils;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

public class MiscUtils {
	public interface Function1<Arg,Ret> {
		public Ret apply(Arg v);
	}
	public interface Function2<Arg1,Arg2,Ret> {
		public Ret apply(Arg1 v1, Arg2 v2);
	}
	
	public static<K,V> void addToSet(Map<K,Set<V>> map,K key, V val) {

		Set<V> set = map.get(key);
		if(set == null) {
			set = new HashSet<V>();
			map.put(key,set);
		}
		set.add(val);
	}

	/** partially sort the given collection by inserting the elements into buckets,
	 *  indexed by the given priority function.
	 * @param <K>
	 * @param <V>
	 * @param values
	 * @param priority
	 * @return
	 */
	public static<K,V> 
	TreeMap<K, Vector<V>> partialSort(
			Collection<V> values, Function1<V, K> priority) {

		TreeMap<K, Vector<V>> buckets = new TreeMap<K, Vector<V>>();
		for(V v : values) {
			K prio = priority.apply(v);
			Vector<V> bucket = buckets.get(prio);
			if(bucket == null) {
				bucket = new Vector<V>();
				buckets.put(prio,bucket);
			}
			bucket.add(v);
		}
		return buckets;
	}

	public static int bytesToWords(int by) {
		return ((by + 3) / 4);
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
