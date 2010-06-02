/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2010, Wolfgang Puffitsch <wpuffits@mail.tuwien.ac.at>

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

package cruiser.control;

import joprt.RtThread;

public class Main {

	public static void main(String [] args) {

		SpeedManager manager = new SpeedManager();
		RtThread smThread = new RtThread(manager, 10*1000, 5);

		Controller control = new Controller(manager);
		RtThread ctrlThread = new RtThread(control, 100*1000, 4);

		Filter frontLeftFilter = new Filter("FLF", manager.frontLeftSpeed);
		RtThread flThread = new RtThread(frontLeftFilter, 5*1000, 6);

		Filter frontRightFilter = new Filter("FRF", manager.frontRightSpeed);
		RtThread frThread = new RtThread(frontRightFilter, 5*1000, 6);

		Filter rearLeftFilter = new Filter("RLF", manager.rearLeftSpeed);
		RtThread rlThread = new RtThread(rearLeftFilter, 5*1000, 6);

		Filter rearRightFilter = new Filter("RRF", manager.rearRightSpeed);
		RtThread rrThread = new RtThread(rearRightFilter, 5*1000, 6);


		Dispatcher dispatch = new Dispatcher(manager,
											 frontLeftFilter,
											 frontRightFilter,
											 rearLeftFilter,
											 rearRightFilter);
		RtThread dThread = new RtThread(dispatch, -1, 3);

		RtThread.startMission();

		for (;;) {
			// wait forever
		}
	}

}