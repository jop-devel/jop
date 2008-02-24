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

/*
 * Created on 30.05.2005
 *
 */
package dsp;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

/**
 * @author admin
 *
 */
public class Example {

	// This values depends on the ADC!!!
	final static int OFFSET = 1550;
	final static int FSAMP = 30000;
	
	final static int ABC = 65535;

	public static void main(String[] args) {
		
		int cnt = 0;
		int cnt2 = 0;
		int add = 1;
		int add2 = 3;
		int left, right;
		int val;
		int sig = 0;
		

		for(;;) {
			// wait for next sample time
			val = Native.rdMem(Const.WB_TS0);
			if (val>0) {
				continue;
			}
			// we don't use val
			
			cnt += add;
			if (cnt>=ABC) {
				cnt = ABC;
				add = -11;
			} else if (cnt<=0) {
				add = 11;
				cnt = 0;
			}
			cnt2 += add2;
			if (cnt2>=ABC) {
				cnt2 = ABC;
				add2 = -13;
			} else if (cnt2<=0) {
				cnt2 = 0;
				add2 = 13;
			}

			sig = (sig+1) & 0x7f;
			int rec = (sig & 0x40)<<1;
			int saw = sig;
			
			left = (rec*cnt + saw*(0xffff-cnt2))>>16;
			right = (saw*cnt2 + rec*(0xffff-cnt))>>16;

			
			

			val = left + (right<<16);
			Native.wrMem(val, Const.WB_TS0);
			
			// Test if we missed the sample output
			val = Native.rdMem(Const.WB_TS0);
			if (val<0) {
				System.out.print('*');
			}
		}



	}
}
