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
import util.Dbg;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

public class ContextSwitch {

	public final static int CNT = 500;

	static int[] result;
	static int ts;
	static int t_diff;

	public static void main(String[] args) {

		Dbg.initSerWait();				// use serial line for debug output
		result = new int[CNT];

		// low priority thread
		RtThread lprt = new RtThread(5, 100000) {

			public void run() {

				for (;;) {
					ts = Native.rd(Const.IO_CNT);
				}
			}
		};


		RtThread rt = new RtThread(10, 5000) {
			public void run() {

				int i;

				// give lprt a chance to start
				waitForNextPeriod();

				for (i=0; i<CNT; ++i) {
					waitForNextPeriod();
					result[i] = Native.rd(Const.IO_CNT)-ts;
				}
			
				result();
			}

			void result() {

				int max = 0;
				int min = 999999999;
				int i;

				for (i=0; i<CNT; ++i) {
					int val = result[i]-t_diff;
					if (val<min) min = val;
					if (val>max) max = val;
					Dbg.intVal(val);
					Dbg.wr('\n');
				}
				Dbg.intVal(min);
				Dbg.intVal(max);
				Dbg.wr('\n');

				for (;;) waitForNextPeriod();
			}
		};

		// measure time for measurement
		ts = Native.rd(Const.IO_CNT);
		ts = Native.rd(Const.IO_CNT)-ts;
		t_diff = ts;

		RtThread.startMission();

		for (;;) {
			;			// busy do nothing
		}
	}

}
