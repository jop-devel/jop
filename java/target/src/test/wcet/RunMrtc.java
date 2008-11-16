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

import wcet.mrtc.BinarySearch;
import wcet.mrtc.BubbleSort;
import wcet.mrtc.CyclicRedundancyCheck;
import wcet.mrtc.DiscreteCosineTransform;
import wcet.mrtc.Fibonacci;

import com.jopdesign.sys.*;

public class RunMrtc {

	/**
	 * Set to false for the WCET analysis, true for measurement
	 */
	final static boolean MEASURE = false;
	static int ts, te, to;
	static BubbleSort b = new BubbleSort();
    static BinarySearch bs = new BinarySearch();
    static int[] crcVal = new int[2];
    static CyclicRedundancyCheck crc = new CyclicRedundancyCheck();
    static DiscreteCosineTransform d = new DiscreteCosineTransform();
    static Fibonacci fib = new Fibonacci();

	public static void main(String[] args) {
		
		ts = Native.rdMem(Const.IO_CNT);
		te = Native.rdMem(Const.IO_CNT);
		to = te-ts;
		invoke();
		if (MEASURE) System.out.println(te-ts-to);
	}
	
	static void invoke() {
		measure();
		if (MEASURE) te = Native.rdMem(Const.IO_CNT);
	}
	
	static void measure() {
		if (MEASURE) ts = Native.rdMem(Const.IO_CNT);
	    // bs.binarySearch(-1);  // Use non-existent key to drive worst-case performance
		// b.bubbleSort();
	    // crc.crc(crcVal);
	    d.fdct(d.block, 8);
		// fib.fib(30);		
	}
}
