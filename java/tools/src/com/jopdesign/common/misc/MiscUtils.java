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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

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

    public interface Function1<Arg, Ret> {
        Ret apply(Arg v);
    }

    public interface Function2<Arg1, Arg2, Ret> {
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
     * partially sort the given collection by inserting the elements into buckets,
     * indexed by the given priority function.
     *
     * @param <K>
     * @param <V>
     * @param values
     * @param priority
     * @return
     */
    public static <K, V>
    TreeMap<K, List<V>> partialSort(
            Collection<V> values, Function1<V, K> priority) {

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
    void printMap(PrintStream out, Map<K, V> map, int fill) {
        final int _fill = fill;
        printMap(out, map, new Function2<K, V, String>() {
            public String apply(K v1, V v2) {
                return String.format("%" + _fill + "s ==> %s", v1, v2);
            }
        });
    }

    public static <K, V>
    void printMap(PrintStream out,
                  Map<? extends K, ? extends V> map,
                  Function2<K, V, String> printer) {
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
    public static String toString(Collection entries, int max) {
        StringBuffer sb = new StringBuffer("[");
        int cnt = Math.min(entries.size(), max);
        Iterator it = entries.iterator();
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
}
