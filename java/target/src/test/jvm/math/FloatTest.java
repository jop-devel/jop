package jvm.math;

import jvm.TestCase;


public class FloatTest extends TestCase {

	public String getName() {
		return "FloatTest";
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

		f1 = 0F;
		f2 = 1F;
		f3 = 2F;
		
		i = (int) (f1+f2+f3);
		ok = ok && (i==3);
		


		return ok;
	}
}
