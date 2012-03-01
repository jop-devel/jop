package com.jopdesign.io;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.JVMHelp;
import com.jopdesign.sys.Native;

public class I2CFactory extends IOFactory {
	
	private I2Cport iic;
	
	// Handles should be the first static fields!
	private static int I2C_M_PTR;
	private static int I2C_M_MTAB;
	
	I2CFactory() {
		iic = (I2Cport) makeHWObject(new I2Cport(),Const.I2C_M_BASE, 0);

	};
	// that has to be overridden by each sub class to get
	// the correct cp
	private static Object makeHWObject(Object o, int address, int idx) {
		int cp = Native.rdIntMem(Const.RAM_CP);
		return JVMHelp.makeHWObject(o, address, idx, cp);
	}
	
	static I2CFactory single = new I2CFactory();
	
	public static I2CFactory getFactory() {		
		return single;
	}

	public I2Cport getI2Cport() { return iic; }

}
