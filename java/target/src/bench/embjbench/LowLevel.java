package embjbench;

import com.jopdesign.sys.*;
import util.*;

public class LowLevel {

	static boolean init;

	static int timeMillis() {
		return Native.rd(Native.IO_US_CNT)/1000;
	}

	static void msg(String msg) {

		if (!init) {
			Dbg.initSer();
			init = true;
		}

		Dbg.wr(msg);
	}

	static void msg(int val) {

		if (!init) {
			Dbg.initSer();
			init = true;
		}

		Dbg.intVal(val);
	}

	static void msg(String msg, int val) {

		msg(msg);
		msg(" ");
		msg(val);
	}
}
