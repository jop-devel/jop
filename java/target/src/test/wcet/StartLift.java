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

import jbe.lift.LiftControl;
import jbe.lift.TalIo;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;


public class StartLift {
    /* Debugging signals to manipulate the cache */
    final static int CACHE_FLUSH = -51;
    final static int CACHE_DUMP = -53;

	/**
	 * Set to false for the WCET analysis, true for measurement
	 */
	final static boolean MEASURE = false;
	final static boolean MEASURE_CACHE = false;
	private static LiftControl ctrl;
	private static TalIo io;


	static int ts, te, to;


	public static void main(String[] args) {

		ts = Native.rdMem(Const.IO_CNT);
		te = Native.rdMem(Const.IO_CNT);
		to = te-ts;
		// initialization
		ctrl = new LiftControl();
		io = new TalIo();

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
		if (MEASURE) System.out.println(min);
		if (MEASURE) System.out.println(max);
	}
	
	static void invoke() {
		measure();
		if (MEASURE) te = Native.rdMem(Const.IO_CNT);
		if (MEASURE_CACHE) Native.rdMem(CACHE_DUMP);
	}

	static void measure() {
		if (MEASURE) ts = Native.rdMem(Const.IO_CNT);
		loop();
	}
	
	static void loop() {
		ctrl.setVals();
		ctrl.getVals();
		ctrl.loop(io);
	}

}
