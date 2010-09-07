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
public class EnumeratedExecutor {

	/**
	 * A helper class that runs forever and executes work
	 * if there is some more to do.
	 * 
	 * @author martin
	 *
	 */
	private class Worker extends Thread {

		volatile boolean finished;
		WorkUnit ex;

		public Worker() {
			finished = true;
		}

		void setExecute(WorkUnit e) {
			ex = e;
		}

		public void run() {
			for (;;) {
				if (cnt<size && !finished) {
					int i;
					synchronized(ex) {
						i = cnt++;
					}
					if (i<size) {
						ex.executeUnit(i);
					}
				} else {
					finished = true;
					if (requestStop) {
						// break out and terminate this thread
						break;
					}
				}
			}
		}
	}

	/** tracking the work done by all runners */
	private volatile int cnt;
	/** size of workload */
	private volatile int size;
	/** Executor not needed anymore - stop threads */
	private volatile boolean requestStop;
	
	private Worker runner[];
	int cpus = Util.getNrOfCores();

	public EnumeratedExecutor() {

		runner = new Worker[cpus-1];
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

	public void executeParallel(WorkUnit e, int size) {

		this.size = size;
		cnt = 0;

		// distribute the work to all cores.
		for (int i=0; i<cpus-1; ++i) {
			runner[i].setExecute(e);
			runner[i].finished = false;
		}
		// do also some work
		while (cnt<size) {
			int i;
			synchronized(e) {
				i = cnt++;
			}
			if (i<size) {
				e.executeUnit(i);
			}
		}
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


		// that would be a serial version for tests:
//		for (int i=0; i<size; ++i) {
//			e.execute(i);
//		}
	}
	/**
	 * Just a simple main for the usage example
	 * @param args
	 */
	public static void main(String[] args) {

		WorkUnit e = new Test();
		EnumeratedExecutor pe = new EnumeratedExecutor();
		pe.start();
		pe.executeParallel(e, e.getNrOfUnits());
		pe.stop();
		Test.result();
	}

	/**
	 * An example how to use the executor framework.
	 * 
	 * @author martin
	 *
	 */
	private static class Test implements WorkUnit {

		final static int N = 20;
		static int a[] = new int[N];
		static int cnt;
		
		public void executeUnit(int nr) {
			synchronized (this) {
				a[nr] = ++cnt;
			}
			// waste some time with busy wait
			long t = System.currentTimeMillis();
			while (System.currentTimeMillis()-t < 10) {
				;
			}
		}

		public static void result() {
			for (int i=0; i<N; ++i) {
				System.out.println(a[i]);
			}
		}

		public int getNrOfUnits() {
			return N;
		}
	}
}
