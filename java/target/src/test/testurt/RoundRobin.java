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
import jopurt.*;
import util.Dbg;
import util.Timer;

public class RoundRobin extends Scheduler {

	/**
	*	test threads
	*/
	static class Work extends RtUserThread {
		int c;
		Work(int ch) {

			super(5, 100000);
			c = ch;
		}

		public void run() {

			for (;;) {
				Dbg.wr(c);
				// busy wait to simulate
				// 3 ms workload in Work.
				int ts = Scheduler.getNow();
				ts += 3000;
				while (ts-Scheduler.getNow()>0)
					;
			}
		}
	}


	//
	//	user scheduler starts here
	//
	public void RoundRobin() {
	}

	public void addTask(Task t) {
		// we do not allow tasks
		// to be added after start()
	}


	//
	//	called by JVM
	//
	public void schedule() {

		RtUserThread th = active.next;
		if (th==null) th = RtUserThread.head;
		dispatch(th, getNow()+10000);
	}


	public static void main(String[] args) {

		Dbg.initSer();				// use serial line for debug output

		new Work('a');
		new Work('b');
		new Work('c');

		RoundRobin rr = new RoundRobin();

		rr.start();

		// sleep
		for (;;) {
			Dbg.wr('M');
			Timer.wd();
			RtUserThread.sleepMs(1200);
		}
	}

}
