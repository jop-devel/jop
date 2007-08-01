/*
 * failes for interfaces
 */
package jvm.obj;

import jvm.TestCase;

/**
 * @author Martin Schoeberl (martin@jopdesign.com)
 *
 */
public class CheckCast extends TestCase implements Runnable {
	
	public String getName() {
		return "CheckCast";
	}
	
	static interface A extends sup{}

	static interface sup {}

	static class B implements A {}

	static class C implements A {}
	
	public boolean test() {
		
		boolean ok = true;
		
		Object o = new CheckCast();
		CheckCast cc;
		cc = (CheckCast) o;
		
		// Issue: JOP does not check interfaces on checkcast!
		//Runnable r = (Runnable) o;
		
		A a = new B();
		B b = new B();
		C c = new C();

		ok = ok && b instanceof A;
		ok = ok && c instanceof A;
		ok = ok && a instanceof Object;
	
		ok = ok && a instanceof A; 
		ok = ok && a instanceof sup; 
		return ok;
	}



	public void run() {
		// just dummy to use an interface
	}

}
