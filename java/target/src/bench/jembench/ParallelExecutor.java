/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2009-2010, Martin Schoeberl (martin@jopdesign.com)

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
 * A minimal framework for work distribution to the CMP cores.
 *
 * @author Martin Schoeberl (martin@jopdesign.com)
 *
 */
public class ParallelExecutor {

	/**
	 * A helper class that runs forever and executes work
	 * if there is some more to do.
	 * 
	 * @author martin
	 *
	 */
	private class Worker extends Thread {

		volatile boolean finished;
		Runnable work;

		public Worker() {
			finished = true;
		}

		void setExecute(Runnable r) {
			work = r;
		}

		public void run() {
			for (;;) {
				if (!finished) {
					work.run();
					finished = true;
				} else {
					if (requestStop) {
						// break out and terminate this thread
						break;
					}
				}
			}
		}
	}

	/** Executor not needed anymore - stop threads */
	private volatile boolean requestStop;
	
	private Worker runner[];
	int cpus = Util.getNrOfCores();

	private ParallelExecutor() {

		runner = new Worker[cpus-1];
	}
	
	private static ParallelExecutor pe;
	
	static ParallelExecutor getExecutor() {
		synchronized (ParallelExecutor.class) {
			if (pe==null) {
				pe = new ParallelExecutor();
			}
		}
		return pe;
	}


	/**
	 * Create and start all worker threads
	 */
	public void start() {
		requestStop = false;
		for (int i=0; i<cpus-1; i++) {
			runner[i] = new Worker();
			runner[i].start();
		}		
	}
	
	/**
	 * Request termination from the worker threads
	 */
	public void stop() {
		requestStop = true;
		// we could do some waiting for the terminating threads....
	}

	/**
	 * Does the parallel execution. Can reuse the worker threads
	 * without stop/start.
	 * @param r
	 */
	public void executeParallel(Runnable r) {

		// distribute the work to all cores.
		for (int i=0; i<cpus-1; ++i) {
			runner[i].setExecute(r);
			runner[i].finished = false;
		}
		// do also some work
		r.run();
		// Now wait for others finishing their work.
		// We could use join, but the following is
		// also OK.
		boolean allFinished;

		do {
			allFinished = true;
			for (int i=0; i<cpus-1; ++i) {
				allFinished &= runner[i].finished;
			}
		} while (!allFinished);
		// now we can return
	}

	/**
	 * Does the parallel execution. Can reuse the worker threads
	 * without stop/start.
	 * @param r
	 */
	public void executeParallel(Runnable r[]) {

		// distribute the work to all cores.
		for (int i=0; i<cpus-1; ++i) {
			runner[i].setExecute(r[i+1]);
			runner[i].finished = false;
		}
		// do also some work
		r[0].run();
		// Now wait for others finishing their work.
		// We could use join, but the following is
		// also OK.
		boolean allFinished;

		do {
			allFinished = true;
			for (int i=0; i<cpus-1; ++i) {
				allFinished &= runner[i].finished;
			}
		} while (!allFinished);
		// now we can return
	}
}
