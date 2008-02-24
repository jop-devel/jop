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
 * @author Martin
 *
 */
public class SigDel {

	// This values depend on the ADC!!!
	final static int OFFSET = 1550;
	

	public static void main(String[] args) {
		
		int val;
		int min, max;
		int t1, t2;

		min = 999999;
		max = 0;

		int[] samples = new int[100];
		
		for(;;) {
			val = Native.rdMem(Const.IO_MICRO);
			if (val>0) {
				continue;
			}
			val &= 0xffff;

			Native.wrMem(val, Const.IO_MICRO);
			
			// Test if we missed the sample output
			val = Native.rdMem(Const.IO_MICRO);
			if (val<0) {
				System.out.print('*');
				continue;
			}

//			if (val>max) max = val;
//			if (val<min) min = val;
//			System.out.print(min);
//			System.out.print(" ");
//			System.out.print(max);
//			System.out.print(" ");
//			System.out.println(val);
		}

/*
		t1 = Native.rd(Const.IO_CNT);
		t2 = Native.rd(Const.IO_CNT);
		System.out.print("JOP counter: ");
		System.out.println(t2-t1);
*/


	}
}
