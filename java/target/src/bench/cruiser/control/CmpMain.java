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

import com.jopdesign.io.*;
import joprt.RtThread;

public class CmpMain {

	public static void main(String [] args) {

		SysDevice sys = IOFactory.getFactory().getSysDevice();

		SpeedManager manager = new SpeedManager();
		RtThread smThread = new RtThread(manager, 5, 10*1000);
		smThread.setProcessor(3%sys.nrCpu);

		System.out.println("created SpeedManager");

		Controller control = new Controller(manager);
		RtThread ctrlThread = new RtThread(control, 4, 10*1000);
		ctrlThread.setProcessor(0);

		System.out.println("created Controller");

		Filter frontLeftFilter = new Filter("FLF", manager.frontLeftSpeed);
		RtThread flThread = new RtThread(frontLeftFilter, 6, 5*1000);
		flThread.setProcessor(1%sys.nrCpu);

		Filter frontRightFilter = new Filter("FRF", manager.frontRightSpeed);
		RtThread frThread = new RtThread(frontRightFilter, 6, 5*1000);
		frThread.setProcessor(2%sys.nrCpu);

		Filter rearLeftFilter = new Filter("RLF", manager.rearLeftSpeed);
		RtThread rlThread = new RtThread(rearLeftFilter, 6, 5*1000);
		rlThread.setProcessor(2%sys.nrCpu);

		Filter rearRightFilter = new Filter("RRF", manager.rearRightSpeed);
		RtThread rrThread = new RtThread(rearRightFilter, 6, 5*1000);
		rrThread.setProcessor(1%sys.nrCpu);

		System.out.println("created Filters");

		Dispatcher dispatch = new Dispatcher(manager,
											 frontLeftFilter,
											 frontRightFilter,
											 rearLeftFilter,
											 rearRightFilter);
		RtThread dThread = new RtThread(dispatch, 7, 1000);
		dThread.setProcessor(0);

		System.out.println("created Dispatcher");

		RtThread.startMission();

		System.out.println("started Mission");

		for (;;) {
			// wait forever
		}
	}

}