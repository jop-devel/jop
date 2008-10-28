package com.jopdesign.dfa.testdata;

public class SimpleTest {

	public static void main(String [] args) {
		SimpleTest s = new SimpleTest();
		s.test(1);
	}
	
	A t;
	static Object o;
	
	public void test(int i) {
		if ((i & 1) != 0) {
			t = new A();
		} else {
			t = new B();
		}
		o = t.foo();
		t = new C();	
	}
	
}

class A {
	Object foo() {
		return new X1();
	}
}

class B extends A {
	Object foo() {
		return new X1();
	}
}

class C extends B {
	Object foo() {
		return new X2();
	}
}

class X1 {
}

class X2 {
}
