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

package gcinc;

import joprt.RtThread;

import com.jopdesign.sys.GC;

public class Concurrent {
	

	static SimpVector a, b, c;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		a = new SimpVector(20);
		b = new SimpVector(100);
		c = new SimpVector(999);

		new RtThread(2, 300000) {
			public void run() {
				for (;;) {
					c.run();
					waitForNextPeriod();
				}
			}			
		};
		new RtThread(3, 100560) {
			public void run() {
				for (;;) {
					b.run();
					waitForNextPeriod();
				}
			}			
		};
		new RtThread(4, 50450) {
			public void run() {
				for (;;) {
					a.run();
					waitForNextPeriod();
				}
			}			
		};

		new RtThread(1, 1000) {
			public void run() {
				for (;;) {
//					int time = RtSystem.currentTimeMicro();
					System.out.print("G");
					GC.gc();
//					int now = RtSystem.currentTimeMicro();
//					System.out.println(now-time);
//					time = now;
					
//					waitForNextPeriod();
				}
			}
		};
		
		GC.setConcurrent();
		RtThread.startMission();
	}

}
