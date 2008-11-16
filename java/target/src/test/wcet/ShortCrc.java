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

package wcet;

import com.jopdesign.sys.*;

public class ShortCrc {

	/**
	 * Set to false for the WCET analysis, true for measurement
	 */
	final static boolean MEASURE = false;
	static int ts, te, to;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		ts = Native.rdMem(Const.IO_CNT);
		te = Native.rdMem(Const.IO_CNT);
		to = te-ts;
		// measurement + return takes 22+22+21=65 cycles
		// WCET measured: 1442/1552
		// WCET analysed: 1685-65=1620
		// Those numbers are fomr 2006
		int min = 0x7fffffff;
		int max = 0;
		int time = 0;
		int val = -1;
		for (int i=0; i<100000; ++i) { // @WCA loop=100
			val = invoke(val);
			time = te-ts-to;
			if (time<min) min = time;
			if (time>max) max = time;
		}
		if (MEASURE) System.out.println(min);
		if (MEASURE) System.out.println(max);

	}
	
	static int invoke(int val) {
		int res = measure(val);
		if (MEASURE) te = Native.rdMem(Const.IO_CNT);
		return res;
	}

/*
	better values for polynom on short messages see:

		'Determining optimal cyclic codes for embedded networks'
		(www.cmu.edu)
*/
/**
*	claculate crc value with polynom x^8 + x^2 + x + 1
*	and initial value 0xff
*	on 32 bit data
*/
	
	static int measure(int val) {
		if (MEASURE) ts = Native.rdMem(Const.IO_CNT);
		int reg = -1;

		for (int i=0; i<32; ++i) { // @WCA loop=32
			reg <<= 1;
			if (val<0) reg |= 1;
			val <<=1;
			if ((reg & 0x100) != 0) reg ^= 0x07;
		}
		reg &= 0xff;
		return reg;
	}
	
}
