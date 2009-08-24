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
package sp;

import com.jopdesign.io.SysDevice;
import com.jopdesign.io.IOFactory;

/**
 * The sheduler API for the single-path based CMP system.
 * 
 * @author Martin Schoeberl (martin@jopdesign.com) Raimund Kirner
 *         (raimund@vmars.tuwien.ac.at)
 * 
 */
public class STScheduler implements Runnable {
	int i;
	public int time = 0;
	int period = 0;
	static SysDevice sys = IOFactory.getFactory().getSysDevice();
	static final int MEM_TDMA_ROUND = 18; // length of the mem TDA round (=
											// #Cores * size_of_TDMA_slot)

	/**
	 * A helper runnable to treat the different task phases in uniform way.
	 * 
	 * @author Raimund Kirner (raimund@vmars.tuwien.ac.at)
	 * 
	 */
	static abstract class Runner implements Runnable {
		SimpleTask task;
		int tmeas;

		public Runner(SimpleTask st) {
			task = st;
		}

		public void measure() {
			int tstart, tstop;
			tstart = sys.cntInt;
			run();
			tstop = sys.cntInt;
			tmeas = tstop-tstart;
		}

		public int getMeasResult() {
			int tstart, tstop;
			tstart = sys.cntInt;
			tstop = sys.cntInt;
			return tmeas - (tstop-tstart);
		}
		
	}

	/**
	 * A helper runnable for the read phase
	 * 
	 * @author Martin Schoeberl (martin@jopdesign.com)
	 * 
	 */
	static class RRunner extends Runner {
		public RRunner(SimpleTask st) {
			super(st);
		}

		public void run() {
			task.read();
		}
	}

	/**
	 * A helper runnable for the execute phase
	 * 
	 * @author Martin Schoeberl (martin@jopdesign.com)
	 * 
	 */
	static class XRunner extends Runner {
		public XRunner(SimpleTask st) {
			super(st);
		}

		public void run() {
			task.execute();
		}
	}

	/**
	 * A helper runnable for the write phase
	 * 
	 * @author Martin Schoeberl (martin@jopdesign.com)
	 * 
	 */
	static class WRunner extends Runner {
		public WRunner(SimpleTask st) {
			super(st);
		}

		public void run() {
			task.write();
		}
	}

	/**
	 * A table for the cyclic executive...
	 * 
	 * @author Raimund Kirner (raimund@vmars.tuwien.ac.at)
	 * 
	 */
	static class TabCyclicExec {
		Runner tsk;
		int tactivation;
	}

	public TabCyclicExec[] tabCyclicExec; /*
										 * this table must be filled with the
										 * task schedules
										 */

	// Constructor
	public STScheduler(int maxtask) {
		tabCyclicExec = new TabCyclicExec[maxtask];
		for (i = 0; i < maxtask; i++) {
			tabCyclicExec[i] = new TabCyclicExec();
		}
		// System.out.println("STScheduler.constructor("+maxtask+")");System.out.flush();
	}

	/**
	 * Get the operating frequency of the processor in clock ticks per
	 * millisecond.
	 * 
	 * @return
	 */
	public int getMsCycles() {
		// TODO: add query method to the I/O factory
		return 60000;
	}

	/**
	 * Shall we really provide a wrapper for a standard Java class/method?
	 * 
	 * @return
	 */
	public int getNrCores() {
		return Runtime.getRuntime().availableProcessors();
	}

	/**
	 * The major cycle for all cores.
	 * 
	 * @param period
	 */
	public void setMajorCycle(int period) {
		this.period = period;
	}

	/**
	 * Perform a wait till the begin of the next scheduling cycle
	 * 
	 * @return
	 */
	public boolean waitForNextPeriod() {
		time = time + period;
		// sys.deadLine = time; /* perform the wait */
		return true;
	}

	/**
	 * Perform a wait till the start of a new round of the TDMA memory arbiter.
	 * This allows to synchronize the start of a task to be allways in the same
	 * sync with the TDMA memory arbiter. Drawback of implementation: is
	 * hardware dependent, as it has to wait longer than the calculation of the
	 * waiting time takes.
	 * 
	 * @return
	 */
	public static boolean syncWithMEMTDMA() {
		sys.deadLine = ((sys.cntInt + 2253) /*
											 * 2253 cycles is the determined
											 * WCET of this method
											 */
		/ MEM_TDMA_ROUND) * MEM_TDMA_ROUND;
		// + 60000*100; // (mod time + 100ms);
		return true;
	}

	/**
	 * Start the execution of the remaining cores of the JOP
	 * 
	 * @return
	 */
	public void startCPUs() {
		sys.signal = 1;
	}

	/**
	 * Add a simple task to the static schedule.
	 * 
	 * @param task
	 *            the task
	 * @param core
	 *            the CMP core where it shall run
	 * @param readStart
	 *            start time relative to the major frame in clock cycles for the
	 *            data read
	 * @param exeStart
	 *            start time of the execute phase
	 * @param writeStart
	 *            start time for the data write phase
	 */
	public void addTask(SimpleTask task, int core, int readStart, int exeStart,
			int writeStart) {
		// TODO: insert the task in a runtime data structure
	}

	public void genShedule() {
		// TODO wrap all tasks into lists of runnables for each core
	}

	/**
	 * Start the mission phase. All cores execute their static schedule.
	 */
	public void startMission() {

	}

	/**
	 * Start the cyclic executive....
	 */
	public void run() {
		waitForNextPeriod();
		for (;;) {
			for (i = 0; i < tabCyclicExec.length; i++) {
				// System.out.println("STSscheduler.run().i="+i);
				if (tabCyclicExec[i].tsk != null) {
					/* wait for the begin of the task activation */
					// sys.deadLine = (time + tabCyclicExec[i].tactivation);
					/* start the task (either read(), execute(), or write()) */
					tabCyclicExec[i].tsk.run();
				}
			}
			waitForNextPeriod();
		}
	}
}
