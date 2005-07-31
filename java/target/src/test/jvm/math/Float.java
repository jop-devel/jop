package jvm.math;

import jvm.TestCase;


public class Float extends TestCase {

	public String getName() {
		return "Float";
	}
	
	public boolean test() {

		boolean ok = true;
		
		float f1 = 1.3F;
		float f2 = 2.9F;

		float f3 = f1+f2;

		int i = (int) f3;
		ok = ok && (i==4);
		

		f3 = f1-f2;
		i = (int) f3;
		ok = ok && (i==-1);

		return ok;
	}
}
