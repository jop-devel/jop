/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2009, Martin Schoeberl (martin@jopdesign.com)

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


/**
 * 
 */
package ptolemy.deadline;

import com.jopdesign.io.IOFactory;
import com.jopdesign.io.SysDevice;
import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

/**
 * Low-level measurements for bytecodes on the TDMA based CMP system.
 * 
 * @author Martin Schoeberl (martin@jopdesign.com)
 *
 */
public class Measure {

	final static int TDMA_LENGTH = 3*6;
	final static int CLOCK_FREQ = 60000000;
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		lowLevel();
	}
	
	public static void lowLevel() {

		SysDevice sys = IOFactory.getFactory().getSysDevice();
		int a[] = new int[1];
		
		// A 0.1s interval in multiple of the TDMA round plus 1
		int shift = CLOCK_FREQ/10/(TDMA_LENGTH)*TDMA_LENGTH+1;
		
		// get measurement overhead
		int time = Native.rd(Const.IO_CNT);
		time = Native.rd(Const.IO_CNT)-time;
		int off = time;
		
		int start = sys.cntInt + shift;
		for (int i=0; i<3*TDMA_LENGTH; ++i) {
			sys.deadLine = start;
			time = Native.rd(Const.IO_CNT);
			a[0] = 1;
			time = Native.rd(Const.IO_CNT)-time;
			System.out.println(time-off);
			start += shift;
		}
	}
	
}
