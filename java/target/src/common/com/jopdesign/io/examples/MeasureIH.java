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

import util.Timer;

import com.jopdesign.io.*;
import com.jopdesign.sys.*;

public class MeasureIH implements Runnable {

	static int ts, te, to;

	public static void main(String[] args) {

		IOFactory fact = IOFactory.getFactory();		
		SerialPort sp = fact.getSerialPort();
		SysDevice sys = fact.getSysDevice();

		MeasureIH ih = new MeasureIH();
		fact.registerInterruptHandler(1, ih);
		
		System.out.println("Measure IH respons time");
		
		// enable software interrupt 1
		fact.enableInterrupt(1);

		// measure overhead
		ts = Native.rdMem(Const.IO_CNT);
		sys.intNr = 2;
		te = Native.rdMem(Const.IO_CNT);
		to = te-ts;
		ts = Native.rdMem(Const.IO_CNT);

		for (int i=0; i<200; ++i) {
//			Timer.wd();
//			int t = Timer.getTimeoutMs(200);
//			while (!Timer.timeout(t));
			ts = Native.rdMem(Const.IO_CNT);
			// trigger a SW interrupt via the system HW object
			sys.intNr = 1;
		}
	}

	
	public void run() {
		te = Native.rdMem(Const.IO_CNT);
		System.out.print(te-ts-to);
		System.out.print(" ");
// need to be set in JVMHelp
//		System.out.print(JVMHelp.ts-ts-to);
		System.out.print(" ");
	}
}
