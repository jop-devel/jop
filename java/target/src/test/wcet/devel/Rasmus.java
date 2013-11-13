/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright  (C) 2008, Rasmus Pedersen
             (C) 2011, Benedikt Huber (benedikt@vmars.tuwien.ac.at)

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

import com.jopdesign.sys.Config;
import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

public class Rasmus {
	/* Debugging signals to manipulate the cache */
	final static int CACHE_FLUSH = -51;
	final static int CACHE_DUMP = -53;
	static int ts, te, to;
	static A a;
	final static boolean MEASURE_CACHE = false;

	public static void main(String[] args) {

		ts = Native.rdMem(Const.IO_CNT);
		te = Native.rdMem(Const.IO_CNT);
		to = te-ts;
		
		int min = 0x7fffffff;
		int max = 0;
		int val = 0;
		
		for(int i = 0; i < 100; i++) {
			if((i&2) == 0) a = new A();
			else           a = new B();
			invoke();
			val = te-ts-to;
			if (val<min) min = val;
			if (val>max) max = val;
		}
		
		if (Config.MEASURE) { 
			System.out.print("min: "); System.out.println(min);
			System.out.print("max: "); System.out.println(max);
		}
	}

	static void invoke() {
		if (MEASURE_CACHE) Native.wrMem(1,CACHE_FLUSH);
		measure();
		if (Config.MEASURE) te = Native.rdMem(Const.IO_CNT);
		if (MEASURE_CACHE) Native.rdMem(CACHE_DUMP);
	}

	static void measure() {
    	if (Config.MEASURE) ts = Native.rdMem(Const.IO_CNT);
    	dowcet();
    	dowcet2();
	}

	static public int dowcet() {
		for(int j = 0; j < 5; j++) // @WCA loop=5
			bar();

		return 0;
	}

	static void bar() {
		int i = 0;
		i = i + 1;
	}
	
	static public int dowcet2() {
		bar(a);

		return 0;
	}

	static void bar(A a) {
		a.foo();
	}


	//a can now be an A or it can also be a B. So
	//either A.foo() or B.foo() is invoked. We don't know,
	//so we have to consider both possibilities for the WCET


	// from ms mail
	static class A {

	   void foo() {
	     int ia = 1;
	   }
	}

	static class B extends A {

	   // overwrites A.foo()
	   void foo() {
	     int ib = 3;
	     ib++;
	     ib--;
	   }

	}
}
