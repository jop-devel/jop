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
import cruiser.common.*;

public class Controller implements Runnable {

	private final SpeedManager manager;

	public Controller(SpeedManager manager) {
		this.manager = manager;
	}

	private int integralError = 0;
	private int lastDelta = 0;

	private int pidControl(int target, int current) {
		int delta = target - current;

		int P = delta;
		int I = (integralError*255)/256 + delta;
		integralError = I;
		int D = delta - lastDelta;
		lastDelta = delta;

		// System.err.println("P:\t"+P+"\tI:\t"+I+"\tD:\t"+D);
	
		return 64*P+I/8+D/256;
	}

	private void control() {
		// estimated current speed
		int currentSpeed = manager.getCurrentSpeed();
		int targetSpeed = manager.getTargetSpeed();
		
		// System.err.println("Estimated speed: "+currentSpeed);
		// System.err.println("Target speed: "+targetSpeed);
		
		// a standard PID controller
		int controlValue = pidControl(targetSpeed, currentSpeed);
		// System.err.println("Ctrl: "+controlValue);
		
		// "nearly zero" is not quite zero...
		if (targetSpeed == 0 && currentSpeed < 33 && currentSpeed > -33) {
			controlValue = -0x7fff;
		}
		
		// cap controlValue to valid range
		if (controlValue > 0x7fff) {
			controlValue = 0x7fff;
		} else if (controlValue < -0x7fff) {
			controlValue = -0x7fff;
		}				
		
		// send control messages
		WireMessage msg;
		if (controlValue > 0) {
			msg = new WireControlMessage(WireMessage.Type.BRAKE, (short)0);
			System.out.print(msg.toString());
			msg = new WireControlMessage(WireMessage.Type.THROTTLE, (short)controlValue);
			System.out.print(msg.toString());
		} else if (controlValue < 0) {
			msg = new WireControlMessage(WireMessage.Type.BRAKE, (short)-controlValue);
			System.out.print(msg.toString());
			msg = new WireControlMessage(WireMessage.Type.THROTTLE, (short)0);
			System.out.print(msg.toString());
		}
	}

	public void run() {
		for (;;) {
			control();
			RtThread.currentRtThread().waitForNextPeriod();
		}
	}

}