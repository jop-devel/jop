/*
  This file is part of JOP, the Java Optimized Processor (http://www.jopdesign.com/)

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

/**
*	Test SoftFloat.java against PC.
*
*/
package test;

import java.io.*;
import java.util.*;

import com.jopdesign.sys.SoftFloat;

public class TestFloat {

	static long l = 0;

	public static void main (String[] args) {


		final long tim = System.currentTimeMillis();

		new Thread() {

			public void run() {
				for (;;) {
					System.out.print((l/1000000)+" M ");
					long diff = System.currentTimeMillis()-tim;
					diff /= 1000;
					if (diff!=0) {
						System.out.print((l/diff)+"/s");
					}
					System.out.print("\r");
					try { Thread.sleep(1000); } catch(Exception e) {}
				}
			}

		}.start();
		// t.setPriority(Thread.MAX_PRIORITY);


		int ia, ib;
		Random rnd = new Random();

		ia = ib = 0;

		for (l=0; true; ++l) {
			ia = rnd.nextInt();
			ib = rnd.nextInt();
			if (!test(ia, ib)) {
				fail(ia, ib);
			}
		}
	}

	static boolean test(int ia, int ib) {

		float a = Float.intBitsToFloat(ia);
		float b = Float.intBitsToFloat(ib);
		float result;
		int ires;

/*
		// fadd
		ires = SoftFloat.float32_add(ia, ib);
		result = a+b;
		if (ires != Float.floatToIntBits(result)) return false;

		// fsub
		ires = SoftFloat.float32_sub(ia, ib);
		result = a-b;
		if (ires != Float.floatToIntBits(result)) return false;

		// f2i
		ires = SoftFloat.float32_to_int32_round_to_zero(ia);
		if (ires != (int) a) return false;

		// i2f
		ires = SoftFloat.int32_to_float32(ia);
		result = ia;
		if (ires != Float.floatToIntBits(result)) return false;
*/

		// ...?
		ires = SoftFloat.float32_cmpg(ia, ib);
		int cmp = 2;
		if (a<b) cmp = -1;
		if (a==b) cmp = 0;
		if (a>b) cmp = 1;

		if (cmp!=ires) return false;

		return true;
	}

	static void fail(int ia, int ib) {

		System.out.println();
		System.out.println("error: "+ia+" "+ib);
		float a = Float.intBitsToFloat(ia);
		float b = Float.intBitsToFloat(ib);
		System.out.println(a+" "+b);
/*
		System.out.println(ires+" "+Float.floatToIntBits(result));
		System.out.println(Float.intBitsToFloat(ires)+" "+result);
*/
//		System.exit(-1);
	}
}
