package vmtest;

import util.*;

public class Imul {

static long x(long a, long b) {

	return a-b;
}

	public static void main(String[] agrgs) {

		mul(1, 2, 2);
		mul(123, 456, 56088);
		mul(-1, 5, -5);
		mul(5, -1, -5);
		mul(-1, -1, 1);
		mul(0x80000000 ,0x80000000 , 0);
		mul(0xffffffff ,0x80000000 , 0x80000000);
		mul(0x80000000 ,0xffffffff , 0x80000000);
		mul(0x7fffffff ,0x80000000 , 0x80000000);
		mul(0x80000000 ,0x7fffffff , 0x80000000);
		mul(0x7fffffff ,0xffffffff , -2147483647);
		mul(0xffffffff ,0x7fffffff , -2147483647);
	}

	static void mul(int a, int b, int exp) {

		if (a*b == exp) {
			System.out.println("ok");
		} else {
			System.out.println("failed");
		}
	}

}
