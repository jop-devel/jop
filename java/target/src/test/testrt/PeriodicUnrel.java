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

//	use different (unrelated) period to find WC jitter

public class PeriodicUnrel {

	static class Busy extends RtThread {

		private int c;

		Busy(int per, int ch) {
			super(5, per);
			c = ch;
		}

		public void run() {
			for (;;) {
				waitForNextPeriod();
			}
		}
	}
	
	public static void main(String[] args) {

		RtThread rt = new RtThread(10, 100000) {
			public void run() {

				waitForNextPeriod();
				int ts_old = Native.rd(Const.IO_US_CNT);

				for (;;) {
					waitForNextPeriod();
					int ts = Native.rd(Const.IO_US_CNT);
					Result.printPeriod(ts_old, ts);
					ts_old = ts;
				}
			}
		};

		int i;
		for (i=0; i<10; ++i) {
			new Busy(2345+456*i, i+'a');
		}

		RtThread.startMission();

		// sleep
		for (;;) {
			System.out.print('M');
			Timer.wd();
			for (;;) ;
			// try { Thread.sleep(1200); } catch (Exception e) {}
		}
	}
}
