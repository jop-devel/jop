package com.jopdesign.io.examples;

import util.Timer;

import com.jopdesign.io.*;
import com.jopdesign.sys.*;

public class TestPerformance {

	public static void main(String[] args) {

		int val = '*';
		System.out.println("Performance Test of HW Objects");

		DspioFactory fact = DspioFactory.getDspioFactory();		
		SerialPort sp = fact.getSerialPort();
		SysDevice sys = fact.getSysDevice();
		
		int ts, te, to;
		ts = Native.rdMem(Const.IO_CNT);
		te = Native.rdMem(Const.IO_CNT);
		to = te-ts;
		ts = Native.rdMem(Const.IO_CNT);
//		val = Native.rdMem(Const.IO_UART);
//		Native.wrMem(val, Const.IO_UART);
//		val = sp.data;
		sp.data = val;
		te = Native.rdMem(Const.IO_CNT);
		System.out.println(te-ts-to);

		
	}
}
