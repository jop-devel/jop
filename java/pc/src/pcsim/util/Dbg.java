package util;

/**
*	debug output for simulation with pc.
*/

public class Dbg {

	private static int[] buf;			// a generic buffer
	private static final int MAX_SER = 32;

	public static void init() {

		buf = new int[MAX_SER];
	}

	public static void wr(int c) {
		System.out.print((char) c);
	}

	public static int rd() {
		return 'x';
	}

	public static void intVal(int val) {

		int i;
		if (val<0) {
			wr('-');
			val = -val;
		}
		for (i=0; i<MAX_SER-1; ++i) {
			buf[i] = val%10;
			val /= 10;
			if (val==0) break;
		}
		for (val=i; val>=0; --val) {
			wr('0'+buf[val]);
		}
		wr(' ');
	}

	public static void hexVal(int val) {

		int i, j;
		if (val<16) wr('0');
		for (i=0; i<MAX_SER-1; ++i) {
			j = val & 0x0f;
			if (j<10) {
				j += '0';
			} else {
				j += 'a'-10;
			}
			buf[i] = j;
			val >>>= 4;
			if (val==0) break;
		}
		for (val=i; val>=0; --val) {
			wr(buf[val]);
		}
		wr(' ');
	}

	public static void byteVal(int val) {

		int j;
		j = (val>>4) & 0x0f;
		if (j<10) { j += '0'; } else { j += 'a'-10; }
		wr(j);
		j = val & 0x0f;
		if (j<10) { j += '0'; } else { j += 'a'-10; }
		wr(j);
		wr(' ');
	}
}
