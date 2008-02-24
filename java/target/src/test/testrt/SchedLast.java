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

//	Measure time with for scheduling of lowest priority thread

public class SchedLast {

	static class Busy extends RtThread {

		private int w, c;

		Busy(int per, int i) {
			super(10+i, per);
			w = per*9/100;
			c = i+'a';
		}

		public void run() {
			for (;;) {
				Dbg.wr(c);
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

		Dbg.initSer();				// use serial line for debug output

		RtThread rt = new RtThread(9, 1000000) {
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
			new Busy(100000, i);
		}

		RtThread.startMission();

// RtThread.debug();
		// sleep
		for (;;) {
			Timer.wd();
			try { RtThread.sleepMs(1200); } catch (Exception e) {}
		}
	}
}
