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

	final static int CNT = 100; // that's 1 KB
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

		System.out.println("Hello CSP World from CPU 0");

		SysDevice sys = IOFactory.getFactory().getSysDevice();
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

		// receive n words via CSP, 1-word packets
		start = sys.cntInt;

		// Native.wr(2, NoC.NOC_REG_SNDDST);
		// Native.wr(1, NoC.NOC_REG_SNDCNT);
		// Native.wr(0xcafebabe, NoC.NOC_REG_SNDDATA);

		for (int i = 0; i < CNT; ++i) {
			while (!((Native.rd(NoC.NOC_REG_STATUS) & NoC.NOC_MASK_RCV) != 0))
				;
			int val = Native.rd(NoC.NOC_REG_RCVDATA);
			//int src = Native.rd(NoC.NOC_REG_RCVSRC);
			Native.wr(0, NoC.NOC_REG_RCVRESET); // aka writeReset();
			//System.out.print(" Received ");
			//System.out.print(val);
			//System.out.print(" from ");
			//System.out.print(src);
		}
		end = sys.cntInt;
		System.out.println("Communication via HW CSP, many 1-word packets");
		System.out.println(CNT + " words received in " + (end - start - off)
				+ " cycles");


		// receive n words via CSP, n-word packet
		start = sys.cntInt;
		while (!((Native.rd(NoC.NOC_REG_STATUS) & NoC.NOC_MASK_RCV) != 0))
				;
		for (int i = 0; i < CNT; ++i) {
			
			int val = Native.rd(NoC.NOC_REG_RCVDATA);
			// System.out.print(" Received ");
			// System.out.print(val);
		}
		Native.wr(0, NoC.NOC_REG_RCVRESET); // aka writeReset();
		end = sys.cntInt;
		System.out.println("Communication via HW CSP, one N-word packet");
		System.out.println(CNT + " words received in " + (end - start - off)
				+ " cycles");



		// receive n words via shared memory
		start = sys.cntInt;
		// start the other copy thread
		startCopy = true;
		while (!endCopy) {
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
				+ " cycles");

	}

	public void run() {

		// Native.wr(0, NoC.NOC_REG_SNDDST);
		// Native.wr(1, NoC.NOC_REG_SNDCNT);
		// Native.wr(0xdeadbeef, NoC.NOC_REG_SNDDATA);

		// 1 word packets send

		while ((Native.rd(NoC.NOC_REG_STATUS) & NoC.NOC_MASK_SND) != 0) {
			// nop
		}
		Native.wr(0, NoC.NOC_REG_SNDDST);

		for (int i = 0; i < CNT; ++i) {
			while ((Native.rd(NoC.NOC_REG_STATUS) & NoC.NOC_MASK_SND) != 0) {
				// nop
			}
			// Native.wr(0, NoC.NOC_REG_SNDDST);
			Native.wr(1, NoC.NOC_REG_SNDCNT);
			Native.wr(i, NoC.NOC_REG_SNDDATA);

		}

		// CNT-words packet send

		while ((Native.rd(NoC.NOC_REG_STATUS) & NoC.NOC_MASK_SND) != 0) {
			// nop
		}
		Native.wr(0, NoC.NOC_REG_SNDDST);
		Native.wr(CNT, NoC.NOC_REG_SNDCNT);

		for (int i = 0; i < CNT; ++i) {
			Native.wr(i, NoC.NOC_REG_SNDDATA);
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
