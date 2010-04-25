/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2005-2010, Martin Schoeberl (martin@jopdesign.com)

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
 * Thread.java
 * 
 * This is a kind of fake Thread class as it work only on a CMP
 * version of JOP. Only n threads can run on a n core system.
 */
package java.lang;

import java.util.Vector;

import com.jopdesign.io.IOFactory;
import com.jopdesign.io.SysDevice;
import com.jopdesign.sys.Startup;

public class Thread implements Runnable {

	public final static int MIN_PRIORITY = 1;
	public final static int NORM_PRIORITY = 5;
	public final static int MAX_PRIORITY = 10;
	
	Runnable run;
	// which core am I running?
	int runningOn;
	// only set when not able to run..
	boolean isDead;
	
	/**
	 * Runnable that is executed on the other CPUs.
	 * Runs forever and executes a Runnable when set.
	 * 
	 * @author martin
	 *
	 */
	static class Helper implements Runnable {

		volatile Runnable work;
		
		/**
		 * Look for work to execute.
		 */
		public void run() {
			for (;;) {
				if (work!=null) {
					work.run();
					work = null;
				}
			}
		}
		
		void setRunnable(Runnable r) {
			work = r;
		}
		
		boolean hasFinished() {
			return work==null;
		}
	}
	
	static Object monitor = new Object();
	static Helper runCmp[];
	
	/**
	 * Initialize all data structures and start the other processors
	 * on the CMP system.
	 */
	void doInit() {
		synchronized (monitor) {
			if (runCmp==null) {
				SysDevice sys = IOFactory.getFactory().getSysDevice();
				runCmp = new Helper[sys.nrCpu-1];
				for (int i=0; i<sys.nrCpu-1; ++i) {
					runCmp[i] = new Helper();
					Startup.setRunnable(runCmp[i], i);
				}
				// start the other CPUs
				sys.signal = 1;
			}			
		}
	}
	
	public Thread() {
		this(null);
	}
	
	public Thread(Runnable r) {
		run = r;
		runningOn = -1;
		doInit();
	}

	public static void yield() {
		;					// do nothing
	}

	public void run() {
		if (run!=null) {
			run.run();
		}
	}
	
	/**
	 * Find a free processor core and set this as his
	 * worker runnable.
	 */
	public void start() {
		synchronized(monitor) {
			for (int i=0; i<runCmp.length; ++i) {
				if (runCmp[i].hasFinished()) {
					runningOn = i;
					runCmp[i].setRunnable(this);
					return;
				}
			}			
		}
		isDead = true;
		// bad news: we did not find a free CPU
		throw new Error("Thread: no CPU free to run this thread");
	}

	public static void sleep(long l) throws InterruptedException {

		int tim = (int) l;
		joprt.RtThread.sleepMs(tim);
	}
	
	/**
	 * Do a busy wait for the thread to finish.
	 */
	public void join() {
		if (isDead) return;
		for (;;) {
			if (runCmp[runningOn].hasFinished()) {
				return;
			}
		}
	}
}
