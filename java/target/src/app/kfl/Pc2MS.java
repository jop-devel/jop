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
*	Echo cmd from serial line to rs 485 and back.
*/

public class Pc2MS {

	public static final int IO_PORT = 0;

	private static final int IO_STATUS = 1;
	private static final int IO_UART = 2;
	private static final int IO_RS485 = 15;
	private static boolean blink;

//
//	buffer for serial and rs485 input
//
	private static int[] buf, bufSer, buf485;
	private static int cntSer, cnt485;

	public static void main(String[] args) {

		blink = true;
		Display.init();
		init();

		forever();

	}

	private static void init() {

		bufSer = new int[4];
		buf485 = new int[4];
		buf = new int[4];
		cntSer = cnt485 = 0;

		int[] str =  {' ', 'P', 'c', '2', 'M', 'S'};

		for (int i=0; i<str.length; ++i) {
			Display.data(str[i]);
		}
	}

/**
*	main loop.
*/
	private static void forever() {

		int cnt = 0;
		int i, val;

		for (;;) {

			while ((JopSys.rd(IO_STATUS)&2)!=0 && cntSer<4) {
				bufSer[cntSer++] = JopSys.rd(IO_UART);
				cnt485 = 0;			// flush 485 buffer on new msg from serial line
			}
			while ((JopSys.rd(IO_STATUS)&32)!=0 && cnt485<4) {
				val = JopSys.rd(IO_RS485);
				if (cnt485>=0) {
					buf485[cnt485] = val;
				}
				++cnt485;
			}
			if (cntSer==4) {
				for (i=0; i<4; ++i) {
					JopSys.wr(bufSer[i], IO_RS485);
				}
				cntSer = 0;
				cnt485 = -4;		// ignore echo on rs485 send
			}
			if (cnt485==4) {
				for (i=0; i<4; ++i) {
					JopSys.wr(buf485[i], IO_UART);
				}
				cnt485 = 0;
			}


			if (cnt==50) {
				if (blink) {
					JopSys.wr(0x0001, IO_PORT);
					blink = false;
				} else {
					JopSys.wr(0x0000, IO_PORT);
					blink = true;
				}
				cnt = 0;
			}
			++cnt;

		}
	}
}
