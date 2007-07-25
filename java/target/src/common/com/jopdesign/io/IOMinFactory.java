package com.jopdesign.io;

public class IOMinFactory {

	IOMinFactory() {
		mypp = (ParallelPort) PPmagic(PP_ADDRESS);
	};
	
	static IOMinFactory single;
	
	public static IOMinFactory getIOMinFactory() {
		
		// TODO: get the shit clinit with new done!!!!
		if (single==null) {
			single = new IOMinFactory();
		}
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
