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

public class LongField extends TestCase {

	
//	static final long C1 = 0x123456789abcdefL;
//	static final long C2 = 0xcafe1122babe3344L;
	static final long C1 = 1L;
	static final long C2 = 2L;
	static final long C3 = 0x00000000ffffffffL;
	static final long C4 = 0x0000000100000000L;
	long a, b;
	static long sa, sb;
	
	public String toString() {
		return "Long Field";
	}
	
	public boolean test() {
	
		boolean ok = true;
		long l1, l2;

		a = C1;
		b = C2;
		sa = C1;
		sb = C2;
		l1 = C1;
		l2 = C2;
				
		ok = ok && a==C1;
		ok = ok && b==C2;
		ok = ok && sa==C1;
		ok = ok && sb==C2;
		ok = ok && l1==C1;
		ok = ok && l2==C2;
		
		ok = ok && a==l1;
		ok = ok && b==l2;
		ok = ok && sa==l1;
		ok = ok && sb==l2;
		
		a=1; b=2; l1=3; l2=4; sa=5; sb=6;
		
		ok = ok && a==1 && b==2 && l1==3 && l2==4 && sa==5 && sb==6;
		
		l1 = C3;
		l1 += 1;
		ok = ok && l1==C4 && l2==4;
		a = C3;
		a += 1;
		ok = ok && a==C4 && b==2;
		sa = C3;
		sa += 1;
		ok = ok && sa==C4 && sb==6;

		return ok;
	}

}
