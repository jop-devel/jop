
package jvm.obj;

import jvm.TestCase;

/**
 * Test <clinit> order
 * 
 * @author Martin Schoeberl (martin@jopdesign.com)
 *
 */
public class Clinit2 extends TestCase {

	
	static int abc = 123;

	static {
		abc = 456;
		System.out.println("clinit2");
		// the X static initializer runns later
		new X();
	}
	
	public String getName() {
		return "Clinit2";
	}

	public boolean test() {		

		boolean ok = true;
		if (abc!=789) ok = false;


		return ok;

	}
	
	public static void main(String[] args) {
		
		Clinit2 clinit = new Clinit2();
		new X();
		
		System.out.print(clinit.getName());
		if (clinit.test()) {
			System.out.println(" ok");
		} else {
			System.out.println(" failed!");
		}
	}
}

class X {
	
	static {
		Clinit2.abc = 789;
		System.out.println("X");
	}
}
