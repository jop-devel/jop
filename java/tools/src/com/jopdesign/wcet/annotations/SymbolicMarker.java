/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2010, Benedikt Huber (benedikt@vmars.tuwien.ac.at)

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

package com.jopdesign.wcet.annotations;

/**
 * Purpose:
 * @author Benedikt Huber (benedikt@vmars.tuwien.ac.at)
 *
 */
public class SymbolicMarker {
	public static enum SymbolicMarkerType { METHOD_MARKER, OUTER_LOOP_MARKER };
	private int outerLoopIndex;
	private String methodName;
	private SymbolicMarkerType markerType;

	public static SymbolicMarker outerLoopMarker(int distance) {
	
		SymbolicMarker sm = new SymbolicMarker();
		sm.markerType = SymbolicMarkerType.OUTER_LOOP_MARKER;
		sm.outerLoopIndex = distance;
		return sm;
	}
	
	public static SymbolicMarker methodMarker(String methodName) {
		
		SymbolicMarker sm = new SymbolicMarker();
		sm.markerType = SymbolicMarkerType.METHOD_MARKER;
		sm.methodName = methodName;
		return sm;
	}
	
	public SymbolicMarkerType getMarkerType() {
		return markerType;
	}
	public int getOuterLoopDistance() {
		if(markerType == SymbolicMarkerType.OUTER_LOOP_MARKER) return outerLoopIndex;
		throw new AssertionError("getOuterLoopDistance: Not an outer loop marker");
	}
	public String getMethodName() {
		if(markerType == SymbolicMarkerType.METHOD_MARKER) return methodName;
		throw new AssertionError("getMethodName: Not a method marker");		
	}
}
