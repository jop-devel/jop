/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2010, Martin Schoeberl (martin@jopdesign.com)
  Copyright (C) Thomas B. Preusser <thomas.preusser@tu-dresden.de>

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
 * Base class for all JBE Benchmarks.
 * 
 * @author Martin Schoeberl (martin@jopdesign.com)
 * @author Thomas B. Preusser <thomas.preusser@tu-dresden.de>
 */
public abstract class Benchmark {
	
	private int countResult;
	private int timeResult;
	
	/**
	 * A benchmark has to run at least for MIN_EXECUTE
	 * milliseconds.
	 */
	public final static int MIN_EXECUTE = 1000;
	/**
	 * The code under test (excluding the overhead)
	 * has to run at least for MIN_MICRO_EXECUTE milliseconds.
	 */
	public final static int MIN_MICRO_EXECUTE = 100;
	
	/**
	 * Perform the benchmarking measurement and return
	 * the number of iterations.
	 * 
	 * @return
	 */
	public abstract int measure();

	/**
	 * Compensate for any overhead in the benchmark function.
	 * 
	 * @param cnt
	 * @return
	 */
	public int overhead(int cnt) {
		return 0;
	}

	/**
	 * Provide the name of the benchmark.
	 */
	public String toString() {
		return "Set name for the benchmark";
	}
	
	/**
	 * The number of iterations and the time in milliseconds.
	 * @param cnt
	 * @param time
	 */
	public void setRawResult(int cnt, int time) {
		countResult = cnt;
		timeResult = time;
	}
	
	public int getResult() {

		int result;

		// no result
		if (timeResult<MIN_MICRO_EXECUTE || countResult<0) {
			return -1;
		}

		// result is test() per second
		if (countResult>2000000) {		// check for overflow on cnt*1000
			result = countResult/timeResult;
			if (result>2000000) {
				return -1;
			}
			result *= 1000;
		} else {
			result = countResult*1000/timeResult;
		}
//		LowLevel.msg(result);
//		LowLevel.msg("1/s");
		
//		if (FREQ!=0) {
//			int clocks = (FREQ*2000000/result+1)/2;
//			if (FREQ>1000) {
//				result /= 10;
//				clocks = (FREQ*200000/result+1)/2;
//			}
////			LowLevel.msg(clocks);
////			LowLevel.msg("clocks");
//		}
				
		return result;
	}
	
	/**
	 * Return the 1/10 and 1/100 result.
	 * 
	 * @return
	 */
	public int getTwoDecimal() {
		if (countResult>20000 || timeResult==0) {
			return 0;
		}
		return countResult*1000*100/timeResult - countResult*1000/timeResult*100;
	}
}
