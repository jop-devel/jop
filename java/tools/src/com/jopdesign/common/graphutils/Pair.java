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

package com.jopdesign.common.graphutils;

import java.io.Serializable;

/**
 * Pairs of objects, following FP terminology (recommended for prototyping only) -
 * it is usually a good idea to subclass <code>Pair</code> to improve the API.
 * As in {@link java.util}, {@link #compareTo} might raise a {@link ClassCastException}
 * if the T1 or T2 doesn't implement {@link Comparable}.
 *
 * @param <T1> type of the first component
 * @param <T2> type of the second component
 *
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 */
public class Pair<T1,T2> implements Comparable<Pair<T1,T2>>, 
						            Serializable
{
	private static final long serialVersionUID = 1L;
	protected T1 fst;
    protected T2 snd;

    public Pair(T1 fst, T2 snd) {
		this.fst = fst;
		this.snd = snd;
	}

    public T1 fst() { return fst; }
	public T2 snd() { return snd; }

	@SuppressWarnings({"unchecked", "AccessingNonPublicFieldOfAnotherObject"})
	public int compareTo(Pair<T1,T2> o) {
		int c1 = ((Comparable<T1>) fst).compareTo(o.fst);
		if(c1 == 0) return ((Comparable<T2>) snd).compareTo(o.snd);
		return c1;
	}

	@Override
	public boolean equals(Object o) {
		if(this == o) return true;
        if (!(o instanceof Pair)) return false;
        if(! fst.equals(((Pair) o).fst())) return false;
        return snd.equals(((Pair) o).snd());
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
