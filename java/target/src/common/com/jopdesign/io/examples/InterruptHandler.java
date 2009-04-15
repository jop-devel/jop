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

public class InterruptHandler implements Runnable {

	public static void main(String[] args) {

		IOFactory fact = IOFactory.getFactory();		
		SysDevice sys = fact.getSysDevice();

		InterruptHandler ih = new InterruptHandler();
		fact.registerInterruptHandler(1, ih);
		
		// enable software interrupt 1
		fact.enableInterrupt(1);

		for (int i=0; i<20; ++i) {
			Timer.wd();
			int t = Timer.getTimeoutMs(200);
			while (!Timer.timeout(t));
			// trigger a SW interrupt via the system HW object
			System.out.println("Trigger");
			sys.intNr = 1;
			if (i==10) {
				fact.disableInterrupt(1);
//				fact.deregisterInterruptHandler(1);
			}
		}
	}

	
	public void run() {
		System.out.println("Interrupt fired!");
	}
}
