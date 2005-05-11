package jbe;

import util.Dbg;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

/**
*	low level time and output for JOP.
*/

public class LowLevel {

	static boolean init;
	

	public static int timeMillis() {
		return Native.rd(Const.IO_US_CNT)/1000;
	}
	
	public static int clockTicks() {
		return Native.rd(Const.IO_US_CNT);
	}

	public static void msg(String msg) {

		if (!init) {
			Dbg.initSerWait();
			init = true;
		}

		Dbg.wr(msg);
		Dbg.wr(' ');
	}

	public static void msg(int val) {

		if (!init) {
			Dbg.initSerWait();
			init = true;
		}

		Dbg.intVal(val);
	}

	static void msg(String msg, int val) {

		msg(msg);
		msg(val);
	}

	static void lf() {

		Dbg.lf();
	}
}
