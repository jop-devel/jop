/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2001-2009, Martin Schoeberl (martin@jopdesign.com)

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

package timing;

public abstract class BenchMark {
	
	/**
	 * Maximum benchmark run time in milliseconds.
	 */
	final static int RUNTIME = 3000;
	/**
	 * Minimum difference between test() and overhead() in milliseconds.
	 */
	final static int DIFFMIN = 300;
	
	/**
	 * Clock frequency in Hz. Set to 0 if not known.
	 */
//	final static long FREQ = 0L;
	// usbmin in default configuration
	final static long FREQ = 60000000L;
	// that's MS's PC
//	final static long FREQ = 2190000000L;

	/**
	 * Provide the test function inside a loop running
	 * cnt times.
	 * @param cnt
	 * @return
	 */
	public abstract int test(int cnt);

	/**
	 * Compensate for any overhead in the test function.
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
		return "Overwrite toString() with the benchmark name!";
	}
	
	
	/**
	 * Run the benchmark.
	 * @param bm
	 */
	final public void execute() {

		long start, stop, time, overhead;
		int cnt;
		cnt = 512;		// run the benchmark loop 1024 times minimum
		time = 0;
		overhead = 0;
		// defeat optimization
		int sum = 0;

		System.out.print(this);
		System.out.print(": ");
		while (time<RUNTIME && (time-overhead) < DIFFMIN) {
			cnt <<= 1;
			if (cnt < 0) {
				break;
			}
			start = System.currentTimeMillis();
			sum += test(cnt);
			stop = System.currentTimeMillis();
			time = stop-start;
			start = System.currentTimeMillis();
			sum += overhead(cnt);
			stop = System.currentTimeMillis();
			overhead = stop-start;
		}

//		LowLevel.msg("time", time);
//		LowLevel.msg("ohd", overhead);
//		LowLevel.msg("ohdm", minus);
//		LowLevel.msg("cnt", cnt);
		time -= overhead;

		// too short time or overflow of cnt
		if (cnt<0) {
			System.out.println("no result");
			return;
		}

		// result is test() per second
		long result = ((long) cnt)*1000/time;
		System.out.print(result);
		System.out.print(" 1/s ");
		
		if (FREQ!=0) {
			// TODO: rounding
			int clocks = (int) ((FREQ*10/result+5)/10);
			System.out.print(clocks);
			System.out.println(" clocks");
		} else {
			System.out.println();
		}
		System.out.print("Dummy out ");
		System.out.println(sum);
	}
			
}
