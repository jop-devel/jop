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
package rttm.jsim_to_implementation;

import java.util.Random;

import com.jopdesign.sys.Const;
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
 * @author Peter Hilber
 * @author michael muck
 *
 */
public class OverlapSort {
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
		if(sys.nrCpu%2 != 0) {
			System.out.println("Must be used with an even number of Processors!");
			System.exit(0);
		}
		
		Random r = new Random();
		
		// fill array with random vars
		for(int x=0; x<SIZE; ++x) {
			array[x] = r.nextInt(MAX_NUMBER);
		}
		
		// setup sorters
		OverlapSorter s[] = new OverlapSorter[sys.nrCpu-1];
		int k1, k2, kb, d;
		
		// regular sorters -> (CORES+1)/2
		int regsorters = (sys.nrCpu)/2;	// sys.nrCpu, cause we don't use core 0 for sorting!
		int ovrlping = sys.nrCpu-regsorters-1;
		
		System.out.println("Regular Sorters: " + regsorters);
		System.out.println("Overlapping Sorters: " + ovrlping);
		
		d = SIZE/regsorters;		// SIZE / regsorters
		
		for(int i=0; i<sys.nrCpu-1; ++i) {	// -1 => without core 0
			if(i%2 == 0 ) {		// gerade	 (core1, core3, core5, ...)
				// e.g. 8 cores	=> regsorters = 8/2 = 4 & d = 100/4 = 25
				k1 = (i/2) * d;			//	-> k1 = i*d = i * 25 => 0, 25, 50, 75
				kb = k1 + d/2;		//	-> kb = k1 + 12 = 12, 37, 62, 87
				k2 = k1 + d;		//	-> k2 = 25, 50, 75, 100
				
				if(i == sys.nrCpu-2) {	// sort really all numbers in our array!
					k2 = SIZE;	// extend sorting boundary to SIZE
				}				
				
				s[i] = new OverlapSorter(i, k1, kb, k2);
				Startup.setRunnable(s[i], i);
			}
		}
		for(int i=0; i<sys.nrCpu-1; ++i) {
			if(i%2 != 0) {	// ungerade		(core2, core4, ...)
				k1 = ((i/2) * d) + d/2;	//	-> k1 = i*d + d/2 = i * 25 + 12 => 12, 37, 62
				kb = k1 + d/2;		//	-> kb = k1 + 12 = 24, 49, 74
				k2 = k1 + d;		//	-> k2 = 37, 62, 87
				
				s[i] = new OverlapSorter(i, k1, kb, k2);
						
				s[i].setSorterL(s[i-1]);
				System.out.println("Sorter" + (i-1) + " left of Sorter"+i);
				
				s[i].setSorterR(s[i+1]);
				System.out.println("Sorter" + (i+1) + " right of Sorter"+i);
				
				s[i-1].setSorterR(s[i]);
				System.out.println("Sorter" + i + " right of Sorter"+(i-1));
				
				s[i+1].setSorterL(s[i]);
				System.out.println("Sorter" + i + " left of Sorter"+(i+1));
				
				Startup.setRunnable(s[i], i);
			}
		}	
		
		// print the whole array
		//printArray();
				
		// measure time
		int startTime, endTime;
		startTime = Native.rd(Const.IO_US_CNT);	
		
		// start the other CPUs
		sys.signal = 1;
		
		// start my work - check sorters for finishing
		int sorted = 0;
		while( sorted < 1 ) {
			//RtThread.busyWait(1000);
			
			{
				int sorted_save = sorted;
				
				do {
					Native.wrMem(Const.TM_START_TRANSACTION, Const.MEM_TM_MAGIC);
					try {
						if(sortstate == sys.nrCpu-1) {	// -1 -> we use sys.nrcpus for sorting
							sortstate = 0;
							sorted ++;
						}
						
						Native.wrMem(Const.TM_END_TRANSACTION, Const.MEM_TM_MAGIC);
					} catch (Throwable e) {
						sorted = sorted_save;
						continue;
					}
				} while (false);
			}
		}
		
		do {
			Native.wrMem(Const.TM_START_TRANSACTION, Const.MEM_TM_MAGIC);
			try {
				sortstate = sys.nrCpu;
				
				Native.wrMem(Const.TM_END_TRANSACTION, Const.MEM_TM_MAGIC);
			} catch (Throwable e) {
				continue;
			}
		} while (false);

		endTime = Native.rd(Const.IO_US_CNT);	
		
		System.out.print("Time: ");
		System.out.print(endTime-startTime);
		System.out.println("\n");
		
		// print the whole array
		//printArray();
		
		// check the sort
		checkArray();
		
		rttm.Diagnostics.saveStatistics();
		rttm.Diagnostics.stat();
		
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
	
	static class OverlapSorter implements Runnable {	
		
		//private boolean sorted = false;
		
		private boolean sortedlow = false;
		private boolean sortedhigh = false;
		
		// my subarray indices
		private int k1, k2;
		// subarray end of sorter n-1
		private int kb;
				
		private OverlapSorter sorterl, sorterr;
		
		private int sorterno;
		
		public OverlapSorter(int _sno, int _k1, int _kb, int _k2) {
			k1 = _k1;
			kb = _kb;
			k2 = _k2;
			
			sorterno = _sno;
			
			System.out.println("Sorter" + sorterno + " k1="+k1+", kb="+kb+", k2="+k2);
		}
		
		public void setSorterL(OverlapSorter _sorterl) {
			sorterl = _sorterl;
		}
		
		public void setSorterR(OverlapSorter _sorterr) {
			sorterr = _sorterr;
		}
		
		public void run() {
			int tmp;
			
			//System.out.println("Sorter" + sorterno + " sorting!");
			
			while( sortstate != sys.nrCpu ) {
				
				if(sortstate == 0) {
					sortedlow = false;
					sortedhigh = false;
				}
				
				if( sortedlow == false ) {
					sortedlow = true;
					
					for(int i=k1; i<kb; ++i) {
						do {
							Native.wrMem(Const.TM_START_TRANSACTION, Const.MEM_TM_MAGIC);
							try {
								if(array[i] > array[i+1]) { 
									tmp = array[i];
									array[i] = array[i+1];
									array[i+1] = tmp;
									sortedlow = false;
									
									//System.out.println("swaplow: " + array[i+1] + " <> " + array[i]);
									
									// if the last element was swapped, our high array must also be sorted
									if(i+1 == kb) { 
										sortedhigh = false; 
										//System.out.println("sortedlow set sortedhigh false");
									}	
								}
								
								Native.wrMem(Const.TM_END_TRANSACTION, Const.MEM_TM_MAGIC);
							} catch (Throwable e) {
								continue;
							}
						} while (false);
					}
					
					// if not sorted ... get sorter n-1 back to sorting
					if(sortedlow == false && sorterl != null) {
						do {
							Native.wrMem(Const.TM_START_TRANSACTION, Const.MEM_TM_MAGIC);
							try {
								sortstate = 0;
								
								Native.wrMem(Const.TM_END_TRANSACTION, Const.MEM_TM_MAGIC);
							} catch (Throwable e) {
								continue;
							}
						} while (false);
					}
				}				
				
				if( sortedhigh == false ) {
					sortedhigh = true;
					
					for(int i=kb; i<k2-1; ++i) {
						do {
							Native.wrMem(Const.TM_START_TRANSACTION, Const.MEM_TM_MAGIC);
							try {
								if(array[i] > array[i+1]) { 
									tmp = array[i];
									array[i] = array[i+1];
									array[i+1] = tmp;
									sortedhigh = false;
									
									//System.out.println("swaphigh: " + array[i+1] + " <> " + array[i]);
									
									// if the first element was swapped, our low array must also be sorted
									if(i == kb) { 
										sortedlow = false; 
										//System.out.println("sortedhigh set sortedlow false"); 
									}	
								}
								
								Native.wrMem(Const.TM_END_TRANSACTION, Const.MEM_TM_MAGIC);
							} catch (Throwable e) {
								continue;
							}
						} while (false);
					}
					
					// if not sorted ... get sorter n+1 back to sorting
					if(sortedhigh == false && sorterr != null) {
						do {
							Native.wrMem(Const.TM_START_TRANSACTION, Const.MEM_TM_MAGIC);
							try {
								sortstate = 0;
								
								Native.wrMem(Const.TM_END_TRANSACTION, Const.MEM_TM_MAGIC);
							} catch (Throwable e) {
								continue;
							}
						} while (false);
					}
				}	
				
				if(sortedhigh == true && sortedlow == true && sortstate == this.sorterno) {
					do {
						Native.wrMem(Const.TM_START_TRANSACTION, Const.MEM_TM_MAGIC);
						try {
							sortstate++;
							
							Native.wrMem(Const.TM_END_TRANSACTION, Const.MEM_TM_MAGIC);
						} catch (Throwable e) {
							continue;
						}
					} while (false);
				}
								
			}
			
			//System.out.println("Sorter" + sorterno + " end!");
			
			rttm.Diagnostics.saveStatistics();
		}		
		
	}
	

}
