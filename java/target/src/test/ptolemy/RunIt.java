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
package ptolemy;

import joprt.RtThread;

/**
 * Test code generated from Ptolemy.
 * 
 * @author Martin Schoeberl (martin@jopdesign.com)
 * 
 */
public class RunIt {

	static Model model = new Model();
	// static WatchDog model = new WatchDog();
	// static WriteOutput model = new WriteOutput();
	// static ReadInput model = new ReadInput();
	// static SerialWrite model = new SerialWrite();
	// static SerialRead model = new SerialRead();

	/**
	 * Create and run a Ptolemy model.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		model.initialize();

		int us = (int) (model.PERIOD * 1000000);
		// If there is a useful period run it in a periodic
		// thread. If not just in a tight loop.
		if (us >= 100) {
			new RtThread(1, us) {
				public void run() {
					for (;;) {
						try {
							model.run();
						} catch (Exception e) {
							e.printStackTrace();
						}
						waitForNextPeriod();
					}
				}
			};
			RtThread.startMission();
		} else {
			for (;;) {
				try {
					model.run();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}

		// never invoked as we run forever
		// model.doWrapup();
		// System.exit(0);
	}

	/**
	 * A static method just for the WCET analysis.
	 * 
	 * @throws Exception
	 */
	public static void foo() throws Exception {
		model.run();
	}
}
