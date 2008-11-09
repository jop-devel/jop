/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2007, Peter Hilber and Alexander Dejaco

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

package lego;

/**
 * Originally ran on prototype board. Ported to LRBJOP interface. Untested.
 */

import lego.lib.*;

import joprt.RtThread;

public class LineFollower {

	static final int IR_SENSOR = 2;
	
	static final int MAX = 1000;
	static Motor left, right;
	static boolean black;
	
	public static void init() {
		
		left = new Motor(0);
		right = new Motor(1);

		black = false;
	}
	
	public static void loop() {

		int val = Sensors.readSensor(IR_SENSOR);
		black = val > 285; // XXX
							
		left.setDutyCyclePercentage(75);
		right.setDutyCyclePercentage(75);
		
		if (black) {
			right.setState(Motor.STATE_FORWARD);
			left.setState(Motor.STATE_BRAKE);
		} else {
			left.setState(Motor.STATE_FORWARD);
			right.setState(Motor.STATE_BRAKE);
		}
	}

	public static void main(String[] agrgs) {


		System.out.println("Hello LEGO world!");
				
		init();
		
		new RtThread(10, 20*1000) {
			public void run() {
				for (;;) {
					loop();
					waitForNextPeriod();
				}
			}
		};

		RtThread.startMission();

		for (;;) {
			int val = Sensors.readSensor(IR_SENSOR);
			System.out.println(val);
			RtThread.sleepMs(500);
		}

	}

}
