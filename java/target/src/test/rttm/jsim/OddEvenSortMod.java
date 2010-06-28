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

import java.util.Random;

import com.jopdesign.sys.Startup;
import com.jopdesign.io.IOFactory;
import com.jopdesign.io.SysDevice;
import com.jopdesign.sys.JVMHelp;
import com.jopdesign.sys.Native;
import com.jopdesign.sys.Const;

/**
 * An Example on how to use TM with Sorting
 * 
 * @author michael muck
 *
 */
public class OddEvenSortMod {
	private static final int MAGIC = -10000;	
	
	static SysDevice sys = IOFactory.getFactory().getSysDevice();
	
	static Random rn = new Random();
	
	static final int SIZE = 100;
	static int[] array = new int[SIZE];
	
	static boolean end = false;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		// check cpu cnt - minimum of 2 cpus
		if (sys.nrCpu<2) {
			System.out.println("Not enogh CPUs for this example");
			System.exit(-1);
		}
		
		// fill array with random vars
		for(int x=0; x<SIZE; ++x) {
			array[x] = rn.nextInt()%1000;
			if(array[x] < 0) { array[x] *= -1; }
		}
		
		// setup sorters
		Sorter s;
		for(int i=0; i<sys.nrCpu-1; ++i) {
			s = new Sorter();
			Startup.setRunnable(s, i);
		}
		
		// print the whole array
		printArray();
				
		// start the other CPUs
		sys.signal = 1;
		
		// start my work - array sort check
		boolean unsorted = true;
		while(unsorted) {
			unsorted = false;
			for(int i=0; i+1<SIZE; ++i) {
				Native.wrMem(1, MAGIC);
					if(array[i] > array[i+1]) {
						unsorted = true;
					}
				Native.wrMem(0, MAGIC);
			}			
		}
		end = true;

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
				System.out.print(array[i+1]);
				errors++;
			}
		}
		if(errors == 0) {
			System.out.println("Sort Algorithm correct!");
		}
		System.out.println("---------------");
	}
	
	static class Sorter implements Runnable {
	
		public Sorter() {
			
		}

		public void run() {
			while(!end) {
				sortArray();
			}
		}
		
		private void sortArray() {
			for(int i=0; i+1<SIZE; i+=2) {
				Native.wrMem(1, MAGIC);
					if(array[i] > array[i+1]) { 
						swap(i, i+1); 
					}				   
				Native.wrMem(0, MAGIC);
			}			
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for(int i=1; i+1<SIZE; i+=2) {
				Native.wrMem(1, MAGIC);
					if(array[i] > array[i+1]) { 
						swap(i, i+1); 
					}				   
				Native.wrMem(0, MAGIC);
			}
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		private void swap(int x1, int x2) { 
			int a = array[x1];
			array[x1] = array[x2];
			array[x2] = a;
		}
		
	}
	

}
