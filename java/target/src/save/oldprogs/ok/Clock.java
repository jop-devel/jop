//
//	Clock.java
//

public class Clock {

	public static final int IO_PORT = 0;
	public static final int IO_STATUS = 1;
	public static final int IO_UART = 2;
	public static final int IO_ECP = 3;
	public static final int IO_CNT = 10;
	public static final int IO_MS = 11;


	public static void main( String s[] ) {

		time();
	}

	static void time() {

		int next;
		int h, m, s, ms;


		h = m = s = ms = 0;
		next = 0;

		for (;;) {

			++ms;
			if (ms==1000) {
				ms = 0;
				++s;
				if (s==60) {
					s = 0;
					++m;
				}
				if (m==60) {
					m = 0;
					++h;
				}
				if (h==24) h = 0;
				print_02d(h);
				print_char(':');
				print_02d(m);
				print_char(':');
				print_02d(s);
/*
if (s==10) {
	for (int i=0; i<10;++i) print_char('.');
}
*/
				print_char('\r');

				if ((s & 1) == 0) {
					JopSys.wr(0x01, IO_PORT);
				} else {
					JopSys.wr(0x04, IO_PORT);
				}
			}

			next = waitForNextInterval(next);
		}
	}

	static int waitForNextInterval(int next) {

		final int INTERVAL = 24000;		// one ms

		if (next==0) {
			next = JopSys.rd(IO_CNT)+INTERVAL;
		} else {
			next += INTERVAL;
		}

		int diff = next-JopSys.rd(IO_CNT);
		if (diff <= 0) {
			// missed time!!!
			print_char('\n');
			print_char('m');
			print_char('i');
			print_char('s');
			print_char('s');
			print_char('e');
			print_char('d');
			print_char('\n');
			print_06d(diff);
			print_char(' ');
//			print_hex(diff);
for (;;) ;
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
