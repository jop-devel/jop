package com.jopdesign.io;

/**
 * Not used - just the paper example
 * @author martin
 *
 */

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
