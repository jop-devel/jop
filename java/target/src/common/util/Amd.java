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

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;
/**
*	Flash programmer (for AMD Am29LV040).
*		read and write data to address 0x80000.
*
*	timing:
*		byte program: typ 9 us max. 300 us
*		sector erase: typ 0.7 s + 0.6 s max 15 s + 20 s
*
*	asumes Timer.java is initialized
*/


public class Amd {

	public static int read(int addr) {

		return Native.rdMem(addr+0x80000);
	}

	/** program and handle timeout (without wd handling!) */

	public static void program(int addr, int data) {

		int i;

		addr += 0x80000;

//		Native.wr(0, Const.IO_INT_ENA);
		
		Native.wrMem(0xaa, 0x80555);
		Native.wrMem(0x55, 0x802aa);
		Native.wrMem(0xa0, 0x80555);
		Native.wrMem(data, addr);

		int j = Native.rd(Const.IO_US_CNT);
		j += 350;						// maximum 350 us timeout
		data &= 0xff;

		for (;;) {
			if (Native.rdMem(addr) == data) break;
			if (j-Native.rd(Const.IO_US_CNT) < 0) break;
		}
		
//		Native.wr(1, Const.IO_INT_ENA);

	}

	/**
	*	erase one sector and handle wd.
	*/
	public static boolean erase(int addr) {

		int i;

		addr += 0x80000;

//		Native.wr(0, Const.IO_INT_ENA);
		
		Native.wrMem(0xaa, 0x80555);
		Native.wrMem(0x55, 0x802aa);
		Native.wrMem(0x80, 0x80555);
		Native.wrMem(0xaa, 0x80555);
		Native.wrMem(0x55, 0x802aa);
		Native.wrMem(0x30, addr);

//		Native.wr(1, Const.IO_INT_ENA);

		for (i=0; i<400; ++i) {					// maximum 40 s timeout
			joprt.RtThread.sleepMs(100);
			System.out.print('.');
			Timer.wd();
			if (Native.rdMem(addr) == 0xff) return true;
		}
		return false;
	}
}
