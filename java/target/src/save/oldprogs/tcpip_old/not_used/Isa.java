package tcpip;

/**
*	Isa.java
*
*	isa bus interface (for ethernet chip)
*/

import com.jopdesign.sys.*;
import util.*;

public class Isa {

	private static final int IO_CTRL = 5;
	private static final int IO_DATA = 6;

	private static final int RESET = 0x20;
	private static final int RD = 0x40;
	private static final int WR = 0x80;
	private static final int DIR = 0x100;	// means driving out
/*
			isa_a <= din(4 downto 0);
			isa_reset <= din(5);
			isa_nior <= not din(6);
			isa_niow <= not din(7);
			isa_dir <= din(8);
*/


	public static void main(String[] args) {

		Timer.init(20000000, 5);
		for (;;) {
			Timer.sleepWd(4000);
			reset();
		}
	}

/**
*	reset isa bus
*/
	public static void reset() {

		Native.wr(RESET, IO_CTRL);				// isa bus reset
		Timer.sleep(5);
		Native.wr(0, IO_CTRL);					// disable reset
		Timer.sleep(5);
	}
/**
*	'ISA Bus' io write cycle.
*/
	public static void wr(int addr, int data) {

		Native.wr(data, IO_DATA);				// data in buffer

		Native.wr(addr | DIR, IO_CTRL);			// addr and drive data out
		Native.wr(addr | WR | DIR, IO_CTRL);	// niow low
		Native.wr(addr | DIR, IO_CTRL);			// niow high again
		Native.wr(addr, IO_CTRL);				// disable dout
	}

/**
*	'ISA Bus' io read cycle.
*/
	public static int rd(int addr) {

		Native.wr(addr, IO_CTRL);				// addr
		Native.wr(addr | RD, IO_CTRL);			// nior low
		int ret = Native.rd(IO_DATA);			// read data
		Native.wr(addr, IO_CTRL);				// nior high again

		return ret;
	}
}
