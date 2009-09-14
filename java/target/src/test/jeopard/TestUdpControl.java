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
package jeopard;

import joprt.RtThread;

/**
 * Test main for the UDP based JOP/JVM control.
 * 
 * @author Martin Schoeberl (martin@jopdesign.com)
 *
 */
public class TestUdpControl {
	
	static class MyControl extends UdpControl {
		int x, y;
		
		public MyControl() {
			super();
		}
		public String toString() {
			return "x="+x+" y="+y;
		}
	}

	
	/**
	 * Test the Control class.
	 * @param args
	 */
	public static void main(String[] args) {


		final MyControl ctrl = new MyControl();

		new RtThread(1, 500000) {
			public void run() {
				for (;;) {
					System.out.print("*");
					if (ctrl.dataAvail()) {
						ctrl.receive();
						// let's print the result without generating garbage:
						System.out.print("got message: x=");
						System.out.print(ctrl.x);
						System.out.print(" y=");
						System.out.println(ctrl.y);
					}
					waitForNextPeriod();
				}
			}
		};

		RtThread.startMission();		
	}
}
