package oebb;

import com.jopdesign.sys.Native;

/**
*	Scan keyboard.
*/

public class Keyboard {

	public static final int C = 1;
	public static final int B = 56;
	public static final int E = 4;
	public static final int UP = 40;
	public static final int DOWN = 24;
	public static final int BLACK = 8;

	public static final int K0 = 2;
	public static final int K1 = 49;
	public static final int K2 = 50;
	public static final int K3 = 52;
	public static final int K4 = 33;
	public static final int K5 = 34;
	public static final int K6 = 36;
	public static final int K7 = 17;
	public static final int K8 = 18;
	public static final int K9 = 20;

	private static int io_addr;

	public static boolean pressed;

	private static int key;
	private static int cnt;
	private static int new_key;

	public static void init(int addr) {

		io_addr = addr;
		key = 0;
		cnt = 0;
	}

/*
	key_inv <= not key_in;
	key_out(0) <= 'Z' when key_oc(0)='0' else '0';
	key_out(1) <= 'Z' when key_oc(1)='0' else '0';
	key_out(2) <= 'Z' when key_oc(2)='0' else '0';
	key_out(3) <= 'Z' when key_oc(3)='0' else '0';
*/
	static void loop() {

		int val = Native.rd(io_addr);

		if (new_key==0 && val!=0) {
			new_key = (cnt<<4)+val;
		}
		++cnt;
		if (cnt==4) {
			cnt = 0;
			if (new_key==0) {
				pressed = false;
			} else if (new_key!=key) {
				pressed = true;
			}
			key = new_key;
			new_key = 0;
		}
		Native.wr(0x01<<cnt, io_addr);
	}

	static int rd() {

		if (pressed) {
			pressed = false;
			return key;
		} else {
			return -1;
		}
	}

	/** get value without consuming it */
	static int peek() {

		if (pressed) {
			return key;
		} else {
			return -1;
		}
	}

	static void unread(int val) {

		key = val;
		pressed = true;
	}

	public static int num(int val) {

		if (val==K0) return 0;
		if (val==K1) return 1;
		if (val==K2) return 2;
		if (val==K3) return 3;
		if (val==K4) return 4;
		if (val==K5) return 5;
		if (val==K6) return 6;
		if (val==K7) return 7;
		if (val==K8) return 8;
		if (val==K9) return 9;

		return -1;
	}
}
