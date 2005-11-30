package kfl.test;

import kfl.*;
//
//	I.java
//

public class I {

	public static final int IO_PORT = 0;
	public static final int IO_STATUS = 1;
	public static final int IO_UART = 2;
	public static final int IO_ECP = 3;
	public static final int IO_IADC = 5;
	public static final int IO_WD = 7;
	public static final int IO_CNT = 10;


	public static void main( String s[] ) {

		JopSys.wr(0, IO_WD);		// make WD happy
		JopSys.wr(1, IO_WD);
		JopSys.wr(0, IO_WD);
		loop();
	}

	static void loop() {

		int i;
		int next;

		next = 0;

		for (;;) {


			i = JopSys.rd(IO_IADC);
			print_04d(i);
			print_char(' ');

			for (i=0; i<10; ++i) {
				JopSys.wr(1, IO_WD);
				next = waitForNextInterval(next);
				JopSys.wr(0, IO_WD);
				next = waitForNextInterval(next);
			}
		}
	}

	static int waitForNextInterval(int next) {

		final int INTERVAL = 20000;		// one ms

		if (next==0) {
			next = JopSys.rd(IO_CNT)+INTERVAL;
		} else {
			next += INTERVAL;
		}

		while (next-JopSys.rd(IO_CNT) >= 0)
				;

		return next;
	}

	static void print_02d(int i) {

		int j;
		for (j=0;i>9;++j) i-= 10;
		print_char(j+'0');
		print_char(i+'0');
	}
		
	static void print_04d(int i) {

		if (i<0) {
			print_char('-');
			i = -i;
		}

		int j, k, l;
		for (j=0;i>999;++j) i-= 1000;
		for (k=0;i>99;++k) i-= 100;
		for (l=0;i>9;++l) i-= 10;
		print_char(j+'0');
		print_char(k+'0');
		print_char(l+'0');
		print_char(i+'0');
	}
		

	static void print_06d(int i) {

		if (i<0) {
			print_char('-');
			i = -i;
		}

		int j;

		for (j=0;i>99999;++j) i-= 100000;
		print_char(j+'0');
		for (j=0;i>9999;++j) i-= 10000;
		print_char(j+'0');
		for (j=0;i>999;++j) i-= 1000;
		print_char(j+'0');
		for (j=0;i>99;++j) i-= 100;
		print_char(j+'0');
		for (j=0;i>9;++j) i-= 10;
		print_char(j+'0');
		print_char(i+'0');
	}
		


/*
	static void print_string(int[] istr) {

		int i;

		for (i=0; istr[i]!=0; ++i) {
			print_char(istr[i]);
		}
	}
*/

	static void print_hex(int i) {

		int j, k;

		for (j=0; j<8; ++j) {
			k = i>>>((7-j)<<2);
			k &= 0x0f;
			k = k<10 ? k+'0' : k-10+'a';
			print_char(k);
		}
	}

	static void wait_serial() {

		while ((JopSys.rd(IO_STATUS)&1)==0) ;
	}

	static void print_char(int i) {

		wait_serial();
		JopSys.wr(i, IO_UART);
	}

}
