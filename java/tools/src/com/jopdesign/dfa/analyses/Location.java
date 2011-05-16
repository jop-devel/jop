/*
 * This file is part of JOP, the Java Optimized Processor
 * see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2008, Wolfgang Puffitsch
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

import java.io.Serializable;

public class Location implements Serializable {

	private static final long serialVersionUID = 1L;

	public final int stackLoc;
    public final String heapLoc;
    private final int hash;

    public Location(int loc) {
        if (loc < 0) throw new AssertionError("Invalid stack ptr: " + loc);
        stackLoc = loc;
        heapLoc = "";
        hash = stackLoc + 31 * heapLoc.hashCode();
    }

    public Location(String loc) {
        stackLoc = -1;
        heapLoc = loc;
        hash = stackLoc + 31 * heapLoc.hashCode();
    }

    @SuppressWarnings({"AccessingNonPublicFieldOfAnotherObject"})
    public Location(Location loc) {
        stackLoc = loc.stackLoc;
        heapLoc = loc.heapLoc;
        hash = loc.hash;
    }

    public boolean equals(Object o) {
        if (!(o instanceof Location)) return false;
        Location loc = (Location) o;
        return (stackLoc == loc.stackLoc)
                && heapLoc.equals(loc.heapLoc);
    }

    public int hashCode() {
        return hash;
    }

    public String toString() {
        if (!isHeapLoc()) {
            return "stack[" + stackLoc + "]";
        } else {
            return heapLoc;
        }
    }

    public boolean isHeapLoc() {
        return stackLoc < 0;
    }

}
