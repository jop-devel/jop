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

package kfl;

/**
*	Triac control, U/I Sensor, ext. Sensors (O/U/I).
*	Main functions for Mast.
*/

public class Triac {

	private static final int MAX_CNT = 1064;

	private static int dir;						// direction

	private static final int OPTO_LEN = 4;		// opto_idx with mask 0x03!
	private static int[] opto;
	private static int opto_idx;				// index in ring buffer
	private static int opto_val;

	private static int[] curr;					// current values

	private static int impVal;					// value of impuls sensor

	private static int cnt;						// counter for impuls sensor
	private static int endCnt;					// counter after first sensor impuls (stop counter)
	private static int upCnt, downCnt;			// count threshold after sensor
	private static int maxCnt;					// count value for up position

//
//	timer, count in loop times (5ms main loop)
//
	private static int timerWait;				// no up or down 100ms after stop
	private static int timerImp;				// stop if no imp from sensor for 1 sec
	private static int timerStrom;				// wait till start measure on rauf/runter, stop

	private static final int TIM_WAIT = 20;		// 100 ms
	private static final int TIM_IMP = 200;		// 1 s
	private static final int TIM_STROM = 20;	// 100 ms

	private static final int STROM_THRES = 60;	// TBS
	private static final int MIN_STROM = 120;	// TBS
	private static final int MAX_STROM = 360;	// TBS

	private static int timerPause;				// make some rest
	private static final int TIM_PAUSE = 200;	// 1 s

//
//	for service mode
//		counts impulse till sensor leaving
//		will ONLY set to 0 after reset => switch power off befor/after service mode
//
	public static int serviceCnt;





	public static void init() {

		int i;

		opto = new int[OPTO_LEN];		// 5 ms loop, min one val for 20 ms
		for (i=0; i<opto.length; ++i) {
			opto[i] = BBSys.MSK_U;	// usfull defaults
		}
		opto_idx = 0;

		curr = new int[3];
		for (i=0; i<3; ++i) curr[i] = 0;

		dir = 0;
		timerWait = 0;
		timerImp = 0;
		timerStrom = TIM_STROM;

		timerPause = 0;
// default Werte
		cnt = 0;
		upCnt = 3;
		downCnt = 3;
		maxCnt = 0;					// not set!!!

		endCnt = 0;

		serviceCnt = 0;

		impVal = JopSys.rd(BBSys.IO_SENSOR) & BBSys.BIT_SENSI;
	}

	public static void stop() {

		JopSys.wr(0, BBSys.IO_TRIAC);

		// minimum xxx ms off!
		if (dir!=0) {
			timerWait = TIM_WAIT;
			timerStrom = TIM_STROM*3;				// delay current measure for 300 ms
		}
		dir = 0;
	}

	public static void pause() {

		timerPause = TIM_PAUSE;
	}

	public static void rauf() {

		if (dir==-1) {
			stop();
			return;
		}
		if (timerWait==0) {
			JopSys.wr(BBSys.BIT_TR_ON, BBSys.IO_TRIAC);
			dir = 1;
			timerStrom = TIM_STROM;				// delay current measure for 100 ms
		}
	}
	public static void runter() {

		if (dir==1) {
			stop();
			return;
		}
		if (timerWait==0) {
			JopSys.wr(BBSys.BIT_TR_ON | BBSys.BIT_TR_DOWN, BBSys.IO_TRIAC);
			dir = -1;
			timerStrom = TIM_STROM;				// delay current measure for 100 ms
		}
	}
		

	public static void loop() {

		// wait timer after stop
		if (timerWait!=0) {
			--timerWait;
		}

		doOpto();
		doSensor();
		doStrom();
		doPause();
	}


/**
*	'filter' for U/I sensors
*/
	private static void doOpto() {

		int i, j;

		i = opto_idx;
		opto[i] = JopSys.rd(BBSys.IO_TRIAC);
		++i;
		i &= 0x03;
		opto_idx = i;

		j = 0;
		for (i=0; i<OPTO_LEN; ++i) {
			j |= opto[i];								// or means minimum one impuls
		}												// in 20 ms
		opto_val = j;

/* disabled for my tests
*/
		if ((j&BBSys.MSK_U) != BBSys.MSK_U) {			// check all Us
			if (dir!=0) stop();
			if (Mast.state!=BBSys.MS_RESET && Mast.state!=BBSys.MS_DBG) {
				Mast.state = BBSys.MS_ERR;
			}
			if ((j&BBSys.BIT_UL1) == 0) Mast.lastErr = Err.MS_NO_UL1;
			if ((j&BBSys.BIT_UL2) == 0) Mast.lastErr = Err.MS_NO_UL2;
			if ((j&BBSys.BIT_UL3) == 0) Mast.lastErr = Err.MS_NO_UL3;
		}

	}


	private static void doSensor() {

		int i, sens;

		// impuls, end sensors
		sens = JopSys.rd(BBSys.IO_SENSOR);
		i = sens & BBSys.BIT_SENSI;
		if (impVal != i) {
			impVal = i;
			if (dir > 0) {			// going UP
				doImpulsUp(sens);
			} else if (dir < 0) {	// going DOWN
				doImpulsDown(sens);
			}
			timerImp = 0;
		} else if (timerPause>0) {
			timerImp = 0;			// reset timer in pause
		} else {
			if (dir!=0) {
				++timerImp;
				if (timerImp > TIM_IMP) {
					stop();
					Mast.state = BBSys.MS_ERR;
					Mast.lastErr = Err.MS_NO_IMP;
				}
			} else {
				timerImp = 0;
			}
		}
	}


	private static void doImpulsUp(int sens) {

		++cnt;
		if ((sens & BBSys.BIT_SENSO) != 0) {
			++endCnt;
			if (endCnt >= upCnt) {
				stop();
				Mast.state = BBSys.MS_RDY;
			}
		} else {
			endCnt = 0;
			if (maxCnt!=0) {
				if (cnt>=maxCnt) {
					stop();
					Mast.state = BBSys.MS_ERR;
					Mast.lastErr = Err.MS_NO_SENSO;
				}
			}
		}
		if ((sens & BBSys.BIT_SENSU) != 0) {
			++serviceCnt;
		}
	}

	private static void doImpulsDown(int sens) {

		--cnt;
		if ((sens & BBSys.BIT_SENSU) != 0) {
			++endCnt;
			if (endCnt >= downCnt) {
				stop();
				Mast.state = BBSys.MS_RDY;
			}
		} else {
			endCnt = 0;
			if (maxCnt!=0) {
				if (cnt<=0) {
					stop();
					Mast.state = BBSys.MS_ERR;
					Mast.lastErr = Err.MS_NO_SENSU;
				}
			}
		}
		if ((sens & BBSys.BIT_SENSO) != 0) {
			++serviceCnt;
		}

	}


/**
*	check current.
*/
	private static void doStrom() {

		int i, val;

		val = JopSys.rd(BBSys.IO_IADC);
		for (i=0; i<3; ++i) {
			curr[i] = val & 1023;
			val = val>>10;
		}

		if (timerStrom!=0) {			// is set in rauf(), runter() and stop() to delay measure
			--timerStrom;
			return;
		}

/* disabled for first test at BB
*/
		if (Mast.state==BBSys.MS_UP || Mast.state==BBSys.MS_DOWN) {

			for (i=0; i<3; ++i) {
				val = curr[i];
				if (val<STROM_THRES) {					// kein Strom
					stop();
					Mast.state = BBSys.MS_ERR;
					Mast.lastErr = Err.MS_NO_IL1+i;
				} else if (val>MAX_STROM) {				// zu viel Strom
					stop();
					Mast.state = BBSys.MS_ERR;
					Mast.lastErr = Err.MS_MAX_IL1+i;
				} else if (val<MIN_STROM) {				// zu wenig Strom
					stop();
					Mast.state = BBSys.MS_ERR;
					Mast.lastErr = Err.MS_MIN_IL1+i;
				}
			}

		} else {

			for (i=0; i<3; ++i) {
				val = curr[i];
				if (val>=STROM_THRES) {					// es fliesst Strom in Ruhestellung
					stop();
					Mast.state = BBSys.MS_ERR;
					Mast.lastErr = Err.MS_IL1+i;
				}
			}
		}
	}

/**
*	handle pause timer.
*/
	private static void doPause() {

		if (timerPause>0) {
			--timerPause;
			if (timerPause==0) {
				if (dir==1) {					// restart motor
					rauf();
				} else if(dir==-1) {
					runter();
				}
			} else {
				JopSys.wr(0, BBSys.IO_TRIAC);	// stop but keep dir!
				timerStrom = TIM_STROM*3;		// delay current measure for 300 ms
			}
		}
	}

	public static int getCnt() {
		return cnt;
	}
	public static void setCnt(int val) {
		cnt = val;
	}

	public static void setDownCnt(int val) {
		downCnt = val;
	}
	public static void setUpCnt(int val) {
		upCnt = val;
	}
	public static void setMaxCnt(int val) {
		maxCnt = val;
	}

	public static int getDir() {
		return dir;
	}
	public static int getOpto() {
		return opto_val;
	}

	public static int getIadc(int nr) {

		if (nr>=0 && nr<3) {
			return curr[nr];
		} else {
			return -1;
		}
	}
}
