
package test;

import util.*;
import com.jopdesign.sys.Native;

class M {

	public static void main( String s[] ) {

		Dbg.initSerWait();
		Timer.init(20000000, 10);

		int t1 = Native.rd(Native.IO_CNT);
		int t2 = Native.rd(Native.IO_CNT);
		int diff = t2-t1;

/*
		Dbg.intVal(diff);

		t1 = Native.rd(Native.IO_CNT);
		Util.mul(2, 3);
		t2 = Native.rd(Native.IO_CNT);
		t2 = t2-t1-diff;

		Dbg.intVal(t2);

		for (;;) ;
*/

		int val = 0;
		for (;;) {
			if (val - Native.rd(Native.IO_US_CNT) < 0) {
				Dbg.intVal(val);
				val = Native.rd(Native.IO_US_CNT) + 1000000;
			}
		}

	}
}
