/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2010, Martin Schoeberl (martin@jopdesign.com)

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

package csp;

import com.jopdesign.sys.Native;

import joprt.RtThread;

/**
 * This shall be a simple Hello World for the NoC based CSP.
 * 
 * 
 * @author Martin
 *
 */
public class HelloSpm {

	static volatile boolean finished;
	final static int CNT = 100;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		int start = 0;
		int stop = 0;
		int time = 0;

		System.out.println("SPM Hello World");

		int nrCpu = Runtime.getRuntime().availableProcessors();
		if (nrCpu < 3) {
			throw new Error("Not enogh CPUs");
		}

		Runnable sender = new Runnable() {

			public void run() {
				
				PrivateScope scope = new PrivateScope(1000);
				Runnable r = new Runnable() {
					public void run() {
						// do nothing at the moment
					}	
				};
				scope.enter(r);
			}
		};
		
		Runnable receiver = new Runnable() {

			public void run() {
				
				PrivateScope scope = new PrivateScope(1000);
				Runnable r = new Runnable() {
					public void run() {
						int buffer[] = new int[CNT];
						for (int i = 0; i < CNT; ++i) {					
							buffer[i] = i;
						}
						finished = true;
						
					}
				};
				scope.enter(r);
			}	
		};
		// ni must be translated from proc index to NoC address!
		new RtThread(sender, 1, 1000).setProcessor(1);
		new RtThread(receiver, 1, 1000).setProcessor(2);

		// start the other CPUs
		System.out.println("starting cpus.");
		RtThread.startMission();

		start = (int) System.currentTimeMillis();

		while (!finished) {
			;
		}
		// End of measurement
		stop = (int) System.currentTimeMillis();

		System.out.println("StartTime: " + start);
		System.out.println("StopTime: " + stop);
		time = stop - start;
		System.out.println("TimeSpent: " + time);

	}

}
