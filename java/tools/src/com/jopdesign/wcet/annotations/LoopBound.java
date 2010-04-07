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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.jopdesign.wcet.annotations.SymbolicMarker.SymbolicMarkerType;
import com.jopdesign.wcet.graphutils.Pair;

/**
 * Purpose: Instances represent execution frequency bounds for loops.
 *          Simple loop bounds are relative to the execution frequency
 *          of the entry edges of the loop (marker {@code outer(0)}); more
 *          elaborate bounds are relative outer loop or method.
 *
 * @author Benedikt Huber (benedikt@vmars.tuwien.ac.at)
 *
 */
public class LoopBound {
	private static final long serialVersionUID = 1L;
	/* Invariant: There is always an entry for the marker outer(0) */
	private Map<SymbolicMarker, Pair<Long,Long>> markerBounds;

	private LoopBound(Map<SymbolicMarker, Pair<Long,Long>> other)
	{
		markerBounds = new HashMap<SymbolicMarker, Pair<Long,Long>>(other);
	}
	
	public LoopBound clone()
	{
		return new LoopBound(markerBounds);
	}

	public void merge(LoopBound other)
	{
		for(Entry<SymbolicMarker, Pair<Long, Long>> entry : other.markerBounds.entrySet()) {
			SymbolicMarker marker = entry.getKey();
			markerBounds.put(marker, mergeBounds(this.markerBounds.get(marker),entry.getValue()));
		}
	}

	private LoopBound(Pair<Long,Long> simpleBound) {
		markerBounds = new HashMap<SymbolicMarker, Pair<Long,Long>>();
		markerBounds.put(SymbolicMarker.LOOP_ENTRY, simpleBound);
	}
	
	public LoopBound(Long lb, Long ub) {
		this(new Pair<Long,Long>(lb,ub));
	}

	public Pair<Long,Long> getSimpleBound() {
		return markerBounds.get(SymbolicMarker.LOOP_ENTRY);
	}
	
	public long getLowerBound()  { return getSimpleBound().fst(); }
	
	public long getUpperBound()  { return getSimpleBound().snd(); }
	
	public static LoopBound boundedAbove(long ub) {
		return new LoopBound(0L,ub);
	}

	public static LoopBound markerBound(long lb, long ub, SymbolicMarker marker)
	{
		LoopBound loopBound = new LoopBound(0L, ub);
		loopBound.setBoundMarker(lb,ub,marker);
		return loopBound;
	}
	
	public void improveUpperBound(long newUb) {
		improveSimpleBound(0L, newUb);
	}
	
	public void improveSimpleBound(long newLb, long newUb) {
		SymbolicMarker loopMarker = SymbolicMarker.LOOP_ENTRY;
		markerBounds.put(loopMarker, mergeBounds(markerBounds.get(loopMarker),
						 newLb, newUb));		
	}

	public void setUpperBoundMarker(long ub, SymbolicMarker marker) {
		setBoundMarker(0L, ub, marker);
	}

	public void setBoundMarker(long lb, long ub, SymbolicMarker marker) {
		markerBounds.put(marker, mergeBounds(markerBounds.get(marker),lb,ub));
		improveSimpleBound(lb,ub);
	}

	private static Pair<Long, Long> 
	mergeBounds(Pair<Long, Long> oldBound, long newLb, long newUb) {
		if(oldBound == null) return new Pair<Long,Long>(newLb,newUb);
		return new Pair<Long,Long>(Math.max(oldBound.fst(), newLb), 
								   Math.min(oldBound.snd(), newUb));
	}
	
	private Pair<Long, Long> mergeBounds(
			Pair<Long, Long> oldBound,
			Pair<Long, Long> newBound) {
		return mergeBounds(oldBound, newBound.fst(), newBound.snd());
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for(Entry<SymbolicMarker, Pair<Long, Long>> lbEntry : markerBounds.entrySet()) {
			sb.append("; ");
			boundToString(sb, lbEntry.getValue(), lbEntry.getKey());
		}
		return sb.toString();
	}
	
	private static void boundToString(StringBuffer sb, Pair<Long, Long> bound,
			SymbolicMarker marker) {
		sb.append("[");
		sb.append(bound.fst());
		sb.append(",");
		sb.append(bound.snd());
		sb.append("] ");
		if(marker!=null){
			if(marker.getMarkerType() == SymbolicMarkerType.OUTER_LOOP_MARKER) {
				if(marker.getOuterLoopDistance() != 0) {
					sb.append("outer(");
					sb.append(marker.getOuterLoopDistance());
					sb.append(")");
				}
			} else if(marker.getMarkerType() == SymbolicMarkerType.METHOD_MARKER) {
				sb.append("method(");
				sb.append(marker.getMethodName());
				sb.append(")");				
			}
		}		
	}

	public Set<Entry<SymbolicMarker, Pair<Long, Long>>> getLoopBounds() {
		return this.markerBounds.entrySet();
	}
}