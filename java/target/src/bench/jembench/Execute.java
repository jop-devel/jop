/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2001-2010, Martin Schoeberl (martin@jopdesign.com)

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

package jembench;

/**
 * Execution logic of the benchmarks.
 * 
 * @author Martin Schoeberl (martin@jopdesign.com)
 *
 */
public class Execute {

	/**
	 * Clock frequency (in MHz) for the target to calculate clock cycles
	 * of the micro benchmarks. Set to 0 if not known.
	 * Clock cycle calculation works only up to 2 GHz (integer overflow).
	 * For > 2 GHz leave it 0 and do the clock cycle calculation manually.
	 */
	public static final int FREQ = 0;

	/**
	 * Execute a single threaded benchmark with self calibration
	 * of execution time.
	 * @param bm
	 * @return
	 */
	public static int measure(SerialBenchmark bm) {

		int start, stop, cnt, time, overhead, minus;
		cnt = 512;		// run the benchmark loop 1024 times minimum
		time = 1;
		overhead = 0;
		minus = 0;

//		LowLevel.msg(bm.toString());
		while (time<1000) {
			cnt <<= 1;
			if (cnt < 0) {
				break;
			}
			start = (int) System.currentTimeMillis();
			bm.perform(cnt);
			stop = (int) System.currentTimeMillis();
			time = stop-start;
			start = (int) System.currentTimeMillis();
			bm.overhead(cnt);
			stop = (int) System.currentTimeMillis();
			overhead = stop-start;
		}

		time -= overhead;
		time += minus;

		if (time<25 || cnt<0) {
//			LowLevel.msg(bm.toString());
//			LowLevel.msg(" no result");
//			LowLevel.lf();
			return -1;
		}

		// result is test() per second
		int result;
		if (cnt>2000000) {		// check for overflow on cnt*1000
			result = cnt/time;
			if (result>2000000) {
//				LowLevel.msg(bm.toString());
//				LowLevel.msg(" no result");
//				LowLevel.lf();
				return -1;
			}
			result *= 1000;
		} else {
			result = cnt*1000/time;
		}
//		LowLevel.msg(result);
//		LowLevel.msg("1/s");
		if (FREQ!=0) {
			int clocks = (FREQ*2000000/result+1)/2;
			if (FREQ>1000) {
				result /= 10;
				clocks = (FREQ*200000/result+1)/2;
			}
//			LowLevel.msg(clocks);
//			LowLevel.msg("clocks");
		}
//		LowLevel.lf();
		
		return time;
	}
	
}
