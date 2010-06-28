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
package rttm.WaitFreeQueue;

import com.jopdesign.io.IOFactory;
import com.jopdesign.io.SysDevice;
import com.jopdesign.sys.Startup;

public class ReadWriteRunner {

	static SysDevice sys = IOFactory.getFactory().getSysDevice();

	private static WaitFreeReadWriteQueue qAB;
	private static WaitFreeReadWriteQueue qBC;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("creating queues.");
		qAB = new WaitFreeWriteQueue(Const.CAPACITY);
		qBC = new WaitFreeWriteQueue(Const.CAPACITY);

		System.out.println("started");
		if (sys.nrCpu < 4) {
			System.out.println("Not enough CPUs for this example");
			System.exit(-1);
		}
		ReadWriteQueueInserter ins = new ReadWriteQueueInserter(qAB);
		// ReadWriteQueueInserter ins2 = new ReadWriteQueueInserter(qAB);
		ReadWriteQueueMover mov = new ReadWriteQueueMover(qAB, qBC);
		// ReadWriteQueueMover mov2 = new ReadWriteQueueMover(qAB, qBC);
		ReadWriteQueueRemover rem = new ReadWriteQueueRemover(qBC);
		// ReadWriteQueueRemover rem2 = new ReadWriteQueueRemover(qBC);

		System.out.println("initialized");

		Startup.setRunnable(mov, 1);
		Startup.setRunnable(ins, 0);
		// Startup.setRunnable(ins2, 4);
		Startup.setRunnable(rem, 2);
		// Startup.setRunnable(mov2, 5);
		// Startup.setRunnable(rem2, 6);

		System.out.println("setting runnables done.");

		// start the CPUs
		sys.signal = 1;

		while (!ins.finished || !mov.finished || !rem.finished) {
		}

		System.out.println("done");
	}
}
