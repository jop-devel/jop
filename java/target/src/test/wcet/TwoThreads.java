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

package wcet;

import joprt.*;
import util.Timer;
import jbe.kfl.Mast;
import jbe.lift.LiftControl;
import jbe.lift.TalIo;

/**
 * The example for CPs JOP/DMa paper
 * 
 * @author martin
 *
 */
public class TwoThreads {

	private static LiftControl ctrl;
	private static TalIo io;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// initialization
		Mast.main(null);

		new RtThread(1, 3000) {
			
			public void run() {
				
				for (;;) {
					Mast.loop();
//					System.out.print("*");
					if (!waitForNextPeriod()) {
						System.out.println("Kfl missed a deadline");
					}
				}
			}
		};


		new RtThread(2, 500) {
			

			public void run() {

				ctrl = new LiftControl();
				io = new TalIo();
				waitForNextPeriod();
				
				for (;;) {
					ctrl.setVals();
					ctrl.getVals();
					ctrl.loop(io);
//					System.out.print('.');
					if (!waitForNextPeriod()) {
						System.out.println("Lift missed a deadline");
					}
				}
			}
		};

		System.out.println("Start Mission");
		RtThread.startMission();
		
		for (;;) {
			Timer.wd();
			RtThread.sleepMs(1000);
		}
	}

}
