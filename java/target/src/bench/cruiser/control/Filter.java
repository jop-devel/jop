/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2010, Wolfgang Puffitsch <wpuffits@mail.tuwien.ac.at>

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

package cruiser.control;

import java.util.Collections;
import java.util.List;
import java.util.LinkedList;

import joprt.RtThread;
import cruiser.common.*;

public class Filter implements Runnable {

	private static final long MAX_AGE = 100*1000*1000; // nano seconds

	private final List<StampedMessage> queue = Collections.synchronizedList(new LinkedList<StampedMessage>());
	private final String name;
	private final SpeedState state;

	public Filter(String name, SpeedState state) {
		this.name = name;
		this.state = state;
	}

	public void enqueue(StampedMessage msg) {
		synchronized(queue) {
			queue.add(0, msg);
		}
	}

	private void filter(long now) {
		// weed out old messages, message inter-arrival time and
		// MAX_AGE determine maximum length of queue
		synchronized (queue) {
			while (!queue.isEmpty()) { //@WCA loop <= 110
				if (queue.get(queue.size()-1).getStamp() < now-MAX_AGE) {
					queue.remove(queue.size()-1);
				} else {
					break;
				}
			}
		}
		// compute average speed
		int sum = 0;
		int sqsum = 0;
		int cnt = 0;
		synchronized (queue) {
			for (StampedMessage msg : queue) { //@WCA loop <= 100
				WireSpeedMessage m = (WireSpeedMessage)msg.getMessage();
				int speed = m.getSpeed();
				sum += speed;
				sqsum += speed*speed;
				cnt++;
			}
		}

		if (cnt == 0) {
			state.valid = false;
		} else {
			int avg = sum/cnt;
			// should divide by cnt-1 for deviation, but avoid division by zero
			int dev = (sqsum-(sum*sum)/cnt)/cnt;
			dev = dev < 0 ? -dev : dev;
			// sensor values should have a reasonable deviation
			// System.err.println("> dev: "+dev+" avg: "+avg+" avg**2: "+avg*avg);
			if (dev > 8000) {
				// System.err.println(name+": Dropping tire value");
			 	state.valid = false;
			} else {
				state.speed = avg;
				state.valid = true;
			}
		}
	}

	public void run() {
		for (;;) {
			long now = System.nanoTime();
			filter(now);

			RtThread.currentRtThread().waitForNextPeriod();
		}
	}

}