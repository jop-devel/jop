package com.jopdesign.io.paper;

import com.jopdesign.io.SerialPort;

public class IOSystem {
	
	private IOSystem() {}
	
	static native ParallelPort JVMPPMagic();
	
	// do some JVM magic to create the object
	private static ParallelPort pp = JVMPPMagic();
	
	private static SerialPort sp;
	
	public static ParallelPort getParallelPort() {
		return pp;
	}
	
	public static SerialPort getSerialPort() {
		return sp;
	}
}
