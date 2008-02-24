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

package test;

import util.Dbg;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

/**
*	div. utilites, test von mul/div/rem
*
*	todo: div, mod mit 0x80000000 ist FALSCH!!!
*/

public class Util {

	public static int mul(int a, int b) {

		int c, i;
		boolean neg = false;

		if (a<0) {
			neg = true;
			a = -a;
		}
		if (b<0) {
			neg = !neg;
			b = -b;
		}

		c = 0;
		for (i=0; i<32; ++i) {
			c <<= 1;
			if ((a & 0x80000000)!=0) c += b;
			a <<= 1;
		}
		if (neg) c = -c;
		return c;
	}
public static int add(int a, int b) { return a+b; }

	public static int div(int a, int b) {

		boolean neg = false;
		if (a<0) {
			neg = true;
			a = -a;
		}
		if (b<0) {
			neg = !neg;
			b = -b;
		}

		int c = 0;
		int r = 0;
		for (int i=0; i<32; ++i) {
			c <<= 1;
			r <<= 1;
			if ((a & 0x80000000)!=0) {
				r |= 1;
			}
			a <<= 1;
			if (r>=b) {
				r -= b;
				c |= 1;
			}
		}

		if (neg) {
			c = -c;
		}
		return c;
	}

	public static int rem(int a, int b) {

		boolean neg = false;
		if (a<0) {
			neg = true;
			a = -a;
		}
		if (b<0) {
			b = -b;
		}

		int c = 0;
		int r = 0;
		for (int i=0; i<32; ++i) {
			c <<= 1;
			r <<= 1;
			if ((a & 0x80000000)!=0) {
				r |= 1;
			}
			a <<= 1;
			if (r>=b) {
				r -= b;
				c |= 1;
			}
		}

		if (neg) {
			r = -r;
		}
		return r;
	}

	public static void main(String[] args) {

		int t1, toff;
/*
		int a = -1;
		int b = -1234;
*/
		int c;

		Dbg.initSer();

/*
		t1 = Native.rd(Const.IO_CNT);
		t1 = Native.rd(Const.IO_CNT) - t1;
		toff = t1;
		Dbg.intVal(toff);

		t1 = Native.rd(Const.IO_CNT);
		c = a*b;
		t1 = Native.rd(Const.IO_CNT) - t1;
		Dbg.intVal(t1-toff);

*/
		t1 = Native.rd(Const.IO_CNT);
		c = mul(-1, -1234);
		t1 = Native.rd(Const.IO_CNT) - t1;
		Dbg.intVal(t1);

		for (;;) ;
	}
/*
	public static void test(int a, int b) {

		System.out.println(a+"*"+b+"=\t"+mul(a, b)+" : "+(a*b));
		if (b!=0) {
			System.out.println(a+"/"+b+"=\t"+div(a, b)+" : "+(a/b));
			System.out.println(a+"%"+b+"=\t"+rem(a, b)+" : "+(a%b));
		}
	}

	public static void main(String[] args) {

		test(0, 0);
		test(0, 1);
		test(1, 0);
		test(3, 2);
		test(-3, 2);
		test(3, -2);
		test(-3, -2);
		test(10000, -3);
		test(10000, -287652987);
		test(123456789, -3);
		test(123456789, 123456789);
		test(-1, -1);
		test(0x7fffffff, 2);
		test(2, 0x7fffffff);

		test(0x80000000, 3);
		test(3, 0x80000000);

		test(0x80000000, 0x80000000);
		test(0x80000000, 0x7fffffff);
		test(0x7fffffff, 0x80000000);
		test(0x7fffffff, 0x7fffffff);

		test(0x80000001, 0x80000001);
		test(0x80000001, 0x7fffffff);
		test(0x7fffffff, 0x80000001);
		test(0x7fffffff, 0x7fffffff);

//		for (int i=1; i!=0; ++i) {
//			for (int j=1; j!=0; ++j) {
//				if (i*j != mul(i, j)) {
//					System.out.println("Fehler: "+i+" "+j);
//					System.exit(-1);
//				}
//			}
//			System.out.print(i+"\r");
		}
	}
*/
}
