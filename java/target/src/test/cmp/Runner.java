/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2008, Martin Schoeberl (martin@jopdesign.com)

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

/**
 * A utility class for CMP tests without a scheduler.
 * 
 * @author Martin Schoeberl
 *
 */
class Runner implements Runnable {

	Runnable[] workList;
	static volatile boolean stop;
	
	public Runner(Runnable[] list) {
		workList = list;
	}
	
	static Runnable dummy = new Runnable() {

		public void run() {
			while(!stop) {
				util.Timer.usleep(10);
			}
		}
		
	};
	
	/**
	 * Distribute the work list. Runner 1 to n-1 get ceil(length, n) Runnables.
	 * The last Runner gets the rest.
	 * @param work
	 * @param nrCpu
	 * @return
	 */
	public static Runner[] distributeWorklist(Runnable[] work, int nrCpu) {
		// Distribute the workload
		Runner[] runner = new Runner[nrCpu];
		int cnt = 0;
		System.out.println(nrCpu);
		for (int i=0; i<nrCpu; i++) {
			
			int toDistribute = work.length-cnt;
			int remCpus = nrCpu-i;
			int perCpu = 1;
			if (toDistribute%remCpus!=0) {
				// ceiling
				perCpu = (toDistribute+remCpus-1)/remCpus;
			} else {
				perCpu = toDistribute/remCpus;
			}
//			System.out.println("toDist="+toDistribute+"remCpus="+remCpus+" perCpu="+perCpu);
//			if (perCpu<0) perCpu = 0;
			Runnable localWork[] = new Runnable[perCpu];
			if (perCpu==0) {
				localWork = new Runnable[] {
						dummy,
				};
				System.out.println("cpu="+i+" dummy runnable");
			}
			for (int j=0; j<perCpu; ++j) {
				System.out.println("cpu="+i+" runnable="+cnt);
				if (cnt>=work.length) {
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
	
	static void stop() {
		stop = true;
	}
	
	public void run() {
		while(!stop) {
			for (int i=0; i<workList.length; ++i) {
				workList[i].run();
			}
		}
	}
}