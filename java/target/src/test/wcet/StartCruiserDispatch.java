/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2010, Benedikt Huber (benedikt@vmars.tuwien.ac.at)

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

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

import cruiser.common.WireMessage;
import cruiser.common.WireMessage.Type;
import cruiser.control.Dispatcher;
import cruiser.control.Filter;
import cruiser.control.SpeedManager;

/**
 * Purpose: Measure the dispatch method of the cruiser benchmark
 * @author Benedikt Huber (benedikt@vmars.tuwien.ac.at)
 *
 */
public class StartCruiserDispatch {
    /* Debugging signals to manipulate the cache */
    final static int CACHE_FLUSH = -51;
    final static int CACHE_DUMP = -53;

	/**
	 * Set to false for the WCET analysis, true for measurement
	 */
	final static boolean MEASURE = true;
    final static boolean MEASURE_CACHE = false;
	static int ts, te, to;
	private static Dispatcher dispatch;
	private static String message;


	public static void main(String[] args) {

		ts = Native.rdMem(Const.IO_CNT);
		te = Native.rdMem(Const.IO_CNT);
		to = te-ts;
		// initialization
		SpeedManager manager = new SpeedManager();

		Filter frontLeftFilter = new Filter("FLF", manager.frontLeftSpeed);
		Filter frontRightFilter = new Filter("FRF", manager.frontRightSpeed);
		Filter rearLeftFilter = new Filter("RLF", manager.rearLeftSpeed);
		Filter rearRightFilter = new Filter("RRF", manager.rearRightSpeed);

		dispatch = new Dispatcher(manager,
											 frontLeftFilter,
											 frontRightFilter,
											 rearLeftFilter,
											 rearRightFilter);

		int min = 0x7fffffff;
		int max = 0;
		int val = 0;
		for (int i=0; i<1000; ++i) {
			message = WireMessage.buildMessage(Type.SPEED_FRONT_RIGHT, 2344234233L);
		    if (MEASURE_CACHE) Native.wrMem(1,CACHE_FLUSH);
			invoke();
			val = te-ts-to;
			if (val<min) min = val;
			if (val>max) max = val;
		}
		if (MEASURE) {
                    System.out.print("bcet:");
                    System.out.println(min);
                    System.out.print("wcet:");
                    System.out.println(max);
                }
	}
	
	static void invoke() {
		measure();
		if (MEASURE) te = Native.rdMem(Const.IO_CNT);
		if (MEASURE_CACHE) Native.rdMem(CACHE_DUMP);
	}

	static void measure() {
		if (MEASURE) ts = Native.rdMem(Const.IO_CNT);
		dispatch.dispatch(message);
	}
}
