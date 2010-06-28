/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2008, Christof Pitter

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

/*
 * Created on 28.02.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package cmp;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;


/**
 * @author martin
 *
 * JOP can also say 'Hello World'
 */
public class HelloWorld {		
	
	public static int whoAmI = 3;

	public static void main(String[] args) {

		int cpu_id;
		cpu_id = Native.rdMem(Const.IO_CPU_ID);
		
		if (cpu_id == 0x00000000)
		{
			whoAmI = 0;
			Native.wrMem(0x00000001, Const.IO_SIGNAL);
			time();			
		}
		else
		{
			if (cpu_id == 0x00000001)
			{			
				while(true)
				{
					if (whoAmI == 0)
						whoAmI = cpu_id;
				}
			}
			/*else
			{
				while(true)
				{
					if (whoAmI == 1)
						whoAmI = cpu_id;
				}
			}*/
				
		}
	}
	
	
	static void time() {

		int next;
		int h, m, s, ms;
		int address;
		int readAddress;
		
		address = 0x3ffff;

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
				print_02d(h);
				print_char(':');
				print_02d(m);
				print_char(':');
				print_02d(s);
				print_char(' ');
				print_char('H');
				print_char('e');
				print_char('l');
				print_char('l');
				print_char('o');
				print_char(' ');
				print_char('f');
				print_char('r');
				print_char('o');
				print_char('m');
				print_char(' ');
				print_char('J');
				print_char('O');
				print_char('P');
				print_02d(whoAmI);
				
				
				//readAddress = Native.rdMem(address);
				//print_hex(readAddress);

				print_char('\r');
			
				Native.wr(s & 1, Const.IO_WD);
			}
			
			next = waitForNextInterval(next);
		}
	}
	
	static int waitForNextInterval(int next) {

		final int INTERVAL = 1000;		// one ms
		
		if (next==0) {
			next = Native.rd(Const.IO_US_CNT)+INTERVAL;
		} else {
			next += INTERVAL;
		}

		while (next-Native.rd(Const.IO_US_CNT) >= 0)
				;

		return next;
	}
	
		static void print_02d(int i) {

		int j;
		for (j=0;i>9;++j) i-= 10;
		print_char(j+'0');
		print_char(i+'0');
	}
	
		static void print_hex(int i) {

		int j, k;

		for (j=0; j<8; ++j) {
			k = i>>>((7-j)<<2);
			k &= 0x0f;
			k = k<10 ? k+'0' : k-10+'a';
			print_char(k);
		}
	}

	static void print_char(int i) {

		System.out.print((char) i);
		/*
		wait_serial();
		Native.wr(i, Const.IO_UART);
		*/
	}
	
	
}