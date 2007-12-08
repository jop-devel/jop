package com.jopdesign.io;

import com.jopdesign.sys.JVMHelp;
import com.jopdesign.sys.Native;
import com.jopdesign.sys.Const;

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
	// declare all constants AFTER the static fields for
	// the HW Object handles
	
	// test serial port access via array
	private int[] arrTest;
	private static int ARR_PRT;
	private static int ARR_LENGTH;

	IOFactory() {
		sp = (SerialPort) makeHWObject(new SerialPort(),
				Const.IO_UART1_BASE, 0);
		sys = (SysDevice) makeHWObject(new SysDevice(),
				Const.IO_SYS_DEVICE, 1);
		arrTest = makeHWArray(2, Const.IO_UART1_BASE, 2);
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
	
	public int[] getArray() { return arrTest; }
	
	
}
