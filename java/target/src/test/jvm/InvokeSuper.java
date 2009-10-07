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
 * @author Stefan Resch, Wolfgang Puffitsch
 *
 */
public class InvokeSuper extends TestCase {

	public String toString() {
		return "Invokesuper";
	}

	class A {
		int exec1() {
			return 1;
		}
		int exec2() {
			return 1;
		}
	}
	
	
	class B extends A {
		int exec2() {
			return 10 + super.exec2();
		}
	}
	
	
	class C extends B {
		int exec1() {
			return 100 + super.exec1();
		}
	}
	
	class D extends C {
		int exec1() {
			return 1000 + super.exec1();
		}
		int exec2() {
			return 1000 + super.exec2();
		}
	}
	
	class E extends D {
	}
	
	static int e1;
	static int e2;

	public boolean test() {
	
		boolean ok = true;

		A ac = new A();
		B bc = new B();
		C cc = new C();
		D dc = new D();
		E ec = new E();

		ac.exec1();

		ok = ok && (ac.exec1() == 1);
		ok = ok && (bc.exec1() == 1);
		ok = ok && (cc.exec1() == 101);
		ok = ok && (dc.exec1() == 1101);
		ok = ok && (ec.exec1() == 1101);

		ok = ok && (ac.exec2() == 1);
		ok = ok && (bc.exec2() == 11);
		ok = ok && (cc.exec2() == 11);
		ok = ok && (dc.exec2() == 1011);
		ok = ok && (ec.exec2() == 1011);

		return ok;
	}

}