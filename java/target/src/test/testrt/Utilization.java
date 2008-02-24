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
import util.*;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

public class Utilization {

	final static int MIN_US = 10;
	final static int PERIOD = 100000;
	
	
	public static void main(String[] args) {


		RtThread rt = new RtThread(10, PERIOD) {
			public void run() {

				int busy = PERIOD*9/10;
				for (;;) {
					int t = Native.rd(Const.IO_US_CNT)+busy;
					while (t-Native.rd(Const.IO_US_CNT)>0) {
						;
					}
					waitForNextPeriod();
				}
			}
		};


		RtThread.startMission();

		int t1, t2, t3;
		int idle, timeout;
		
		idle = 0;	
		t1 = Native.rd(Const.IO_US_CNT);
		timeout = t1;

		for (;;) {
			t2 = Native.rd(Const.IO_US_CNT);
			t3 = t2-t1;
			t1 = t2;
			if (t3<MIN_US) {
				idle += t3;
			}
			if (t2-timeout>1000000) {
				Timer.wd();
				t2 -= timeout;
				System.out.print(t2);
				System.out.print(" ");
				System.out.print(idle);
				System.out.print(" ");
				idle *= 100;
				idle /= t2;
				idle = 100-idle;
				System.out.print("CPU utilization [%]: ");
				System.out.print(idle);
				System.out.println();
				idle = 0;	
				t1 = Native.rd(Const.IO_US_CNT);
				timeout = t1;
			}
		}
	}

}
