package com.jopdesign.io.examples;

import util.Timer;

import com.jopdesign.io.*;
import com.jopdesign.sys.*;

public class InterruptHandler implements Runnable {

	public static void main(String[] args) {

		IOFactory fact = IOFactory.getFactory();		
		SerialPort sp = fact.getSerialPort();

		InterruptHandler ih = new InterruptHandler();
		fact.registerInterruptHandler(1, ih);
		
		
		// should be a function in some system class to
		// set individual enables and remember the mask
		Native.wr(-1, Const.IO_INTMASK);

		for (int i=0; i<20; ++i) {
			Timer.wd();
			int t = Timer.getTimeoutMs(200);
			while (!Timer.timeout(t));
			Native.wr(1, Const.IO_SWINT);
			Native.wr(2, Const.IO_SWINT);
		}
	}

	
	public void run() {
		System.out.println("Interrupt fired!");
	}
}
