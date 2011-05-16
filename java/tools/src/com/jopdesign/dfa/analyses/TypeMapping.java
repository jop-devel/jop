/*
 * This file is part of JOP, the Java Optimized Processor
 * see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2010, Wolfgang Puffitsch
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
package com.jopdesign.dfa.analyses;

public class TypeMapping {
    public final int stackLoc;
    public final String heapLoc;
    public final String type;
    public final int hash;

    public TypeMapping(int l, String t) {
        stackLoc = l;
        heapLoc = "";
        type = t;
        hash = stackLoc + heapLoc.hashCode() + type.hashCode();
    }

    public TypeMapping(String l, String t) {
        stackLoc = -1;
        heapLoc = l;
        type = t;
        hash = stackLoc + heapLoc.hashCode() + type.hashCode();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TypeMapping)) return false;

        TypeMapping m = (TypeMapping) o;

        return (stackLoc == m.stackLoc)
                && heapLoc.equals(m.heapLoc)
                && type.equals(m.type);
    }

    public int hashCode() {
        return hash;
    }

    public String toString() {
        if (stackLoc >= 0) {
            return "<stack[" + stackLoc + "], " + type + ">";
        } else {
            return "<" + heapLoc + ", " + type + ">";
        }
    }
}