/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2008, Martin Schoeberl (martin@jopdesign.com)

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
package rttm.jsim;

import com.jopdesign.io.IOFactory;
import com.jopdesign.io.SysDevice;
import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;
import com.jopdesign.sys.Startup;

/**
 * each core reads the current global increment value and tries to write 
 * the sum of its own id and this value into a global array.
 * additionally each core saves the array indices it has written to.
 * after the array has been filled, each core indice list is compared to
 * the real written values and any inconsistency is reported
 *    
 * @author michael muck
 * simple increment test
 */
public class IncrementTest {
	
	static SysDevice sys = IOFactory.getFactory().getSysDevice();
	
	static final int SIZE = 1000;
	static final int EMPTY = -1;
	
	static int[] ia = new int[SIZE];

	static int incrementVar = 0;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		Incrementer r[] = new Incrementer[sys.nrCpu];
		for (int i=0; i<sys.nrCpu; ++i) {
			if(i<sys.nrCpu-1) {
				r[i] = new Incrementer(i+1);
				Startup.setRunnable(r[i], i);
			}
			else {
				r[i] = new Incrementer(0);
			}
		}			

		// reset array
		for(int i=0; i<SIZE; ++i) {
			ia[i] = EMPTY;
		}
		
		// time measurement
		int startTime, endTime;
		startTime = Native.rd(Const.IO_US_CNT);		
		
		// start the other CPUs
		sys.signal = 1;
		
		// start my work
		r[sys.nrCpu-1].run();

		// wait for other CPUs to finish
		boolean allDone = false;
		while (!allDone) {
			allDone = true;
			for (int i=0; i<sys.nrCpu-1; ++i) {
				allDone &= r[i].finished;
			}			
		}
		
		endTime = Native.rd(Const.IO_US_CNT);
		System.out.print("Time: ");
		System.out.print(endTime-startTime);
		System.out.println("\n");
		
		// verify
		for(int i=0; i<sys.nrCpu; ++i) {
			r[i].verify();
		}
		
		// print array
		System.out.println("Raw output: ");
		String space = " ";
		for(int i=0; i<SIZE; ++i) {
			System.out.print(ia[i]);
			System.out.print(space);
		}
		
		// wipe off cpuid from the numbers
		for(int i=0; i<sys.nrCpu; ++i) {			
			r[i].clean();
		}
		
		// print array
		System.out.println("\nWithout cpuid: ");
		for(int i=0; i<SIZE; ++i) {
			System.out.print(ia[i]);
			System.out.print(space);
		}
		
	}
	
	static class Incrementer implements Runnable {

		public boolean finished;
		
		private static final int MAGIC = -10000;
		
		private boolean[] writtenPosition = new boolean[SIZE];	
		private int cpuid;
		
		public Incrementer(int id) {
			cpuid = id;
			
			for(int i=0; i<SIZE; ++i) {
				writtenPosition[i] = false;
			}
		}
		
		public void run() {	
			boolean ok = true;
			
			while(ok) {							
				Native.wrMem(1, MAGIC);	// start transaction
					
					if(incrementVar < SIZE) {
						if(ia[incrementVar] == EMPTY) {
							ia[incrementVar] = incrementVar + cpuid;
							writtenPosition[incrementVar] = true;
							
							incrementVar++;					
						}
					}
					else { 
						ok = false;
					}					
				
				Native.wrMem(0, MAGIC);	// end transaction
			}
			
			finished = true;
		}
	
		public void verify() {
			boolean good = true;
			for(int i=0; i<SIZE; ++i) {
				if(writtenPosition[i] == true) {
					if(ia[i]-i != cpuid) {
						System.out.println("Read/Write error at position " + i + " from cpu" + cpuid);
						good = false;
					}
				}
			}
			if(good) {
				System.out.println("cpu" + cpuid + ": verify ok!");
			}
		}

		public void clean() {
			for(int i=0; i<SIZE; ++i) {
				if(writtenPosition[i] == true) {
					if(ia[i]-i == cpuid) {
						// remove cpuid from numbers
						ia[i] = ia[i] - cpuid;
					}
				}
			}
		}
		
	}

}
