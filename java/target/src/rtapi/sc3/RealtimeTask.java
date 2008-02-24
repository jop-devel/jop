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

package sc3;

import java.util.*;

public abstract class RealtimeTask {
	
	int period;
	int deadline;
	int offset;
	String event;
	
	static Vector eventList;

	
	/**
	 * A periodic real-time event, equivalent to a periodic thread
	 * @param period
	 * @param deadline
	 * @param start
	 */
	public RealtimeTask(RelativeTime period, RelativeTime deadline, RelativeTime start) {

		if (eventList==null) {
			eventList = new Vector();
		}
		this.period = period.usTime;
		this.deadline = deadline.usTime;
		if (start!=null) {
			this.offset = start.usTime;
		} else {
			this.offset = 0;
		}
		this.event = null;
		
		// insert in a deadline monotonic order
		int cnt = eventList.size();
		int pos;
		for (pos=0; pos<cnt; ++pos) {
			RealtimeTask re = (RealtimeTask) eventList.elementAt(pos);
			if (deadline.usTime<=re.deadline) {
				break;
			}
		}

		eventList.insertElementAt(this, pos);
	}
	
	public RealtimeTask(RelativeTime period, RelativeTime deadline) {
		this(period, deadline, null);
	}
	
	public RealtimeTask(RelativeTime period) {
		this(period, period, null);
	}

	// only package view as this is not part of the API,
	// but just the implementation
	RealtimeTask(String event, RelativeTime minInterval, RelativeTime deadline) {
		
		this(minInterval, deadline, null);
		this.event = event;
	}
	
	
	abstract void run();
	
	public void cleanup() {
	}
}
