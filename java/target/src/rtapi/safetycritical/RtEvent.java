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

package safetycritical;

import java.util.*;

public abstract class RtEvent {
	
	int period;
	int deadline;
	int offset;
	String event;
	
	static Vector eventList;

	
	/**
	 * A periodic real-time event, equivalent to a periodic thread
	 * @param period
	 * @param deadline
	 * @param offset
	 */
	public RtEvent(int period, int deadline, int offset) {

		if (eventList==null) {
			eventList = new Vector();
		}
		this.period = period;
		this.deadline = deadline;
		this.offset = offset;
		this.event = null;
		
		// insert in a deadline monotonic order
		int cnt = eventList.size();
		int pos;
		for (pos=0; pos<cnt; ++pos) {
			RtEvent re = (RtEvent) eventList.elementAt(pos);
			if (deadline<=re.deadline) {
				break;
			}
		}

		eventList.insertElementAt(this, pos);
	}
	
	public RtEvent(int period, int deadline) {
		this(period, deadline, 0);
	}
	
	public RtEvent(int period) {
		this(period, period, 0);
	}
	
	/**
	 * A sporadic event with a minimum interarrival time
	 * @param event
	 * @param minInterval
	 * @param deadline
	 */
	public RtEvent(String event, int minInterval, int deadline) {
		
		this(minInterval, deadline, 0);
		this.event = event;
	}
	

	public RtEvent(String event, int minInterval) {
		this(event, minInterval, 0);
	}

	
	/**
	 * The logic for the event. run() gets invoked from the
	 * scheduler either periodic, or on a hardware event or
	 * on a software event (fire):
	 * @return true if ready for termination
	 */
	abstract protected boolean run();
	
	/**
	 * Gets invoked in the shutdown phase at the same period as
	 * run (instead of run()). Invoked until return true.
	 * @return true if shutdown is finished.
	 */
	protected boolean cleanup() {
		return true;
	}
}
