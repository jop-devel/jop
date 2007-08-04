package com.jopdesign.io.examples;

import util.Timer;

import com.jopdesign.io.*;

public class Example {

	public static void main(String[] args) {

		System.out.println("Hello World");

		DspioFactory fact = DspioFactory.getDspioFactory();		
		SerialPort sp = fact.getSerialPort();
		SysDevice sys = fact.getSysDevice();
		SerialPort usb = fact.getUsbPort();
		
		String hello = "Hello World via Hardware Objects!";
		
		// Hello world with low-level device access
		for (int i=0; i<hello.length(); ++i) {
			// busy wait on transmit buffer empty
			while ((sp.status & SerialPort.MASK_TDRE) == 0)
				;
			// write a character
			sp.data = hello.charAt(i);
		}
		
		hello = "write to USB port";
		// Hello world with low-level device access
		for (int i=0; i<hello.length(); ++i) {
			// busy wait on transmit buffer empty
			while (!usb.txEmpty())
				;
			// write a character
			usb.write(hello.charAt(i));
		}

		int [] sa = fact.getArray();
		// array does not work
		// xaload/xastore or implemented in the memory interface
		// no connection to IO SimpCon!
		// TODO: do the IO/Memory MUX after sc_mem

		for (int i=0; i<20; ++i) {
			Timer.wd();
			int t = Timer.getTimeoutMs(200);
			while (!Timer.timeout(t)) ;
		}
	}
}
