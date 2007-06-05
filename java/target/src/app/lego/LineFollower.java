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
		System.out.println(val);
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
