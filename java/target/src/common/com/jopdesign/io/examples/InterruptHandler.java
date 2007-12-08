package com.jopdesign.io.examples;

import util.Timer;

import com.jopdesign.io.*;
import com.jopdesign.sys.*;

public class InterruptHandler implements Runnable {

	public static void main(String[] args) {

		IOFactory fact = IOFactory.getFactory();		
		SerialPort sp = fact.getSerialPort();
		SysDevice sys = fact.getSysDevice();

		InterruptHandler ih = new InterruptHandler();
		fact.registerInterruptHandler(1, ih);
		
		// enable software interrupt 1
		fact.enableInterrupt(1);

		for (int i=0; i<20; ++i) {
			Timer.wd();
			int t = Timer.getTimeoutMs(200);
			while (!Timer.timeout(t));
			// trigger a SW interrupt via the system HW object
			sys.swInterrupt = 1;
			if (i==10) {
				fact.disableInterrupt(1);
//				fact.deregisterInterruptHandler(1);
			}
		}
	}

	
	public void run() {
		System.out.println("Interrupt fired!");
	}
}
