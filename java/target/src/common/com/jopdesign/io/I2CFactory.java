package com.jopdesign.io;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.JVMHelp;
import com.jopdesign.sys.Native;

public class I2CFactory extends IOFactory {
	
	private I2Cport iic_A;
	private I2Cport iic_B;
	
	// Handles should be the first static fields!
	private static int I2C_A__M_PTR;
	private static int I2C_A_M_MTAB;

	private static int I2C_B__M_PTR;
	private static int I2C_B_M_MTAB;
	
	I2CFactory() {
		iic_A = (I2Cport) makeHWObject(new I2Cport(),Const.I2C_A_BASE, 0);
		iic_B = (I2Cport) makeHWObject(new I2Cport(),Const.I2C_B_BASE, 1);

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

	public I2Cport getI2CportA() { return iic_A; }
	public I2Cport getI2CportB() { return iic_B; }

}
