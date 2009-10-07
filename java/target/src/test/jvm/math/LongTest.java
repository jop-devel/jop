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

public class LongTest extends TestCase {

	static long x(long a, long b) {
	
		return a-b;
	}
	public String toString() {
		return "LongTest";
	}
	
	public boolean test() {
	
		boolean ok = true;
				
		ok = ok && testArray();
		ok = ok && testArithOps1();
		ok = ok && testArithOps2();
		ok = ok && testCmpCastOps();
		ok = ok && testLogicOps();
		ok = ok && testShiftOps();
	
		return ok;
	}
	

	static boolean testArray()
	{
		boolean ok = true;
		long[] a = new long[4];
	
//		System.out.println("len="+a.length);
		
		
		long a0 = 0xffffffffL;
		long a1 = 0xfffffffff0L;
		long a2 = 0xfffffffff00L;
		long a3 = 0xfffffffff000L;
		
//		int t1, t2, diff;
//		t1 = Native.rd(Const.IO_CNT);
//		t2 = Native.rd(Const.IO_CNT);
//		diff = t2-t1;
//		
//		t1 = Native.rd(Const.IO_CNT);

		a[0] = a0;

//		t2 = Native.rd(Const.IO_CNT);
//		System.out.println("lastore="+(t2-t1-diff-4));

		a[1] = a1;
		a[2] = a2;
		a[3] = a3;

//		t1 = Native.rd(Const.IO_CNT);

		a0 = a[0];

//		t2 = Native.rd(Const.IO_CNT);
//		System.out.println("laload="+(t2-t1-diff-4));

		ok = ok && (a[0] == a0);
		ok = ok && (a[1] == a1);
		ok = ok && (a[2] == a2);
		ok = ok && (a[3] == a3);
		
	/*	long[] b1 = new long[1];
		long[] b2 = new long[1];
		long[] b3 = new long[1];
 		b1[0] = 0;
 		b2[0] = 0;
 		b3[0] = 0;

 		b2[1] = 1;*/
 		
 	/*	printLong(b2[2]);
 		printLong(b1[0]);
 		printLong(b3[0]);*/
		
		return ok;
	}
	
	static boolean testCmpCastOps() 
	{
		boolean ok = true;
		
		ok = ok && lcmp(1L, 0L, 1);
		ok = ok && lcmp(0L, 1L, -1);
		ok = ok && lcmp(0L, 0L, 0);
		ok = ok && lcmp(1L, 1L, 0);
		ok = ok && lcmp(0L, -1L, 1);
		ok = ok && lcmp(-1L, 0L, -1);
		ok = ok && lcmp(-1L, -1L, 0);
		ok = ok && lcmp(0x100000000L, 0xffffffffL, 1);
		ok = ok && lcmp(0xffffffffL, 0x100000000L, -1);
		ok = ok && lcmp(0x8000000000000000L, 0x8000000000000001L, -1);
		ok = ok && lcmp(0x8000000000000000L, 0xffffffffffffffffL, -1);
		ok = ok && lcmp(0x8000000000000000L, 0x7fffffffffffffffL, -1);
		ok = ok && lcmp(0x8000000000000000L, 0x1L, -1);
		ok = ok && lcmp(0x8000000000000000L, 0x0L, -1);
		ok = ok && lcmp(0x8000000000000000L, 0x8000000000000000L, 0);

		ok = ok && i2l(0, 0L);
		ok = ok && i2l(1, 1L);
		ok = ok && i2l(-1, -1L);
		ok = ok && i2l(-123, -123L);
		ok = ok && i2l(123, 123L);
		ok = ok && i2l(0x80000000, 0xffffffff80000000L);
		ok = ok && i2l(0x7fffffff, 0x7fffffffL);
		
		return ok;
	}

	static boolean testShiftOps() 
	{
		boolean ok = true;
		ok = ok && lushr(0L,0,0L);
		ok = ok && lushr(-1L,0,-1L);
		ok = ok && lushr(1L,0,1L);
		ok = ok && lushr(0xfedcba9800000000L,8,0x00fedcba98000000L);
		ok = ok && lushr(0x00000000fedcba98L,8,0x0000000000fedcbaL);
		ok = ok && lushr(0xfedcba9800000000L,40,0x0000000000fedcbaL);
		ok = ok && lushr(-1L,16,0x0000ffffffffffffL);
		ok = ok && lushr(0xfedcba9800000000L,64,0xfedcba9800000000L);
		ok = ok && lushr(0x00000000fedcba98L,72,0x0000000000fedcbaL);
		
		ok = ok && lshr(0L,0,0L);
		ok = ok && lshr(1L,0,1L);
		ok = ok && lshr(0xfedcba9800000000L,8,0xfffedcba98000000L);
		ok = ok && lshr(0x00000000fedcba98L,8,0x0000000000fedcbaL);		
		ok = ok && lshr(0xfedcba9800000000L,32,0xfffffffffedcba98L);
		ok = ok && lshr(0x40000000fedcba98L,32,0x0000000040000000L);
		ok = ok && lshr(0xfedcba9800000000L,64,0xfedcba9800000000L);
		ok = ok && lshr(0x00000000fedcba98L,72,0x0000000000fedcbaL);

		ok = ok && lshl(0L,0,0L);
		ok = ok && lshl(1L,0,1L);
		ok = ok && lshl(0xfedcba9800000000L,8,0xdcba980000000000L);
		ok = ok && lshl(0x00000000fedcba98L,8,0x000000fedcba9800L);		
		ok = ok && lshl(0x00000000fedcba98L,16,0x0000fedcba980000L);
		ok = ok && lshl(0x0000fedcba980000L,16,0xfedcba9800000000L);
		ok = ok && lshl(0xfedcba9800000001L,32,0x0000000100000000L);
		ok = ok && lshl(0x00000000fedcba98L,48,0xba98000000000000L);
		ok = ok && lshl(0xfedcba9800000000L,64,0xfedcba9800000000L);
		ok = ok && lshl(0x00000000fedcba98L,72,0x000000fedcba9800L);
		return ok;
	}
	
	static boolean testArithOps1() 
	{
		boolean ok = true;

		ok = ok && lmul(0, 0, 0);
		ok = ok && lmul(0, -1, 0);
		ok = ok && lmul(-1, 1, -1);
		ok = ok && lmul(1, 0, 0);
		ok = ok && lmul(-1, -1, 1);
		ok = ok && lmul(1, 1, 1);
		ok = ok && lmul(200000L, 200000L, 40000000000L);
		ok = ok && lmul(200000L, -200000L, -40000000000L);
		ok = ok && lmul(-200000L, -200000L, 40000000000L);
		ok = ok && lmul(-200000L, 200000L, -40000000000L);
		ok = ok && lmul(0xfL, 0xfL, 0xe1L);
		ok = ok && lmul(0xffL, 0xffL, 0xfe01L);
		ok = ok && lmul(0xfffL, 0xfffL, 0xffe001L);
		ok = ok && lmul(0xffffL, 0xffffL, 0xfffe0001L);
		ok = ok && lmul(0xfffffL, 0xfffffL, 0xffffe00001L);
		ok = ok && lmul(0xffffffL, 0xffffffL, 0xfffffe000001L);
		ok = ok && lmul(0xfffffffL, 0xfffffffL, 0xffffffe0000001L);
		ok = ok && lmul(0xffffffffL, 0xffffffffL, 0xfffffffe00000001L);
		ok = ok && lmul(0xfffffffffL, 0xfffffffffL, 0xffffffe000000001L);
		ok = ok && lmul(0xf0000000fL, 0xf0000000fL, 0x1C2000000E1L);
		ok = ok && lmul(-384384L, 931931L, -358219365504L);
		ok = ok && lmul(0xf0000000fL, 0xf0000000fL, 0x1C2000000E1L);
		ok = ok && lmul(0x8000000000000000L, 0L, 0L);
		ok = ok && lmul(0x8000000000000000L, 0x8000000000000000L, 0L);
		ok = ok && lmul(0x8000000000000000L, 1L, 0x8000000000000000L);
		ok = ok && lmul(0x8000000000000001L, -1L, 0x7fffffffffffffffL);
		
		return ok;
	}

		static boolean testArithOps2() 
		{
			boolean ok = true;

		ok = ok && lneg(0L, 0L);
		ok = ok && lneg(1L, -1L);
		ok = ok && lneg(0x8000000000000000L,0x8000000000000000L);
		ok = ok && lneg(0x8000000000000001L,0x7fffffffffffffffL);
		ok = ok && lneg(0x7fffffffffffffffL,0x8000000000000001L);
		ok = ok && lneg(0xffffffff80000000L,0x0000000080000000L);
		ok = ok && lneg(0x0000000080000000L,0xffffffff80000000L);

		//ok = ok && ldiv(0L, 0L, 0L);
		ok = ok && ldiv(0L, 1L, 0L);
		ok = ok && ldiv(1L, 1L, 1L);
		ok = ok && ldiv(1L, -1L, -1L);
		ok = ok && ldiv(-1L, 1L, -1L);
		ok = ok && ldiv(-1L, -1L, 1L);
		ok = ok && ldiv(1000000000L, 3L, 333333333L);
		ok = ok && ldiv(0x8000000000000000L, 0x7fffffffffffffffL, -1L);
     	ok = ok && ldiv(0x8000000000000000L, 0x8000000000000000L, 1L);
		ok = ok && ldiv(0x8000000000000000L, 1L, 0x8000000000000000L);
		ok = ok && ldiv(0x8000000000000001L, -1L, 0x7fffffffffffffffL);
		ok = ok && ldiv(0x7fffffffffffffffL, 0x7fffffffffffffffL, 1L);
		ok = ok && ldiv(0x8000000000000000L, 0x4000000000000000L, -2L);
		ok = ok && ldiv(0x8000000000000001L, 0x8000000000000000L, 0L);
		
		ok = ok && lrem(0L, 1L, 0L);
		ok = ok && lrem(1L, 1L, 0L);
		ok = ok && lrem(100000000000L,3L, 1L);
		ok = ok && lrem(0x8000000000000000L, 0x8000000000000000L, 0L);
		ok = ok && lrem(100000000000L,33333333333L, 1L);
		ok = ok && lrem(0x7fffffffffffffffL, 0x7fffffffffffffffL, 0L);
		ok = ok && lrem(0x8000000000000001L, 0x7fffffffffffffffL, 0L);
		ok = ok && lrem(0x8000000000000001L, 0x8000000000000000L, 0x8000000000000001L);
		
		ok = ok && ladd(0L, 0L, 0L);
		ok = ok && ladd(1L, 1L, 2L);
		ok = ok && ladd(-1L, -1L, -2L);
		ok = ok && ladd(0x80000000L, 0x80000000L, 0x100000000L);
		ok = ok && ladd(0xffffffffL, 0x00000001L, 0x100000000L);
		ok = ok && ladd(0x8000000000000000L, 0x8000000000000000L, 0L);
		ok = ok && ladd(0x7fffffffffffffffL, 0x7fffffffffffffffL, -2L);
		ok = ok && ladd(0x7fffffffffffffffL, 0x8000000000000000L, -1L);

		ok = ok && lsub(0L, 0L, 0L);
		ok = ok && lsub(1L, -1L, 2L);
		ok = ok && lsub(-1L, -1L, 0L);
		ok = ok && lsub(0x100000000L, 0x80000000L, 0x80000000L);
		ok = ok && lsub(0x100000000L, 0xffffffffL, 0x00000001L);
		ok = ok && lsub(0x8000000000000000L, 0x8000000000000000L, 0L);
		ok = ok && lsub(0x7fffffffffffffffL, 0x7fffffffffffffffL, 0L);
		ok = ok && lsub(0x8000000000000000L, 1, 0x7fffffffffffffffL);

		return ok;
	}

	static boolean testLogicOps()
	{
		boolean ok = true;
		ok = ok && land(0xffffffffffffffffL,0xffffffffffffffffL,0xffffffffffffffffL);
		ok = ok && land(0xffffffffffffffffL,0x0000000000000000L,0x0000000000000000L);
		ok = ok && land(0xffffffffffffffffL,0xffffffff00000000L,0xffffffff00000000L);
		ok = ok && land(0xffffffffffffffffL,0x00000000ffffffffL,0x00000000ffffffffL);
		ok = ok && land(0x0000ffffffff0000L,0x00ffffffffffff00L,0x0000ffffffff0000L);

		ok = ok && lor(0x0000000000000000L,0xffffffffffffffffL,0xffffffffffffffffL);
		ok = ok && lor(0x0000000000000000L,0x0000000000000000L,0x0000000000000000L);
		ok = ok && lor(0x0000000000000000L,0xffffffff00000000L,0xffffffff00000000L);
		ok = ok && lor(0x0000000000000000L,0x00000000ffffffffL,0x00000000ffffffffL);
		ok = ok && lor(0x00ffffffffffff00L,0x0000ffffffff0000L,0x00ffffffffffff00L);

		ok = ok && lxor(0x0000000000000000L,0xffffffffffffffffL,0xffffffffffffffffL);
		ok = ok && lxor(0x0000000000000000L,0x0000000000000000L,0x0000000000000000L);
		ok = ok && lxor(0x0000000000000000L,0xffffffff00000000L,0xffffffff00000000L);
		ok = ok && lxor(0x0000000000000000L,0x00000000ffffffffL,0x00000000ffffffffL);
		ok = ok && lxor(0x00ffffffffffff00L,0x0000ffffffff0000L,0x00ff00000000ff00L);
		
		return ok;
	}

	static boolean ladd(long a, long b, long exp) {

		return (a+b == exp);
	}
	
	static boolean lsub (long a, long b, long exp) {

		return (a-b == exp);
	}

	
	static boolean ldiv (long a, long b, long exp) {

		return (a/b == exp);
	}

	static boolean lrem (long a, long b, long exp) {

		return (a%b == exp);
	}
	
	static boolean lmul(long a, long b, long exp) {

	//	return (f_lmul(a,b) == exp);
	/*	System.out.print("a=");
        printLong(a);
		System.out.print("b=");
        printLong(b);
		System.out.print("a*b=");
        printLong(a*b);
        System.out.print("exp=");
        printLong(exp);*/
		return (a*b == exp);
	}


	static boolean land(long a, long b, long exp) {

		return ((a&b) == exp);
	}

	static boolean lor(long a, long b, long exp) {

		return ((a|b) == exp);
	}
	
	static boolean lxor(long a, long b, long exp) {

		return ((a^b) == exp);
	}
	
	static boolean lcmp(long a, long b, int exp) {

		int res = 0;
		if (a > b) {
			res = 1;
		} else if (a < b) {
			res = -1;
		} else if (a == b) {
			res = 0;
		}
		return (res == exp);
	}

	static boolean i2l(int val, long exp) {

		long res;
		res = val;
		return (res == exp);
	}

	static boolean lneg(long val, long exp) {

		long res;
		res = -val;
		return (res == exp);
	}
	
	static boolean lushr(long val, int cnt, long exp) {

		long res;
		res = val>>>cnt;
		return (res == exp);
	}
	
	static boolean lshr(long val, int cnt, long exp) {

		long res;
		res = val>>cnt;
/*		System.out.println(""+val);
		System.out.println(""+res);*/
		return (res == exp);
	}
	
	static boolean lshl(long val, int cnt, long exp) {

		long res;
		res = val<<cnt;
	/*	System.out.println(""+val);
		System.out.println(""+res);*/
		return (res == exp);
	}
	
	static void printLong(long a)
	{
		System.out.print((int)(a>>32)+" "+(int)a);
	//	System.out.print(a+"");
	//	Dbg.hexVal((int)a);
		System.out.println("");
	}
}
