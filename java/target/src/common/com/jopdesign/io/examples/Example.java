package com.jopdesign.io.examples;

import util.Timer;

import com.jopdesign.io.*;

public class Example {

	public static void main(String[] args) {

		System.out.println("Hello World");

		IOFactory fact = IOFactory.getFactory();		
		SerialPort sp = fact.getSerialPort();
		SysDevice sys = fact.getSysDevice();
		
		String hello = "Hello World via Hardware Objects!";
		
		// Hello world with low-level device access
		for (int i=0; i<hello.length(); ++i) {
			// busy wait on transmit buffer empty
			while ((sp.status & SerialPort.MASK_TDRE) == 0)
				;
			// write a character
			sp.data = hello.charAt(i);
		}
		
		for (int i=0; i<20; ++i) {
			Timer.wd();
			int t = Timer.getTimeoutMs(200);
			while (!Timer.timeout(t)) ;
		}
	}
}
