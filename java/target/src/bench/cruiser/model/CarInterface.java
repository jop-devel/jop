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

package cruiser.model;

import java.io.IOException;
import java.util.Random;

import cruiser.common.*;

public class CarInterface {

	CarModel model = new CarModel();
	OutputThread outThread = new OutputThread();
	InputThread inThread = new InputThread();

	Random rand = new Random(0);

	public void start() {
		model.start();
		outThread.start();
		inThread.start();
	}

	private short noisyScaledSpeed(double realSpeed) {
		// sensors are noisy
		double noisySpeed = realSpeed+rand.nextGaussian()/2.0;
		// and sometimes just return zero
		if (rand.nextInt(400) == 0) {
			return 0;
		}
		// or the maximum speed
		if (rand.nextInt(400) == 0) {
			return 0x7fff;
		}
		// or the minimum speed
		if (rand.nextInt(400) == 0) {
			return (short)0x8000;
		}
		return (short)(noisySpeed*100);
	}

	class OutputThread extends Thread {
		public void run() {
			for (;;) {
				double realSpeed = model.getSpeed();

				WireMessage msg;
				msg = new WireSpeedMessage(WireMessage.Type.SPEED_FRONT_LEFT, noisyScaledSpeed(realSpeed));
				// some messages are dropped
				if (rand.nextInt(100) != 0) {
					System.out.print(msg.toString());
				}
				msg = new WireSpeedMessage(WireMessage.Type.SPEED_FRONT_RIGHT, noisyScaledSpeed(realSpeed));
				// some messages are dropped
				if (rand.nextInt(100) != 0) {
					System.out.print(msg.toString());
				}
				msg = new WireSpeedMessage(WireMessage.Type.SPEED_REAR_LEFT, noisyScaledSpeed(realSpeed));
				// some messages are dropped
				if (rand.nextInt(100) != 0) {
					System.out.print(msg.toString());
				}
				msg = new WireSpeedMessage(WireMessage.Type.SPEED_REAR_RIGHT, noisyScaledSpeed(realSpeed));
				// some messages are dropped
				if (rand.nextInt(100) != 0) {
					System.out.print(msg.toString());
				}
				try {
					sleep(10);
				} catch (InterruptedException exc) {
					// ignore
				}
			}
		}
	}

	class InputThread extends Thread {
		public void run() {

			StringBuilder msgBuffer = new StringBuilder(16);

			for (;;) {
			
				try {
					int c = System.in.read();
					if (c < 0) {
						break;
					}
					msgBuffer.append((char)c);
					if (c != '\n') {
						continue;
					}
				} catch (IOException exc) {
					System.err.println(exc);
					msgBuffer.setLength(0);
					continue;
				}

				String msg = new String(msgBuffer);
				// prepare for next message
				msgBuffer.setLength(0);
				
				if (!WireMessage.checkMessage(msg)) {
					System.err.println("Message not correct: "+msg);
					continue;
				}

				switch (WireMessage.parseType(msg)) {
				case THROTTLE: {
					WireControlMessage m = WireControlMessage.fromString(msg);
					model.setThrottle(m.getValue()/10000.0);
					break;
				}
				case BRAKE: {
					WireControlMessage m = WireControlMessage.fromString(msg);
					model.setBrake(m.getValue()/10000.0);
					break;
				}
				}
			}
		}
	}


}