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

import com.jopdesign.sys.Config;
import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

public class StartBenchUdpIp {
    /* Debugging signals to manipulate the cache */
    final static int CACHE_FLUSH = -51;
    final static int CACHE_DUMP = -53;

	final static boolean MEASURE_CACHE = false;
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
		    if (MEASURE_CACHE) Native.wrMem(1,CACHE_FLUSH);
			invoke();
			val = te-ts-to;
			if (val<min) min = val;
			if (val>max) max = val;
		}
		if (Config.MEASURE) { System.out.print("min: "); System.out.println(min); }
		if (Config.MEASURE) { System.out.print("max: "); System.out.println(max); }
	}
	
	static void invoke() {
		measure();
		if (Config.MEASURE) te = Native.rdMem(Const.IO_CNT);
		if (MEASURE_CACHE) Native.rdMem(CACHE_DUMP);
	}

	static void measure() {
		if (Config.MEASURE) ts = Native.rdMem(Const.IO_CNT);
		bui.loop();
	}
			
}
