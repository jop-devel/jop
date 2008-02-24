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

/*
 * Created on 09.12.2005
 *
 */
package gctest;

import util.Timer;
import joprt.RtThread;

import com.jopdesign.sys.GC;

public class Periodic {

	static final int SIZE = 1000;
//	static int[] ia;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		new RtThread(2, 50000) {
			public void run() {

				int i, iteration = 0;
//				This does not work because we don't get the
//				roots from the other threads!
				int [] ia = null;
				
				waitForNextPeriod();

				for (;;) {
					if (ia!=null) {
						for (i=0; i<SIZE; ++i) {
							if (ia[i] != iteration*SIZE + i) {
								System.out.println("GC Error");
								System.exit(-1);
							}
						}
					}
					++iteration;
					System.out.print("Alloc");
					ia = new int[SIZE];
					for (i=0; i<SIZE; ++i) {
						ia[i] = iteration*SIZE + i;
					}
					waitForNextPeriod();
				}
			}
		};

		new RtThread(3, 20000) {
			public void run() {

				for (;;) {
					System.out.print("m=");
					System.out.print(GC.freeMemory());
					waitForNextPeriod();
				}
			}
		};

		//
		// GC thread
		//
		new RtThread(1, 500000) {
			public void run() {

				GC.setConcurrent();
				for (;;) {
					System.out.print("G");
					GC.gc();
					waitForNextPeriod();
				}
			}
		};

		GC.gc();
		RtThread.startMission();

		// sleep
		for (int i=0;i<5;++i) {
			System.out.print("M");
			Timer.wd();
			RtThread.sleepMs(1000);
		}
		System.exit(0);

	}

}
