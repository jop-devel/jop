package com.jopdesign.io;

import com.jopdesign.sys.Native;

public class IOFactory {
	
	private SerialPort sp;

	// Handles should be the first static fields!
	private static int SP_PTR;
	private static int SP_MTAB;

	// declare all constants AFTER the static fields for
	// the HW Object handles
	private final static int SP_BASE_ADDRESS = 0xffffff90;

	IOFactory() {
		sp = (SerialPort) makeHWObject(new SerialPort(),
				SP_BASE_ADDRESS, 0);
	};
	
	static IOFactory single = new IOFactory();;
	
	public static IOFactory getFactory() {		
		return single;
	}
	
	public SerialPort getSerialPort() {
		return sp;
	}
	
	private Object makeHWObject(Object o, int address, int idx) {
		int ref = Native.toInt(o);
		int pcl = Native.rdMem(ref+1);
		int cp = Native.rdIntMem(1);
		int p = Native.rdMem(cp-1);
		p = Native.rdMem(p+1);
		p += idx*2;
		Native.wrMem(address, p);
		Native.wrMem(pcl, p+1);
		return Native.toObject(p);
	}
	
}
