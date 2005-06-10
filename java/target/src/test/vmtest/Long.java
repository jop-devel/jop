package vmtest;

import util.Dbg;

public class Long {

static long x(long a, long b) {

	return a-b;
}

	public static void main(String[] agrgs) {

		Dbg.initSerWait();				// use serial line for debug output

		Dbg.wr("Long Test\n");

		long l1, l2;
		
//		double d = 0.1;

		l1 = 1;
		l2 = 2;


		add(1, 2, 3);
		add(1L, 2L, 3L);
		add(-3, 5, 2);
		add(5, -1, 4);
		add(-3, -5, -8);
		add(0x80000000L ,0x80000000L , 0x100000000L);
		add(0xffffffffL ,0x00000001L , 0x100000000L);

		lcmp(1, 2, -1);
		lcmp(3, 2, 1);
		lcmp(123, 123, 0);
		lcmp(123456789012345L, 123456789012345L, 0);
		lcmp(123456789012347L, 123456789012345L, 1);
		lcmp(123456789012347L, 123456789012567L, -1);
		lcmp(2, -1, 1);
		lcmp(-287346, 1, -1);
		lcmp(-87245983472L, -1, -1);
		lcmp(-87245983472L, 871234619831L, -1);

/*
		i2l(1, 1L);
		i2l(-1, -1L);
		i2l(0x80000000, 0xffffffff80000000L);
		i2l(-123, -123L);
		i2l(0x7fffffff, 0x7fffffffL);
		lneg(1L, -1L);
		lneg(2L, -2L);
		lneg(123, -123);
		lneg(-1L, 1L);
		lneg(-2L, 2L);
		lneg(-123, 123);
		lneg(123456789012345L, -123456789012345L);
		lneg(-123456789012345L, 123456789012345L);
*/
	}

	static void add(long a, long b, long exp) {

		if (a+b == exp) {
			Dbg.wr("ok");
		} else {
			Dbg.wr("failed");
		}
		Dbg.lf();
	}

	static void lcmp(long a, long b, int exp) {

		int res = 0;
		if (a > b) {
			res = 1;
		} else if (a < b) {
			res = -1;
		} else if (a == b) {
			res = 0;
		}
		if (res == exp) {
			Dbg.wr("ok");
		} else {
			Dbg.wr("failed");
		}
		Dbg.lf();
	}

	static void i2l(int val, long exp) {

		long res;
		res = val;
		if (res == exp) {
			Dbg.wr("ok");
		} else {
			Dbg.wr("failed");
		}
		Dbg.lf();
	}
	static void lneg(long val, long exp) {

		long res;
		res = -val;
		if (res == exp) {
			Dbg.wr("ok");
		} else {
			Dbg.wr("failed");
		}
		Dbg.lf();
	}
}
