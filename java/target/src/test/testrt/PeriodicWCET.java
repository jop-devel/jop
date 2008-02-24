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

//
//	max threads, max stack, max function length
//

public class PeriodicWCET {

	public static void main(String[] args) {

		RtThread rt = new RtThread(10, 100000) {
			public void run() {
				// f1();	does NOT work!!!
				f4();
			}

			void f1() { f2(); }
			void f2() { f3(); }
			void f3() { f4(); }
			void f4() { f5(); }
			void f5() { f6(); }
			void f6() { f7(); }
			void f7() { f8(); }
			void f8() { f9(); }
			void f9() { f10(); }
			void f10() { loop(); }

			void loop() {

// for maximum code length
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
Timer.cnt();
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

		//
		// do busy work
		//

		for (int i=0; i<6; ++i) {
			RtThread rts = new RtThread(9, 1000000) {
				public void run() {
					f1();
				}
				void f1() { f2(); }
				void f2() { f3(); }
				void f3() { f4(); }
				void f4() { f5(); }
				void f5() { f6(); }
				void f6() { f7(); }
				void f7() { f8(); }
				void f8() { f9(); }
				void f9() { f10(); }
				void f10() { f11(); }
				void f11() { loop(); }

				void loop() {
					for (;;) {
						System.out.print('*');
						waitForNextPeriod();
						int ts = Native.rd(Const.IO_US_CNT) + 890000;
						while (ts-Native.rd(Const.IO_US_CNT)>0)
							;
					}
				}
			};
		}

		RtThread.startMission();

		// sleep
		for (;;) {
			Timer.wd();
			try { RtThread.sleepMs(1200); } catch (Exception e) {}
		}
	}
}
