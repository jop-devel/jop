package jvm.math;

import jvm.TestCase;


public class Imul extends TestCase {
	
	public String getName() {
		return "Imul";
	}
	
	public boolean test() {

		boolean ok = true;
		
		ok = ok && mul(1, 2, 2);
		ok = ok && mul(123, 456, 56088);
		ok = ok && mul(-1, 5, -5);
		ok = ok && mul(5, -1, -5);
		ok = ok && mul(-1, -1, 1);
		ok = ok && mul(0x80000000 ,0x80000000 , 0);
		ok = ok && mul(0xffffffff ,0x80000000 , 0x80000000);
		ok = ok && mul(0x80000000 ,0xffffffff , 0x80000000);
		ok = ok && mul(0x7fffffff ,0x80000000 , 0x80000000);
		ok = ok && mul(0x80000000 ,0x7fffffff , 0x80000000);
		ok = ok && mul(0x7fffffff ,0xffffffff , -2147483647);
		ok = ok && mul(0xffffffff ,0x7fffffff , -2147483647);
		
		return ok;
	}

	static boolean mul(int a, int b, int exp) {

		if (a*b == exp) {
			return true;
		} else {
			return false;
		}
	}

}
