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
import java.util.Map.Entry;

import com.jopdesign.wcet.annotations.SymbolicMarker.SymbolicMarkerType;
import com.jopdesign.wcet.graphutils.Pair;

/**
 * Purpose: Instances represent execution frequency bounds for loops.
 *          Simple loop bounds are relative to the execution frequency
 *          of the entry edges of the loop, relative bounds are relative
 *          to some marker (outer loop or method).
 *
 * @author Benedikt Huber (benedikt@vmars.tuwien.ac.at)
 *
 */
public class LoopBound {
	private static final long serialVersionUID = 1L;
	private Pair<Long,Long> simpleBound;
	private Map<SymbolicMarker, Pair<Long,Long>> markerBounds =
		new HashMap<SymbolicMarker, Pair<Long,Long>>();
	
	public LoopBound clone()
	{
		LoopBound lb = new LoopBound(simpleBound);
		lb.markerBounds = new HashMap<SymbolicMarker,Pair<Long,Long>>(markerBounds);
		return lb;
	}
	public void merge(LoopBound other)
	{
		this.simpleBound = mergeBounds(simpleBound, other.simpleBound);
		for(SymbolicMarker marker : other.markerBounds.keySet()) {
			markerBounds.put(marker, mergeBounds(this.markerBounds.get(marker),other.markerBounds.get(marker)));
		}
	}

	private LoopBound(Pair<Long,Long> simpleBound) {
		this.simpleBound = simpleBound;
	}
	
	public LoopBound(Long lb, Long ub) {
		this.simpleBound = new Pair<Long,Long>(lb,ub);
	}

	public long getLowerBound()  { return simpleBound.fst(); }
	public long getUpperBound()  { return simpleBound.snd(); }
	
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
		simpleBound = LoopBound.mergeBounds(simpleBound,0, newUb);
	}

	public void setUpperBoundMarker(long ub, SymbolicMarker marker) {
		markerBounds.put(marker, mergeBounds(markerBounds.get(marker),0,ub));
	}
	public void setBoundMarker(long lb, long ub, SymbolicMarker marker) {
		markerBounds.put(marker, mergeBounds(markerBounds.get(marker),lb,ub));
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
		mergeBounds(oldBound, newBound.fst(), newBound.snd());
		return null;
	}
	public String toString() {
		StringBuffer sb = new StringBuffer();
		boundToString(sb, simpleBound,null);
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
				sb.append("outer(");
				sb.append(marker.getOuterLoopDistance());
				sb.append(")");
			} else if(marker.getMarkerType() == SymbolicMarkerType.METHOD_MARKER) {
				sb.append("method(");
				sb.append(marker.getMethodName());
				sb.append(")");				
			}
		}
		// TODO Auto-generated method stub
		
	}
}