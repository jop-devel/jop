/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

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
 * Base class of all SerialBenchmarks.
 * 
 * @author Thomas B. Preusser <thomas.preusser@tu-dresden.de>
 * @author Martin Schoeberl (martin@jopdesign.com)
 */
public abstract class SerialBenchmark extends Benchmark {

	protected SerialBenchmark() {
	}

	/**
	 * Execute a single threaded benchmark with self calibration
	 * of execution time.
	 * @param bm
	 * @return
	 */
	public int measure() {

		int start, stop, cnt, time, overhead;
		// run the benchmark loop 1024 times minimum
		cnt = 512;
		time = 0;
		overhead = 0;

		while (time < MIN_EXECUTE || time-overhead < MIN_MICRO_EXECUTE) {
			cnt <<= 1;
			if (cnt < 0) {
				break;
			}
			start = Util.getTimeMillis();
			perform(cnt);
			stop = Util.getTimeMillis();
			time = stop-start;
			start = Util.getTimeMillis();
			overhead(cnt);
			stop = Util.getTimeMillis();
			overhead = stop-start;
		}

		time -= overhead;

		// save raw values
		setRawResult(cnt, time);

		// return iterations per second
		return getResult();

	}

	/**
	 * Provide the benchmark function inside a loop running cnt times.
	 * 
	 * @param cnt
	 * @return
	 */
	public abstract int perform(int cnt);
}
