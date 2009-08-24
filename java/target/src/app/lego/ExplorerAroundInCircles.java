/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2009, Benedikt Huber

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

/*
 * The explorer is a lego bot project.
 * The task is to write a robot that explores (later on in coop mode) its environment,
 * builds a model and synchronizes it with the server.
 * 
 * As a test application, it should use as many sensors, actuators and I/O devices as possible :)
 * Really, we need more complex applications !
 * 
 * Currently, I'm still fighting with simple LineFollowing examples, though --
 * takes time to get used to time-triggered programming.
 * So complexity seems to be a relative thing.
 */

package lego;

import joprt.RtThread;
import lego.explorer.BotInterface;
import lego.explorer.BotMode;
import lego.explorer.ManualCalibrationMode;
import lego.explorer.BotInterface.TimeStamp;
import lego.lib.Motor;


/**
 * ExplorerAroundInCircles:
 *  The first not-quite-yet explorer, very simple, using a modified TriBot.
 *  Follows a circle line, and corrects if he isn't able to follow it any more.
 *  Simple, but fun to watch.
 *  - Bot: Tribot
 *  - Implementation: Work in progress, but basic functionality is here
 *  - Buttons: B0: Start, B1: Calibrate IR, B3: Emergency Stop (in all modes)
 *  - Provides IR Calibration (B0: Record Low, B1: Record High: B2: Finish)
 *  - Follows the Line on the floor. Either to its left (ccw, start) or right (cw). Pushing one
 *    of the touch sensors stops the explorer and changes orientation.
 *    If the explorer fails to see the line for a certain amount of time, he goes backwards
 *    to correct its course.
 *    
 * @author benedikt
 *
 */
public class ExplorerAroundInCircles {
	/* Board Config */
	static final int IR_SENSOR_ID = 0x0;
	static final int MOTOR_LEFT = 0;
	static final int MOTOR_RIGHT = 1;
	static final int DIGITAL_RIGHT = 0;
	static final int DIGITAL_LEFT = 1;

	public static final int BUTTON_STOP = 3;

	static final int MODE_IDLE = 0, MODE_CALIB = 1, MODE_RUN = 2;

	private static MasterMode control;
	private static BotInterface iface;
	public static void controlLoop() {
		iface.update();
		control.driver(false);
	}
	public static class MasterMode implements BotMode {
		private static final int BUTTON_START = 0;
		private static final int BUTTON_CALIB = 1;
		private int state = 0;
		private BotInterface iface;
		private BotMode runningMode = null;

		private static MasterMode inst = new MasterMode();
		private MasterMode() { }
		public static MasterMode start(BotInterface iface) {
			inst.iface = iface;
			return inst;
		}
		public boolean driver(boolean stop) {
			for(int i = 0; i < 4; i++) {
				iface.ledStatus[i] = BotInterface.LED_STATUS_OFF;
			}

			stop = iface.buttonEdge[BUTTON_STOP];
			if(runningMode != null) {
				boolean active = runningMode.driver(stop);
				if(! active) { 
					runningMode = null; 
					state = 0;
			    }
			} else if(! stop) {
			  if(iface.buttonEdge[BUTTON_CALIB]) {				
				state = 1;
				iface.ledStatus[3] = BotInterface.LED_STATUS_BLINK;
				runningMode = ManualCalibrationMode.start(iface);
			  } else if(iface.buttonEdge[BUTTON_START]) {
				state = 2;
				runningMode = LineFollowerMode.start(iface);
			  } else {
				iface.ledStatus[1] = BotInterface.LED_STATUS_BLINK;
			  }
			}
			return true; // never stop master mode
		}
	}
	/** Spec
	 * correct-mode: 
	 *   Go backwards until T seconds passed or black is sensed
	 *   Turning left if black^clockwise, right otherwise
	 * run-mode:
	 *   If no black for T second: goto correct-mode
	 *   Otherwise: Turn right if black^clockWise, left otherwise
	 * 
	 * @author Benedikt Huber <benedikt.huber@gmail.com>
	 *
	 */
	public static class LineFollowerMode implements BotMode {
		private BotInterface iface;
		private boolean clockWise;
		private static final int MODE_RUN = 0, MODE_CORRECT = 1;
		private static final int STOP_TIME_MS = 1600;
		private int mode;
		private TimeStamp ts = new TimeStamp();
		private static LineFollowerMode inst = new LineFollowerMode();
		
		private LineFollowerMode() { }
		public static LineFollowerMode start(BotInterface iface) {
			inst.iface = iface;
			inst.mode = MODE_RUN;
			iface.time.sync(inst.ts);
			return inst;
		}
		public boolean driver(boolean stop) {
			boolean obstacle = iface.digitals[DIGITAL_LEFT] || iface.digitals[DIGITAL_RIGHT];
			if(obstacle) stop = true;
			if(! stop) {
				iface.ledStatus[3] = BotInterface.LED_STATUS_ON;
				if(mode == MODE_RUN) modeRun();
				else if(mode == MODE_CORRECT) modeCorrect();
				return true;
			} else {
				if(obstacle) clockWise = ! clockWise;
				iface.drive.stopMotor();
				return false;
			}
		}
		private void modeCorrect() {
			iface.ledStatus[1] = BotInterface.LED_STATUS_OFF;
			iface.ledStatus[2] = BotInterface.LED_STATUS_BLINK;
			boolean black = iface.irSensor.isHigh;
			if(black || iface.time.msDiff(inst.ts) > (STOP_TIME_MS>>1)) {
				mode = MODE_RUN;
				iface.time.sync(inst.ts);										
			} else {
				int left = Motor.STATE_BACKWARD, right = Motor.STATE_BACKWARD;
				int leftD = 20, rightD = 20;
				if (! clockWise) {
					leftD = 90;
				} else {
					rightD = 90;
				}
				iface.drive.set(left, leftD, right, rightD);				
			}			
		}
		private void modeRun() {
			iface.ledStatus[1] = BotInterface.LED_STATUS_BLINK;
			iface.ledStatus[2] = BotInterface.LED_STATUS_OFF;
			boolean black = iface.irSensor.isHigh;
			if(iface.time.msDiff(inst.ts) > STOP_TIME_MS) {
				mode = MODE_CORRECT;
				iface.time.sync(inst.ts);						
			} else {
				if(black) iface.time.sync(inst.ts);
				int left = Motor.STATE_BRAKE, right = Motor.STATE_BRAKE;
				if (black ^ clockWise) {
					right = Motor.STATE_FORWARD;
				} else {
					left = Motor.STATE_FORWARD;
				}
				iface.drive.set(left, 90, right, 90);
			}			
		}		
	}
	
	
	public static void init() {
		iface = new BotInterface(IR_SENSOR_ID, MOTOR_LEFT, MOTOR_RIGHT);
		control = MasterMode.start(iface);
	}

	/* Currently just one loop (boring). But this will change */
	public static void main(String[] agrgs) {


		System.out.println("Hello LEGO world!");
				
		init();
		
		new RtThread(15, 15*1000) {
			public void run() {
				for (;;) {
					controlLoop();
					waitForNextPeriod();
				}
			}
		};

		RtThread.startMission();

		for (;;) {
			System.out.println("<<<Explorer1 up and running>>>");
			iface.dump();
			System.out.print("control.state: ");System.out.println(control.state);
			System.out.print("lf.state: ");System.out.println(LineFollowerMode.inst.mode);
			System.out.print("lf.clockwise: ");System.out.println(LineFollowerMode.inst.clockWise);
			System.out.print("lf.timer ms: ");System.out.println(iface.time.msDiff(LineFollowerMode.inst.ts));
			RtThread.sleepMs(400);
		}

	}

}
