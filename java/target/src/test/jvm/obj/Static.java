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
public class Static extends TestCase {
	
	public String getName() {
		return "Object Static";
	}
	
	static class A {
		
		static int a, b;
		
		void setVals() {
			a = 1;
			b = 2;
		}
		
		void setOtherVals() {
			a = 123;
			b = 456;
		}
	}
	
	static class B extends A {
		
		static int b, c;
		
		void setVals() {
			a = 11;
			b = 22;
			c = 33;
		}
		
		void setOtherVals() {
			a = 111;
			b = 222;
			c = 333;
		}
	}
	
	void invokeOther(A val) {
		val.setOtherVals();
	}
	
	public boolean test() {
		
		boolean ok = true;
		
		A a = new A();
		B b = new B();
		A sup = b;

		a.setVals();
		b.setVals();
		ok = ok && (a.a==11); 
		ok = ok && (a.b==2); 
		ok = ok && (b.a==11); 
		ok = ok && (b.b==22); 
		ok = ok && (b.c==33);
		
		invokeOther(a);
		invokeOther(b);
		
		ok = ok && (a.a==111); 
		ok = ok && (a.b==456); 
		ok = ok && (b.a==111); 
		ok = ok && (b.b==222); 
		ok = ok && (b.c==333);
		
		b.setVals();
		ok = ok && (b.a==11); 
		ok = ok && (b.b==22); 
		ok = ok && (b.c==33);
		sup.setVals();
		ok = ok && (b.a==11); 
		ok = ok && (b.b==22); 
		ok = ok && (b.c==33);
//		System.out.println(a.a+" "+a.b+" "+b.a+" "+b.b+" "+b.c);
		
		invokeOther(b);
		sup.a = 4;
		sup.b = 5;
		ok = ok && (b.a==4); 
		ok = ok && (b.b==222); 
		ok = ok && (b.c==333);
		ok = ok && (sup.a==4); 
		ok = ok && (sup.b==5); 
		
		invokeOther(b);
		ok = ok && (b.a==111); 
		ok = ok && (b.b==222); 
		ok = ok && (b.c==333);
		ok = ok && (sup.a==111); 
		ok = ok && (sup.b==5); 
		
		return ok;
	}

}
