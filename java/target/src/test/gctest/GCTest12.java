/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2007, Alberto Andreotti

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

package gctest;

public class GCTest12 {

	static GcTClassA sa;
	static GcTClassB sb;
	static GcTClassC sc;
	
	GcTClassA a;
	GcTClassB b;
	GcTClassC c;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		GcTClassA la = new GcTClassA();
		GcTClassB lb = new GcTClassB();
		GcTClassC lc = new GcTClassC();
		
		sa = new GcTClassA();
		sb = new GcTClassB();
		sc = new GcTClassC();
		
		GCTest12 gct = new GCTest12();
		gct.a = new GcTClassA();
		gct.b = new GcTClassB();
		gct.c = new GcTClassC();
		
		if (la.foo()!=1) fail("1");
		if (lb.fee(123)!=2) fail("2");
		if (lc.fum("abc", 456)!=3) fail("3");
		gct.testOther();
		for (int i=0; i<100000; ++i) {
			new GCTest12();
		}
		if (la.foo()!=1) fail("a");
		if (lb.fee(123)!=2) fail("b");
		if (lc.fum("abc", 456)!=3) fail("c");
		gct.testOther();
		System.out.println("Test passed");
	}

	private void testOther() {
		if (sa.foo()!=1) fail("4");
		if (sb.fee(123)!=2) fail("5");
		if (sc.fum("abc", 456)!=3) fail("6");
		if (a.foo()!=1) fail("7");
		if (b.fee(123)!=2) fail("8");
		if (c.fum("abc", 456)!=3) fail("9");
	}

	static void fail(String s) {
		System.out.println("Test failed "+s);
		System.exit(0);
	}
}

class GcTClassA {
	
	int foo() {
		return 1;
	}
}

class GcTClassB {
	
	int fee(int abc) {
		return 2;
	}
}

class GcTClassC {
	
	int fum(String s, int x) {
		return 3;
	}
}