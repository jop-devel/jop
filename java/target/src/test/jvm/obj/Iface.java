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
public class Iface extends TestCase {
	
	public String toString() {
		return "Interface";
	}
	
	static interface A {
		int x = 10;		// is implicit final
		int funca();
	}
	
	static interface B {
		
		int x = 20;		// is implicit final
		int funcb();
	}
	static interface C extends  A, B {
		int funcc();
	}
	
	static class D implements C {

		public int funcc() {
			return 3;
		}

		public int funca() {
			return 1;
		}

		public int funcb() {
			return 2;
		}
		
	}

	static class E extends D {

		public int funca() {
			return 111;
		}
	}

	static class F extends E {		

		public int funca() {
			return 1111;
		}
	}

	static class X implements A {

		public int funca() {
			return 11;
		}
	}
	
	static class Y implements B {

		public int funcb() {
			return 22;
		}
	}
	
	static class Z implements A, B {

		public int funca() {
			return 31;
		}

		public int funcb() {
			// TODO Auto-generated method stub
			return 32;
		}

		
	}
	public boolean test() {
		
		boolean ok = true;
		
		D d = new D();
		E e = new E();
		F f = new F();
		X x = new X();
		Y y = new Y();
		Z z = new Z();
			
		ok = ok && (d.funca()==1);
		ok = ok && (d.funcb()==2);
		ok = ok && (d.funcc()==3);
		ok = ok && (e.funca()==111);
		ok = ok && (e.funcb()==2);
		ok = ok && (e.funcc()==3);
		ok = ok && (f.funca()==1111);
		ok = ok && (f.funcb()==2);
		ok = ok && (f.funcc()==3);
		ok = ok && (x.funca()==11);
		ok = ok && (y.funcb()==22);
		ok = ok && (z.funca()==31);
		ok = ok && (z.funcb()==32);

		A a = d;
		B b = d;
		C c = d;
		ok = ok && (c.funca()==1);
		ok = ok && (c.funcb()==2);
		ok = ok && (c.funcc()==3);
		ok = ok && (a.funca()==1);
		ok = ok && (b.funcb()==2);

		a = e;
		b = e;
		c = e;
		ok = ok && (c.funca()==111);
		ok = ok && (c.funcb()==2);
		ok = ok && (c.funcc()==3);
		ok = ok && (a.funca()==111);
		ok = ok && (b.funcb()==2);

		a = f;
		b = f;
		c = f;
		ok = ok && (c.funca()==1111);
		ok = ok && (c.funcb()==2);
		ok = ok && (c.funcc()==3);
		ok = ok && (a.funca()==1111);
		ok = ok && (b.funcb()==2);

		a = x;
		b = y;
		ok = ok && (a.funca()==11);
		ok = ok && (b.funcb()==22);

		a = z;
		b = z;
		ok = ok && (a.funca()==31);
		ok = ok && (b.funcb()==32);
		
		
		return ok;
	}

}
