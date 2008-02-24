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
*	Write Rs485 Data with timestamp to serial line.
*/

public class DbgRs485 {

	public static final int IO_PORT = 0;

	private static final int IO_UART = 2;
	private static final int IO_RS485 = 15;
	private static final int IO_STATUS = 1;

	public static void main(String[] args) {

		Timer.init();
		Timer.start();

		int val, ts;

		for (;;) {

			if ((JopSys.rd(IO_STATUS)&32)!=0) {
				val = JopSys.rd(IO_RS485);
				wr(val);							// write value
				ts = JopSys.rd(JopSys.IO_CNT);
				for (int i=0; i<4; ++i) {			// write timestamp
					wr(ts);							// low byte first
					ts >>= 8;
				}

			}
			Timer.wd();
		}
	}

	static void wr(int c) {
		while ((JopSys.rd(IO_STATUS)&1)==0) ;
		JopSys.wr(c, IO_UART);
	}
}
