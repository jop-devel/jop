package com.jopdesign.wcet08.graphutils;

import java.io.Serializable;

/**
 * Pairs of objects, following FP terminology (recommended for prototyping only) -
 * it is usually a good idea to subclass <code>Pair</code> to improve the API.
 * As in {@link java.util}, {@link compareTo} might raise a {@link ClassCastException}
 * if the T1 or T2 doesn't implement {@link Comparable}.
 *
 * @param <T1> type of the first component
 * @param <T2> type of the second component
 *
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 */
public class Pair<T1,T2> implements Comparable<Pair<T1,T2>>, 
						            Serializable  {
	private static final long serialVersionUID = 1L;
	protected T1 fst; 
	public T1 fst() { return fst; }
	protected T2 snd; 
	public T2 snd() { return snd; }
	public Pair(T1 fst, T2 snd) {
		this.fst = fst;
		this.snd = snd;
	}
	@SuppressWarnings("unchecked")
	public int compareTo(Pair<T1,T2> o) {
		int c1 = ((Comparable<T1>) fst).compareTo(o.fst);
		if(c1 == 0) return ((Comparable<T2>) snd).compareTo(o.snd);
		return c1;
	}
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object o) {
		if(this == o) return true;
		try {
			if(! fst.equals(((Pair) o).fst())) return false;
			return snd.equals(((Pair) o).snd());
		} catch(ClassCastException ex) { 
			return false; 
		}
	}
	@Override
	public int hashCode() {
		return fst.hashCode() + snd.hashCode();		
	}
	@Override
	public String toString() {
		return "("+fst+", "+snd+")";
	}
}
