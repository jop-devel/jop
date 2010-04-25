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
package rttm.jsim;

import joprt.RtThread;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.RtThreadImpl;
import com.jopdesign.sys.Startup;
import com.jopdesign.io.IOFactory;
import com.jopdesign.io.SysDevice;
import com.jopdesign.sys.Native;

/**
 * An Example on how to use TM with Sorting
 * 
 * Overlap Sort takes an Array and splits it (virtually) 
 * into sys.nrCpu subarrays with size n/(sys.nrCpu-1)
 * all subarrays are now sorted by its core
 * and at the end the whole array is sorted
 * 
 * @author michael muck
 *
 */
public class RefBubbleSort {
	private static final int MAGIC = -10000;	
	
	// read a random number from IO
	private static final int IO_RAND = Const.IO_CPUCNT+1;	// its likely that this var needs to be changed!
	// read a positive random number from IO
	private static final int IO_PRAND = IO_RAND+1;	
	
	static SysDevice sys = IOFactory.getFactory().getSysDevice();
	
	static final int SIZE = 1000;	
	static final int MAX_NUMBER = 1000;
	static int[] array = new int[SIZE];
	
	static int sortstate = 0;	// 0 means unsorted, sys.nrCpu-1 = sorted
	
	static boolean end = false;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// fill array with random vars
		for(int x=0; x<SIZE; ++x) {
			array[x] = Native.rdMem(IO_PRAND)%MAX_NUMBER;
		}

		BubbleSorter bs = new BubbleSorter();
		
		// print the whole array
		printArray();
				
		// measure time
		int startTime, endTime;
		startTime = Native.rd(Const.IO_US_CNT);	
		
		// start the other CPUs
		sys.signal = 1;

		bs.run();

		endTime = Native.rd(Const.IO_US_CNT);	
		
		System.out.print("Time: ");
		System.out.print(endTime-startTime);
		System.out.println("\n");
		
		// print the whole array
		printArray();
		
		// check the sort
		checkArray();
		
		// write the magic string to stop the simulation
		System.out.println("\r\nJVM exit!\r\n");

		System.exit(0);
		
	}

	private static void printArray() {		
		System.out.println("\n--ARRAY STAT--");	
		for(int i=0; i<SIZE; ++i) {
			System.out.print(array[i]);
			System.out.print(" ");
		}
		System.out.println("\n--------------");
	}
	
	private static void checkArray() {
		int errors = 0;
		
		System.out.println("\n--ARRAY CHECK--");
		for(int i=0; i+1<SIZE; ++i) {
			if(array[i] > array[i+1]) {
				System.out.print("EE: ");
				System.out.print(array[i]);
				System.out.print(" greater than ");
				System.out.println(array[i+1]);
				errors++;
			}
		}
		if(errors == 0) {
			System.out.println("Sort Algorithm correct!");
		}
		System.out.println("---------------");
	}
	
	static class BubbleSorter implements Runnable {	
		
		public BubbleSorter() {
		}
		
		public void run() {
			int tmp;
			boolean sorted = false;
			
			while(sorted == false) {
				sorted = true;
				
				for(int i=0; i<SIZE-1; ++i) {
					if(array[i] > array[i+1]) { 
						tmp = array[i];
						array[i] = array[i+1];
						array[i+1] = tmp;
						
						sorted = false;
					}	
				}
			}
		}		
		
	}
	

}
