/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2010, Martin Schoeberl (martin@jopdesign.com)

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
package csp;

import util.Timer;

import joprt.RtThread;

import com.jopdesign.sys.Native;
import com.jopdesign.io.IOFactory;
import com.jopdesign.io.SysDevice;

/**
 * @author flavius
 *
 */


// Note: This program finishes on the main processor, when PRIMECNT primes were detected
//       The rest of the processors may continue to wait for further input!
//	 Thus the time is for computing nrCpu*(PRIMECNT-1) + 1 primes.
 
public class EratosthenesCsp3 extends RtThread {
	
		final static int PRIMECNT = 20;
		final static int PRIORITY = 1;
		final static int PERIOD = 1000;
		
		static Object lock = new Object();
		public static int endCalculation = 0;
		volatile static boolean go = false;
		
		int id;
                int nid; // next processor id	

		public EratosthenesCsp3(int i, int ni) {
			super(PRIORITY, PERIOD);
			id = i;
			nid = ni;
			//crtlvl = 0;
			//primes = new int[PRIMECNT];
		}

		public static int cpuIndex2NoCAddress(int i) {
		// this is when I use two rings, with the other ring starting at Addr 4 
		//	if(i==2) return 4;
		//	else return i;
		// this is with a single ring!
			return i;
		}

		/**
		 * @param args
		 */
		public static void main(String[] args) {

			// Initialization for benchmarking 
			int start = 0;
			int stop = 0;
			int time = 0;
			
			System.out.println("Eratosthenes Sieve, v3, SPM");
			SysDevice sys = IOFactory.getFactory().getSysDevice();

			int nrCpu = Runtime.getRuntime().availableProcessors();

// ni must be translated from proc index to NoC address!

			for (int i=0; i<nrCpu; i++) {
				int ni = i+1;
				if(ni==nrCpu) ni = 0;
				System.out.println(cpuIndex2NoCAddress(ni));
				RtThread rtt = new EratosthenesCsp3(i, cpuIndex2NoCAddress(ni));
				rtt.setProcessor(i);
			}

			System.out.println("starting cpus.");
			// start threads and other cpus
			RtThread.startMission();
		
			// give threads time to setup their memory
			// RtThread.sleepMs(100);
			
			// using clock cycles instead
			start = sys.cntInt; // (int) System.currentTimeMillis();
			// let them run
			go = true;

			// wait for finish
			while (true) {
			synchronized (lock) {
				if (endCalculation == 1) // nrCpu) // just the first needs to finish!
					break;
				}
			}

			// End of measurement
			stop = sys.cntInt; // (int) System.currentTimeMillis();

			System.out.println("StartTime: " + start);
			System.out.println("StopTime: " + stop);
			time = stop - start;
			System.out.println("TimeSpent: " + time);


		}

/*
		public void run() {
			computePrimes(id, nid);
		}
*/


		public void run() {		
		Runnable r = new Runnable() {
			public void run() {
				computePrimes(id, nid);
				
			}
		};
		PrivateScope scope = new PrivateScope(1000);			
		
		while (!go) {
			waitForNextPeriod();
		}

		scope.enter(r);
		}


		static void computePrimes(int id, int nid) {

		    int crtlvl = 0;
		    int[] primes = new int[PRIMECNT];
		    int[] mulprimes = new int[PRIMECNT];

		    // proc 0
		    int i = 2;

		    while(crtlvl < PRIMECNT) {
			// receive a level and a candidate
			// int lvl, candidate;
			
		   	// proc 0
			
		    	int	lvl = 0;
	            	int candidate = i;
			
	
			if(id == 0) {
				if(((Native.rd(NoC.NOC_REG_STATUS) & NoC.NOC_MASK_RCV) != 0)) { // if(NoC.isReceiving()) {
//					System.out.println("<");
					// something to process
/////////////////////// receive a two word message instead ////////////////
					lvl = Native.rd(NoC.NOC_REG_RCVDATA);
//					while(NoC.isReceiveBufferEmpty());
					candidate = Native.rd(NoC.NOC_REG_RCVDATA);
					Native.wr(0, NoC.NOC_REG_RCVRESET);
///////////////////////////////////////////////////////////////////////////
					// level increases here
					lvl++;
				} else {
					i++;
				}			
			} else {
/////////////////////// receive a two word message instead ////////////////
			while((Native.rd(NoC.NOC_REG_STATUS) & NoC.NOC_MASK_RCV) == 0); // while(!NoC.isReceiving());
			lvl = Native.rd(NoC.NOC_REG_RCVDATA);
//			while(NoC.isReceiveBufferEmpty());
			candidate = Native.rd(NoC.NOC_REG_RCVDATA);
			Native.wr(0, NoC.NOC_REG_RCVRESET);
///////////////////////////////////////////////////////////////////////////
			}
			// check it against the current prime
			if(lvl==crtlvl) {
				// this is a new prime, so store it
				primes[crtlvl] = candidate;
				// next number to check is..
				mulprimes[crtlvl] = candidate; // + candidate;
				crtlvl++;
				// do not send it further
			} else {
				// check whether is divisible with the current prime
				// % was way too slow
				// if(candidate % primes[lvl] != 0) {

				// must bring the current multiple up to the candidate size
				while(mulprimes[lvl] < candidate)
					mulprimes[lvl] = mulprimes[lvl] +  primes[lvl];

				if(candidate != mulprimes[lvl]) {
					// may be prime, send it further!
////////////////// send a two word message instead ///////////////////////////
					while((Native.rd(NoC.NOC_REG_STATUS) & NoC.NOC_MASK_SND) != 0); // while(NoC.isSending());
					Native.wr(nid, NoC.NOC_REG_SNDDST);
					Native.wr(2, NoC.NOC_REG_SNDCNT);
					Native.wr(lvl, NoC.NOC_REG_SNDDATA);
//					while(NoC.isSendBufferFull());
					Native.wr(candidate, NoC.NOC_REG_SNDDATA);
//////////////////////////////////////////////////////////////////////////////
				} else {
					// this number should be discarded
					// must update the multiples to check against
					// may not need this with the adjustment
					// mulprimes[lvl] = candidate + primes[lvl];
				}
			
			}	
		   }

		   synchronized (lock) {
				endCalculation++;
		   }
		   // all entries are full
		   if(id == 0) {
			// print out the result for this processor
			System.out.print("Primes on cpu0 ");
			for(i=0;i<PRIMECNT;i++)
				System.out.println(primes[i]);

		   }

		}


	}

