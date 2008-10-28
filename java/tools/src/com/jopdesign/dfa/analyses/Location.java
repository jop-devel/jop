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
