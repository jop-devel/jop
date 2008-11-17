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

package wcet;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

/**
 * We test dynamic dispatch for interfaces and classes here.
 * Interface: I { b } 
 * Classes: abstract A { a }, X { b }, abstract S extends A { a }, T extends S { a, b }, U extends S {  }, W extends U { a,b }
 *  (1) Receiver: U, Method a : Either S_a or W_a* is called, but not T_a**
 *  (2) Receiver: A, Method b : Either T_b or W_b* is called, but not X_b**
 *  (3) Receiver: I, Method b : One implementation of b is called (X_b**, S_b* or W_b)
 */ 
public class DispatchI {

	static int ts, te, to;

	public static void main(String[] args) {
		ts = Native.rdMem(Const.IO_CNT);
		te = Native.rdMem(Const.IO_CNT);
		to = te-ts;
		X x = new X();
		T t = new T();
		U u = new U();
		W w = new W();
		U[] args1 = { u, w };
		A[] args2 = { t,u,w };
		I[] args3 = { t,u,w,x };
		for(int i = 0; i < args1.length; i ++) {
    		for(int j = 0; j < args2.length; j ++) {		    
        		for(int k = 0; k < args3.length; k ++) {
		            measure(args1[i],args2[j],args3[k]);
		            System.out.println(te-ts-to);
        		}	    
    		}
		}
	}

	static void measure(U u, A a, I i) {
		ts = Native.rdMem(Const.IO_CNT);
		u.a();
		a.b();
		i.b();
		te = Native.rdMem(Const.IO_CNT);
	}
	interface I {
	    void b();
	}
	static abstract class A implements I {
	    abstract void a();
	}
	static class X implements I {
	    public void b() {
			int val = 123;
			for (int i=0; i<1000; ++i) { // @WCA loop=1000
				val *= i;
				val *= i;
			}
		}	    	    
	}
	abstract static class S extends A {
	    public void a() {
			int val = 123;
			for (int i=0; i<10; ++i) { // @WCA loop=10
				val *= i;
			}
		}
	}
	static class T extends A implements I {
	    public void a() {
			int val = 123;
			for (int i=0; i<1000; ++i) { // @WCA loop=1000
				val *= i;
			}
		}
	    public void b() {
			int val = 123;
			for (int i=0; i<10; ++i) { // @WCA loop=10
				val *= i;
				val *= i;
			}
		}	    
	} 
	static class U extends S implements I {
	    public void b() {
			int val = 123;
			for (int i=0; i<10; ++i) { // @WCA loop=10
				val *= i;
				val *= i;
			}
		}	    
	}
	static class W extends U {
	    public void a() {
			int val = 123;
			for (int i=0; i<100; ++i) { // @WCA loop=100
				val *= i;
			}
		}
	    public void b() {
			int val = 123;
			for (int i=0; i<100; ++i) { // @WCA loop=100
				val *= i;
				val *= i;
			}
		}	    
	}
}
