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

package testrt;

import joprt.RtThread;
import util.*;

/**
 * Test synchronized methods
 * @author martin
 *
 */
public class Synch {

	static Synch syn = new Synch();
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		new RtThread(2, 11000) {
			public void run() {

				for (;;) {
					syn.modify();
					waitForNextPeriod();
				}
			}
		};

		new RtThread(3, 5000) {
			public void run() {

				for (;;) {
					if (syn.read()!=0) {
						System.out.println("Synchronization error");
					}
					waitForNextPeriod();
				}
			}
		};

		new RtThread(1, 500000) {
			public void run() {

				for (;;) {
					System.out.print("*");
					Timer.wd();
					waitForNextPeriod();
				}
			}
		};
		
		RtThread.startMission();
	}

	int val;
	synchronized int read() {
		return val;
	}
	synchronized void modify() {
		val = 1;
		int t = Timer.getTimeoutMs(10);
		while (!Timer.timeout(t)) {
			; // busy wait
		}
		val = 0;
	}
}
