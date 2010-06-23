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

  protected StreamBenchmark() {}

	public int measure() {

		int start, cnt, time;
		// run the benchmark loop 1 times minimum
		cnt = 1;
		time = 0;

		while (time < MIN_EXECUTE) {
			cnt <<= 1;
			if (cnt < 0) {
				break;
			}
			
			// a quick hack with the Executor - should be changed
			final ThreadPool pool = Executor.getExecutor().getPool();
		    pool.ensure(getDepth()-1);

		    start = Util.getTimeMillis();
			for (int i = 0; i < cnt; ++i) {
								
			      // Start Workers
			      final Runnable[]  workers = getWorkers();
			      for(int  j = workers.length; --j > 0; pool.pushTask(workers[j]));
			      workers[0].run();

			      // Join Threads
			      pool.waitForAll();

			}
			time = Util.getTimeMillis() - start;
		}

		// save raw values
		setRawResult(cnt, time);

		// return iterations per second
		return getResult();

	}

  public final long accept(Executor exec, int complexity) {
    return  exec.execute(this, complexity);
  }

  protected abstract int        getDepth();
  protected abstract Runnable[] getWorkers();
}

