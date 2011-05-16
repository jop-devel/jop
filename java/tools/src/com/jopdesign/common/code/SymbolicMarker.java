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

package com.jopdesign.common.code;

/**
 * Purpose:
 *
 * @author Benedikt Huber (benedikt@vmars.tuwien.ac.at)
 */
public class SymbolicMarker {

    public enum SymbolicMarkerType {
        METHOD_MARKER, OUTER_LOOP_MARKER
    }

    public static final SymbolicMarker LOOP_ENTRY = SymbolicMarker.outerLoopMarker(0);
    public static final SymbolicMarker METHOD_ENTRY = SymbolicMarker.methodMarker(null);
    
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
        if (markerType == SymbolicMarkerType.OUTER_LOOP_MARKER) return outerLoopIndex;
        throw new AssertionError("getOuterLoopDistance: Not an outer loop marker");
    }

    public String getMethodName() {
        if (markerType == SymbolicMarkerType.METHOD_MARKER) return methodName;
        throw new AssertionError("getMethodName: Not a method marker");
    }
	public boolean inSameMethod() {
		return this.markerType == SymbolicMarkerType.OUTER_LOOP_MARKER ||
		       methodName == null;
	}

    @Override
    public String toString() {
    	if (markerType == SymbolicMarkerType.OUTER_LOOP_MARKER) {
    		if(outerLoopIndex == 0) return "LOOP-ENTRY";
    		else return "OUTER-LOOP-"+outerLoopIndex+"-ENTRY";
    	} else {
    		return "METHOD"+(methodName != null ? "-"+methodName : "")+"-ENTRY";
    	}
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * markerType.hashCode();
        switch (markerType) {
            case METHOD_MARKER:
                result = prime * result + methodName.hashCode();
                break;
            case OUTER_LOOP_MARKER:
                result = prime * result + outerLoopIndex;
                break;
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SymbolicMarker other = (SymbolicMarker) obj;
        if (!this.markerType.equals(other.markerType)) return false;
        switch (markerType) {
            case METHOD_MARKER:
                return this.methodName.equals(other.methodName);
            case OUTER_LOOP_MARKER:
                return this.outerLoopIndex == other.outerLoopIndex;
        }
        throw new AssertionError("equals: Inconsistent representation of SymbolicMarker");
    }


}
