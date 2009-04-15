/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2001-2008, Martin Schoeberl (martin@jopdesign.com)

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

package com.jopdesign.io.examples;

import joprt.*;
import util.Timer;

import com.jopdesign.io.*;
import com.jopdesign.sys.*;

public class ScheduledIH implements Runnable {

	static int ts, te, to;
	
	static SwEvent sw;
	static SysDevice sys;

	public static void main(String[] args) {

		IOFactory fact = IOFactory.getFactory();		
		SerialPort sp = fact.getSerialPort();
		sys = fact.getSysDevice();

		ScheduledIH ih = new ScheduledIH();
		fact.registerInterruptHandler(1, ih);
		
		sw = new SwEvent(10, 1000) {
			public void handle() {
				te = Native.rdMem(Const.IO_CNT);
				System.out.print(te-ts-to);
				System.out.print(" ");
//				System.out.print(JVMHelp.ts-ts-to);
//				System.out.print(" ");				
			}
		};
		
		new RtThread(9, 100000) {
			public void run() {
				for (;;) {
					ts = Native.rdMem(Const.IO_CNT);
					// trigger a SW interrupt via the system HW object
//					sw.fire();
					sys.intNr = 1;
					waitForNextPeriod();
				}
			}
		};
		
		System.out.println("Measure IH respons time with RT scheduling");
		
		// enable all interrupts
		fact.enableInterrupt(-1);

		// measure overhead
		ts = Native.rdMem(Const.IO_CNT);
		sys.intNr = 2;
		te = Native.rdMem(Const.IO_CNT);
		to = te-ts;
		
		RtThread.startMission();

		for (int i=0; i<20; ++i) {
			Timer.wd();
//			RtThread.sleepMs(200);
			int t = Timer.getTimeoutMs(200);
			while (!Timer.timeout(t));
		}
	}

	
	public void run() {
		// do we have to enable interrupts for the
		// timer interrupt fire? I don't think so
		// Native.wr(1, Const.IO_INT_ENA);
		sw.fire();
	}
}
