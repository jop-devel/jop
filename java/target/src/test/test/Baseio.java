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
		for (;;) {

			i = Native.rd(Const.IO_IN);
			for (j=0x200; j!=0; j>>=1) {
				if ((i & j)!=0) {
					System.out.print('0');			// inverted
				} else {
					System.out.print('1');
				}
				System.out.print(' ');
			}

			--cnt;
			if (cnt==0) {
				Timer.wd();
				cnt = 5;
				o >>= 1;
				if (o==0) o = 0x08;
				Native.wr(o, Const.IO_OUT);
			}

			i = Native.rd(Const.IO_ADC1);
			System.out.print(i);
			i = Native.rd(Const.IO_ADC2);
			System.out.print(i);
			System.out.print("    \r");
			// i = (i-460)/17+27; // this was temperature

			i = Timer.getTimeoutMs(200);
			while (!Timer.timeout(i)) {
				;
			}
		}
	}

}
