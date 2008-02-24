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

public class FUCAMethod {

	static int ts, te, to;

	static boolean dummy = true;
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		FUCAMethod m = new FUCAMethod();
		ts = Native.rdMem(Const.IO_CNT);
		te = Native.rdMem(Const.IO_CNT);
		to = te-ts;
		// WCET with var. block cache: 12279
		// WCET with two block cache: x
		// WCET analysed: 11820
		measure();
		System.out.println(te-ts-to);
	}

	static void measure() {
		ts = Native.rdMem(Const.IO_CNT);
		for (int i=0; i<10; ++i) { // @WCA loop=10
			a();
		}
		te = Native.rdMem(Const.IO_CNT);
	}
	
	static void a() {
		int i;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
        if (dummy) {
            b();
        } else {
            d();
        }		
	}

	static void b() {
		int i;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		for (i=0; i<5; ++i) { // @WCA loop=5
			c();
		}
	}
	static void c() {

		int a, b, c, d, e;
		int i = 123;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;

	}
	static void d() {

		int a, b, c, d, e;
		int i = 123;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;
		i = 456;

	}
}
