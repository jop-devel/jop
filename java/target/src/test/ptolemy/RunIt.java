/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2009, Martin Schoeberl (martin@jopdesign.com)

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
 * 
 */
package ptolemy;

import joprt.RtThread;

/**
 * @author Martin Schoeberl (martin@jopdesign.com)
 *
 */
public class RunIt {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
        final Model model = new Model();
        model.initialize();

		int us = (int) (Model.PERIOD*1000000);
		if (us<100) {
			throw new Error("Period of "+us+" us not supported");
		}
		new RtThread(1, us) {
			public void run() {
				for (;;) {
			        try {
						model.run();
					} catch (Exception e) {
						e.printStackTrace();
					}
					waitForNextPeriod();
				}
			}
		};
		
		RtThread.startMission();
		// never invoked as we run forever
//        model.doWrapup();
//        System.exit(0);

	}

}
