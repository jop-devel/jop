package com.jopdesign.tools;

import com.jopdesign.sys.Const;

/**
 * Just a simple IO example class that implements the watchdog LED
 * to show how to extend IOSimMin with useful simulation.
 * 
 * @author martin
 *
 */
public class IOSimWD extends IOSimMin {

	int read(int addr) {
		return super.read(addr);
	}
	
	void write(int addr, int val) {

		switch (addr) {
			case Const.IO_WD:
				System.out.print((val==1) ? '*' : 'o');
				break;
			default:
				super.write(addr, val);
		}
	}
}
