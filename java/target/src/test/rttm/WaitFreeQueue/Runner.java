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

public class Runner {

	static SysDevice sys = IOFactory.getFactory().getSysDevice();

	private static WaitFreeQueue qAB;
	private static WaitFreeQueue qBC;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (Const.implType == Const.ImplType.CAS_LOCK) {
			qAB = new WaitFreeQueueCAS_LOCK();
			qBC = new WaitFreeQueueCAS_LOCK();
		} else if (Const.implType == Const.ImplType.CAS_TM) {
			qAB = new WaitFreeQueueCAS_TM();
			qBC = new WaitFreeQueueCAS_TM();
		} else if (Const.implType == Const.ImplType.LOCK) {
			qAB = new WaitFreeQueueLOCK();
			qBC = new WaitFreeQueueLOCK();
		} else if (Const.implType == Const.ImplType.TM) {
			qAB = new WaitFreeQueueTM();
			qBC = new WaitFreeQueueTM();
		} else {
			System.exit(-1);
		}

		System.out.println("started");
		if (sys.nrCpu < 7) {
			System.out.println("Not enough CPUs for this example");
			System.exit(-1);
		}
		Inserter ins = new Inserter(qAB);
		Inserter ins2 = new Inserter(qAB);
		Mover mov = new Mover(qAB, qBC);
		Mover mov2 = new Mover(qAB, qBC);
		Remover rem = new Remover(qBC);
		Remover rem2 = new Remover(qBC);

		System.out.println("initialized");

		Startup.setRunnable(ins, 0);
		Startup.setRunnable(mov, 1);
		Startup.setRunnable(ins2, 3);
		Startup.setRunnable(rem, 2);
		Startup.setRunnable(mov2, 4);
		Startup.setRunnable(rem2, 5);

		System.out.println("setting runnables done.");

		// start the CPUs
		sys.signal = 1;

		while (!ins.finished || !mov.finished || !rem.finished) {
		}

		System.out.println("done");
	}
}
