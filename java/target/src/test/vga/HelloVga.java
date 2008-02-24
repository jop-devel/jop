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

import util.*;
import com.jopdesign.sys.Native;
import com.jopdesign.sys.Const;

public class HelloVga {

	public static void main(String[] agrgs) {
    
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
				Native.wrMem(0x33333333, addr); // orange
			}
			else if ( (i > 97280) && (i <= 98304) )
			{
				Native.wrMem(0xffffffff, addr); // gelb
			}
			else
			{
				Native.wrMem(0xCCCCCCCC, addr); // grün 
			}
		}
		          		
		System.out.println("Hello World from JOP!");
 
		for (;;) {
			Timer.wd();
			int i = Timer.getTimeoutMs(500);
			while (!Timer.timeout(i)) {
				;
			}
		}
	}
}
