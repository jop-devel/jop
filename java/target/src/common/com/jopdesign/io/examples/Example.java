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

public class Example {

	public static void main(String[] args) {

		System.out.println("Hello World");

		DspioFactory fact = DspioFactory.getDspioFactory();		
		SerialPort sp = fact.getSerialPort();
		SysDevice sys = fact.getSysDevice();
		SerialPort usb = fact.getUsbPort();
		
		String hello = "Hello World via Hardware Objects!";
		
		// Hello world with low-level device access
		for (int i=0; i<hello.length(); ++i) {
			// busy wait on transmit buffer empty
			while ((sp.status & SerialPort.MASK_TDRE) == 0)
				;
			// write a character
			sp.data = hello.charAt(i);
		}
		
		hello = "write to USB port";
		// Hello world with low-level device access
		for (int i=0; i<hello.length(); ++i) {
			// busy wait on transmit buffer empty
			while (!usb.txEmpty())
				;
			// write a character
			usb.write(hello.charAt(i));
		}

//		int [] sa = fact.getArray();
		// array does not work
		// xaload/xastore or implemented in the memory interface
		// no connection to IO SimpCon!
		// TODO: do the IO/Memory MUX after sc_mem

		for (int i=0; i<20; ++i) {
			Timer.wd();
			int t = Timer.getTimeoutMs(200);
			while (!Timer.timeout(t)) ;
		}
	}
}
