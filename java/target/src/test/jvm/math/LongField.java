package jvm.math;

import jvm.TestCase;

public class LongField extends TestCase {

	
//	static final long C1 = 0x123456789abcdefL;
//	static final long C2 = 0xcafe1122babe3344L;
	static final long C1 = 1L;
	static final long C2 = 2L;
	static final long C3 = 0x00000000ffffffffL;
	static final long C4 = 0x0000000100000000L;
	long a, b;
	static long sa, sb;
	
	public String getName() {
		return "Long Field";
	}
	
	public boolean test() {
	
		boolean ok = true;
		long l1, l2;

		a = C1;
		b = C2;
		sa = C1;
		sb = C2;
		l1 = C1;
		l2 = C2;
				
		ok = ok && a==C1;
		ok = ok && b==C2;
		ok = ok && sa==C1;
		ok = ok && sb==C2;
		ok = ok && l1==C1;
		ok = ok && l2==C2;
		
		ok = ok && a==l1;
		ok = ok && b==l2;
		ok = ok && sa==l1;
		ok = ok && sb==l2;
		
		a=1; b=2; l1=3; l2=4; sa=5; sb=6;
		
		ok = ok && a==1 && b==2 && l1==3 && l2==4 && sa==5 && sb==6;
		
		l1 = C3;
		l1 += 1;
		ok = ok && l1==C4 && l2==4;
		a = C3;
		a += 1;
		ok = ok && a==C4 && b==2;
		sa = C3;
		sa += 1;
		ok = ok && sa==C4 && sb==6;

		return ok;
	}

}
