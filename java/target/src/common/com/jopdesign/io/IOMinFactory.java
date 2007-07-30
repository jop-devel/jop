package com.jopdesign.io;

public class IOMinFactory {

	IOMinFactory() {
		mypp = (ParallelPort) PPmagic(PP_ADDRESS);
	};
	
	static IOMinFactory single = new IOMinFactory();;
	
	public static IOMinFactory getIOMinFactory() {
		
		return single;
	}
	
	final static int PP_ADDRESS = 0x300;
	ParallelPort mypp;
	
	public ParallelPort getParallelPort() {
		return mypp;
	}
	
	// here comes the magic!!!
	native Object PPmagic(int address);
}
