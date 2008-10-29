/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2008, Wolfgang Puffitsch

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.jopdesign.dfa.analyses;

public class Location {
	
	public int stackLoc;
	public String heapLoc;
	
	public Location(int loc) {
		stackLoc = loc;
		heapLoc = "";
	}

	public Location(String loc) {
		stackLoc = -1;
		heapLoc = loc;
	}

	public Location(Location loc) {
		stackLoc = loc.stackLoc;
		heapLoc = loc.heapLoc;
	}
	
	public boolean equals(Object o) {
		Location loc = (Location)o;
		return (stackLoc == loc.stackLoc)
			&& heapLoc.equals(loc.heapLoc);
	}
			
	public int hashCode() {
		return stackLoc+31*heapLoc.hashCode();
	}
	
	public String toString() {
		if (stackLoc >= 0) {
			return "stack["+stackLoc+"]";
		} else {
			return heapLoc;				
		}
	}

}
