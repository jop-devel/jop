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
public class Basic2 extends TestCase {
	
	public String getName() {
		return "Object Basic2";
	}
	
	static class A {
		
		int a, b, c;
		
	}
	
	static class B extends A {
		
		int d, e;
	}
	
	
	public boolean test() {
		
		boolean ok = true;
		
		B b = new B();
		A sup = b;

		b.a = 1;
		b.b = 2;
		b.c = 3;
		b.d = 4;
		b.e = 5;

		ok = ok && (b.a==1); 
		ok = ok && (b.b==2); 
		ok = ok && (b.c==3);
		ok = ok && (b.d==4);
		ok = ok && (b.e==5);
		
		b.a = 11;
		b.b = 22;
		b.c = 33;
		
		ok = ok && (b.a==11); 
		ok = ok && (b.b==22); 
		ok = ok && (b.c==33);
		ok = ok && (b.d==4);
		ok = ok && (b.e==5);
		
		sup.a = 123;
		sup.b = 456;
		sup.c = 789;
		
		ok = ok && (b.a==123); 
		ok = ok && (b.b==456); 
		ok = ok && (b.c==789);
		ok = ok && (b.d==4);
		ok = ok && (b.e==5);
		
		return ok;
	}

}
