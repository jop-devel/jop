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
 * @author Benedikt Huber (benedikt.huber@gmail.com)
 * @param <T1> type of the first component
 * @param <T2> type of the second component
 */
public class Pair<T1, T2> implements Comparable<Pair<T1, T2>>,
        Serializable
{
    private static final long serialVersionUID = 1L;

    protected T1 first;
    protected T2 second;

    public Pair() {
    }

    public Pair(T1 first, T2 second) {
        this.first = first;
        this.second = second;
    }

    public T1 first() {
        return first;
    }

    public T2 second() {
        return second;
    }

    public void setFirst(T1 first) {
        this.first = first;
    }

    public void setSecond(T2 second) {
        this.second = second;
    }

    @SuppressWarnings({"unchecked", "AccessingNonPublicFieldOfAnotherObject"})
    public int compareTo(Pair<T1, T2> o) {
        int c1 = ((Comparable<T1>) first).compareTo(o.first);
        if (c1 == 0) return ((Comparable<T2>) second).compareTo(o.second);
        return c1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pair)) return false;
        if (!first.equals(((Pair) o).first())) return false;
        return second.equals(((Pair) o).second());
    }

    @Override
    public int hashCode() {
        return first.hashCode() + second.hashCode();
    }

    @Override
    public String toString() {
        return "<" + first + ", " + second + ">";
    }
}
