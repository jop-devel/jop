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

		/*-
		 * test DTR and switch baud rate, HW hand shake
		Serial ser = new Serial(Const.IO_UART1_BASE);
		// switches also baud rate to 2400
		ser.setDTR(true);
		*/
		for (;;) {

			i = Native.rd(Const.IO_IN);
			Native.wr(i, Const.IO_LED);
			// battery switch is !d31 of LED port
			// Native.wr(-1, Const.IO_LED);
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

			printmA(Const.IO_ADC1);
			printmA(Const.IO_ADC2);
			i = Native.rd(Const.IO_ADC3);	// U = 11 * ADCout * 3.3 / (2^16-1)
			i *= 100;
			i /= 18054;
			// value is now in 1/10 mA or 1/10 V
			System.out.print(' ');
			System.out.print(i/10);
			System.out.print('.');
			System.out.print(i%10);
			System.out.print('V');

			// i = (i-460)/17+27; // this was temperature
			System.out.print("    \r");

			i = Timer.getTimeoutMs(200);
			while (!Timer.timeout(i)) {
				;
			}
		}
	}
	
	static void printmA(int addr) {
		
		int i;
		i = Native.rd(addr);	// I = ADCout * 3.3 / (100 * (2^16-1))
		i *= 100;
		i /= 19859;
		System.out.print(' ');
		System.out.print(i/10);
		System.out.print('.');
		System.out.print(i%10);
		System.out.print("mA");

	}

}
