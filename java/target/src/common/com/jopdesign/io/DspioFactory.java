package com.jopdesign.io;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.JVMHelp;
import com.jopdesign.sys.Native;

public class DspioFactory extends IOFactory {

	private SerialPort usb;

	// Handles should be the first static fields!
	private static int USB_PTR;
	private static int USB_MTAB;

	DspioFactory() {
		usb = (SerialPort) makeHWObject(new SerialPort(),
				Const.IO_USB, 0);
	};
	// that has to be overridden by each sub class to get
	// the correct cp
	private static Object makeHWObject(Object o, int address, int idx) {
		int cp = Native.rdIntMem(Const.RAM_CP);
		return JVMHelp.makeHWObject(o, address, idx, cp);
	}
	
	static DspioFactory single = new DspioFactory();
	
	public static DspioFactory getDspioFactory() {		
		return single;
	}

	public SerialPort getUsbPort() { return usb; }

}
