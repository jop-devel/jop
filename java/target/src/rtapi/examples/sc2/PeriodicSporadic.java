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

package examples.sc2;

import sc2.*;

public class PeriodicSporadic {

	/**
	 * @param args
	 */
	public static void main(String[] args) {


		RtLogic sth = new RtLogic() {

			public void init() {
			}

			public void terminate() {
			}

			public void restart() {
			}

			public void run() {
				System.out.print("S fired");
			}			
		};

		RtSporadic rsp = new RtSporadic("", sth, 2000000);
		MyPeriodicLogic mp = new MyPeriodicLogic();
		mp.setEvent(rsp);
		new RtPeriodic(mp, 1000000);
		
		RtMission.prepare();
		
		RtMission.start();
		
		// this thread can terminate
	}

}

class MyPeriodicLogic implements RtLogic {
	
	int counter;
	RtSporadic event;

	public void init() {
		counter = 0;
	}
	
	public void setEvent(RtSporadic se) {
		event = se;
	}

	public void terminate() {
	}

	public void restart() {
	}

	public void run() {
		System.out.print("P");
		++counter;
		if (counter%2==1) {
			event.fire();
		}
		if (counter==10) {
			RtMission.stop();
		}
	}			

}
