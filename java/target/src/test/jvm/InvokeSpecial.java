/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2008, Wolfgang Puffitsch

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package jvm;

import jvm.TestCase;

/**
 * @author Wolfgang Puffitsch
 *
 */
public class InvokeSpecial extends TestCase {
	
    public String toString() {
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
