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

import com.jopdesign.wcet.graphutils.Pair;

public class LoopBound extends Pair<Integer,Integer> {
	private static final long serialVersionUID = 1L;
	public LoopBound(Integer lb, Integer ub) {
		super(lb, ub);
	}
	public int getLowerBound()  { return fst(); }
	public int getUpperBound() { return snd(); }
	public static LoopBound boundedAbove(int ub) {
		return new LoopBound(0,ub);
	}
	public LoopBound improveUpperBound(int bound) {
		return new LoopBound(fst(), Math.min(snd(),bound));
	}
}