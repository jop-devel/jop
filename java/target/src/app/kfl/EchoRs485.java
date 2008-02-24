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
*	Rs232 - Rs485 router as stans alone program.
*/

public class EchoRs485 {

	public static final int IO_PORT = 0;

	private static final int IO_UART = 2;
	private static final int IO_RS485 = 15;
	private static final int IO_STATUS = 1;

	public static void main(String[] args) {

		Display.init();
		init();
		Timer.init();
		Timer.start();

		for (;;) {

			if ((JopSys.rd(IO_STATUS)&2)!=0) {
				JopSys.wr(JopSys.rd(IO_UART), IO_RS485);
				while ((JopSys.rd(IO_STATUS)&32)==0)
					;									// wait on RS485 Echo
				JopSys.rd(IO_RS485);					// consume Echo
			}
			if ((JopSys.rd(IO_STATUS)&32)!=0) JopSys.wr(JopSys.rd(IO_RS485), IO_UART);

			Timer.wd();

		}
	}

	public static void init() {

		int[] str =  {'K', 'F', 'L', ' ', 'P', 'C', '-', 'R', 'S', '4', '8', '5'};

		for (int i=0; i<str.length; ++i) {
			Display.data(str[i]);
		}
	}
}
