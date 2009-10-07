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
 * Created on 30.07.2005
 *
 */
package jvm.obj;

import jvm.TestCase;

/**
 * @author Martin Schoeberl (martin@jopdesign.com)
 *
 */
public class InstanceCheckcast extends TestCase {
	
	public String toString() {
		return "Instance Checkcast";
	}
	
	static class A {}
	
	static class B extends A {}
	
	static class C extends B {}
	
	static class X {}
	
	static class Y extends X {}
	
	static class Z extends X {}
	
	public boolean test() {
		
		boolean ok = true;
		
		A a = new A();
		B b = new B();
		C c = new C();
		X x = new X();
		Y y = new Y();
		Z z = new Z();
		Object o = new Object();
		
		
		
		ok = ok && a instanceof A;
		ok = ok && !(a instanceof B);
		ok = ok && b instanceof A;
		ok = ok && b instanceof B;
		ok = ok && !(b instanceof C);
		ok = ok && c instanceof A;
		ok = ok && c instanceof B;
		ok = ok && c instanceof C;
			
		ok = ok && a instanceof Object;
		ok = ok && o instanceof Object;
		o = this;
		ok = ok && !(o instanceof A);
		o = b;
		ok = ok && o instanceof A;
		ok = ok && o instanceof B;
		ok = ok && !(o instanceof C);
		ok = ok && !(o instanceof X);
		
		ok = ok && x instanceof X;
		ok = ok && !(x instanceof Y);
		ok = ok && y instanceof X;
		ok = ok && y instanceof Y;

		o = null;
		ok = ok && !(o instanceof Object);
		
		
		A sup = b;
		ok = ok && sup instanceof A;
		ok = ok && sup instanceof B;
		ok = ok && !(sup instanceof C);

//		b = (B) a; // exception
		
		a = b;
		b = (B) a; // no excpetion
		
		o = c;
		a = (A) o; // no exception
		a = (B) o; // no exception
		a = (C) o; // no exception
//		x = (Y) o; // exception
//		x = (X) o; // exception
//		y = (Y) o; // excpetion
		o = null;
		a = (A) o;
		b = (C) null;
		
		return ok;
	}

}
