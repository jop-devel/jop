//
//	Motor.java
//
//	Phasenanschnittsteuerung
//

public class Motor {

	public static final int IO_CNT = 10;


	static int next;				// counter for next intervall

	static int p1u;
	static int old_p1u;
	static int pos_cnt, neg_cnt;

	static int motor;
	static boolean stop;

	public static void main(String s[]) {

		init();
		next = JopSys.rd(IO_CNT);

int i = 0;	// simulate 50 Hz
p1u = 0;

		for (;;) {

if (i<10) {		
	p1u = 1;
} else {
	p1u = 0;
}
++i;
if (i==20) {
	i = 0;
}

			do_key();
			do_disp();
			do_user();
			do_motor();
			do_serial();

			waitForNextInterval();
		}
	}





	static void init() {

		next = 0;

		key = 0;
		key_pressed = 0;

		key_cnt = 0;
		new_key = 0;

		old_p1u = 0;
		motor = 0;
		pos_cnt = 0;
		neg_cnt = 0;
		stop = false;

		dispInit();

		dispData('a');
		dispCmd(0x080 | 0x00);		// back to pos 0;


	}


/**
*	Phasenanschnittssteuerung.
*	
*	Eine Halbwelle dauert 10ms => 10 Schritte bei Zykluszeit von 1ms
*
*	Zwei Counter fuer positive und negative Halbwelle.
*
*/
	// public static final int IO_TRIAC = 14;

	static void do_motor() {

		int p1, p2, p3;

		p1 = p2 = p3 = 0;

		// p1u = JopSys.rd(14) & 0x01;

		if (pos_cnt > 50) {
			stop = true;
			motor = 0;
		} else {
			stop = false;
			++pos_cnt;
			++neg_cnt;
		}
		if (old_p1u != p1u)	{		// Flanke
			old_p1u = p1u;
			if (p1u == 1) {
				pos_cnt = 0;
			} else {
				neg_cnt = 0;
			}
		}

		int abs_motor = motor;
		int dir = 0;
		if (motor < 0) {
			abs_motor = -motor;
			dir = 1;
		}

		if (abs_motor > 0) p1 = 1;

		if (abs_motor>0) {
			if (abs_motor==8) {
				p2 = p3 = 1;
			} else {
				int p2_nr = 2-abs_motor;
				if (p2_nr<0) p2_nr += 10;
				int p3_nr = 5-abs_motor;
				if (p3_nr<0) p3_nr += 10;

				if (p2_nr==pos_cnt || p2_nr==neg_cnt) {
					p2 = 1;
				}
				if (p3_nr==pos_cnt || p3_nr==neg_cnt) {
					p3 = 1;
				}
			}
		}

		if (stop) {
			p1 = p2 = p3 = 0;
		}
		JopSys.wr((dir<<3) + (p3<<2) + (p2<<1) + p1, 14);

	}

	static void do_serial() {
/*
print_char('0'+p1u);
print_char(' ');
// print_02d(pos_cnt);
// print_char(' ');
// print_02d(neg_cnt);
// print_char(' ');
print_char('0'+p1);
print_char(' ');
print_char('0'+p2);
print_char(' ');
print_char('0'+p3);
print_char('\n');
for (int i=0; i<100; ++i) wait1ms();
next = JopSys.rd(IO_CNT);
*/

		if (stop) {
			print_char('s');
			print_char(' ');
		} else {
			if (motor<0) {
				print_char('-');
				print_char('0'-motor);
			} else {
				print_char(' ');
				print_char('0'+motor);
			}
		}
		print_char('\r');
	}





	static void do_disp() {

		if (motor<0) {
			dispData('-');
			dispData('0'-motor);
		} else {
			dispData(' ');
			dispData('0'+motor);
		}
		dispCmd(0x080 | 0x00);		// back to pos 0;
	}

	static void do_user() {

		if (key_pressed==1) {
			key_pressed = 0;
			if (key==4 && motor<8) {
				++motor;
			} else if (key==2 && motor>-8) {
				--motor;
			} else if (key==56) {
				motor = 0;
			}
		}
	}


	static int key;
	static int key_pressed;

	static int key_cnt;
	static int new_key;

//
//	scan keyboard
//
/*
	key_inv <= not key_in;
	key_out(0) <= 'Z' when key_oc(0)='0' else '0';
	key_out(1) <= 'Z' when key_oc(1)='0' else '0';
	key_out(2) <= 'Z' when key_oc(2)='0' else '0';
	key_out(3) <= 'Z' when key_oc(3)='0' else '0';
*/
	// public static final int IO_KEY = 13;

	static void do_key() {

		int val = JopSys.rd(13);

		if (new_key==0 && val!=0) {
			new_key = (key_cnt<<4)+val;
		}
		++key_cnt;
		if (key_cnt==4) {
			key_cnt = 0;
			if (new_key==0) {
				key_pressed = 0;
			} else if (new_key!=key) {
				key_pressed = 1;
			}
			key = new_key;
			new_key = 0;
		}
		JopSys.wr(0x01<<key_cnt, 13);
	}


/*
	disp_d(7 downto 4) <= disp(3 downto 0);
	disp_rs <= disp(4);
	disp_e <= disp(5);
	disp_nwr <= '1';
*/
	// public static final int IO_DISP = 12;

	static void dispNib(int val) {

		JopSys.wr(val, 12);
		wait1us();
		JopSys.wr(0x20 | val, 12);	// set e to 1
		wait1us();
		JopSys.wr(val, 12);		// set e back to 0
		wait1us();
	}

	static void dispCmd(int val) {

		dispNib(val>>>4);
		dispNib(val&0x0f);
	}

	static void dispData(int val) {

		dispNib((val>>>4) | 0x10);
		dispNib((val&0x0f) | 0x10);
	}

	static void dispInit() {

		dispNib(0x20);					// function mode 4-bit, only one nibble!!!
		dispData(0x02);					// brightness
		dispCmd(0x02);					// cursor home
		dispCmd(0x06);					// entry mode
		dispCmd(0x0c);					// display on
		dispCmd(0x14);					// shift cursor
		dispCmd(0x080);					// set dd ram address
		dispCmd(0x01);					// display clear
		for (int j=0; j<3; ++j) {		// wait 3 ms
			wait1ms();
		}
	}

	// final static int INTERVAL = 24000;	// one ms
	// final static int USEC = 24;			// one us

	static void wait1us() {

		int i = JopSys.rd(IO_CNT)+24;
		while (i-JopSys.rd(IO_CNT) >= 0)
				;
	}

	static void wait1ms() {

		int i = JopSys.rd(IO_CNT)+24000;
		while (i-JopSys.rd(IO_CNT) >= 0)
				;
	}

	static void waitForNextInterval() {

		next += 24000;
/*
		int diff = next-JopSys.rd(IO_CNT);
		if (diff <= 0) {
			// missed time!!!
//			rdpt = wrpt = 0;		// flush serial buffer
			print_char('\n');
			print_char('m');
			print_char('i');
			print_char('s');
			print_char('s');
			print_char('e');
			print_char('d');
			print_char('\n');
			print_06d(diff);
//for (;;) do_serial();
for (;;) ;
		}
*/

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

	static void print_char(int i) {

	//public static final int IO_STATUS = 1;
	//public static final int IO_UART = 2;

		while ((JopSys.rd(1)&1)==0) ;
		JopSys.wr(i, 2);
	}

}
