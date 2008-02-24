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

/*
 * Created on 12.07.2004
 *
 * 
·	Taste Beladehoehe anfahren	Eingang 9
·	Taste Unterste Tasse anfahren UP	Eingang 8
·	Taste Oberste Tasse anfahren  DOWN	Eingang 7
·	Taste Hoehe + (72,0mm 13x)	Eingang 5
·	Taste Hoehe - (72,0mm 13x)	Eingang 6
·	Richtung Auf	Ausgang 2
·	Motor Ein	Ausgang 1
·	Impulsgeber vom Motor	Eingang 1
·	Sensor Unterste Tasse (Lift oben) TOP	Eingang 2
·	Sensor Oberste Tasse (Lift unten) BOTTOM	Eingang 3
·	Sensor Beladungsebene	Eingang 4

 */
package jbe.lift;

/**
 * @author martin
 *
 */
public class LiftControl extends Control {

	final static int GO_LOAD = 8;
	final static int GO_TOP = 6;
	final static int GO_BOTTOM = 7;
	final static int GO_UP = 4;
	final static int GO_DOWN = 5;
	final static int SENS_IMPULS = 0;
	final static int SENS_TOP = 1;
	final static int SENS_BOTTOM = 2;
	final static int SENS_LOAD = 3;
	
	final static int MOTOR_ON = 0;
	final static int MOTOR_UP = 1;
	
	int [] levelPos;
	int one_level;
	
	/**
	 * Is the counter valid for level positioning?
	 */
	boolean cntValid;
	/**
	 * Position absolut or relativ.
	 */
	int cnt;
	/**
	 * Last stoped level (1..13) if position is absolute else 0.
	 */
	int level;
	
	/**
	 * load position in level, 0 means we don't know
	 */
	int loadLevel;
	/**
	 * we're going TOP or BOTTOM, but stop at load position.
	 */
	boolean loadPending;
	/**
	 * we're waiting for the load sensor to go.
	 */
	boolean loadSensor;
	
	final static int CMD_NONE = 0;
	final static int CMD_TOP = 1;
	final static int CMD_BOTTOM = 2;
	final static int CMD_UP = 3;
	final static int CMD_DOWN = 4;
//	final static int CMD_LOAD = 3;
	/**
	 * Wait for motor stop and continue impuls counting.
	 * Go to CMD_NONE after the wait time (IMP_CONT).
	 * All commands should end here. 
	 */
	final static int CMD_WAIT = 99;
	
	/**
	 * cmd keeps the value of the command until the command is finished.
	 * It is only updated by the switches if it's current value is CMD_NONE.
	 */
	int cmd;
	
	final static int PERIOD = 10;		// in ms
	
	/**
	 * Wait time till motor will start after dircetion.
	 */
	final static int MOTOR_WAIT_MS = 500;
	final static int MOTOR_WAIT_CNT = MOTOR_WAIT_MS/PERIOD;
	
	int timMotor;
	
	/**
	 * Additional time where pulse are still counted after motor stop.
	 * This time also delayes new commands for a clean direction switch.
	 */
	final static int IMP_CONT_MS = 500;
	final static int IMP_CONT_CNT = IMP_CONT_MS/PERIOD;
	
	int timImp;
	
	/**
	 * Remember last direction for impuls count after motor off;
	 */
	boolean directionUp;
	
	/**
	 * Last value of impuls sensor.
	 */
	boolean lastImp;
	
	/**
	 * compensate running motor after stop.
	 */
	final static int OFFSET = 1;
	/**
	 * Call super class with period in ms.
	 */
	public LiftControl() {
		super(PERIOD);
		cntValid = false;
		cnt = 0;
		cmd = CMD_NONE;
		timMotor = 0;
		timImp = 0;
		directionUp = true;
		lastImp = false;
		loadLevel = 0;
		loadPending = false;
		loadSensor = false;

		
		int[] tmp = {
			0,
			58,
			115,
			173,
			230,
			288,
			346,
			403,
			461,
			518,
			576,
			634,
			691,
			749,
			806,
			864
		};
		levelPos = tmp;
// for simpler debugging
// for (int i=0; i<levelPos.length; ++i) levelPos[i] /= 10;
		one_level = levelPos[1];
	}

	int dbgCnt;
	
	public void loop(TalIo io) {
		
		if (cmd==CMD_NONE) {
			checkCmd(io);
		} else {
			doImpuls(io.in[SENS_IMPULS], io.out[MOTOR_ON], io.in[SENS_BOTTOM]);
			doCmd(io);
		}
		checkLevel(io);
		io.led[13] = (dbgCnt&0x80) != 0;
		++dbgCnt;
		if ((dbgCnt&0x3f) == 0) {
//			dbg(io);
		}
	}
	/**
	 * Check current level and set LEDs.
	 * @param io
	 */
	private void checkLevel(TalIo io) {
		
		int middle = one_level>>2;
		if (cntValid) {
			for (level=1; level<levelPos.length; ++level) { // @WCA loop=14
				if (cnt < levelPos[level]-middle) {
					break;
				}
			}
		} else {
			level = 0;
		}
		for (int i=0; i<io.led.length; ++i) { // @WCA loop=14
			io.led[i] = (i == level-1);
		}
		
	}
	/**
	 * Generate a command when system is idle and start timer.
	 * @param io
	 */
	void checkCmd(TalIo io) {

		if (loadPending) {
			if (io.in[SENS_BOTTOM]) {
				cmd = CMD_TOP;		
			}
		} else if (io.in[GO_UP]) {
			if (!io.in[SENS_TOP] && level!=levelPos.length) {
				cmd = CMD_UP;
			}
		} else if (io.in[GO_DOWN]) {
			if (!io.in[SENS_BOTTOM] && level!=1) {
				cmd = CMD_DOWN;
			}
		} else if (io.in[GO_LOAD]) {
			if (loadLevel!=0 && level<loadLevel) {
				cmd = CMD_TOP;
			} else {
				cmd = CMD_BOTTOM;
			}
			loadPending = true;
			loadSensor = false;
		} else if (io.in[GO_TOP]) {
			if (!io.in[SENS_TOP]) {
				cmd = CMD_TOP;
			}
		} else if (io.in[GO_BOTTOM]) {
			if (!io.in[SENS_BOTTOM]) {
				cmd = CMD_BOTTOM;
			}
		}
		if (cmd!=CMD_NONE) {
			timMotor = MOTOR_WAIT_CNT;
		}
	}
	
	void doImpuls(boolean val, boolean motor, boolean reset) {
		
		if (val && !lastImp) {
			if (motor || timImp>0) {
				if (directionUp) {
					++cnt;
				} else {
					--cnt;
				}
			}
		}
		if (reset) {
			cnt = 0;
			cntValid = true;
		} 
		lastImp = val;
		if (timImp>0) {
			--timImp;
			if (timImp==0 && cmd!=CMD_NONE) {
				cmd = CMD_NONE;
			}
		}
	}
	
	/**
	 * stop value for the counter.
	 */
	int endCnt;
	
	void doCmd(TalIo io) {
		
		if (timMotor > 0) {
			waitForMotorStart(io);
		} else {
			boolean run = checkRun(io);
			if (io.out[MOTOR_ON] && !run) {
				// motor stopped:
				cmd = CMD_WAIT;
				timImp = IMP_CONT_CNT;
			}
			io.out[MOTOR_ON] = run; 
		}
	}

	void waitForMotorStart(TalIo io) {
		
		--timMotor;
		directionUp = (cmd==CMD_UP || cmd==CMD_TOP);
		io.out[MOTOR_UP] =  directionUp;
		if (!cntValid) {
			cnt = 0;		// use relative counter
			if (cmd==CMD_UP) {
				endCnt = one_level; 
			} else {
				endCnt = -one_level;
			}
		} else {
			endCnt = cnt;
			int newLevel = -99;
			if (cmd==CMD_UP) {
				newLevel = level+1;
			} else if (cmd==CMD_DOWN){
				newLevel = level-1;
			}
			--newLevel;	// level is one based
			if (newLevel>=0 && newLevel<levelPos.length) {
				endCnt = levelPos[newLevel]; 
			}
		}
	}
	
	boolean checkRun(TalIo io) {
		
		if (cmd == CMD_UP) {
			if (cnt < endCnt - OFFSET && !io.in[SENS_TOP])
				return true;
		} else if (cmd == CMD_DOWN) {
			if (cnt > endCnt + OFFSET && !io.in[SENS_BOTTOM])
				return true;
		} else if (cmd == CMD_TOP) {
			if (loadPending && io.in[SENS_LOAD]) {
				// we are at load position
				loadLevel = level;
				loadPending = false;
				return false;
			}
			if (!io.in[SENS_TOP])
				return true;
			// for shure if load sensor does not work
			loadPending = false;
		} else if (cmd == CMD_BOTTOM) {
			if (loadPending) {
				if (loadSensor) {
					if (!io.in[SENS_LOAD]) {
						loadSensor = false;
						// we are at load position
						loadPending = false;
						loadLevel = level;
						return false;
					}
				}
				loadSensor = io.in[SENS_LOAD];
			}
			if (!io.in[SENS_BOTTOM])
				return true;
		}		
		return false;	
	}
  	
}
