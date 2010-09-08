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
 * A benchmark where the workload is defined by n independent
 * units of work, which are automatically distributed to m processing
 * cores.
 * 
 * @author Martin Schoeberl (martin@jopdesign.com)
 *
 */
public class EnumeratedParallelBenchmark extends Benchmark implements WorkUnit {

	static EnumeratedExecutor ee;
	
	public EnumeratedParallelBenchmark() {
		synchronized (EnumeratedParallelBenchmark.class) {
			if (ee==null) {
				ee = new EnumeratedExecutor();
			}
		}
	} 
	
	public String toString() {

		return "Dummy test";
	}
	
	public int measure() {

		int start, cnt, time;
		// run the benchmark loop 1 times minimum
		cnt = 1;
		time = 0;
		// create the worker threads
		ee.start();
		int size = getNrOfUnits();

		while (time < MIN_EXECUTE) {
			cnt <<= 1;
			if (cnt < 0) {
				break;
			}
		    start = Util.getTimeMillis();
			for (int i=0; i<cnt; ++i) {
				ee.executeParallel(this, size);			
			}
		    time = Util.getTimeMillis() - start;
		}

		// let the worker threads terminate
		ee.stop();
		// save raw values
		setRawResult(cnt, time);

		// return iterations per second
		return getResult();

	}


	/**
	 * Here comes the workload.
	 */
	public void executeUnit(int nr) {
		for (int i=0; i<5000; ++i) {
			;
		}
	}

	/**
	 * return number of independent tasks
	 */
	public int getNrOfUnits() {
		return 10;
	}


}
