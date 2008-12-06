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

package com.jopdesign.io;

import com.jopdesign.sys.JVMHelp;
import com.jopdesign.sys.Native;
import com.jopdesign.sys.Const;
import com.jopdesign.sys.GC;

/**
 * Base class for all IO device factories
 * @author Martin
 *
 */
public class IOFactory {
	
	private SerialPort sp;
	private SysDevice sys;

	// Handles should be the first static fields!
	private static int SP_PTR;
	private static int SP_MTAB;
	
	private static int SYS_PTR;
	private static int SYS_MTAB;
	// test serial port access via array
	private int[] spMem;
	private static int ARR_PRT;
	private static int ARR_LENGTH;

	// declare all constants AFTER the static fields for
	// the HW Object handles
	
	IOFactory() {
		
		// TODO: can I avoid the cp trick and just pass
		// a pointer to this to JVMHelp.make....
		// IOFactory x = this;
		// that would have following benefits:
		//		1. no Native.rd
		//		2. we could check if the class is indeed a
		//			IOFactory to avoid misuse of the public
		//			JVMHelp.makeHWObject
		
		sp = (SerialPort) makeHWObject(new SerialPort(),
				Const.IO_UART1_BASE, 0);
		sys = (SysDevice) makeHWObject(new SysDevice(),
				Const.IO_SYS_DEVICE, 1);
		spMem = makeHWArray(GC.getScratchpadSize(), Const.SCRATCHPAD_ADDRESS, 2);
	};
	// that has to be overridden by each sub class to get
	// the correct cp
	private static Object makeHWObject(Object o, int address, int idx) {
		int cp = Native.rdIntMem(Const.RAM_CP);
		return JVMHelp.makeHWObject(o, address, idx, cp);
	}
	private static int[] makeHWArray(int len, int address, int idx) {
		int cp = Native.rdIntMem(Const.RAM_CP);
		return JVMHelp.makeHWArray(len, address, idx, cp);
	}
	
	private static IOFactory single = new IOFactory();
	
	/**
	 * Get the factory singleton
	 * @return an HW object factory
	 */
	public static IOFactory getFactory() {		
		return single;
	}
	
	/**
	 * Add a Runnable as a first level interrupt handler
	 * @param nr interrupt number
	 * @param r Runnable the represents the interrupt handler
	 */
	public void registerInterruptHandler(int nr, Runnable logic) {
		JVMHelp.addInterruptHandler(nr, logic);
	}
	
	/**
	 * Remove the Runnable
	 * @param nr interrupt number
	 */
	public void deregisterInterruptHandler(int nr) {
		JVMHelp.removeInterruptHandler(nr);
	}
	
	// TODO: This does NOT work with CMP based interrupts!
	static int interruptMask;

	/**
	 * Individual interrupt enable
	 * @param nr interrupt number
	 */
	public void enableInterrupt(int nr) {
		interruptMask |= 1 << nr;
		sys.intMask = interruptMask;
	}
	/**
	 * Individual interrupt disable
	 * @param nr interrupt number
	 */
	public void disableInterrupt(int nr) {
		int mask = 1 << nr;
		mask = ~mask;
		interruptMask &= mask;
		sys.intMask = interruptMask;
	}

	
	/**
	 * The main serial port (= System.out)
	 * @return
	 */
	public SerialPort getSerialPort() { return sp; }
	
	public SysDevice getSysDevice() { return sys; }
	
	public int[] getScratchpadMemory() { return spMem; }
	
	
}
