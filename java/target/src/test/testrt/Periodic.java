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
import util.Dbg;
import util.Timer;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

public class Periodic {

	public static void main(String[] args) {

		RtThread rt = new RtThread(10, 100000) {
			public void run() {

				int ts, ts_old;

				waitForNextPeriod();
				ts_old = Native.rd(Const.IO_US_CNT);

				for (;;) {
					waitForNextPeriod();
					ts = Native.rd(Const.IO_US_CNT);
					Result.printPeriod(ts_old, ts);
					ts_old = ts;
				}
			}
		};

		//
		// do busy work
		//

		RtThread rts = new RtThread(9, 1000000) {
			public void run() {
				for (;;) {
					System.out.print('*');
					waitForNextPeriod();
					int ts = Native.rd(Const.IO_US_CNT) + 990000;
					while (ts-Native.rd(Const.IO_US_CNT)>0)
						;
				}
			}
		};

		RtThread.startMission();

		// sleep
		for (;;) {
			System.out.print('m');
// RtThread.debug();
			Timer.wd();
			RtThread.sleepMs(1200);
		}
	}

}
