package java.io;

import com.jopdesign.sys.*;


// public class PrintStream extends FilterOutputStream
public class PrintStream {

	private static final int MAX_TMP = 32;
	private static int[] tmp;			// a generic buffer

	static void wr(char c) {
		// no buffering => busy wait on serial line!
		while ((Native.rd(Const.IO_STATUS)&1)==0)
			;
		Native.wr(c, Const.IO_UART);
	}
	
	static void wr(String s) {

		int i = s.length();
		for (int j=0; j<i; ++j) {
			wr(s.charAt(j));
		}
	}

	static void wr(int val) {

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
			wr((char) tmp[val]);
		}
		wr(' ');
	}

	/**
	* the one and only constructor to make System.out work!
	*/
	public PrintStream() {
		if (tmp==null) {
			tmp = new int[MAX_TMP];
		}
	}

	public void print(String s) {
		wr(s);
	}

	public void print(int i) {
		wr(i);
	}

	public void println() {
		wr('\n');
	}

	public void println(String s) {
		print(s);
		println();
	}

	public void println(int i) {
		print(i);
		println();
	}

}
