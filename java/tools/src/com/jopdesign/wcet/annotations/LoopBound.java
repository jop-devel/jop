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

import com.jopdesign.wcet.graphutils.Pair;

/**
 * Purpose: Instances represent non-symbolic, loop bounds relative to the
 *          execution frequency of the edges entering the loop.
 *
 * @author Benedikt Huber (benedikt@vmars.tuwien.ac.at)
 *
 */
public class LoopBound {
	private static final long serialVersionUID = 1L;
	private Pair<Integer,Integer> absoluteBound;
	private Map<SymbolicMarker, Pair<Integer,Integer>> relativeBounds =
		new HashMap<SymbolicMarker, Pair<Integer,Integer>>();
	
	public LoopBound clone()
	{
		LoopBound lb = new LoopBound(absoluteBound);
		lb.relativeBounds = new HashMap<SymbolicMarker,Pair<Integer,Integer>>(relativeBounds);
		return lb;
	}

	private LoopBound(Pair<Integer,Integer> absBound) {
		this.absoluteBound = absBound;
	}
	
	public LoopBound(Integer lb, Integer ub) {
		this.absoluteBound = new Pair<Integer,Integer>(lb,ub);
	}

	public int getLowerBound()  { return absoluteBound.fst(); }
	public int getUpperBound() { return absoluteBound.snd(); }
	
	public static LoopBound boundedAbove(int ub) {
		return new LoopBound(0,ub);
	}
	public void improveUpperBound(int newUb) {
		absoluteBound = LoopBound.mergeBounds(absoluteBound,0, newUb);
	}

	public void setRelativeUpperBound(int ub, SymbolicMarker marker) {
		relativeBounds.put(marker, mergeBounds(relativeBounds.get(marker),0,ub));
	}
	public void setRelativeBound(int lb, int ub, SymbolicMarker marker) {
		relativeBounds.put(marker, mergeBounds(relativeBounds.get(marker),lb,ub));
	}

	private static Pair<Integer, Integer> mergeBounds(Pair<Integer, Integer> oldBound, int newLb, int newUb) {
		if(oldBound == null) return new Pair<Integer,Integer>(newLb,newUb);
		return new Pair<Integer,Integer>(Math.max(oldBound.fst(), newLb), Math.min(oldBound.snd(), newUb));
	}
	
}