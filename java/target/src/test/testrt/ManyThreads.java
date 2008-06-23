/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2005-2008, Martin Schoeberl (martin@jopdesign.com)

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
package testrt;

import joprt.RtThread;

/**
 * @author martin
 *
 */
public class ManyThreads {

	final static int NR_THREADS = 400;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		System.out.println("Many threads test");
		
		for (int i=0; i<NR_THREADS; ++i) {

			final int nr = i;
			new RtThread(i, 1000*1000) {
				public void run() {
					for (;;) {
						System.out.print(nr);
						System.out.print(" ");
						waitForNextPeriod();
					}
				}
			};
		}

		RtThread.startMission();
	}

}
