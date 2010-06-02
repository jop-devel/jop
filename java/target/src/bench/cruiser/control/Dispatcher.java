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

import java.io.IOException;

import joprt.RtThread;
import cruiser.common.*;

public class Dispatcher implements Runnable {

	SpeedManager manager;
	Filter frontLeftFilter;
	Filter frontRightFilter;
	Filter rearLeftFilter;
	Filter rearRightFilter;

	public Dispatcher(SpeedManager manager, Filter flf, Filter frf, Filter rlf, Filter rrf) {
		this.manager = manager;
		this.frontLeftFilter = flf;
		this.frontRightFilter = frf;
		this.rearLeftFilter = rlf;
		this.rearRightFilter = rrf;
	}

	private void dispatch(String msg) {

		if (!WireMessage.checkMessage(msg)) {
			System.err.println("Message not correct: "+msg);
			return;
		}

		WireMessage.Type type = WireMessage.parseType(msg);
		switch (type) {
		case SPEED_FRONT_LEFT:
		case SPEED_FRONT_RIGHT:
		case SPEED_REAR_LEFT:
		case SPEED_REAR_RIGHT: {
			WireSpeedMessage m = WireSpeedMessage.fromString(msg);
			StampedMessage s = new StampedMessage(m);
			switch (type) {
			case SPEED_FRONT_LEFT:
				frontLeftFilter.enqueue(s);
				break;
			case SPEED_FRONT_RIGHT:
				frontRightFilter.enqueue(s);
				break;
			case SPEED_REAR_LEFT:
				rearLeftFilter.enqueue(s);
				break;
			case SPEED_REAR_RIGHT:
				rearRightFilter.enqueue(s);
				break;
			}
			break;
		}
		case TARGET_SPEED: {
			WireTargetSpeedMessage m = WireTargetSpeedMessage.fromString(msg);
			StampedDistanceMessage s = new StampedDistanceMessage(m, manager);
			manager.setTargetSpeed(s);
			break;
		}
		}
	}

	public void run() {

		StringBuilder msgBuffer = new StringBuilder(WireMessage.MAX_LENGTH);

		for (;;) {
			
			// I/O handling
			try {
				int c = System.in.read();
				// end of file
				if (c < 0) {
					break;
				}
				// append only if the message fits the buffer
				if (msgBuffer.length() < WireMessage.MAX_LENGTH) {
					msgBuffer.append((char)c);
				}
				// okay, we probably have a full message now
				if (c != '\n') {
					continue;
				}				
			} catch (IOException exc) {
				System.err.println(exc);
				msgBuffer.setLength(0);
				continue;
			}

			// convert buffer to immutable string
			String msg = msgBuffer.toString();
			// prepare for next message
			msgBuffer.setLength(0);

			// actual dispatch
			dispatch(msg);

			// we assume System.in.read() blocks and therefore governs the timing
			// RtThread.currentRtThread().waitForNextPeriod();
		}

	}
}