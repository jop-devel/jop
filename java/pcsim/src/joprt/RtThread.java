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

/**
*	RtThread.java on PC for simulation of JOP
*/

package joprt;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

public class RtThread {

	private boolean nextOk;
	private int period;			// period in us
	private int offset;			// offset in us
private int next;

	private static boolean start;

	public RtThread(int prio, int us) {
	
		this(prio, us, 0);
	}

	public RtThread(int prio, int us, int off) {

		period = us;
		nextOk = false;

		new Thread() {
			public void run() {

				runner();
			}
		}.start();
	}

	public void run() {
	}

	void runner() {

		waitForMission();
		run();
	}

	public static void startMission() {
		start = true;
	}

	public void waitForMission() {

		while (!start) {
			yield();
		}
		nextOk = true;
		next = Native.rd(Const.IO_US_CNT);
	}


	public boolean waitForNextPeriod() {

		if (!nextOk) {
			next = Native.rd(Const.IO_US_CNT);		// this should not happen!
			nextOk = true;							// you forgot to wait on start mission
		}

		next += period;

		int i = Native.rd(Const.IO_US_CNT);
		if (next-i < 0) {							// missed time!
			next = i;								// correct next
			return false;
		}
/*
		state = WAITING;
		yield();
*/
		while (next-Native.rd(Const.IO_US_CNT) >= 0) {	// 'busy' wait with yield.
try { Thread.sleep(1); } catch (Exception e) {}
			yield();
		}
		return true;
	}

	/**
	*	dummy yield() for compatibility reason.
	*/
	public static void yield() {}


	public static void sleepMs(int millis) {
	
		int next = Native.rd(Const.IO_US_CNT)+millis*1000;
		while (Native.rd(Const.IO_US_CNT)-next < 0) {
try { Thread.sleep(1); } catch (Exception e) {}
			yield();
		}
	}
}
