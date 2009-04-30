package com.jopdesign.wcet.graphutils;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import java.util.Map.Entry;

import org.jgrapht.DirectedGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;

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

	/** 
	 * Remove problematic characters from a method name 
	 * Note that fully qualified methods might become non-unique,
	 * so use an additional unique identifier if you need unique names. */
	public static String sanitizeFileName(String str) {
		StringBuffer sanitized = new StringBuffer(str.length());
		for(int i = 0; i < str.length(); i++) {
			if(Character.isLetterOrDigit(str.charAt(i)) || str.charAt(i) == '.') {
				sanitized.append(str.charAt(i));
			} else {
				sanitized.append('_');
			}
		}
		return sanitized.toString();
	}
	
	/** Escape non-alpha numeric characters
	 *  q -> qq
	 *  . -> qd
	 *  , -> qD
	 *  / -> qs
	 *  \ -> qS
	 *  ' ' -> _
	 *  _ -> q_
	 *  ( -> qp
	 *  ) -> qP
	 *  x -> q$(chr x)
	 */
	public static String qEncode(String s) {
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			switch(c) {
			case 'q' : sb.append("qq");break;
			case '.' : sb.append("qd");break;
			case ',' : sb.append("qD");break;
			case '/' : sb.append("qs");break;
			case '\\' : sb.append("qS");break;
			case ' ' : sb.append("_"); break;
			case '_' : sb.append("q_");break;
			case '(' : sb.append("qp");break;
			case ')' : sb.append("qP");break;
			default:
				if(Character.isJavaIdentifierPart(c)) {
					sb.append(c);
				} else {
					sb.append('q');
					sb.append((int)(c));
				}
				break;
			}
		}
		return sb.toString();
	}

	public static void printMap(PrintStream out,Map<?,?> map, int fill) {
		for(Entry<?, ?> entry : map.entrySet()) {
			StringBuilder sb = new StringBuilder();
			String s1 = entry.getKey().toString();
			sb.append(s1);
			for(int i = s1.length(); i < fill; i++) sb.append(' ');
			sb.append(" ==> ");
			sb.append(entry.getValue().toString());
			out.println(sb.toString());
		}
	}

	public static <V,E>
	List<V> topologicalOrder(DirectedGraph<V,E> acyclicGraph) 
	{
		TopologicalOrderIterator<V,E> topo = new TopologicalOrderIterator<V,E>(acyclicGraph);
		List<V> topoList = new ArrayList<V>();
		while(topo.hasNext()) {
			topoList.add(topo.next());
		}
		return topoList;
	}
	public static <V,E>
	List<V> reverseTopologicalOrder(DirectedGraph<V,E> acyclicGraph) 
	{
		List<V> revTopo = topologicalOrder(acyclicGraph);
		Collections.reverse(revTopo);
		return revTopo;
	}
	public static final int BYTES_PER_WORD = 4;


}
