package com.jopdesign.io.examples;

import util.Timer;

import com.jopdesign.io.*;
import com.jopdesign.sys.*;

public class MeasureIH implements Runnable {

	static int ts, te, to;

	public static void main(String[] args) {

		IOFactory fact = IOFactory.getFactory();		
		SerialPort sp = fact.getSerialPort();
		SysDevice sys = fact.getSysDevice();

		MeasureIH ih = new MeasureIH();
		fact.registerInterruptHandler(1, ih);
		
		System.out.println("Measure IH respons time");
		
		// enable software interrupt 1
		fact.enableInterrupt(1);

		// measure overhead
		ts = Native.rdMem(Const.IO_CNT);
		sys.swInterrupt = 2;
		te = Native.rdMem(Const.IO_CNT);
		to = te-ts;
		ts = Native.rdMem(Const.IO_CNT);

		for (int i=0; i<200; ++i) {
//			Timer.wd();
//			int t = Timer.getTimeoutMs(200);
//			while (!Timer.timeout(t));
			ts = Native.rdMem(Const.IO_CNT);
			// trigger a SW interrupt via the system HW object
			sys.swInterrupt = 1;
		}
	}

	
	public void run() {
		te = Native.rdMem(Const.IO_CNT);
		System.out.print(te-ts-to);
		System.out.print(" ");
		System.out.print(JVMHelp.ts-ts-to);
		System.out.print(" ");
	}
}
