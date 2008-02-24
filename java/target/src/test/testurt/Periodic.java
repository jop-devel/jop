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

package testurt;
import util.Dbg;
import util.Timer;
import jopurt.*;

public class Periodic {

	public static void main(String[] args) {

		Dbg.initSer();				// use serial line for debug output

		RtUserThread rt = new RtUserThread(10, 100000) {
			public void run() {

				for (;;) {
					Dbg.wr('.');
					waitForNextPeriod();
				}
			}
		};

		RtUserThread rtx = new RtUserThread(9, 500000) {
			public void run() {

				for (;;) {
					Dbg.wr('+');
					waitForNextPeriod();
				}
			}
		};

		//
		// do busy work
		//

		RtUserThread rts = new RtUserThread(8, 1000000) {
			public void run() {
				for (;;) {
					Dbg.wr('*');
					int ts = Scheduler.getNow() + 990000;
					while (ts-Scheduler.getNow()>0)
						;
					waitForNextPeriod();
				}
			}
		};

		Dbg.wr("befor Start\n");
		RtUserThread.sleepMs(1000);
		Dbg.wr("after sleep\n");

		RtUserThread.startMission();

		Dbg.wr("after Start\n");

		// sleep
		for (;;) {
			Dbg.wr('M');
// RtThread.debug();
			Timer.wd();
			RtUserThread.sleepMs(1200);
		}
	}

}
