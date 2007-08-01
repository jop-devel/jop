package com.jopdesign.io;

public class IOMinFactory {
	
	private SysDevice sys;
	private SerialPort sp;

	private final static int SYS_ADDRESS = 0xffff0000;
	private final static int SERIAL_ADDRESS = 0xffff0010;

	IOMinFactory() {
		sys = (SysDevice) JVMIOMagic(SYS_ADDRESS);
		sp = (SerialPort) JVMIOMagic(SERIAL_ADDRESS);
	};
	
	static IOMinFactory single = new IOMinFactory();;
	
	public static IOMinFactory getFactory() {		
		return single;
	}
	
	public SerialPort getSerialPort() {
		return sp;
	}
	
	public SysDevice getSysDevice() {
		return sys;
	}
	
	// here comes the magic!!!
	native Object JVMIOMagic(int address);
}
