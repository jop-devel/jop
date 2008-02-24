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

package examples.sc3;

import sc3.*;

public class PeriodicSporadic {


	public static void main(String[] args) {

		final RTlet rtl = new RTlet();

		final SporadicThread rte = new SporadicThread(
				new SporadicParamters(
						new RelativeTime(2000, 0),	// intervall
						new RelativeTime(0, 0),		// we don't know the cost
						new RelativeTime(2000, 0)	// deadline
						),
				"SWEVENT"
				) {
			public void run() {
				System.out.println("SW event fired");
			}		
		};

		new PeriodicThread(
				new PeriodicParameters(
						new RelativeTime(0,0),		// startt
						new RelativeTime(1000, 0),	// intervall
						new RelativeTime(0, 0),		// we don't know the cost
						new RelativeTime(1000, 0)	// deadline
						)
				) {

			int counter = 0;
			
			public void run() {
				System.out.println("P1");
				++counter;
				if (counter%2==1) {
					rte.fire();
				}
				if (counter==10) {
					rtl.stopRT();
				}
			}
			
			public void cleanup() {
				System.out.println("cleanup invoked!");
			}
			
		};
		
		rtl.initializeRT();
		
		rtl.startRT();
	}

}