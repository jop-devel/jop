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

package kfl;

/**
*	Display.java
*
*		handle all display stuff.
*/

public class Display {

	private static final int IO_DISP = 12;

	private static final int COLS = 20;

	private static int[] buf;


	public static void line1() {
		cmd(0x080 | 0x00);		// first line
	}
	public static void line2() {
		cmd(0x080 | 0x40);		// second line
	}

	public static void cls() {

		int i;
		cmd(0x080 | 0x00);
		for (i=0; i<COLS; ++i) data(' ');
		cmd(0x080 | 0x40);
		for (i=0; i<COLS; ++i) data(' ');
		cmd(0x080 | 0x00);
	}

	public static void line1(int[] str) {
		line1();
		data(str);
	}
	public static void line2(int[] str) {
		line2();
		data(str);
	}

	public static void line1(int[] str, int val) {
		line1();
		data(str, val);
	}
	public static void line2(int[] str, int val) {
		line2();
		data(str, val);
	}

	public static void data(int[] str) {

		int i;
		for (i=0; i<str.length && i<COLS; ++i) {
			Display.data(str[i]);
		}
		for (; i<COLS; ++i) {
			Display.data(' ');
		}
	}

	public static void data(int[] str, int val) {

		int i;
		for (i=0; i<str.length && i<COLS; ++i) {
			Display.data(str[i]);
		}
		Display.data(' ');
		Display.data('0'+val/10);
		Display.data('0'+val%10);
		i += 3;
		for (; i<COLS; ++i) {
			Display.data(' ');
		}
	}

	public static void intVal(int val) {

		int i;
		for (i=0; i<COLS-1; ++i) {
			buf[i] = val%10;
			val /= 10;
			if (val==0) break;
		}
		for (val=i; val>=0; --val) {
			data('0'+buf[val]);
		}
	}

	public static void data(int val) {

		nibble((val>>>4) | 0x10);
		nibble((val&0x0f) | 0x10);
	}

	public static void init() {

		nibble(0x20);				// function mode 4-bit, only one nibble!!!
		data(0x00);					// brightness (0..100%, 3..25%)
		cmd(0x02);					// cursor home
		cmd(0x06);					// entry mode
		cmd(0x0c);					// display on
		cmd(0x14);					// shift cursor
		cmd(0x080);					// set dd ram address
		cmd(0x01);					// display clear
		for (int j=0; j<3; ++j) {		// wait 3 ms
			int i = JopSys.rd(JopSys.IO_CNT)+JopSys.MS;
			while (i-JopSys.rd(JopSys.IO_CNT) >= 0)
				;
		}
		buf = new int[COLS];
	}

	static void cmd(int val) {

		nibble(val>>>4);
		nibble(val&0x0f);
	}

/*
	disp_d(7 downto 4) <= disp(3 downto 0);
	disp_rs <= disp(4);
	disp_e <= disp(5);
	disp_nwr <= '1';
*/
	private static void nibble(int val) {

		JopSys.wr(val, IO_DISP);
		wait1us();
		JopSys.wr(0x20 | val, IO_DISP);	// set e to 1
		wait1us();
		JopSys.wr(val, IO_DISP);		// set e back to 0
		wait1us();
	}

	private static void wait1us() {

		int i = JopSys.rd(JopSys.IO_CNT)+JopSys.USEC;
		while (i-JopSys.rd(JopSys.IO_CNT) >= 0)
				;
	}
}
