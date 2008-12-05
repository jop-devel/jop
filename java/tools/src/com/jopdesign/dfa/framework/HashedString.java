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

package com.jopdesign.dfa.framework;

public class HashedString {

	private String value;
	private int hash;
	
	public HashedString(String value) {
		this.value = value;
		this.hash = value.hashCode();
	}
	
	public int hashCode() {
		return hash;
	}
	
	public String toString() {
		return value;
	}
	
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (getClass() != o.getClass())
			return false;
		return value.equals(o.toString());
	}
}
