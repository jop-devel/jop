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

import com.jopdesign.io.IOFactory;
import com.jopdesign.io.SysDevice;
import com.jopdesign.sys.Native;
import com.jopdesign.sys.Startup;

/**
 * @author martin
 *
 */
public class EratosthenesCsp2 implements Runnable {
	
		final static int PRIMECNT = 20;
		
		int id;
                int nid; // next processor id
		int crtlvl;
		int[] primes;
		int[] mulprimes;		

		public EratosthenesCsp2(int i, int ni) {
			id = i;
			nid = ni;
			crtlvl = 0;
			primes = new int[PRIMECNT];
			mulprimes = new int[PRIMECNT];
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
			
			System.out.println("Eratosthenes Sieve, v2");
//			System.out.print("Status: ");
//			System.out.println(Native.rd(NoC.NOC_REG_STATUS));


			
			SysDevice sys = IOFactory.getFactory().getSysDevice();

// ni must be translated from proc index to NoC address!

			for (int i=1; i<sys.nrCpu; i++) {
				int ni = i+1;
				if(ni==sys.nrCpu) ni = 0;
//				System.out.println(i+" -> "+cpuIndex2NoCAddress(ni));
				Runnable r = new EratosthenesCsp2(i, cpuIndex2NoCAddress(ni));
				Startup.setRunnable(r, i-1);
			}
			EratosthenesCsp2 r = new EratosthenesCsp2(0,1); 
//			Startup.setRunnable(r, 0);
			
			System.out.println("starting cpus.");
			// start the other CPUs
			sys.signal = 1;
			// set the WD LED for the simulation
			sys.wd = 1;
			
			int i=2;

			// The main loop. 
			while(r.crtlvl < PRIMECNT) {
				int lvl = 0;
				int candidate = i;
				// attempt to receive
				if(NoC.isReceiving()) {
//					System.out.println("<");
					// something to process
/////////////////////// receive a two word message instead ////////////////
//					while(!NoC.isReceiving());
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

//				System.out.print(" Processing ");
//				System.out.print(candidate);
//				System.out.print(" level = ");
//				System.out.print(lvl);
				
				if(r.sendAlong(lvl, candidate)) {
//					System.out.println("->");
////////////////// send a two word message instead ///////////////////////////
					while(NoC.isSending());
					Native.wr(1, NoC.NOC_REG_SNDDST);
					Native.wr(2, NoC.NOC_REG_SNDCNT);
					Native.wr(lvl, NoC.NOC_REG_SNDDATA);
//					while(NoC.isSendBufferFull());
					Native.wr(candidate, NoC.NOC_REG_SNDDATA);
//////////////////////////////////////////////////////////////////////////////
				
				} else {
//					System.out.println(".");
				}	
//				RtThread.sleepMs(10);
			}
			// print out the result for this processor
			System.out.print("Primes on cpu0 ");
			for(i=0;i<PRIMECNT;i++)
				System.out.println(r.primes[i]);

		}

		public void run() {
		    while(crtlvl < PRIMECNT) {
			// receive a level and a candidate
			int lvl, candidate;
/////////////////////// receive a two word message instead ////////////////
			while(!NoC.isReceiving());
			lvl = Native.rd(NoC.NOC_REG_RCVDATA);
//			while(NoC.isReceiveBufferEmpty());
			candidate = Native.rd(NoC.NOC_REG_RCVDATA);
			Native.wr(0, NoC.NOC_REG_RCVRESET);
///////////////////////////////////////////////////////////////////////////
			// check it against the current prime
			if(sendAlong(lvl, candidate)) {
				// send it further
////////////////// send a two word message instead ///////////////////////////
					while(NoC.isSending());
					Native.wr(nid, NoC.NOC_REG_SNDDST);
					Native.wr(2, NoC.NOC_REG_SNDCNT);
					Native.wr(lvl, NoC.NOC_REG_SNDDATA);
//					while(NoC.isSendBufferFull());
					Native.wr(candidate, NoC.NOC_REG_SNDDATA);
//////////////////////////////////////////////////////////////////////////////
//				RtThread.sleepMs(10);
			}
		   }
		   // all entries are full

		}

		public boolean sendAlong(int lvl, int candidate) {
			if(lvl==crtlvl) {
				// this is a new prime, so store it
				primes[crtlvl] = candidate;
				// next number to check is..
				mulprimes[crtlvl] = candidate; // + candidate;
				crtlvl++;
				return false;  // do not send further
			} else {
				// check whether is divisible with the current prime
				// if(candidate % primes[lvl] == 0)

				// must bring the current multiple up to the candidate size
				while(mulprimes[lvl] < candidate)
					mulprimes[lvl] = mulprimes[lvl] +  primes[lvl];

				if(candidate == mulprimes[lvl])
					// not prime, discard
					 return false;
				else
					// might still be prime	
					return true;
			}
		}

	}

