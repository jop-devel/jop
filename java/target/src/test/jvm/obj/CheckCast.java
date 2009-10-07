/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2001-2008, Martin Schoeberl (martin@jopdesign.com)

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

/*
 * failes for interfaces
 */
package jvm.obj;

import jvm.TestCase;

/**
 * @author Martin Schoeberl (martin@jopdesign.com)
 *
 */
public class CheckCast extends TestCase implements Runnable {
	
	public String toString() {
		return "CheckCast";
	}
	
	static interface A extends sup {
		public String foo();
	}

	static interface sup {}

	static class B implements A {
		public String foo() {
			return "B";
		}
	}

	static class C implements A {
		public String foo() {
			return "C";
		}
	}

	static interface X extends sup {
		public String foo();
	}

	public boolean test() {
		
		boolean ok = true;
		boolean trycheck;
		
		Object o = new CheckCast();
		CheckCast cc;
		cc = (CheckCast) o;
		
		// Issue: JOP does not check interfaces on checkcast!
		//Runnable r = (Runnable) o;
		
		A a = new B();
		B b = new B();
		C c = new C();

		ok = ok && a instanceof sup;
		ok = ok && a instanceof A; 
		ok = ok && ((A)a).foo().equals("B");
		ok = ok && a instanceof B; 
		ok = ok && ((B)a).foo().equals("B");
		ok = ok && a instanceof Object;

		ok = ok && b instanceof sup;
		ok = ok && b instanceof A;
		ok = ok && ((A)b).foo().equals("B");
		ok = ok && b instanceof B;
		ok = ok && ((B)b).foo().equals("B");
		ok = ok && b instanceof Object;

		ok = ok && c instanceof sup;
		ok = ok && c instanceof A;
		ok = ok && ((A)c).foo().equals("C");
		ok = ok && c instanceof C;
		ok = ok && ((C)c).foo().equals("C");
		ok = ok && c instanceof Object;

		Object d = new B();
		Object e = new C();

		ok = ok && d instanceof sup;
		ok = ok && d instanceof A;
		ok = ok && ((A)d).foo().equals("B");
		ok = ok && d instanceof B;
		ok = ok && ((B)a).foo().equals("B");
		ok = ok && !(d instanceof C);
		trycheck = false;
		try {
			((C)d).foo();
		} catch (ClassCastException exc) {
			trycheck = true;
		}
		ok = ok && trycheck;
		ok = ok && !(d instanceof X);
		trycheck = false;
		try {
			((X)d).foo();
		} catch (ClassCastException exc) {
			trycheck = true;
		}
		ok = ok && trycheck;
		ok = ok && d instanceof Object;

		ok = ok && e instanceof sup;
		ok = ok && e instanceof A;
		ok = ok && ((A)e).foo().equals("C");
		ok = ok && !(e instanceof B);
		trycheck = false;
		try {
			((B)e).foo();
		} catch (ClassCastException exc) {
			trycheck = true;
		}
		ok = ok && trycheck;
		ok = ok && e instanceof C;
		ok = ok && ((C)e).foo().equals("C");
		ok = ok && !(e instanceof X);
		trycheck = false;
		try {
			((X)e).foo();
		} catch (ClassCastException exc) {
			trycheck = true;
		}
		ok = ok && trycheck;
		ok = ok && e instanceof Object;

		return ok;
	}



	public void run() {
		// just dummy to use an interface
	}

}
