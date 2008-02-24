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

package vm04;
import joprt.RtThread;
import joprt.SwEvent;
import util.Dbg;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

public class Event {

	public final static int CNT = 500;
	static int[] result;
	static SwEvent sev;

	static int f_tim;

	public static void main(String[] args) {

		Dbg.initSerWait();				// use serial line for debug output
		result = new int[CNT];


		sev = new SwEvent(11, 10000) {

			public void handle() {
				f_tim = Native.rd(Const.IO_CNT);
			}
		};

		RtThread rt = new RtThread(10, 10000) {
			public void run() {

				int i, ts;

				for (i=0; i<CNT; ++i) {
					waitForNextPeriod();
					ts = Native.rd(Const.IO_CNT);
					sev.fire();
					result[i] = f_tim-ts;
				}
			
				result();
			}

			void result() {

				int max = 0;
				int min = 999999999;
				int i;

				for (i=0; i<CNT; ++i) {
					int diff = result[i];
					if (diff<min) min = diff;
					if (diff>max) max = diff;
					Dbg.intVal(diff);
					Dbg.wr('\n');
				}
				Dbg.intVal(min);
				Dbg.intVal(max);
				Dbg.wr('\n');

				for (;;) waitForNextPeriod();
			}
		};


		RtThread.startMission();

		for (;;) {
			;			// busy do nothing
		}
	}

}
