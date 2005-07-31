package jvm.math;

import jvm.TestCase;

public class Long extends TestCase {

	static long x(long a, long b) {
	
		return a-b;
	}
	public String getName() {
		return "Long";
	}
	
	public boolean test() {
	
		boolean ok = true;

		long l1, l2;
		
//		double d = 0.1;

		l1 = 1;
		l2 = 2;


		ok = ok && add(1, 2, 3);
		ok = ok && add(1L, 2L, 3L);
		ok = ok && add(-3, 5, 2);
		ok = ok && add(5, -1, 4);
		ok = ok && add(-3, -5, -8);
		ok = ok && add(0x80000000L ,0x80000000L , 0x100000000L);
		ok = ok && add(0xffffffffL ,0x00000001L , 0x100000000L);

		ok = ok && lcmp(1, 2, -1);
		ok = ok && lcmp(3, 2, 1);
		ok = ok && lcmp(123, 123, 0);
		ok = ok && lcmp(123456789012345L, 123456789012345L, 0);
		ok = ok && lcmp(123456789012347L, 123456789012345L, 1);
		ok = ok && lcmp(123456789012347L, 123456789012567L, -1);
		ok = ok && lcmp(2, -1, 1);
		ok = ok && lcmp(-287346, 1, -1);
		ok = ok && lcmp(-87245983472L, -1, -1);
		ok = ok && lcmp(-87245983472L, 871234619831L, -1);

/*
		ok = ok && i2l(1, 1L);
		ok = ok && i2l(-1, -1L);
		ok = ok && i2l(0x80000000, 0xffffffff80000000L);
		ok = ok && i2l(-123, -123L);
		ok = ok && i2l(0x7fffffff, 0x7fffffffL);
		ok = ok && lneg(1L, -1L);
		ok = ok && lneg(2L, -2L);
		ok = ok && lneg(123, -123);
		ok = ok && lneg(-1L, 1L);
		ok = ok && lneg(-2L, 2L);
		ok = ok && lneg(-123, 123);
		ok = ok && lneg(123456789012345L, -123456789012345L);
		ok = ok && lneg(-123456789012345L, 123456789012345L);
*/
		return ok;
	}

	static boolean add(long a, long b, long exp) {

		return (a+b == exp);
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
}
