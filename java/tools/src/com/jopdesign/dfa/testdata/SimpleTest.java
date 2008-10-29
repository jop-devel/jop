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
