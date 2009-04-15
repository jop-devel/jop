/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2001-2008, Martin Schoeberl (martin@jopdesign.com)

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package util;

/**
*	serial output for debug on uart 1.
*
*/

public abstract class Dbg {

	abstract void dbgWr(int c);
	abstract int dbgReadBuffer(int[] buf, int pos);

	private static Dbg st;

	private static final int MAX_TMP = 32;
	private static int[] tmp = new int[MAX_TMP];			// a generic buffer


	/** init serial or UDP Debugging */
	public static void init() {

		st = new DbgUdp();
	}

	/** force serial Debugging */
	public static void initSer() {

		st = new DbgSerial();
	}

	/** force serial Debugging with waiting */
	public static void initSerWait() {

		st = new DbgSerial(true);
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
		// dummy annotation
		for (int j=0; j<i; ++j) { // @WCA loop<=80
			wr(s.charAt(j));
		}
	}

	public static void wr(StringBuffer s) {

		int i = s.length();
		// dummy annotation
		for (int j=0; j<i; ++j) {  // @WCA loop<=80
			wr(s.charAt(j));
		}
	}
	
	public static void wr(boolean b) {
		
		wr(b ? "true " : "false ");
	}


	public static void intVal(int val) {

		int i;
		int sign = 1;
		if (val<0) {
			wr('-');
			//val = -val;
			sign = -1;
		}
		for (i=0; i<MAX_TMP-1; ++i) { // @WCA loop=31
			//tmp[i] = (val%10)+'0';
			tmp[i] = ((val%10)*sign)+'0';
			val /= 10;
			if (val==0) break;
		}
		for (val=i; val>=0; --val) { // @WCA loop<=10
			wr((char) tmp[val]);
		}
		wr(' ');
	}
	
	public static void hexVal(int val) {

		int i, j;
		if (val<16 && val>=0) wr('0');
		for (i=0; i<MAX_TMP-1; ++i) { // @WCA loop=31
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
		for (val=i; val>=0; --val) { // @WCA loop<=8
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
