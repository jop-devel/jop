/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2009, Martin Schoeberl (martin@jopdesign.com)

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


/**
 *
 */
package cmp;

import com.jopdesign.io.IOFactory;
import com.jopdesign.io.SysDevice;
import com.jopdesign.sys.Startup;

/**
 * A minimal framework for work distribution to the CMP cores.
 *
 * @author Martin Schoeberl (martin@jopdesign.com)
 *
 */
public class ParallelExecutor {

	private class Runner implements Runnable {

		volatile boolean finished;
		Execute ex;

		public Runner() {
			finished = true;
		}

		void setExecute(Execute e) {
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
						ex.execute(i);
					}
				} else {
					finished = true;
				}
			}
		}

	}

	private volatile int cnt;
	private volatile int size;
	private Runner runner[];
	SysDevice sys = IOFactory.getFactory().getSysDevice();

	public ParallelExecutor() {

		runner = new Runner[sys.nrCpu-1];
		for (int i=0; i<sys.nrCpu-1; i++) {
			runner[i] = new Runner();
			Startup.setRunnable(runner[i], i);
		}
		// we already start all cores here
		sys.signal = 1;
	}

	public void executeParallel(Execute e, int size) {

		this.size = size;
		cnt = 0;

		// distribute the work to all cores.
		                                    //rup: setting 12 cores as max to avoid changing all the time
		for (int i=0; i<sys.nrCpu-1; ++i) { //@WCA loop=12
			runner[i].setExecute(e);
			runner[i].finished = false;
		}
		// do also some work
		                   //number of data points
		while (cnt<size) { //@WCA loop=14
			int i;
			synchronized(e) {
				i = cnt++;
			}
			if (i<size) {
				e.execute(i);
			}
		}
		// now wait for others finishing their work
		boolean allFinished;

		do {
			allFinished = true;                 //rup: setting 12 cores as max to avoid changing all the time
			for (int i=0; i<sys.nrCpu-1; ++i) { //@WCA loop=12
				allFinished &= runner[i].finished;
			}                   // rup: this is tricky, how to model it martin?
		} while (!allFinished); //@WCA loop=100
		// now we can return


		// that would be a serial version for tests:
//		for (int i=0; i<size; ++i) {
//			e.execute(i);
//		}
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Execute e = new Test();
		ParallelExecutor pe = new ParallelExecutor();
		pe.executeParallel(e, Test.N);
		Test.result();
	}

	private static class Test implements Execute {

		final static int N = 100;
		static int a[] = new int[N];

		public void execute(int nr) {
			a[nr] = IOFactory.getFactory().getSysDevice().cpuId+1;
		}

		public static void result() {
			for (int i=0; i<N; ++i) {
				System.out.println(a[i]);
			}
		}
	}


}
