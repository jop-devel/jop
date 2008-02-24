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

package test;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;
//
//	Clock.java
//

public class Clock {

	public static void main( String s[] ) {

		Native.wr(0, Const.IO_WD);		// make WD happy
		Native.wr(1, Const.IO_WD);
		Native.wr(0, Const.IO_WD);
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
				print_02d(h);
				print_char(':');
				print_02d(m);
				print_char(':');
				print_02d(s);
				print_char('\r');

				Native.wr(s & 1, Const.IO_WD);
			}

			Native.wr(~s & 1, Const.IO_WD);
			Native.wr(s & 1, Const.IO_WD);

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
		
/*
	static void print_04d(int i) {

		if (i<0) {
			print_char('-');
			i = -i;
		}

		int j, k, l;
		for (j=0;i>999;++j) i-= 1000;
		for (k=0;i>99;++k) i-= 100;
		for (l=0;i>9;++l) i-= 10;
		print_char(j+'0');
		print_char(k+'0');
		print_char(l+'0');
		print_char(i+'0');
	}
		

	static void print_06d(int i) {

		if (i<0) {
			print_char('-');
			i = -i;
		}

		int j;

		for (j=0;i>99999;++j) i-= 100000;
		print_char(j+'0');
		for (j=0;i>9999;++j) i-= 10000;
		print_char(j+'0');
		for (j=0;i>999;++j) i-= 1000;
		print_char(j+'0');
		for (j=0;i>99;++j) i-= 100;
		print_char(j+'0');
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
*/

	static void wait_serial() {

		while ((Native.rd(Const.IO_STATUS)&1)==0) ;
	}

	static void print_char(int i) {

		System.out.print((char) i);
		/*
		wait_serial();
		Native.wr(i, Const.IO_UART);
		*/
	}

}
