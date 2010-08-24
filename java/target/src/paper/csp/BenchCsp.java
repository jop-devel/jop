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

import java.util.Vector;

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
public class BenchCsp implements Runnable {

	final static int CNT = 100;
	static int[] buffer = new int[CNT];
	
	static volatile boolean startCopy, endCopy;

	int id;

	static Vector msg;

	public BenchCsp(int i) {
		id = i;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		msg = new Vector();

		System.out.println("Hello World from CPU 0");
		System.out.print("Status: ");
		System.out.println(Native.rd(NoC.NOC_REG_STATUS));

		SysDevice sys = IOFactory.getFactory().getSysDevice();
		// for (int i=0; i<sys.nrCpu-1; ++i) {
		// Runnable r = new Main(i+1);
		// Startup.setRunnable(r, i);
		// }
		Runnable r = new BenchCsp(1);
		Startup.setRunnable(r, 0);
		// Startup.setRunnable(new BenchCspNew(2), 1);

		// start the other CPUs
		sys.signal = 1;
		// set the WD LED for the simulation
		sys.wd = 1;

		int start, end, off;
		start = sys.cntInt;
		end = sys.cntInt;
		off = end - start;

		// receive n words via CSP
		start = sys.cntInt;
		for (int i = 0; i < 100; ++i) {
			while(!((Native.rd(NoC.NOC_REG_STATUS) & NoC.NOC_MASK_RCV) != 0));
			int d = Native.rd(NoC.NOC_REG_RCVDATA);
			// NoC.isEoD() should be true here for single word messages!
			//
			// does reset for more receive. after this,
			// the source, count and everything else may be faulty
			Native.wr(0, NoC.NOC_REG_RCVRESET); // aka writeReset();		
			int val = d;
			// System.out.print(" Received ");
			// System.out.print(val);
		}
		end = sys.cntInt;
		System.out.println("Communication via HW CSP");
		System.out.println(CNT + " words received in " + (end - start - off)
				+ " micro seconds");

		// receive n words via shared memory
		start = sys.cntInt;
		// start the other copy thread
		startCopy = true;
		while (!endCopy){
			;
		}
		for (int i = 0; i < 100; ++i) {
			int val = buffer[i];
			// System.out.print(" Received ");
			// System.out.print(val);
		}
		end = sys.cntInt;
		System.out.println("Communication via shared memory");
		System.out.println(CNT + " words received in " + (end - start - off)
				+ " micro seconds");
	
	
	}

	public void run() {

		for (int i = 0; i < CNT; ++i) {
			while (NoC.isSending()) {
				// nop
			}
			NoC.nb_send1(0, i);
		}
		while (!startCopy) {
			;
		}
		for (int i = 0; i < CNT; ++i) {
			buffer[i] = i;
		}
		endCopy = true;
	}

}
