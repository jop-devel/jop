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



/**
 * @author martin
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class MyEvent extends Initializer {

	public void run() {

		// init

		//
		//	an event handler
		//
		SporadicEventHandler evh = new SporadicEventHandler(
			new PriorityParameters(15),		// higher then periodic thread
			new SporadicParameters(
				new RelativeTime(33, 0),
				1							// what means buffer size?
			))
		{
			public void handleAsyncEvent() {

				System.out.print("fire!");
			}
		};

		//
		//	an sw event
		//
		final SporadicEvent ev = new SporadicEvent(evh);


		//
		//	one periodic Thread
		//
		new PeriodicThread(
			new PriorityParameters(10),
			new PeriodicParameters(
				new AbsoluteTime(0, 0),
				new RelativeTime(1000, 0)
			),
			new Runnable() {
				public void run() {
					System.out.print("befor ");
					ev.fire();
					System.out.println(" after");
				}
			}
		);
	}


	public static void main(String[] args) {

		MyEvent ma = new MyEvent();
		ma.start();
	}
}
