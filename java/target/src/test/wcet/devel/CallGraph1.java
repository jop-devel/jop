/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2011, Benedikt Huber (benedikt@vmars.tuwien.ac.at)

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

package wcet.devel;

/**
 * Purpose: Simple (almost no code, single-path) test for callgraph and supergraph construction
 * @author Benedikt Huber (benedikt@vmars.tuwien.ac.at)
 *
 */
public class CallGraph1 {
	interface I {
		public int i();
	}
	static class A implements I {
		public int a() { return 3000 + this.b() + this.i();  }
		public int b() { return  300  + this.c1();  }
		public int c1() { return  30  + this.i() + 2 * this.i(); }
		public int i() { return    3;  }
	}
	static class B extends A implements I {
		public int a() { return  4000 + this.b() + this.i();  }
		public int b() { return   400 + this.c2();  }
		public int c2() { return   40 + this.i() +  2 * this.i(); }
		public int i() { return     4;  }
	}
	public static void main(String[] argv) {
		A a = new A();
		B b = new B();
		run(a,b);
	}
	private static int run(A a, B b) {
		return a.a() + b.a();
	}
}
