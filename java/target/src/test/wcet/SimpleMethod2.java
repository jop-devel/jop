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

import com.jopdesign.sys.*;

public class SimpleMethod2 {

	static int ts, te, to;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		ts = Native.rdMem(Const.IO_CNT);
		te = Native.rdMem(Const.IO_CNT);
		to = te-ts;
		// measurement + return takes 22+22+21=65 cycles
		// WCET measured: 211
		// WCET analysed: 278-65 = 213
		// diff is 2 cycles:
		//	    As the invokestatic of the native methods get substituted
		//	    by the special bytecode, the method length gets shorter (1 byte
		//	    for the special bytecode instead of 3 bytes for the
		//	    invokestatic). Therefore, foo() is now 5 words instead of the
		//	    original 6 words. That means the miss from the xxx() return is 2
		//	    cycles less then in the original, analyzed method.
		measure();
//		System.out.println(te-ts-to);
	}
	
	public static void measure() {
		ts = Native.rdMem(Const.IO_CNT);
		foo();
		te = Native.rdMem(Const.IO_CNT);		
	}
	
	static int foo() {
		xxx();
		return 123;
	}
	static int xxx() {
		return 456;
	}
	
}
