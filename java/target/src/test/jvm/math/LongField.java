package jvm.math;

import jvm.TestCase;

public class LongField extends TestCase {
	
	static final long C1 = 0x123456789abcdefL;
	static final long C2 = 0xcafe1122babe3344L;
	static final long C3 = 0x00000000ffffffffL;
	static final long C4 = 0x0000000100000000L;
	long a, b;
	
	public String getName() {
		return "Long Field";
	}
	
	public boolean test() {
	
		boolean ok = true;
		long l1, l2;

		a = C1;
		b = C2;
		l1 = C1;
		l2 = C2;
		
		ok = ok && a==C1;
		ok = ok && b==C2;
		ok = ok && l1==C1;
		ok = ok && l2==C2;
		
		ok = ok && a==l1;
		ok = ok && b==l2;
		
		a=1; b=2; l1=3; l2=4;
		
		ok = ok && a==1 && b==2 && l1==3 && l2==4;
		
		l1 = C3;
		l1 += 1;
		ok = ok && l1==C4 && l2==4;
		
		return ok;
	}

}
