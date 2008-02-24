/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2007, Christof Pitter

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

package vga;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;
//
//	Clock.java
//

public class BlinkerVga {

	public static void main( String s[] ) {

		Native.wr(0, Const.IO_WD);		// make WD happy
		Native.wr(1, Const.IO_WD);
		Native.wr(0, Const.IO_WD);
		
		int addr = 0;
		int offset = 0x28000;
		boolean j = true;
		
		Native.wr(0, Const.IO_WD);		// make WD happy
		Native.wr(1, Const.IO_WD);
		Native.wr(0, Const.IO_WD);
		Native.wrMem(0x11111111, 0x28000); // first VGA Address	
		Native.wrMem(0x11111111, 0x3ffff); // last VGA Address
		
		for (int i=0x0; i<0x18000; i++)
		{
			addr = i + offset;
			if( i <= 1024)
			{
				Native.wrMem(0x22222222, addr); // rot
			}
			else if ( (i > 1024) && (i <= 2048) )
			{
				Native.wrMem(0xffffffff, addr); // gelb
			}
			else if ( (i > 2048) && (i <= 3072) ) 
			{
				Native.wrMem(0x33333333, addr); // hellrot
			}
			else if ( (i > 97280) && (i <= 98304) )
			{
				Native.wrMem(0xffffffff, addr); // gelb
			}
			else
			{
				Native.wrMem(0xCCCCCCCC, addr); // grün
				//Native.wrMem(0x00000000, addr); // schwarz
			}
		}
		
		time();
	}

	static void time() {

		int next;
		int h, m, s, ms;


		h = m = s = ms = 0;
		next = 0;
		s = -1;

		for (;;) {

			++ms;
			if (ms==1000) {
				ms = 0;
				++s;
				if (s==60) {
					s = 0;
					++m;
				}
				if (m==60) {
					m = 0;
					++h;
				}
				if (h==24) h = 0;

				Native.wr(s & 1, Const.IO_WD);
			}

			Native.wr(~s & 1, Const.IO_WD);
			Native.wr(s & 1, Const.IO_WD);

			next = waitForNextInterval(next);
		}
	}

	static int waitForNextInterval(int next) {

		final int INTERVAL = 20000;		// one ms

		if (next==0) {
			next = Native.rd(Const.IO_CNT)+INTERVAL;
		} else {
			next += INTERVAL;
		}

		while (next-Native.rd(Const.IO_CNT) >= 0)
				;

		return next;
	}


}
