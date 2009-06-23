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

/**
 * The sheduler API for the single-path based CMP system.
 * @author Martin Schoeberl (martin@jopdesign.com)
 *
 */
public class STScheduler {
	
	/**
	 * A helper runnable for the read phase
	 * @author Martin Schoeberl (martin@jopdesign.com)
	 *
	 */
	static class RRunner implements Runnable {
		SimpleTask task;
		public RRunner(SimpleTask st) {
			task = st;
		}
		public void run() {
			task.read();
		}
	}

	/**
	 * A helper runnable for the execute phase
	 * @author Martin Schoeberl (martin@jopdesign.com)
	 *
	 */
	static class XRunner implements Runnable {
		SimpleTask task;
		public XRunner(SimpleTask st) {
			task = st;
		}
		public void run() {
			task.execute();
		}
	}

	/**
	 * A helper runnable for the write phase
	 * @author Martin Schoeberl (martin@jopdesign.com)
	 *
	 */
	static class WRunner implements Runnable {
		SimpleTask task;
		public WRunner(SimpleTask st) {
			task = st;
		}
		public void run() {
			task.write();
		}
	}

	/**
	 * Get the operating frequency of the processor in clock
	 * ticks per millisecond.
	 * @return
	 */
	public int getMsCycles() {
		// TODO: add query method to the I/O factory
		return 60000;
	}
	
	/**
	 * Shall we really provide a wrapper for a standard Java class/method?
	 * @return
	 */
	public int getNrCores() {
		return Runtime.getRuntime().availableProcessors();
	}

	/**
	 * The major cycle for all cores.
	 * @param period
	 */
	public void setMajorCycle(int period) {
		
	}
	
	/**
	 * Add a simple task to the static schedule.
	 * @param task the task
	 * @param core the CMP core where it shall run
	 * @param readStart start time relative to the major frame in clock cycles for the data read
	 * @param exeStart start time of the execute phase
	 * @param writeStart start time for the data write phase
	 */
	public void addTask(SimpleTask task, int core, int readStart, int exeStart, int writeStart) {
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
}
