/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2009, Martin Schoeberl (martin@jopdesign.com)

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

package jopsys;

import jvm.TestCase;

/**
 * Test low-level cache stuff.
 * 
 * @author martin
 *
 */
public class Cache extends TestCase {

	public String toString() {
		return "Cache";
	}

	static com.jopdesign.io.SysDevice sys = com.jopdesign.io.IOFactory.getFactory().getSysDevice();
	
	public boolean test() {
		boolean ok = true;
		
		int val = sys.cntInt;
		int val2 = sys.cntInt;
		// I/O should not be cached
		ok &= val != val2;
		return ok;

	}

}