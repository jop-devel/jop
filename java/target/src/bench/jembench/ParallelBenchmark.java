/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2010, Martin Schoeberl (martin@jopdesign.com)

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
 * A benchmark where the workload is automatically distributed
 * to all available cores.
 * 
 * @author Martin Schoeberl (martin@jopdesign.com)
 *
 */
public abstract class ParallelBenchmark extends Benchmark {

	ParallelExecutor pe;
	
	public ParallelBenchmark() {
		pe = ParallelExecutor.getExecutor();
	} 
	
	public String toString() {

		return "Override the name";
	}
	
	public int measure() {

		int start, cnt, time;
		// run the benchmark loop 1 times minimum
		cnt = 1;
		time = 0;
		// create the worker threads
		pe.start();

		while (time < MIN_EXECUTE) {
			cnt <<= 1;
			if (cnt < 0) {
				break;
			}
		    start = Util.getTimeMillis();
			for (int i=0; i<cnt; ++i) {
				pe.executeParallel(getWorker());			
			}
		    time = Util.getTimeMillis() - start;
		}

		// let the worker threads terminate
		pe.stop();
		// save raw values
		setRawResult(cnt, time);

		// return iterations per second
		return getResult();

	}

	public abstract Runnable getWorker();
}
