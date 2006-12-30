/*
 * Created on 30.07.2005
 *
 */
package jvm.obj;

import jvm.TestCase;

/**
 * @author Martin Schoeberl (martin@jopdesign.com)
 *
 */
public class InstanceCheckcast extends TestCase {
	
	public String getName() {
		return "Instance Checkcast";
	}
	
	static class A {}
	
	static class B extends A {}
	
	static class C extends B {}
	
	static class X {}
	
	static class Y extends X {}
	
	static class Z extends X {}
	
	public boolean test() {
		
		boolean ok = true;
		
		A a = new A();
		B b = new B();
		C c = new C();
		X x = new X();
		Y y = new Y();
		Z z = new Z();
		Object o = new Object();
		
		
		
		ok = ok && a instanceof A;
		ok = ok && !(a instanceof B);
		ok = ok && b instanceof A;
		ok = ok && b instanceof B;
		ok = ok && !(b instanceof C);
		ok = ok && c instanceof A;
		ok = ok && c instanceof B;
		ok = ok && c instanceof C;
			
		ok = ok && a instanceof Object;
		ok = ok && o instanceof Object;
		o = this;
		ok = ok && !(o instanceof A);
		o = b;
		ok = ok && o instanceof A;
		ok = ok && o instanceof B;
		ok = ok && !(o instanceof C);
		ok = ok && !(o instanceof X);
		
		ok = ok && x instanceof X;
		ok = ok && !(x instanceof Y);
		ok = ok && y instanceof X;
		ok = ok && y instanceof Y;

		o = null;
		ok = ok && !(o instanceof Object);
		
		
		A sup = b;
		ok = ok && sup instanceof A;
		ok = ok && sup instanceof B;
		ok = ok && !(sup instanceof C);

//		b = (B) a; // exception
		
		a = b;
		b = (B) a; // no excpetion
		
		o = c;
		a = (A) o; // no exception
		a = (B) o; // no exception
		a = (C) o; // no exception
//		x = (Y) o; // exception
//		x = (X) o; // exception
//		y = (Y) o; // excpetion
		o = null;
		a = (A) o;
		b = (C) null;
		
		return ok;
	}

}
