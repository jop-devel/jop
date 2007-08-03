package com.jopdesign.io.examples;

import com.jopdesign.io.*;

public class Example {

	public static void main(String[] args) {

		System.out.println("Hello World");

		IOFactory fact = IOFactory.getFactory();		
		SerialPort sp = fact.getSerialPort();
		
		String hello = "Hello World via Hardware Objects!";
		
		// Hello world with low-level device access
		for (int i=0; i<hello.length(); ++i) {
			// busy wait on transmit buffer empty
			while ((sp.status & SerialPort.MASK_TDRE) == 0)
				;
			// write a character
			sp.data = hello.charAt(i);
		}
	}
}
