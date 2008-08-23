package com.jopdesign.io.paper;

import com.jopdesign.io.SerialPort;

public class SerialHandler extends InterruptHandler {

	// A hardware object represents the serial device
	private SerialPort sp;
	
	final static int BUF_SIZE = 32;
	private volatile byte buffer[];
	private volatile int wrPtr, rdPtr;
	
	public SerialHandler(SerialPort sp) {
		this.sp = sp;
		buffer = new byte[BUF_SIZE];
		wrPtr = rdPtr = 0;
	}
	
	// This method is scheduled by the hardware
	public void handle() {
		byte val = (byte) sp.data;
		// check for buffer full
		if ((wrPtr+1)%BUF_SIZE!=rdPtr) {
			buffer[wrPtr++] = val;		
		}
		if (wrPtr>=BUF_SIZE) wrPtr=0;
		// enable interrupts again
		enableInterrupt();
	}
	
	// This method is invoked by the driver thread
	synchronized public int read() {
		if (rdPtr!=wrPtr) {
			int val = ((int) buffer[rdPtr++]) & 0xff;
			if (rdPtr>=BUF_SIZE) rdPtr=0;
			return val;
		} else {
			return -1;		// empty buffer
		}
	}
}
