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

import java.util.Random;
import com.jopdesign.io.IOFactory;
import com.jopdesign.io.SysDevice;
import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;
import com.jopdesign.sys.RtThreadImpl;
import com.jopdesign.sys.Startup;

/**
 * dining philosophers for jop with tm
 * to show that there is no such problem
 * as a deadlock on a tm-system
 * 
 * uses IOSimRNG!
 * 
 * a single transaction in this test usually takes 600-700us
 * -> if we wait 700us or a multiple we can go through the test with minimal time
 * 
 * @author michael muck
 */
public class DiningPhilosophersCalc2Backoff {
	
	static SysDevice sys = IOFactory.getFactory().getSysDevice();
	
	// read a random number from IO
	private static final int IO_RAND = Const.IO_CPUCNT+1;	// its likely that this var needs to be changed!
	// read a positive random number from IO
	private static final int IO_PRAND = IO_RAND+1;	
	
	static final int FULL = 10000;
	static final int EMPTY = 0;
	
	static int pot;
	
	static int[] chopsticks;	
		
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		chopsticks = new int[sys.nrCpu];	
		
		// reset sticks usage array
		for(int i=0; i<sys.nrCpu; ++i) {
			chopsticks[i] = -1;		
		}
		
		// fill pot
		pot = FULL;
		
		// benchmark our transaction
		BenchPhilosopher bp = new BenchPhilosopher(0, 100);				
		int btime = bp.getBenchmarkTime();
		int ttime = bp.getTransactionTime();
		
		// create philosophers
		Philosopher p[] = new Philosopher[sys.nrCpu];
		for (int i=0; i<sys.nrCpu; ++i) {
			p[i] = new Philosopher(i, btime, ttime);
			if(i > 0) {			
				Startup.setRunnable(p[i], i-1);
			}
		}		
		
		int startTime, endTime;
		startTime = Native.rd(Const.IO_US_CNT);	
		
		// start the other CPUs
		sys.signal = 1;
		
		// start my work
		p[0].run();

		// wait for other CPUs to finish
		boolean allDone = false;
		while (!allDone) {
			allDone = true;
			for (int i=1; i<sys.nrCpu; ++i) {
				allDone &= p[i].finished;
			}			
		}
		
		endTime = Native.rd(Const.IO_US_CNT);
		
		System.out.print("Time: ");
		System.out.print(endTime-startTime);
		System.out.println("\n");
		
		System.out.println("Initial Portions available: " + FULL);
		
		// output stats
		System.out.println("Philosophers Portions:");
		for(int i=0; i<sys.nrCpu; ++i) {
			p[i].stat();
		}
		// chopstick usage
		System.out.println("Chopstick Usage:");
		int usage;
		for(int i=0; i<sys.nrCpu; ++i) {
			usage = p[i].getLeftChopstickUsage();
			
			if(i==0) { usage += p[sys.nrCpu-1].getRightChopstickUsage(); }
			else { usage += p[i-1].getRightChopstickUsage(); }

			System.out.println("\tChopstick " + i + " used " + usage + " times!");
		}
				
		// write the magic string to stop the simulation
		System.out.println("\r\nJVM exit!\r\n");
	}
	
	static class Philosopher implements Runnable {

		public boolean finished;
		
		protected static final int MAGIC = -10000;

		protected int id;
		
		protected int one;

		protected int two;
		protected int portions = 0;		
		
		protected int usage_one = 0;

		protected int usage_two = 0;
		
		protected int myThinkingTime = 0;
		protected int transactionTime = 0;
		
		public Philosopher() {	
		}
		
		public Philosopher(int i, int btime, int ttime) {
			id = i;
			
			one = id;
			two = (id+1)%sys.nrCpu;			
					
			myThinkingTime = ttime * (id+1);
			transactionTime = (ttime * (sys.nrCpu-1))+(ttime/3);
			if(sys.nrCpu == 1) {
				myThinkingTime = 0;
				transactionTime = 0;
			}
			
			System.out.println("Philosophers "+id+" Initial Thinking Time: " + myThinkingTime);
			System.out.println("Philosophers "+id+" Thinking Time: " + transactionTime);
		}
		
		public void run() {	
			boolean ok = true;
			
			// first thought
			RtThreadImpl.busyWait(myThinkingTime);
			myThinkingTime = transactionTime;
			
			while(ok) {
				
				// think ...
				RtThreadImpl.busyWait(myThinkingTime);
				
				// ... and eat				
				Native.wrMem(1, MAGIC);	// start transaction
				
					// check if there are any ressources left
					if(pot > EMPTY) {			
						
						// aquire my ressources (chopsticks)
						chopsticks[one] = this.id;
						chopsticks[two] = this.id;				
									
						// eat
						pot--;						
						portions++;
						
						// lay down chopsticks
						chopsticks[one] = -1;
						chopsticks[two] = -1;
												
						// stats
						usage_one++;
						usage_two++;
					}
					else { 
						ok = false;
					}					
				
				Native.wrMem(0, MAGIC);	// end transaction			
			}
			
			finished = true;
		}
	
		public void stat() {
			System.out.println("\tPhilosopher No" + id + " had " + portions + " portion/s!");
		}
		
		public int getLeftChopstickUsage() {
			return this.usage_one;
		}
		
		public int getRightChopstickUsage() {
			return this.usage_two;
		}

	}
	
	public static class BenchPhilosopher extends Philosopher {

		int startBM, endBM;
		int startWait, endWait;
		int startTrans, endTrans;
			
		public BenchPhilosopher(int i, int ttime) {
			id = i;
			
			one = id;
			two = (id+1)%sys.nrCpu;			
			
			myThinkingTime = ttime;
			
			// 1 rotation
			pot = EMPTY+1;
			
			System.out.println("\nStarting Benchmark ...");
			run();
			
			// reset pot
			pot = FULL;
			
			System.out.println("BM Time: " + (endBM-startBM));
			System.out.println("Wait Time: 0us = " + (endWait-startWait));			
			System.out.println("Trans Time: " + (endTrans-startTrans));
			System.out.println("Benchmarking end!\n");
		}
		
		public void run() {
			boolean ok = true;
			
			startBM = Native.rd(Const.IO_US_CNT);
			
			while(ok) {			
				
				// think ...
				RtThreadImpl.busyWait(myThinkingTime);
				
				// ... and eat				
				Native.wrMem(1, MAGIC);	// start transaction
				
					// check if there are any ressources left
					if(pot > EMPTY) {			
						
						// aquire my ressources (chopsticks)
						chopsticks[one] = this.id;
						chopsticks[two] = this.id;				
									
						// eat
						pot--;						
						portions++;
						
						// lay down chopsticks
						chopsticks[one] = -1;
						chopsticks[two] = -1;
												
						// stats
						usage_one++;
						usage_two++;
					}
					else { 
						ok = false;
					}					
				
				Native.wrMem(0, MAGIC);	// end transaction		
			}
			
			endBM = Native.rd(Const.IO_US_CNT);
			
			// another one
			pot = EMPTY+1;
			
			startTrans = Native.rd(Const.IO_US_CNT);
			Native.wrMem(1, MAGIC);	// start transaction
			
				// check if there are any ressources left
				if(pot > EMPTY) {			
					
					// aquire my ressources (chopsticks)
					chopsticks[one] = this.id;
					chopsticks[two] = this.id;				
								
					// eat
					pot--;						
					portions++;
					
					// lay down chopsticks
					chopsticks[one] = -1;
					chopsticks[two] = -1;
											
					// stats
					usage_one++;
					usage_two++;
				}
				else { 
					ok = false;
				}					
			
			Native.wrMem(0, MAGIC);	// end transaction	
			endTrans = Native.rd(Const.IO_US_CNT);
			
			startWait = Native.rd(Const.IO_US_CNT);
			RtThreadImpl.busyWait(355);
			endWait = Native.rd(Const.IO_US_CNT);
			
			finished = true;		
		}
		
		public int getTransactionTime() {
			return endTrans-startTrans;
		}
		
		public int getBenchmarkTime() {
			return endBM-startBM;
		}
		
	}

}
