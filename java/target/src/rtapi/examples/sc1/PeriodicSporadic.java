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

package examples.sc1;

import safetycritical.RtEvent;
import safetycritical.RtSystem;

public class PeriodicSporadic {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		final RtEvent rte = new RtEvent("SWEVENT", 2000000) {
			protected boolean run() {
				System.out.println("SW event fired");
				return true;
			}		
		};

		new RtEvent(1000000) {

			int counter = 0;
			
			protected boolean run() {
				System.out.println("P1");
				++counter;
				if (counter%2==1) {
					RtSystem.fire(rte);
				}
				if (counter==10) {
					RtSystem.stop();
				}
				return true;
			}
			
			protected boolean cleanup() {
				System.out.println("cleanup invoked!");
				return true;
			}
			
		};
		
		RtSystem.start();
	}

}