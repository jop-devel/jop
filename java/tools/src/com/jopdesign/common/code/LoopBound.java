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

import com.jopdesign.common.code.SymbolicMarker.SymbolicMarkerType;
import com.jopdesign.wcet.annotations.LoopBoundExpr;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Purpose: Instances represent execution frequency bounds for loops.
 * Simple loop bounds are relative to the execution frequency
 * of the entry edges of the loop (marker {@code outer(0)}); more
 * elaborate bounds are relative outer loop or method.
 *
 * @author Benedikt Huber (benedikt@vmars.tuwien.ac.at)
 */
public class LoopBound {
    private static final long serialVersionUID = 1L;

    /* Invariant: There is always an entry for the marker LOOP_ENTRY */
    private Map<SymbolicMarker, LoopBoundExpr> markerBounds;

    private LoopBound(Map<SymbolicMarker, LoopBoundExpr> other) {
        markerBounds = new HashMap<SymbolicMarker, LoopBoundExpr>(other);
    }

    public static LoopBound boundedAbove(long ub) {
    	return new LoopBound(LoopBoundExpr.numUpperBound(ub));
    }
    
	public static LoopBound simpleBound(LoopBoundExpr bound) {
		if(bound == null) throw new AssertionError("loop bound expr: null");
		return new LoopBound(bound);
	}
	/** Create a loop bound relative to the marker. If the marker is an outer loop,
	 *  or the enclosing method, the loop bound is also a valid basic bound
	 */
	public static LoopBound markerBound(LoopBoundExpr bound, SymbolicMarker marker) {
		LoopBoundExpr basicBound;
		if(marker.inSameMethod()) {
			basicBound = bound;
		} else {
			basicBound = LoopBoundExpr.ANY;
		}
		return markerBound(basicBound, bound, marker);
	}

	public static LoopBound markerBound(
			LoopBoundExpr  basicBound,
			LoopBoundExpr  markerBound,
			SymbolicMarker marker) {
		if(basicBound == null) throw new AssertionError("loop bound expr: null");
		if(markerBound == null) throw new AssertionError("loop bound expr: null");
		LoopBound bound = new LoopBound(basicBound);
		bound.addBound(markerBound, marker);
		return bound;
	}


//    public static LoopBound markerBound(LoopBoundExpr bound, SymbolicMarker marker) {
//        LoopBound loopBound = new LoopBound(bound);
//        loopBound.setBoundMarker(bound, marker);
//        return loopBound;
//    }

    public LoopBound clone() {
        return new LoopBound(markerBounds);
    }

    private LoopBound(LoopBoundExpr loopBoundRelativeToEntry) {
        markerBounds = new HashMap<SymbolicMarker, LoopBoundExpr>();
        markerBounds.put(SymbolicMarker.LOOP_ENTRY, loopBoundRelativeToEntry);
    }

    public void addBound(LoopBound other) {
        for (Entry<SymbolicMarker, LoopBoundExpr> entry : other.markerBounds.entrySet()) {
            SymbolicMarker marker = entry.getKey();
            markerBounds.put(marker, entry.getValue().intersect(markerBounds.get(marker)));
        }
    }

    public LoopBoundExpr getSimpleLoopBound() {
        return markerBounds.get(SymbolicMarker.LOOP_ENTRY);
    }
    
	public Long getLowerBound() {
		return getSimpleLoopBound().lowerBound();
	}

	public Long getUpperBound() {
		return getSimpleLoopBound().upperBound();
	}
	
    public void addBound(LoopBoundExpr boundExpr, SymbolicMarker marker) {
        markerBounds.put(marker, boundExpr.intersect(markerBounds.get(marker)));
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (Entry<SymbolicMarker, LoopBoundExpr> lbEntry : markerBounds.entrySet()) {
            sb.append("; ");
            boundToString(sb, lbEntry.getValue(), lbEntry.getKey());
        }
        return sb.toString();
    }

    private static void boundToString(StringBuffer sb, LoopBoundExpr bound,
                                      SymbolicMarker marker) {
        sb.append(bound.toString());
        if (marker != null) {
            if (marker.getMarkerType() == SymbolicMarkerType.OUTER_LOOP_MARKER) {
                if (marker.getOuterLoopDistance() != 0) {
                    sb.append("outer(");
                    sb.append(marker.getOuterLoopDistance());
                    sb.append(")");
                }
            } else if (marker.getMarkerType() == SymbolicMarkerType.METHOD_MARKER) {
                sb.append("method(");
                sb.append(marker.getMethodName());
                sb.append(")");
            }
        }
    }

    public Set<Entry<SymbolicMarker, LoopBoundExpr>> getLoopBounds() {
        return this.markerBounds.entrySet();
    }


}