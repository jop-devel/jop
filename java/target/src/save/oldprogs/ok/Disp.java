//
//	Disp.java
//
//	Test display (and keyboard)
//
//		read_mem geht so nicht mehr!
//

public class Disp {

	public static final int IO_PORT = 0;
	public static final int IO_STATUS = 1;
	public static final int IO_UART = 2;
	public static final int IO_ECP = 3;
	public static final int IO_CNT = 10;
	public static final int IO_MS = 11;
	public static final int IO_DISP = 12;
	public static final int IO_KEY = 13;

	public static final int MEM_RD_ADDR = 4;
	public static final int MEM_RD_DATA = 4;
	public static final int MEM_WR_ADDR = 5;
	public static final int MEM_WR_DATA = 6;
	public static final int MEM_STATUS = 5;

	final static int INTERVAL = 24000;	// one ms
	final static int USEC = 24;			// one us

	static int next = 0;				// counter for next intervall

	public static void main(String s[]) {

		int last_key = 0;


		dispInit();

		next = JopSys.rd(IO_CNT);

		dispData('a');
		dispData('b');
		dispData('c');
		dispData('d');

		for (int j=0; j<3000; ++j) {
			waitForNextInterval();
		}

		dispCmd(0x080 | 0x40);		// second line

		dispData('H');
		dispData('a');
		dispData('l');
		dispData('l');
		dispData('o');
/*
		for (int i=0;i<20 ;++i) {
			int ch = read_mem(i);
			if (ch==0) break;
			dispData(ch);
		}
*/

		for (;;) {

			do_key();

			if (key_pressed==1) {
				print_02d(key);
				key_pressed = 0;
			}

			waitForNextInterval();
		}
	}


	static int key = 0;
	static int key_pressed = 0;

	static int key_cnt = 0;
	static int new_key = 0;

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
	static void do_key() {

		int val = JopSys.rd(IO_KEY);

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
		JopSys.wr(0x01<<key_cnt, IO_KEY);
	}


/*
	disp_d(7 downto 4) <= disp(3 downto 0);
	disp_rs <= disp(4);
	disp_e <= disp(5);
	disp_nwr <= '1';
*/
	static void dispNib(int val) {

		JopSys.wr(val, IO_DISP);
		wait1us();
		JopSys.wr(0x20 | val, IO_DISP);	// set e to 1
		wait1us();
		JopSys.wr(val, IO_DISP);		// set e back to 0
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

	static void wait1us() {

		int i = JopSys.rd(IO_CNT)+USEC;
		while (i-JopSys.rd(IO_CNT) >= 0)
				;
	}

	static void wait1ms() {

		int i = JopSys.rd(IO_CNT)+INTERVAL;
		while (i-JopSys.rd(IO_CNT) >= 0)
				;
	}

	static void waitForNextInterval() {

		next += INTERVAL;
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

		while (next-JopSys.rd(IO_CNT) >= 0)
			;
	}

	static int read_mem(int addr) {

		JopSys.wr(addr, MEM_RD_ADDR);
		while (JopSys.rd(MEM_STATUS)!=0) ;
		return JopSys.rd(MEM_RD_DATA);
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
		
	static void print_char(int i) {

		while ((JopSys.rd(IO_STATUS)&1)==0) ;
		JopSys.wr(i, IO_UART);
	}

}
