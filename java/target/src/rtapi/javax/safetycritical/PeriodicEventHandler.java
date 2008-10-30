/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2008, Martin Schoeberl (martin@jopdesign.com)

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
package javax.safetycritical;

import javax.realtime.PriorityParameters;
import javax.realtime.RelativeTime;

import joprt.RtThread;

/**
 * The class that represents periodic activity. Should be used
 * as the main vehicle for real-time applications.
 * 
 * Now finally we have a class that the implementation can use.
 * 
 * @author Martin Schoeberl
 *
 */
public abstract class PeriodicEventHandler extends ManagedEventHandler {

	PriorityParameters priority;
	RelativeTime start, period;
	ThreadConfiguration tconf;
	String name;
	
	RtThread thread;
	
	/**
	 * Create a periodic event handler.
	 */
	public PeriodicEventHandler(PriorityParameters priority,
			PeriodicParameters parameters,
			ThreadConfiguration tconf,
			String name) {
		this.priority = priority;
		start = parameters.start;
		period = parameters.period;
		this.tconf = tconf;
		this.name = name;
		
		int p = ((int) period.getMilliseconds())*1000 + 
				period.getNanoseconds()/1000;
		int off = ((int) start.getMilliseconds())*1000 + 
				start.getNanoseconds()/1000;
		
		thread = new RtThread(priority.getPriority(), p, off) {
		
			public void run() {
				while(!MissionDescriptor.terminationRequest) {
					handleAsyncEvent();
					waitForNextPeriod();					
				}
			}
		};
	}
	
	public PeriodicEventHandler(PriorityParameters priority,
			PeriodicParameters parameters,
			ThreadConfiguration tconf) {
		this(priority, parameters, tconf, "");
	}
}
