package com.jopdesign.io.examples;

import com.jopdesign.io.*;

public class Example {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		IOMinFactory fact = IOMinFactory.getIOMinFactory();
		
		ParallelPort pp = fact.getParallelPort();
		
		// set upper byte as output
		pp.control = 0xff00;
		// write 0x12 to the output port
		pp.data = 0x1200;
		// read the lower byte
		int val = pp.data;
		
	}

}
