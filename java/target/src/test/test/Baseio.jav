package test;

/**
*	Testprogram for basio board.
*/

import com.jopdesign.sys.*;

import util.*;

class Baseio {

	public static void main( String s[] ) {

		int i, j;
		int cnt = 5;
		int o = 0x08;

		Dbg.init();
		Timer.init(20000000, 10);
		for (;;) {

			i = Native.rd(Native.IO_INOUT);
			for (j=0x80; j!=0; j>>=1) {
				if ((i & j)!=0) {
					Dbg.wr('0');			// inverted
				} else {
					Dbg.wr('1');
				}
				Dbg.wr(' ');
			}

			--cnt;
			if (cnt==0) {
				Timer.wd();
				cnt = 5;
				o >>= 1;
				if (o==0) o = 0x08;
				Native.wr(o, Native.IO_INOUT);
			}

			i = Native.rd(Native.IO_ADC);
			Dbg.intVal(i>>>16);
			i &= 0xffff;
			Dbg.intVal(i);
			i = (i-460)/17+27;
			Dbg.intVal(i);
			Dbg.wr('C');
			Dbg.wr('\r');
			Dbg.wr('\n');

			Timer.sleep(200);
		}
	}

}
