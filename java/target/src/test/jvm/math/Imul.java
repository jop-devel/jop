/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2007, Alberto Andreotti

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

package jvm.math;

import jvm.TestCase;


public class Imul extends TestCase {
	
	public String toString() {
		return "Imul";
	}
	
	public boolean test() {

		boolean ok = true;
		
		ok = ok && mul(1, 2, 2);
		ok = ok && mul(123, 456, 56088);
		ok = ok && mul(-1, 5, -5);
		ok = ok && mul(5, -1, -5);
		ok = ok && mul(-1, -1, 1);
		ok = ok && mul(0x80000000 ,0x80000000 , 0);
		ok = ok && mul(0xffffffff ,0x80000000 , 0x80000000);
		ok = ok && mul(0x80000000 ,0xffffffff , 0x80000000);
		ok = ok && mul(0x7fffffff ,0x80000000 , 0x80000000);
		ok = ok && mul(0x80000000 ,0x7fffffff , 0x80000000);
		ok = ok && mul(0x7fffffff ,0xffffffff , -2147483647);
		ok = ok && mul(0xffffffff ,0x7fffffff , -2147483647);
		
		return ok;
	}

	static boolean mul(int a, int b, int exp) {

		if (a*b == exp) {
			return true;
		} else {
			return false;
		}
	}

}
