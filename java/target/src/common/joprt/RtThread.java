/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2001-2008, Martin Schoeberl (martin@jopdesign.com)

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
*	RtThread.java
*/

package joprt;

import com.jopdesign.sys.RtThreadImpl;

public class RtThread implements Runnable {

	RtThreadImpl thr;
	private Runnable runner = null;

	public static void initClass() {
		RtThreadImpl.init();
	}
	// not necessary
	// private RtThread() {};

	public RtThread(int prio, int us) {	
		this(prio, us, 0);
	}

	public RtThread(Runnable runner, int prio, int us) {
		this(runner, prio, us, 0);
	}

	public RtThread(int prio, int us, int off) {
		this(null, prio, us, off);
	}

	public RtThread(Runnable runner, int prio, int us, int off) {
		this.runner = runner;
		thr = new RtThreadImpl(this, prio, us, off);
	}

	public void run() {
		if (runner != null)
			runner.run();
		// nothing else to do
	}


	public static void startMission() {
		
		RtThreadImpl.startMission();
	}


	public boolean waitForNextPeriod() {

		return thr.waitForNextPeriod();
	}

	/**
	 * Set the processor number
	 * @param id
	 */
	public void setProcessor(int id) {
		thr.setProcessor(id);
	}

	public static RtThread currentRtThread() {
		return RtThreadImpl.currentRtThread();
	}

	/**
	*	dummy yield() for compatibility reason.
	*/
//	public static void yield() {}


	/**
	*	for 'soft' rt threads.
	*/

	public static void sleepMs(int millis) {
		
		RtThreadImpl.sleepMs(millis);
	}

	/**
	 * Waste CPU cycles to simulate work.
	 * @param us execution time in us
	 */
	public static void busyWait(int us) {
		
		RtThreadImpl.busyWait(us);
	}
	
}
