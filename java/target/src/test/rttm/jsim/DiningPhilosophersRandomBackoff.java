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
import com.jopdesign.sys.Native;
import com.jopdesign.sys.Startup;

/**
 * @author Michael Muck
 * dining philosophers with random backoff
 * example to study the effects of random backoffs before access
 */
public class DiningPhilosophersRandomBackoff {
	
	static SysDevice sys = IOFactory.getFactory().getSysDevice();
	
	static final int MAX_BACKOFF = 100;
	
	static final int FULL = 1000;
	static final int EMPTY = 0;
	
	static int pot;
	
	static int[] chopstick;
	static boolean[] chopstickInUse;
	
	static Random r = new Random();
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		chopstick = new int[sys.nrCpu];
		chopstickInUse = new boolean[sys.nrCpu];
		// reset sticks usage & inuse array
		for(int i=0; i<sys.nrCpu; ++i) {
			chopstick[i] = 0;
			chopstickInUse[i] = false;
		}
		// fill pot
		pot = FULL;
		
		// create philosophers
		Philosopher p[] = new Philosopher[sys.nrCpu];
		for (int i=0; i<sys.nrCpu; ++i) {
			if(i == 0) {
				p[i] = new Philosopher(i);
			}
			else {
				p[i] = new Philosopher(i);
				Startup.setRunnable(p[i], i-1);
			}
		}		
		
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
		
		System.out.println("Initial Portions available: " + FULL);
		// output stats
		System.out.println("Philosophers Portions:");
		for(int i=0; i<sys.nrCpu; ++i) {
			p[i].stat();
		}
		// chopstick usage
		System.out.println("Chopstick Usage:");
		for(int i=0; i<sys.nrCpu; ++i) {
			System.out.println("\tChopstick " + i + " used " + chopstick[i] + " times!");
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
		
		public Philosopher(int i) {
			id = i;
			
			one = id;
			two = (id+1)%sys.nrCpu;			
		}
		
		public void run() {	
			boolean ok = true;
			
			while(ok) {
				
				try {
					Thread.sleep(r.nextInt()%MAX_BACKOFF);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				Native.wrMem(1, MAGIC);	// start transaction
					
					if(pot > EMPTY && !chopstickInUse[one] && !chopstickInUse[two]) {
						// take chopsticks
						chopstickInUse[one] = true;
						chopstickInUse[two] = true;
						
						// eat
						pot--;						
						portions++;
						
						// lay down chopsticks
						chopstickInUse[one] = false;
						chopstickInUse[two] = false;
												
						// stats
						chopstick[one]++;
						chopstick[two]++;
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
		
	}

}
