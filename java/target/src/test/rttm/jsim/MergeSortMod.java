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

import joprt.RtThread;
import com.jopdesign.sys.RtThreadImpl;
import com.jopdesign.io.IOFactory;
import com.jopdesign.io.SysDevice;
import com.jopdesign.sys.JVMHelp;
import com.jopdesign.sys.Native;
import com.jopdesign.sys.Const;
import com.jopdesign.sys.Startup;

/**
 * MergeSort Implementation for Real Time Threads
 * 
 * this example should demonstrate that there is no fatal 
 * performance loss in a case of transactional memory misuse 
 * 
 * @author michael muck
 *
 */
public class MergeSortMod {
	private static final int MAGIC = -10000;	
	
	static SysDevice sys = IOFactory.getFactory().getSysDevice();
	
	static Random rn = new Random();
	
	static final int SIZE = 50;
	static int[] array = new int[SIZE];
	
	static boolean end = false;
	
	static int core = 0;	// used in thread assignment	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		// fill array with random vars
		for(int x=0; x<SIZE; ++x) {
			array[x] = rn.nextInt()%1000;
			if(array[x] < 0) { array[x] *= -1; }
		}
		
		/*
		// create dummys for the other cores
		Dummy d;
		for(int i=0; i<sys.nrCpu-1; ++i) {
			d = new Dummy();
			d.setProcessor(i+1);
		}
		*/
	
		// print the whole array
		printArray();
		
		// first setup mergesort
		MSorter no1 = new MSorter(0, SIZE);		
		no1.setProcessor(core);
		core = (++core)%sys.nrCpu;
			
		// start mission and other CPUs
		RtThread.startMission();
		System.out.println("Mission started");

		// wait until the sort has finished
		while(!no1.finished) {
			//System.out.print("finished: ");
			//System.out.println(no1.finished);
			RtThread.sleepMs(50);
		}
		
		// print the whole array
		printArray();
		
		// check the sort
		checkArray();
		
		// write the magic string to stop the simulation
		System.out.println("\r\nJVM exit!\r\n");

		System.exit(0);
		for(;;) {
			RtThread.sleepMs(1000);
		}
		
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
	
	/*
	public static class Dummy extends RtThread {
		boolean cancel = false;
		
		public Dummy() {
			super(RtThreadImpl.MIN_PRIORITY, 1000*1000);	// priority, period in µs			
		}
		
		public void run() {
			while(cancel == false) {
				this.waitForNextPeriod();
			}
		}
	
	}
	*/

	public static class MSorter extends RtThread {
		
		boolean finished = false;
		boolean leaf = false;
		
		int left;
		int m;
		int right;

		MSorter sl, sr;
		
		public MSorter(int l, int r) {
			super(RtThreadImpl.NORM_PRIORITY, 1000);	// RealTime Threads
			
			left = l;
			right = r;
			
			if( right-left <= 1 ) { 
				leaf = true;
				//System.out.println("LEAF");
			}
			else {				
				m = ((right-left) / 2) + left;
								
				sl = new MSorter(left, m);			
				// schedule ql
				sl.setProcessor(core);
				
				System.out.print("setup sl on Core");
				System.out.print(core);
				System.out.print(" with ");
				System.out.print(left);
				System.out.print(" - ");
				System.out.println(m);
				
				core = (++core)%sys.nrCpu;

				sr = new MSorter(m, right);			
				// schedule qr			
				sr.setProcessor(core);
				
				System.out.print("setup sr on Core");
				System.out.print(core);
				System.out.print(" with ");
				System.out.print(m+1);
				System.out.print(" - ");
				System.out.println(right);
				
				core = (++core)%sys.nrCpu;
			}
		}
		
		public void run() {
			if(leaf) {
				finished = true;
				//System.out.println("finished");
			}
			else {
				for(;;) {
					if( sl.finished == true && sr.finished == true ) {
						break;
					}
					this.waitForNextPeriod();
				}
				
				//System.out.println("merging");
				
				merge(left, m, right);
				
				finished = true;
			}
			/*
			for(;;) {
				this.waitForNextPeriod();
			}
			*/
		}
		
		private void merge(int l, int m, int r) {
		    int i, j, k;

		    //System.out.println("b-size=" + (r-l) + " - l=" + l + " - m=" + m + " - r=" + r);
			int[] b = new int[m-l];
			
			Native.wrMem(1, MAGIC);
			k=0;
			for (i=l; i<m; i++) {
		        b[k++]=array[i]; 
		       // System.out.print(array[i] + " ");			        
			}
			//System.out.println();

			i=l; j=m; 
			k=l;
			// jeweils das nächstgrößte Element zurückkopieren
			//System.out.println("i=" + i + " - j=" + j + " - k=" + k);
			while (i<m && j<r) {
				if (b[i-l]<=array[j]) {
					array[k]=b[i-l];
				    i++;
				}
				else {
				    array[k]=array[j];
				    j++;
				}
				k++;
			}

			// Rest der vorderen Hälfte falls vorhanden zurückkopieren
			while (i<m) {
				array[k++]=b[i-l];
				i++;
			}
			
			Native.wrMem(0, MAGIC);

			//for (i=l; i<r; i++) {			        
		    //    System.out.print(array[i] + " ");			        
			//}
			//System.out.println();
		}
		
	}
	

}
