/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) Martin Schoeberl   <martin@jopdesign.com>
                Thomas B. Preusser <thomas.preusser@tu-dresden.de>

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
 * A utility class for CMP tests with work distribution.
 * 
 * @author Martin Schoeberl <martin@jopdesign.com>
 * @author Thomas B. Preusser <thomas.preusser@tu-dresden.de>
 */
public class Runner implements Runnable {

	private static volatile boolean stop;

	private final Runnable[] workList;

	public Runner(Runnable[] list) {
		workList = list;
	}

	private static final Runnable[] DUMMY_ARRAY = new Runnable[] { new Runnable() {
		public void run() {
			// loop on idle CPUs to somewhat reduce the pressure on reading stop
			for (int i = 0; i < 1000; ++i)
				;
		}
	} };

	static Runnable dummy = new Runnable() {

		public void run() {
			while (!stop) {
				// do something when idle
				for (int i = 0; i < 1000; ++i) {
					;
				}
			}
		}

	};

	/**
	 * Distribute the work list so that each Runner becomes responsible for
	 * between floor(work.length/nrCpu) and ceil(work.length/nrCpu) Runnables.
	 * The Runnables for one Runner are taken with a stride of nrCpu from the
	 * original work list.
	 * 
	 * Tom's version as round robin.
	 * 
	 * @param work
	 * @param nrCpu
	 * @return
	 */
	public static Runner[] distributeWorklistTom(final Runnable[] work,
			final int nrCpu) {
		// Distributed workload
		final Runner[] runner = new Runner[nrCpu];

		final int n = work.length;
		for (int i = 0; i < nrCpu; i++) {
			// MS: the following line could contain a bug....
			// why is it -i?
			int s = (n + nrCpu - i - 1) / nrCpu; // Size of local Workload
			final Runnable[] workload;
			if (s == 0) {
				workload = DUMMY_ARRAY;
				// System.out.println("cpu="+i+" dummy runnable");
			} else {
				workload = new Runnable[s];
				for (int j = i; j < n; j += nrCpu) {
					workload[--s] = work[j];
					// System.out.println("cpu="+i+" runnable="+j);
				}
			}
			runner[i] = new Runner(workload);
		}

		return runner;
	}

	/**
	 * Distribute the work list. Runner 1 to n-1 get ceil(work.length, n)
	 * Runnables. The last Runner gets the rest.
	 * 
	 * @param work
	 * @param nrCpu
	 * @return
	 */

	public static Runner[] distributeWorklist(Runnable[] work, int nrCpu) {
		// Distribute the workload
		Runner[] runner = new Runner[nrCpu];
		int cnt = 0;
		for (int i = 0; i < nrCpu; i++) {

			int toDistribute = work.length - cnt;
			int remCpus = nrCpu - i;
			int perCpu = 1;
			if (toDistribute % remCpus != 0) {
				// ceiling
				perCpu = (toDistribute + remCpus - 1) / remCpus;
			} else {
				perCpu = toDistribute / remCpus;
			}
			// System.out.println("toDist="+toDistribute+"remCpus="+remCpus+" perCpu="+perCpu);
			// if (perCpu<0) perCpu = 0;
			Runnable localWork[] = new Runnable[perCpu];
			if (perCpu == 0) {
				localWork = new Runnable[] { dummy, };
				// System.out.println("cpu="+i+" dummy runnable");
			}
			for (int j = 0; j < perCpu; ++j) {
				// System.out.println("cpu="+i+" runnable="+cnt);
				if (cnt >= work.length) {
					localWork[j] = dummy;
				} else {
					localWork[j] = work[cnt];
					++cnt;
				}
			}
			runner[i] = new Runner(localWork);
		}
		return runner;
	}

	static public void stop() {
		stop = true;
	}

	static public void reset() {
		stop = false;
	}

	public void run() {
		while (!stop) {
			for (int i = 0; i < workList.length; ++i) {
				workList[i].run();
			}
		}
	}
}
