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

public class Method2 {

	static int ts, te, to;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		Method2 m = new Method2();
		ts = Native.rdMem(Const.IO_CNT);
		te = Native.rdMem(Const.IO_CNT);
		to = te-ts;
		// WCET with var. block cache: x
		// WCET with two block cache: 15542
		// WCET analysed: 16738
		measure();
		System.out.println(te-ts-to);
	}
	
	static void measure() {
//		Native.wrMem(1, Const.IO_WD);
		ts = Native.rdMem(Const.IO_CNT);
		foo();		
		te = Native.rdMem(Const.IO_CNT);		
//		Native.wrMem(0, Const.IO_WD);
	}
	
	
	static void foo() {
		
		for (int i=0; i<10; ++i) { // @WCA loop=10
			a();
			b();
		}
	}

	static void a() {
		
		int val = 123;
		for (int i=0; i<10; ++i) { // @WCA loop=10
			val *= val;
		}
	}

	static void b() {
		
		int val = 123;
		for (int i=0; i<5; ++i) { // @WCA loop=5
			val += c();
		}
		for (int i=0; i<5; ++i) { // @WCA loop=5
			val += val;
		}
	}
	
	static int c() {
		
		return 456;
	}
}
