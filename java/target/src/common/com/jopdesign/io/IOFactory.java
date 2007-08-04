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

	IOFactory() {
		sp = (SerialPort) makeHWObject(new SerialPort(),
				Const.IO_UART1_BASE, 0);
		sys = (SysDevice) makeHWObject(new SysDevice(),
				Const.IO_SYS_DEVICE, 1);
	};
	
	private static IOFactory single = new IOFactory();;
	
	/**
	 * Get the factory singleton
	 * @return an HW object factory
	 */
	public static IOFactory getFactory() {		
		return single;
	}
	
	/**
	 * The main serial port (= System.out)
	 * @return
	 */
	public SerialPort getSerialPort() {	return sp; }
	
	public SysDevice getSysDevice() { return sys; }
	
	private Object makeHWObject(Object o, int address, int idx) {
		int cp = Native.rdIntMem(Const.RAM_CP);
		return JVMHelp.makeHWObject(o, address, idx, cp);
	}
	
}
