package jvm;

public class Conversion extends TestCase {
	
	public String getName() {
		return "Conversion";
	}


	public boolean test() {

		boolean ok = true;
		
		int i;
		char c;
		short s;
		byte b;
		
		i = 123;
		b = (byte) i;
		ok = ok && (b==123);

		return ok;
	}

}
