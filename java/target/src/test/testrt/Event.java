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
import joprt.SwEvent;
import util.Dbg;

public class Event {

	public final static int CNT = 10;
	static SwEvent sev;

	public static void main(String[] args) {

		sev = new SwEvent(2, 10000) {

			public void handle() {
				System.out.println("fire!");
			}
		};

		RtThread rt = new RtThread(1, 100000) {
			public void run() {

				int i;

				for (i=0; i<CNT; ++i) {
					waitForNextPeriod();
					System.out.println("befor");
					sev.fire();
					System.out.println("after");
				}
			
				for (;;) waitForNextPeriod();
			}
		};

		RtThread.startMission();

		for (;;) {
			;			// busy do nothing
		}
	}

}
