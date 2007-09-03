package jvm;

public class Conversion  extends TestCase {
	
	public String getName() {
		return "Conversion";
	}


	public boolean test() {

		boolean ok = true;
		
		int i;
		
		char c;
		short s;
		long l;
		byte b;
		
		i = 123;
		
		b = (byte) i;
		c = (char) i;
		s = (short) i;
		l = (long) i;
		i = (int) l;
		
		ok = ok && (b==123);
		ok = ok && (c=='{');
		ok = ok && (s==123);
		ok = ok && (l==123);
		ok = ok && (i==123);
		
		return ok;
	}

}
