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

import joprt.RtThread;

public class SpeedManager implements Runnable {

	final SpeedState frontLeftSpeed = new SpeedState();
	final SpeedState frontRightSpeed = new SpeedState();
	final SpeedState rearLeftSpeed = new SpeedState();
	final SpeedState rearRightSpeed = new SpeedState();

	private final SpeedState [] speedState = new SpeedState[4];

	// speed forecast
	private volatile TargetSpeedState targetSpeed = new TargetSpeedState();
	private final SpeedState currentSpeed = new SpeedState();

	// distance estimation
	private long lastNow = 0;
	private long distance = 0;

	public SpeedManager() {
		speedState[0] = frontLeftSpeed;
		speedState[1] = frontRightSpeed;
		speedState[2] = rearLeftSpeed;
		speedState[3] = rearRightSpeed;
	}

	private void manage(long now) {

		// compute average
		int sum = 0;
		int sqsum = 0;
		int cnt = 0;
		for (int i = 0; i < speedState.length; i++) { //@WCA loop = 4
			if (speedState[i].valid) {
				int speed = speedState[i].speed;
				sum += speed;
				sqsum += speed*speed;
				cnt++;
			}
		}

		// not enough sensor values
		if (cnt < speedState.length/2) {
			// System.err.println("No reliable speed estimate available, brake, cnt="+cnt);
			currentSpeed.valid = false;
		} else {
			int avg = sum/cnt;
			// should divide by cnt-1 for deviation, but avoid division by zero
			int dev = (sqsum-(sum*sum)/cnt)/cnt;
			dev = dev < 0 ? -dev : dev;
			// sensor values should have a reasonable deviation
			// System.err.println("$ dev: "+dev+" avg: "+avg+" avg**2: "+avg*avg);
			if (dev > 4000) {
			 	// System.err.println("Speed estimates too divergent, brake, dev="+dev);
			 	currentSpeed.valid = false;
			} else {
				currentSpeed.speed = avg;
				currentSpeed.valid = true;
			}
		}

		distance += ((long)currentSpeed.speed*1000*1000*1000)/Math.max(1, now-lastNow);
		lastNow = now;
	}

	public int getCurrentSpeed() {
		return currentSpeed.speed;
	}

	public void setTargetSpeed(StampedDistanceMessage msg) {
		targetSpeed = new TargetSpeedState(targetSpeed.targetSpeed, msg);
	}

	public int getTargetSpeed() {
		if (!currentSpeed.valid) {
			// no reliable estimate, so we should better stop
			return 0;
		} else {
			TargetSpeedState t = targetSpeed;

			long dist = distance/1000;
			if (t.targetDistance - dist > 0) {
				int retval;

				long delta = t.targetDistance-t.lastTargetDistance;
				delta = delta == 0 ? 1 : delta;

				if (t.targetSpeed > t.lastTargetSpeed) { // accelerating
					// start to accelerate quickly
					long d = 500*(dist-t.lastTargetDistance)/delta;
					d += 500;
					retval = (int)((t.lastTargetSpeed*(1000-d)+(t.targetSpeed*d))/1000);
				} else { // braking
					// start to brake without bias
					long d = 1000*(dist-t.lastTargetDistance)/delta;
					// approach quadratically
					d *= d;
					d /= 1000;
					retval = (int)((t.lastTargetSpeed*(1000-d)+(t.targetSpeed*d))/1000);
					// emergency braking if we run out of room for braking
					int v = currentSpeed.speed - t.targetSpeed;
					if ((t.targetDistance-dist) < (v*v)/190) {
						retval = t.targetSpeed;
					}
				}
				return retval;
			} else {
				return t.targetSpeed;
			}
		}
	}

	public long getDistance() {
		return distance;
	}

	public void run() {

		lastNow = System.nanoTime();

		for (;;) {			
			long now = System.nanoTime();
			manage(now);

			RtThread.currentRtThread().waitForNextPeriod();
		}
	}

}