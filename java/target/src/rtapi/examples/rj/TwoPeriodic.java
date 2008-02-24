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

/*
 * Created on 22.10.2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package examples.rj;

import javax.realtime.AbsoluteTime;
import javax.realtime.RelativeTime;
import ravenscar.*;
import safetycritical.RtSystem;


/**
 * A minimal ravenscar Java example.
 * @author martin
 *
 */
public class TwoPeriodic extends Initializer {

	static boolean stopRequest = false;
	
	public void run() {

		new PeriodicThread(				
			new PriorityParameters(10),
			new PeriodicParameters(
				new AbsoluteTime(0, 0),
				new RelativeTime(500, 0)
			),
			new Runnable() {
				boolean stopped = false;
				
				public void run() {
					if (!stopRequest) {
						System.out.println("P1");						
					} else if (!stopped) {
						stopped = cleanup();
					}
				}
				
				protected boolean cleanup() {
					System.out.println("cleanup in P1 invoked!");
					return true;
				}
			}
		);

		new PeriodicThread(
			new PriorityParameters(9),
			new PeriodicParameters(
				new AbsoluteTime(0, 0),
				new RelativeTime(1000, 0)
			),
			new Runnable() {
				int counter = 0;

				public void run() {
					if (!stopRequest) {
						++counter;
						System.out.println("P2");																		
						if (counter==5) {
							System.out.println("Stop request from P2");
							stopRequest = true;
						}
					}
				}
			}
		);

	}
	
	public static void main(String[] args) {

		new TwoPeriodic().start();
	}
}
