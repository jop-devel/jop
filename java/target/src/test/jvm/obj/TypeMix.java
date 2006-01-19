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
public class TypeMix extends TestCase {
	
	public String getName() {
		return "Object Type Mix";
	}
	
	
	public boolean test() {
		
		boolean ok = true;
		
		A a = new A(123);
		B b = new B(456);
		A sup = b;

		ok = ok && a.val==123;
		ok = ok && b.val==456;
		ok = ok && ((A) b).val==0;
		ok = ok && sup.val==0;
		/*- checkcast not implemented yet
		ok = ok && ((B) sup).val==456;
		*/

		return ok;
	}

	class A {
		int val;
		A(int v) {
			val = v;
		}
	}
	class B extends A {
		long val;
		B(int v) {
			super(0);
			val = v;
		}
	}
	
	public static void main(String args[]) {
		new TypeMix().test();
	}
}
