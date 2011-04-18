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
 * Base class of all StreamBenchmarks.
 * 
 * @author Thomas B. Preusser <thomas.preusser@tu-dresden.de>
 * @author Martin Schoeberl (martin@jopdesign.com)
 */
public abstract class StreamBenchmark extends Benchmark {

	ParallelExecutor pe;

	protected StreamBenchmark() {
		pe = ParallelExecutor.getExecutor();
	}

	public int measure() {

		int start, cnt, time;
		// run the benchmark loop 1 times minimum
		cnt = 1;
		time = 0;

		Runnable[] workers = Runner.distributeWorklist(getWorkers(), Util.getNrOfCores());
		pe.start();

		while (time < MIN_EXECUTE) {
			cnt <<= 1;
			if (cnt < 0) {
				break;
			}
		    start = Util.getTimeMillis();
			reset(cnt);
			Runner.reset();
			pe.executeParallel(workers);			
		    time = Util.getTimeMillis() - start;
		}

		// let the worker threads terminate
		pe.stop();
		// save raw values
		setRawResult(cnt, time);

		// return iterations per second
		return getResult();

	}

	/**
	 * MS: what is this for?
	 * @return
	 */
	protected abstract int getDepth();

	public abstract Runnable[] getWorkers();

	/**
	 * Reset the benchmark for a new run with cnt iterations.
	 */
	public abstract void reset(int cnt);
	
	/**
	 * MS: what for are we using this?
	 * @return
	 */
	public abstract boolean isFinished(); 
}
