/*
 * Created on 30.07.2005
 *
 */
package jvm;

import jvm.TestCase;

/**
 * @author Martin Schoeberl (martin@jopdesign.com)
 *
 */
public class InvokeSpecial extends TestCase {
	
    public String getName() {
	return "Invokespecial";
    }
	
    static class A {
	private int foo() {
	    return 1234;
	}
	public int bar() {
	    return foo();
	}
    }
    
    static class B extends A {
	public int foo() {
	    return 5678;
	}
	public int bar() {
	    return super.bar();
	}
    }

    static class C extends A {
	public int foo() {
	    return 5678;
	}
	public int bar() {
	    return super.bar();
	}
    }
    
    static class D extends B {
	public int bar() {
	    return super.foo();
	}
    }

    static class E extends B {
	public int bar() {
	    return super.foo();
	}
    }

    static class F extends E {
	public int bar() {
	    return super.bar();
	}
    }
    
    public boolean test() {
	
	boolean ok = true;
	
	A a = new A();
	A b = new B();
	A c = new C();
	A d = new D();
	A e = new E();
	A f = new F();

	ok = ok && (a.bar() == b.bar());
	ok = ok && (a.bar() == c.bar());
	ok = ok && (a.bar() != d.bar());
	ok = ok && (a.bar() != e.bar());
	ok = ok && (a.bar() != f.bar());
	ok = ok && (b.bar() == c.bar());
	ok = ok && (b.bar() != d.bar());
	ok = ok && (b.bar() != e.bar());
	ok = ok && (b.bar() != f.bar());
	ok = ok && (c.bar() != d.bar());
	ok = ok && (c.bar() != e.bar());
	ok = ok && (c.bar() != f.bar());
	ok = ok && (d.bar() == e.bar());
	ok = ok && (d.bar() == f.bar());
	ok = ok && (e.bar() == f.bar());
	
	return ok;
    }    
}
