package util;

/**
*	serial output for debug on uart 1.
*/

public abstract class Dbg {

	abstract void dbgWr(int c);
	abstract int dbgReadBuffer(int[] buf, int pos);

	private static Dbg st;

	private static final int MAX_TMP = 32;
	private static int[] tmp;			// a generic buffer


	/** init serial or UDP Debugging */
	public static void init() {

		if (st==null) {
			tmp = new int[MAX_TMP];
			st = new DbgUdp();
		}
	}

	/** force serial Debugging */
	public static void initSer() {

		if (st==null) {
			tmp = new int[MAX_TMP];
			st = new DbgSerial();
		}
	}

	/** force serial Debugging with waiting */
	public static void initSerWait() {

		if (st==null) {
			tmp = new int[MAX_TMP];
			st = new DbgSerial(true);
		}
	}

	public static void wr(int c) { st.dbgWr(c); }
	public static void lf() { st.dbgWr('\r'); st.dbgWr('\n'); }

	public static int readBuffer(int[] buf, int pos) {
		return st.dbgReadBuffer(buf, pos);
	}

	public static void wr(String s, int val) {

		wr(s);
		intVal(val);
		wr("\r\n");
	}

	public static void wr(String s) {

		int i = s.length();
		for (int j=0; j<i; ++j) {
			wr(s.charAt(j));
		}
	}

	public static void wr(StringBuffer s) {

		int i = s.length();
		for (int j=0; j<i; ++j) {
			wr(s.charAt(j));
		}
	}
	
	public static void wr(boolean b) {
		
		wr(b ? "true " : "false ");
	}


	public static void intVal(int val) {

		int i;
		if (val<0) {
			wr('-');
			val = -val;
		}
		for (i=0; i<MAX_TMP-1; ++i) {
			tmp[i] = (val%10)+'0';
			val /= 10;
			if (val==0) break;
		}
		for (val=i; val>=0; --val) {
			wr(tmp[val]);
		}
		wr(' ');
	}

	public static void hexVal(int val) {

		int i, j;
		if (val<16) wr('0');
		for (i=0; i<MAX_TMP-1; ++i) {
			j = val & 0x0f;
			if (j<10) {
				j += '0';
			} else {
				j += 'a'-10;
			}
			tmp[i] = j;
			val >>>= 4;
			if (val==0) break;
		}
		for (val=i; val>=0; --val) {
			wr(tmp[val]);
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
