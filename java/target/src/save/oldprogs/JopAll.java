
public class Jop {

	public static void main( String s[] ) {


/******** uart echo *********/

		int ch;


		print_char('H');
		print_char('a');
		print_char('l');
		print_char('l');
		print_char('o');
		print_char('\r');
		print_char('\n');

		for (;;) {
			while ((JopSys.rd(1)&2)==0); ch = JopSys.rd(2);
			print_char(ch);
		}

/*************/

/******** test invoke static *********

		int a, b, i, j;

		a = 1;
		b = 2;
		for (a=1; a<100; ++a) {
			i = add(a, b);
			print_02d(i);
		}

		for (;;)
			;


*************/

/******** mem read/write *********

		int cmd, ch, val, addr, data, i, j;

		addr = data = 0;
		cmd = ' ';

		for (;;) {
			if (cmd=='a' || cmd=='d') {
				val = 0;
				for (;;) {
					while ((JopSys.rd(1)&2)==0); ch = JopSys.uart_rd();
					while ((JopSys.rd(1)&1)==0); JopSys.uart_wr(ch);
					if (ch>='0' && ch <='9') {
						val <<= 4;
						val += ch-'0';
					} else if (ch>='a' && ch <='f') {
						val <<= 4;
						val += ch-'a'+10;
					} else {
						break;
					}
				}
				if (cmd=='a') addr = val;
				if (cmd=='d') data = val;
				cmd = ch;

			} else if (cmd=='r') {

				i = JopSys.rd(addr);
i &= 0xff;	// only bytes for now
				for (j=0;i>=16;++j) i-= 16; while ((JopSys.rd(1)&1)==0); JopSys.uart_wr(j<10?j+'0':j-10+'a');
				while ((JopSys.rd(1)&1)==0); JopSys.uart_wr(i<10?i+'0':i-10+'a');

				cmd = ' ';

			} else if (cmd=='w') {	// 'write'

				JopSys.wr(addr, data);

				cmd = ' ';

			} else {
				while ((JopSys.rd(1)&2)==0); cmd = JopSys.uart_rd();
				while ((JopSys.rd(1)&1)==0); JopSys.uart_wr(cmd);
			}
		}


******************/



/******** flash programmer *********

		int cmd, ch, val, addr, data, i, j;

		addr = data = 0;
		cmd = ' ';

		for (;;) {
			if (cmd=='a' || cmd=='d') {
				val = 0;
				for (;;) {
					while ((JopSys.rd(1)&2)==0); ch = JopSys.uart_rd();
					while ((JopSys.rd(1)&1)==0); JopSys.uart_wr(ch);
					if (ch>='0' && ch <='9') {
						val <<= 4;
						val += ch-'0';
					} else if (ch>='a' && ch <='f') {
						val <<= 4;
						val += ch-'a'+10;
					} else {
						break;
					}
				}
				if (cmd=='a') addr = val;
				if (cmd=='d') data = val;
				cmd = ch;

			} else if (cmd=='r') {

				i = JopSys.rd(addr+0x80000);
				for (j=0;i>=16;++j) i-= 16; while ((JopSys.rd(1)&1)==0); JopSys.uart_wr(j<10?j+'0':j-10+'a');
				while ((JopSys.rd(1)&1)==0); JopSys.uart_wr(i<10?i+'0':i-10+'a');

				cmd = ' ';

			} else if (cmd=='w') {	// 'write'

				JopSys.wr(addr+0x80000, data);

				cmd = ' ';

			} else if (cmd=='p') {	// program

				JopSys.wr(0x85555, 0x0aa);
				JopSys.wr(0x82aaa, 0x055);
				JopSys.wr(0x85555, 0x0a0);
				JopSys.wr(addr+0x80000, data);

				cmd = ' ';

			} else if (cmd=='!') {	// 'speed' programming

				while ((JopSys.rd(1)&2)==0); data = JopSys.uart_rd();
				JopSys.wr(0x85555, 0x0aa);
				JopSys.wr(0x82aaa, 0x055);
				JopSys.wr(0x85555, 0x0a0);
				JopSys.wr(addr+0x80000, data);
				i = JopSys.rd(11)+2;
				do {
					j = JopSys.rd(addr+0x80000);
				} while (j!=data && JopSys.rd(11)<i);
				while ((JopSys.rd(1)&1)==0); JopSys.uart_wr(j);
				
				if (j==data) addr++;
				cmd = ' ';

			} else if (cmd=='x') {	// erase

				JopSys.wr(0x85555, 0x0aa);
				JopSys.wr(0x82aaa, 0x055);
				JopSys.wr(0x85555, 0x080);
				JopSys.wr(0x85555, 0x0aa);
				JopSys.wr(0x82aaa, 0x055);
				JopSys.wr(0x85555, 0x010);
				addr = 0;
				data = 0xff;

				cmd = ' ';

			} else {
				while ((JopSys.rd(1)&2)==0); cmd = JopSys.uart_rd();
				while ((JopSys.rd(1)&1)==0); JopSys.uart_wr(cmd);
			}
		}


*****************/




/******** prime perf test (ecp) **********


		int i, j, k, t1, t2;

		t1 = JopSys.rd(11);
		t2 = JopSys.rd(11)-t1;

		while ((JopSys.rd(1)&4)==0); JopSys.wr(' ', 3);
		while ((JopSys.rd(1)&4)==0); JopSys.wr(' ', 3);
		while ((JopSys.rd(1)&4)==0); JopSys.wr('\r', 3);
		while ((JopSys.rd(1)&4)==0); JopSys.wr('\n', 3);

		t1 = JopSys.rd(11);
		for (i=3; i<2000; ++i) {
			for (j=2; j<i; ++j) {
				for (k=i; k>0; ) {
					k -= j;
				}
				if (k==0) break;
			}
			k = i;
			if (j==i) {
				for (j=0;i>999;++j) i-= 1000; while ((JopSys.rd(1)&4)==0); JopSys.wr(j+'0', 3);
				for (j=0;i>99;++j) i-= 100; while ((JopSys.rd(1)&4)==0); JopSys.wr(j+'0', 3);
				for (j=0;i>9;++j) i-= 10; while ((JopSys.rd(1)&4)==0); JopSys.wr(j+'0', 3);
				while ((JopSys.rd(1)&4)==0); JopSys.wr(i+'0', 3);
				while ((JopSys.rd(1)&4)==0); JopSys.wr(' ', 3);
			}
			i = k;
		}
		t1 = JopSys.rd(11)-t1-t2;
		i = t1;
		while ((JopSys.rd(1)&4)==0); JopSys.wr('\r', 3);
		while ((JopSys.rd(1)&4)==0); JopSys.wr('\n', 3);
		for (j=0;i>9999;++j) i-= 10000; while ((JopSys.rd(1)&4)==0); JopSys.wr(j+'0', 3);
		for (j=0;i>999;++j) i-= 1000; while ((JopSys.rd(1)&4)==0); JopSys.wr(j+'0', 3);
		while ((JopSys.rd(1)&4)==0); JopSys.wr('.', 3);
		for (j=0;i>99;++j) i-= 100; while ((JopSys.rd(1)&4)==0); JopSys.wr(j+'0', 3);
		for (j=0;i>9;++j) i-= 10; while ((JopSys.rd(1)&4)==0); JopSys.wr(j+'0', 3);
		while ((JopSys.rd(1)&4)==0); JopSys.wr(i+'0', 3);

//		while ((JopSys.rd(1)&4)==0); JopSys.wr('\r', 3);
//		while ((JopSys.rd(1)&4)==0); JopSys.wr('\n', 3);



*************/


/******** prime perf test (uart) **********


		int i, j, k, t1, t2;

		t1 = JopSys.rd(11);
		t2 = JopSys.rd(11)-t1;

		t1 = JopSys.rd(11);
		for (i=3; i<2000; ++i) {
			for (j=2; j<i; ++j) {
				for (k=i; k>0; ) {
					k -= j;
				}
				if (k==0) break;
			}
			k = i;
			if (j==i) {
				for (j=0;i>999;++j) i-= 1000; while ((JopSys.rd(1)&1)==0); JopSys.uart_wr(j+'0');
				for (j=0;i>99;++j) i-= 100; while ((JopSys.rd(1)&1)==0); JopSys.uart_wr(j+'0');
				for (j=0;i>9;++j) i-= 10; while ((JopSys.rd(1)&1)==0); JopSys.uart_wr(j+'0');
				while ((JopSys.rd(1)&1)==0); JopSys.uart_wr(i+'0');
				while ((JopSys.rd(1)&1)==0); JopSys.uart_wr(' ');
			}
			i = k;
		}
		t1 = JopSys.rd(11)-t1-t2;
		i = t1;
		while ((JopSys.rd(1)&1)==0); JopSys.uart_wr('\r');
		while ((JopSys.rd(1)&1)==0); JopSys.uart_wr('\n');
		for (j=0;i>9999;++j) i-= 10000; while ((JopSys.rd(1)&1)==0); JopSys.uart_wr(j+'0');
		for (j=0;i>999;++j) i-= 1000; while ((JopSys.rd(1)&1)==0); JopSys.uart_wr(j+'0');
		while ((JopSys.rd(1)&1)==0); JopSys.uart_wr('.');
		for (j=0;i>99;++j) i-= 100; while ((JopSys.rd(1)&1)==0); JopSys.uart_wr(j+'0');
		for (j=0;i>9;++j) i-= 10; while ((JopSys.rd(1)&1)==0); JopSys.uart_wr(j+'0');
		while ((JopSys.rd(1)&1)==0); JopSys.uart_wr(i+'0');

		while ((JopSys.rd(1)&1)==0); JopSys.uart_wr('\r');
		while ((JopSys.rd(1)&1)==0); JopSys.uart_wr('\n');



*************/

/*********** blinking led *********

		int i, j, k, t1, t2;

		JopSys.outp(-1);
		for (;;) {
			t1 = JopSys.rd(11);
			while (JopSys.rd(11)-t1<500)
				;
			JopSys.outp(0);
			t1 = JopSys.rd(11);
			while (JopSys.rd(11)-t1<500)
				;
			JopSys.outp(-1);
		}

*************/

/******** softuart perf test
		int i, j, k;

		k = 1;
		while ((JopSys.rd(1)&1)==0); JopSys.uart_wr('\r');
		while ((JopSys.rd(1)&1)==0); JopSys.uart_wr('\n');

		i = JopSys.cnt();
		j = JopSys.cnt()-i;

		i = JopSys.cnt();
			k = (k<<1) | (JopSys.cnt() & 0x01);		// should be port
		i = JopSys.cnt()-i;

		i -= j;
		for (j=0;i>999;++j) i-= 1000; while ((JopSys.rd(1)&1)==0); JopSys.uart_wr(j+'0');
		for (j=0;i>99;++j) i-= 100; while ((JopSys.rd(1)&1)==0); JopSys.uart_wr(j+'0');
		for (j=0;i>9;++j) i-= 10; while ((JopSys.rd(1)&1)==0); JopSys.uart_wr(j+'0');
		while ((JopSys.rd(1)&1)==0); JopSys.uart_wr(i+'0');
************ end softuart ***********/


/********* begin for JOP *************/
/*
		int i,j,k;
		int start, end, meastim;
		final int[] istr = { 'h', 'a', 'l', 'l', 'o', ' ',
							'w', 'o', 'r', 'l', 'd', '\n', 0 };

		start = JopSys.cnt();
		end = JopSys.cnt();
		meastim = end-start;

// TestPerf 1
		k = 0;
		start = JopSys.cnt();

			k <<= 1;
			j = JopSys.cnt();		// should be port
			j &= 0x01;
			k |= j;

		end = JopSys.cnt();
		print_04d(end-start-meastim);
		print_char(' ');
		print_char('\n');

// TestPerf 2
		k = 0;
		start = JopSys.cnt();

			k = (k<<1) | (JopSys.cnt() & 0x01);		// should be port

		end = JopSys.cnt();
		print_04d(end-start-meastim);
		print_char(' ');
		print_char('\n');
*/

/*
		print_hex(0x12345678);
		print_char(' ');
		print_hex(0xabcdef00);
		print_char(' ');
		print_char('\n');
		print_string(istr);
*/

//		time();
/*
		for (;;) {
			j = JopSys.rd(iobase);
			j &= 0x01;
			if (j==0) {
				for (i=0; i<50; ++i) {
					j = JopSys.rd(iobase);
					j &= 0x01;
					if (j==0) {
						print_char('0');
					} else {
						print_char('1');
					}
				}
			}
		}
*/
	}

/*
	static void time() {

		int i, j;
		int h, m, s;

		h = m = s = 0;
		i = JopSys.cnt();

		for (;;) {
			j = JopSys.cnt();
			if (j>=i) {
				i += 24000000;
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
				print_char('\r');


				if ((s & 1) == 0) {
//					JopSys.wr(iobase, 0x01);
				} else {
//					JopSys.wr(iobase, 0x04);
				}

			}
		}
	}
*/

	static void print_02d(int i) {

		int j;
		for (j=0;i>9;++j) i-= 10;
		print_char(j+'0');
		print_char(i+'0');
	}
		
	static void print_04d(int i) {

		int j, k, l;
		for (j=0;i>999;++j) i-= 1000;
		for (k=0;i>99;++k) i-= 100;
		for (l=0;i>9;++l) i-= 10;
		print_char(j+'0');
		print_char(k+'0');
		print_char(l+'0');
		print_char(i+'0');
	}
		


	static void print_string(int[] istr) {

		int i;

		for (i=0; istr[i]!=0; ++i) {
			print_char(istr[i]);
		}
	}

	static void print_hex(int i) {

		int j, k;

		for (j=0; j<8; ++j) {
			k = i>>((7-j)<<2);
			k &= 0x0f;
			k = k<10 ? k+'0' : k-10+'a';
			print_char(k);
		}
	}

	static void wait_serial() {

		while ((JopSys.rd(1)&1)==0) ;
	}

	static void print_char(int i) {

		wait_serial();
		JopSys.wr(i, 2);
	}

}
