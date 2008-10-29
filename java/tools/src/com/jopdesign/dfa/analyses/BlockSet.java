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

import java.util.BitSet;

public class BlockSet extends BitSet {

	private static final long serialVersionUID = 1L;

	private boolean must;

	public BlockSet(int size) {
		super(size);
		must = true;
	}

	public boolean isMust() {
		return must;
	}

	public void setMust(boolean must) {
		this.must = must;
	}
	
	public boolean equals(Object o) {
		BlockSet b = (BlockSet)o;
		return (must == b.must) && super.equals(b);
	}
	
	public String toString() {
		return super.toString()+(must?"!":"?");
	}
}
