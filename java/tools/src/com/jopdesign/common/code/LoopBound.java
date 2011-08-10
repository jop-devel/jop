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
 * <p> Purpose: Instances represent execution frequency bounds for loops.
 * Simple loop bounds are relative to the execution frequency
 * of the entry edges of the loop; for more elaborated kinds of
 * relative bounds see below. Note that you need at least one bound
 * where the marker dominates the loop entry, otherwise the WCET problem
 * will be unbounded.</p>
 * We currently support the following loop bounds:<br/>
 * {@code loop = loop-bound-expr marker? }
 * <p>{@code loop-bound-expr} is a symbolic expression which is
 * evaluated in the domain of integer intervals. The form
 * {@code loop <= loop-bound-expr marker? } is a short hand for
 * {@code loop = [0,loop-bound-expr] marker? }.</p>
 * <p>Markers specify to which execution frequency loop bounds
 * are relative to. The mark {@code outer(n)} expresses
 * that the loop bound is relative to execution frequency of
 * the entry edges of the n-th outer loop. If the marker is
 * ommited, this is a short hand for {@code outer(0)}, the
 * execution frequency sum of the entry edges of the current loop.</p>
 * <p>To specify that the execution frequency of the loop body is bounded
 * relative to some method, use the marker {@code method methodID}.</p>
 * <p>TODO: maybe we also want to specify sums of (expr,marker) pairs</p>
 * <p>TODO: we also want to allow absolute execution bounds for the loop
 * as a whole. One idea would be to restrict the loop bound to 1, and then
 * add the absolute cost to continue edge.
 * </p>
 *
 * @author Benedikt Huber (benedikt@vmars.tuwien.ac.at)
 */
public class LoopBound {
    private static final long serialVersionUID = 1L;

    /* Invariant: There is always an entry for the marker LOOP_ENTRY */
    private Map<SymbolicMarker, LoopBoundExpr> markerBounds;

    private final boolean isDefaultBound;

    private LoopBound(Map<SymbolicMarker, LoopBoundExpr> other) {
        markerBounds = new HashMap<SymbolicMarker, LoopBoundExpr>(other);
        isDefaultBound = false;
    }

    public static LoopBound boundedAbove(long ub) {
        return new LoopBound(LoopBoundExpr.numUpperBound(ub));
    }

    public static LoopBound defaultBound(long ub) {
        return new LoopBound(LoopBoundExpr.numUpperBound(ub), true);
    }

    public static LoopBound simpleBound(LoopBoundExpr bound) {
        if (bound == null) throw new AssertionError("loop bound expr: null");
        return new LoopBound(bound);
    }

    /**
     * Create a loop bound relative to the marker. If the marker is an outer loop,
     * or the enclosing method, the loop bound is also a valid basic bound
     */
    public static LoopBound markerBound(LoopBoundExpr bound, SymbolicMarker marker) {
        LoopBoundExpr basicBound;
        if (marker.inSameMethod()) {
            basicBound = bound;
        } else {
            basicBound = LoopBoundExpr.ANY;
        }
        return markerBound(basicBound, bound, marker);
    }

    public static LoopBound markerBound(
            LoopBoundExpr basicBound,
            LoopBoundExpr markerBound,
            SymbolicMarker marker) {
        if (basicBound == null) throw new AssertionError("loop bound expr: null");
        if (markerBound == null) throw new AssertionError("loop bound expr: null");
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
        isDefaultBound = false;
    }

    private LoopBound(LoopBoundExpr loopBoundRelativeToEntry, boolean isDefaultBound) {
        markerBounds = new HashMap<SymbolicMarker, LoopBoundExpr>();
        markerBounds.put(SymbolicMarker.LOOP_ENTRY, loopBoundRelativeToEntry);
        this.isDefaultBound = isDefaultBound;
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

    public Long getLowerBound(ExecutionContext ctx) {
        return getSimpleLoopBound().lowerBound(ctx);
    }

    public Long getUpperBound(ExecutionContext ctx) {
        return getSimpleLoopBound().upperBound(ctx);
    }

    /**
     * @return true if this bound was created as default (fallback) bound using {@link #defaultBound(long)}.
     */
    public boolean isDefaultBound() {
        return isDefaultBound;
    }

    public void addBound(LoopBoundExpr boundExpr, SymbolicMarker marker) {
        markerBounds.put(marker, boundExpr.intersect(markerBounds.get(marker)));
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        boolean first = true;
        for (Entry<SymbolicMarker, LoopBoundExpr> lbEntry : markerBounds.entrySet()) {
            if (first) {
                sb.append("; ");
                first = false;
            }
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