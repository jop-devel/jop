
package test;

import util.*;
import com.jopdesign.sys.*;

class M {

	public static void main( String s[] ) {

		Dbg.initSerWait();

		int t1 = Native.rd(Const.IO_CNT);
		int t2 = Native.rd(Const.IO_CNT);
		int diff = t2-t1;

/*
		Dbg.intVal(diff);

		t1 = Native.rd(Const.IO_CNT);
		Util.mul(2, 3);
		t2 = Native.rd(Const.IO_CNT);
		t2 = t2-t1-diff;

		Dbg.intVal(t2);

		for (;;) ;
*/

		int val = 0;
		for (;;) {
			if (val - Native.rd(Const.IO_US_CNT) < 0) {
				Dbg.intVal(val);
				val = Native.rd(Const.IO_US_CNT) + 1000000;
			}
		}

	}
}
