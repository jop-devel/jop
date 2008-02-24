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
import joprt.RtThread;
import util.Dbg;
import util.Timer;

public class Two extends RtThread {

	int c;
	Two(int ch) {

		super(5, 100000);
		c = ch;
	}

	public void run() {

		for (;;) {
			Dbg.wr(c);
			waitForNextPeriod();
/*
					int ts = Native.rd(Native.IO_US_CNT) + 99000;
					while (ts-Native.rd(Native.IO_US_CNT)>0)
						;
*/
		}
	}
	

	public static void main(String[] args) {

		Dbg.initSer();				// use serial line for debug output

		new Two('a');
		new Two('b');
		new Two('c');

		RtThread.startMission();

		// sleep
		for (;;) {
			Dbg.wr('M');
			Timer.wd();
			RtThread.sleepMs(1200);
		}
	}

}
