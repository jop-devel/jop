/*
 * This file is part of JOP, the Java Optimized Processor
 *   see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2008, Benedikt Huber (benedikt.huber@gmail.com)
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

package com.jopdesign.common.misc;

import org.jgrapht.DirectedGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * Misc. Stuff I found useful.
 * <p/>
 * TODO split functionalities?
 *
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 */
public class MiscUtils {

	public interface Query<Arg> {
        boolean query(Arg a);
    }
    
    public interface F1<Arg, Ret> {
        Ret apply(Arg v);
    }

    public static<A,R> F1<A,R> const1(final R c) {
    	return new F1<A, R>() {
			@Override
			public R apply(A v) {
				return c;
			}
		};
    }
    
    public interface F2<Arg1, Arg2, Ret> {
        Ret apply(Arg1 v1, Arg2 v2);
    }

    public static <K, V> void addToSet(Map<K, Set<V>> map, K key, V val) {

        Set<V> set = map.get(key);
        if (set == null) {
            set = new HashSet<V>();
            map.put(key, set);
        }
        set.add(val);
    }

    public static <K, V> void addToList(Map<K, List<V>> map, K key, V val) {

        List<V> set = map.get(key);
        if (set == null) {
            set = new ArrayList<V>();
            map.put(key, set);
        }
        set.add(val);
    }

	/**
	 * Increment the counter for the given key, inserting the default if the key if it is not present
	 * @param counters a dictionary of counters
	 * @param key the key to modify
	 * @param startValue the start value for the counter if the key is not present
	 * @return the new value for the key
	 */
	public static<T> long increment(Map<T, Long> counters, T key, long startValue) {

		return incrementBy(counters, key, 1, startValue);
	}

	/**
	 * Increment the counter by step for the given key, inserting the default if the key if it is not present
	 * @param counters a dictionary of counters
	 * @param key the key to modify
	 * @param step the amount to increment
	 * @param startValue the start value for the counter if the key is not present
	 * @return the new value for the key
	 */
	public static<T> long incrementBy(Map<T, Long> counters, T key, long step, long startValue) {
		
		long val;
		if(! counters.containsKey(key)) {
			val = startValue + step;
		} else {
			val = counters.get(key) + step;
		}
		counters.put(key, val);
		return val;
	}


	public static <T> T[] concat(T val, T[] vals) {
        List<T> v = new ArrayList<T>(vals.length+1);
        v.add(val);
        v.addAll(Arrays.asList(vals));
        return v.toArray(vals);
    }

    public static <T> T[] concat(T[] vals, T val) {
        T[] v = Arrays.copyOf(vals, vals.length+1);
        v[vals.length] = val;
        return v;
    }


    /**
     * partially sort the given collection by inserting the elements into buckets,
     * indexed by the given priority function.
     *
     * @param values
     * @param priority
     * @return
     */
    public static <K, V>
    TreeMap<K, List<V>> partialSort(
            Collection<V> values, F1<V, K> priority) {

        TreeMap<K, List<V>> buckets = new TreeMap<K, List<V>>();
        for (V v : values) {
            K prio = priority.apply(v);
            List<V> bucket = buckets.get(prio);
            if (bucket == null) {
                bucket = new ArrayList<V>();
                buckets.put(prio, bucket);
            }
            bucket.add(v);
        }
        return buckets;
    }

    public static int bytesToWords(int by) {
        return ((by + 3) / 4);
    }

    /**
     * @param strs A collection of objects to join
     * @param sep  A seperator string
     * @return The concatenated sequence of the given strings, interspersed with the seperator.
     */
    public static String joinStrings(Iterable<?> strs, String sep) {
        StringBuilder b = new StringBuilder("");
        Iterator<?> i = strs.iterator();
        if (!i.hasNext()) return "";
        b.append(i.next());
        while (i.hasNext()) {
            b.append(sep);
            b.append(i.next());
        }
        return b.toString();
    }

    public static String joinStrings(Object[] entries, String sep) {
        return joinStrings(Arrays.asList(entries), sep);
    }

    /**
     * Remove problematic characters from a method name
     * Note that fully qualified methods might become non-unique,
     * so use an additional unique identifier if you need unique names.
     *
     * @param str
     * @return
     */
    public static String sanitizeFileName(String str) {
        StringBuffer sanitized = new StringBuffer(str.length());
        for (int i = 0; i < str.length(); i++) {
            if (Character.isLetterOrDigit(str.charAt(i)) || str.charAt(i) == '.') {
                sanitized.append(str.charAt(i));
            } else {
                sanitized.append('_');
            }
        }
        return sanitized.toString();
    }

    /**
     * Escape non-alpha numeric characters
     * q -> qq
     * . -> qd
     * , -> qD
     * / -> qs
     * \ -> qS
     * ' ' -> _
     * _ -> q_
     * ( -> qp
     * ) -> qP
     * x -> q$(chr x)
     *
     * @param s string to encode
     * @return encoded string
     */
    public static String qEncode(String s) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case 'q':
                    sb.append("qq");
                    break;
                case '.':
                    sb.append("qd");
                    break;
                case ',':
                    sb.append("qD");
                    break;
                case '/':
                    sb.append("qs");
                    break;
                case '\\':
                    sb.append("qS");
                    break;
                case ' ':
                    sb.append("_");
                    break;
                case '_':
                    sb.append("q_");
                    break;
                case '(':
                    sb.append("qp");
                    break;
                case ')':
                    sb.append("qP");
                    break;
                default:
                    if (Character.isJavaIdentifierPart(c)) {
                        sb.append(c);
                    } else {
                        sb.append('q');
                        sb.append((int) (c));
                    }
                    break;
            }
        }
        return sb.toString();
    }

    /**
     * Pretty print the given map
     *
     * @param out  out stream
     * @param map  the map to print
     * @param fill minimal length of the key, filled with whitespace
     */
    public static <K, V>
    void printMap(PrintStream out, Map<K, V> map, int fill, int indent) {

    	final String formatString = "%"+((indent>0) ? indent:"")+"s"+"%" + ((fill>0) ? fill:"") + "s ==> %s";
        printMap(out, map, new F2<K, V, String>() {
            public String apply(K v1, V v2) {
                return String.format(formatString, "", v1, v2);
            }
        });
    }

    public static <K, V>
    void printMap(PrintStream out,
                  Map<? extends K, ? extends V> map,
                  F2<K, V, String> printer) {
        for (Entry<? extends K, ? extends V> entry : map.entrySet()) {
            out.println(printer.apply(entry.getKey(), entry.getValue()));
        }
    }

    public static <V, E>
    List<V> topologicalOrder(DirectedGraph<V, E> acyclicGraph) {
        List<V> topoList = new ArrayList<V>();
        if (acyclicGraph.vertexSet().size() > 0) {
            TopologicalOrderIterator<V, E> topo = new TopologicalOrderIterator<V, E>(acyclicGraph);
            while (topo.hasNext()) {
                topoList.add(topo.next());
            }
        }
        return topoList;
    }

    public static <V, E>
    List<V> reverseTopologicalOrder(DirectedGraph<V, E> acyclicGraph) {
        List<V> revTopo = topologicalOrder(acyclicGraph);
        Collections.reverse(revTopo);
        return revTopo;
    }

    /**
     * Inserts spaces in front of a string.
     *
     * @param len the desired total length
     * @param val the string
     * @return the prepadded string
     */
    public static String prepad(String val, int len) {
        StringBuffer sb = new StringBuffer();
        for (int i = len; i > val.length(); i--) {
            sb.append(" ");
        }
        sb.append(val);
        return sb.toString();
    }

    /**
     * Inserts spaces behind a string.
     *
     * @param len the desired total length
     * @param val the string
     * @return the prepadded string
     */
    public static String postpad(String val, int len) {
        StringBuffer sb = new StringBuffer();
        sb.append(val);
        for (int i = len; i > val.length(); i--) {
            sb.append(" ");
        }
        return sb.toString();
    }

    /**
     * Return n repetitions of a string, which is usually a single character.
     *
     * @param val the string
     * @param n   the repetitions
     * @return the repeated string
     */
    public static String repeat(String val, int n) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < n; i++) {
            sb.append(val);
        }
        return sb.toString();
    }

    /**
     * @param entries a collection of things
     * @param max maximum number of entries to print
     * @return a string representation of this collection with up to max entries. 
     */
    public static String toString(Collection<?> entries, int max) {
        StringBuffer sb = new StringBuffer("[");
        int cnt = Math.min(entries.size(), max);
        Iterator<?> it = entries.iterator();
        for (int i = 0; i < cnt; i++) {
            if (i > 0) sb.append(",");
            sb.append(it.next().toString());
        }
        if (cnt < entries.size()) {
            sb.append(",...");
        }
        sb.append("]");
        return sb.toString();
    }

    public static <T> boolean inArray(T[] array, T element) {
        for (T i : array) {
            if (i == null) {
                if (element == null) return true;
            } else if (i.equals(element)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the path of one File relative to another.
     *
     * @param target the target directory
     * @param base the base directory
     * @return target's path relative to the base directory
     */
    public static File getRelativeFile(File target, File base)
    {
        // This method is from
        // http://stackoverflow.com/questions/204784/how-to-construct-a-relative-path-in-java-from-two-absolute-paths-or-urls

        String[] baseComponents;
        String[] targetComponents;
        try {
            baseComponents = base.getCanonicalPath().split(Pattern.quote(File.separator));
            targetComponents = target.getCanonicalPath().split(Pattern.quote(File.separator));
        } catch (IOException e) {
            throw new AppInfoError("Error resolving canonical path", e);
        }

        // skip common components
        int index = 0;
        for (; index < targetComponents.length && index < baseComponents.length; ++index)
        {
            if (!targetComponents[index].equals(baseComponents[index]))
                break;
        }

        StringBuilder result = new StringBuilder();
        if (index != baseComponents.length)
        {
            // backtrack to base directory
            for (int i = index; i < baseComponents.length; ++i)
                result.append("..").append(File.separator);
        }
        for (; index < targetComponents.length; ++index)
            result.append(targetComponents[index]).append(File.separator);
        if (!target.getPath().endsWith("/") && !target.getPath().endsWith("\\"))
        {
            // remove final path separator
            result.delete(result.length() - File.separator.length(), result.length());
        }
        return new File(result.toString());
    }

	public static void serialize(File outFile, Object obj) throws IOException {
	    FileOutputStream fos = new FileOutputStream(outFile);
	    ObjectOutputStream oos = new ObjectOutputStream(fos);
	    oos.writeObject(obj);
	}

	public static Object deSerialize(File inFile) throws IOException, ClassNotFoundException {
		FileInputStream fis = new FileInputStream(inFile);
		ObjectInputStream ois = new ObjectInputStream(fis);
		return ois.readObject();
	}

	/**
	 * Group a list of objects by one of their attributes, updating
	 * an existing map from keys to object lists
     *
	 * @param <T> the type of the objects
	 * @param <A> the type of the object attribute
	 * @param getAttribute function to extract the attribute from objects
	 * @param groupMap an existing map from attributes to all objects with this attribute.
	 *        if null, a new {@code HashMap} will be created.
	 * @param objects
	 * @return the updated map
	 */
	public static <T,A>
	Map<A, List<T>> group(
			F1<T, A> getAttribute,
			Map<A, List<T>> groupMap,
			Iterable<T> objects
			) {
		if(groupMap == null) groupMap = new HashMap<A, List<T>>();
		for(T e : objects) {
			addToList(groupMap, getAttribute.apply(e), e);
		}
		return groupMap;
	}



}
