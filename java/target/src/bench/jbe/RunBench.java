/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2005-2008, Martin Schoeberl (martin@jopdesign.com)

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


/**
 * 
 */
package jbe;

/**
 * Run a single benchmark for a constant iteration count.
 * @author martin
 *
 */
public class RunBench {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

//		foo();
		

		BenchMark bm[] = {
			new BenchKfl(),
			new BenchUdpIp(),
			new BenchLift()				
		};
		
		for (int i=0; i<bm.length; ++i) {
			// remove the following to avoid initializing the JDK
			System.out.print("Benchmark ");
			System.out.print(bm[i].toString());
			System.out.println(" started");
			
			// start measurement here
			int t1 = (int) System.currentTimeMillis();
//			int cyc1 = com.jopdesign.sys.Native.rdMem(com.jopdesign.sys.Const.IO_CNT);
			bm[i].test(10000);
//			bm[i].test(10);
			// stop measurement here
//			int cyc2 = com.jopdesign.sys.Native.rdMem(com.jopdesign.sys.Const.IO_CNT);
			int t2 = (int) System.currentTimeMillis();
//			System.out.print("cycles: ");
//			System.out.println(cyc2-cyc1);
			System.out.print("ms: ");
			System.out.println(t2-t1);			
		}

	}
	
	
	
	static void foo() {
		System.out.println("Hello foo");
	}

}
