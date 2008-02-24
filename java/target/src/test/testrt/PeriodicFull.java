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
import util.*;
import joprt.*;
import com.jopdesign.sys.*;

//	Measure time with a full thread queue

public class PeriodicFull {
	
	static final int NUM_THREADS = 6;

	static class Busy extends RtThread {

		private int w;
		private char c;

		Busy(int per, char ch) {
			super(5, per);
			// w = per*90/100;
			w = per/(NUM_THREADS+2); // some time for main thread
			c = ch;
		}

		public void run() {
			for (;;) {
				System.out.print(c);
				int ts = Native.rd(Const.IO_US_CNT);
				ts += w;
				// busy wait for period end
				while (ts-Native.rd(Const.IO_US_CNT)>0)
					;
				waitForNextPeriod();
			}
		}
	}
	
	public static void main(String[] args) {

		System.out.println("PeriodicFull test");

		RtThread rt = new RtThread(10, 100000) {
			public void run() {

				waitForNextPeriod();
				int ts_old = Native.rd(Const.IO_US_CNT);

				for (;;) {
					waitForNextPeriod();
					int ts = Native.rd(Const.IO_US_CNT);
//					System.out.print('*');
//					Result.printPeriod(ts_old, ts);
					ts_old = ts;
				}
			}
		};

		for (int i=0; i<NUM_THREADS; ++i) {
			new Busy(1000000, (char) (i+'a'));
		}

		RtThread.startMission();

		// sleep
		for (;;) {
// RtThread.debug();
			Timer.wd();
			RtThread.sleepMs(1200);
			System.out.print('M');
		}
	}
}
