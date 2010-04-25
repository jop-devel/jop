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
 * thinking time = 0
 * 
 * @author michael muck
 */
public class DiningPhilosophersNoBackoff {
	
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
		
		// create philosophers
		Philosopher p[] = new Philosopher[sys.nrCpu];
		for (int i=0; i<sys.nrCpu; ++i) {
			p[i] = new Philosopher(i);
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
		
		private static final int MAGIC = -10000;

		private int id;
		
		private int one, two;
		private int portions = 0;		
		
		private int usage_one = 0, usage_two = 0;
		
		private int bad_res = 0;
		
		public Philosopher(int i) {
			id = i;
			
			one = id;
			two = (id+1)%sys.nrCpu;					
		}
		
		public void run() {	
			boolean ok = true;
			
			while(ok) {		
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
			System.out.println("\tPhilosopher No" + id + " had " + portions + " portion/s! - Bad Res Usage: " + bad_res);
		}
		
		public int getLeftChopstickUsage() {
			return this.usage_one;
		}
		
		public int getRightChopstickUsage() {
			return this.usage_two;
		}
		
	}

}
