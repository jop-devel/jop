//
//	Rt.java
//
//	Test RT loop
//

public class Rt {

	public static final int IO_PORT = 0;
	public static final int IO_STATUS = 1;
	public static final int IO_UART = 2;
	public static final int IO_ECP = 3;
	public static final int IO_CNT = 10;
	public static final int IO_MS = 11;

	final static int INTERVAL = 24000;	// one ms
	final static int USEC = 24;			// one us

	static int next = 0;				// counter for next intervall

//
//	buffer for serial out
//
	static int[] buf;
	static int rdpt, wrpt;

	public static void main(String s[]) {


		buf = new int[16];
		rdpt = wrpt = 0;
		int cnt = 0;
		int x = 0;

		next = JopSys.rd(IO_CNT);

		for (;;) {					// forever loop

			do_serial();
			if (cnt==100) {
				cnt = 0;
				++x;
				putc('\n');
				print_04d(x);
				for (int i=0; i<x; ++i) {
					putc('.');
				}
			}
			++cnt;

			waitForNextInterval();
		}

	}

	static void do_serial() {

		if (rdpt!=wrpt) {
			if ((JopSys.rd(IO_STATUS)&1) != 0) {
				JopSys.wr(buf[rdpt], IO_UART);
				rdpt = (rdpt+1)&0x0f;
			}
		}
	}

	static void putc(int i) {

		if (((wrpt+1)&0x0f) == rdpt) {
			return;
//			do_serial();		// buffer full!	wait for serial line
		}
		buf[wrpt] = i;
		wrpt = (wrpt+1)&0x0f;
	}

	static void waitForNextInterval() {


		next += INTERVAL;
		int diff = next-JopSys.rd(IO_CNT);
		if (diff <= 0) {
			// missed time!!!
			rdpt = wrpt = 0;		// flush serial buffer
			print_char('\n');
			print_char('m');
			print_char('i');
			print_char('s');
			print_char('s');
			print_char('e');
			print_char('d');
			print_char('\n');
			print_06d(diff);
for (;;) do_serial();
		}

		while (next-JopSys.rd(IO_CNT) >= 0)
			;
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
		
	static void wait_serial() {

		while ((JopSys.rd(IO_STATUS)&1)==0) ;
	}

	static void print_char(int i) {

putc(i);
/*
		wait_serial();
		JopSys.wr(i, IO_UART);
*/
	}

}
