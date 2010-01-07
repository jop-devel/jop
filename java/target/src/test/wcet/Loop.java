/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2001-2008, Martin Schoeberl (martin@jopdesign.com)

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

package wcet;

import com.jopdesign.sys.*;

public class Loop {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		foo(true, 123);
	}

	public static int foo(boolean b, int val) {
		
		// measured: 1369
		// wcet: 1393
		// difference is 24 cycles:
		//		iload_1		1
		//		ireturn		23

		int i, j;
		
//		int ts, te, to;
//		ts = Native.rdMem(Const.IO_CNT);
//		te = Native.rdMem(Const.IO_CNT);
//		to = te-ts;
//		ts = Native.rdMem(Const.IO_CNT);
		
		
		for (i=0; i<10; ++i) {	//@WCA loop=10
			if (b) {
				for (j=0; j<3; ++j) {	//@WCA loop=3
					val *= val;
				}
			} else {
				for (j=0; j<4; ++j) {	//@WCA loop=4
					val += val;
				}
			}
		}
		

//		te = Native.rdMem(Const.IO_CNT);
//		System.out.println(te-ts-to);
		
		return val;
	}
}
