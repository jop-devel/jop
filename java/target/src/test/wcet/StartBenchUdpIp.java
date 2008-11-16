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

import jbe.BenchUdpIp;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

public class StartBenchUdpIp {

	/**
	 * Set to false for the WCET analysis, true for measurement
	 */
	final static boolean MEASURE = false;
	static int ts, te, to;

	static BenchUdpIp bui;

	public static void main(String[] args) {

		ts = Native.rdMem(Const.IO_CNT);
		te = Native.rdMem(Const.IO_CNT);
		to = te-ts;
		// initialization
		bui = new BenchUdpIp();

		int min = 0x7fffffff;
		int max = 0;
		int val = 0;
		for (int i=0; i<100; ++i) { // @WCA loop=100
			invoke();
			val = te-ts-to;
			if (val<min) min = val;
			if (val>max) max = val;
		}
		if (MEASURE) System.out.println(min);
		if (MEASURE) System.out.println(max);
	}
	
	static void invoke() {
		measure();
		if (MEASURE) te = Native.rdMem(Const.IO_CNT);
	}

	static void measure() {
		if (MEASURE) ts = Native.rdMem(Const.IO_CNT);
		bui.loop();
	}
			
}
