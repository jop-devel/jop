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
*	All Functions for MS in Zentrale.
*
*	WICHTIG: Fehler fuehren zu Stopp der Anlage (mit for (;;) loop).
*			 Neustart erforderlich!
*/

public class Station {

	public static final int MIN_SENS_CNT = 3;
	public static final int MAX_SENS_CNT = 24;	// Sensor is valid for maximum 27

	public static final int MAX_DIFF = 20;		// = 10 cm
	public static final int CONTROL_DIFF = 10;	// = 5 cm
	public static final int MAX_COMM_ERR = 3;	// maximum (continous) errors befor stop

// public static final int MAX_COMM_ERR = 10;	// maximum (continous) errors befor stop
// bei SEHR hohem Wert Timer.wd in cmdAll und cmd!!!

	public static final int LUNKNOWN = 0;	// unbekannt
	public static final int LU = 1;		// unten
	public static final int LO = 2;		// oben
	public static final int LZ = 3;		// zwischen
	public static final int LUZ = 4;	// unten zwischen

	private static int cnt;				// how many MS
	private static int ltg;				// state of Leitung

	private static int[] ret;

	public static int[] temp;

	private static int[] maxCnt;		// up position of cnt (down pos. is 0)
	private static int[] upCnt;			// impuls count after up sensor
	private static int[] downCnt;		// impuls count after down sensor

	private static int[] lastCnt;		// last known value of MS cnt
	private static int[] servCnt;		// used for service

	public static void init() {

		int i;

		ltg = LUNKNOWN;

		ret = new int[16];			// maximum
		maxCnt = new int[16];
		upCnt = new int[16];
		downCnt = new int[16];
		servCnt = new int[16];
		temp = new int[16];			// maximum
		for (i=0; i<16; ++i) temp[i] = 0;

		cnt = Config.getCnt();

		getVals();
dispVals();
	}

/*
private static void setXXX() {

int i;
// set values
for (i=0; i<3; ++i) {
	Config.setMSmaxCnt(i+1, maxCnt[i]);
	Config.setMSdwnCnt(i+1, downCnt[i]);
	Config.setMSupCnt(i+1, upCnt[i]);
}
}
*/
	private static void getVals() {

		int i;

		for (i=0; i<cnt; ++i) {
			maxCnt[i] = Config.getMSmaxCnt(i+1);
			upCnt[i] = Config.getMSupCnt(i+1);
			downCnt[i] = Config.getMSdwnCnt(i+1);
		}

/*
maxCnt[0] = 1051;
maxCnt[1] = 1051;
maxCnt[2] = 1052;
downCnt[0] = 20;
downCnt[1] = 10;
downCnt[2] = 18;
upCnt[0] = 8;		// should be 3 minimum!!!
upCnt[1] = 7;
upCnt[2] = 8;
*/



		boolean ok = true;

		//	check possibility of values
		for (i=0; i<cnt; ++i) {
			if (maxCnt[i]==0 || maxCnt[i]>1100) {
				ok = false;
			}
			if (upCnt[i]<MIN_SENS_CNT || upCnt[i]>MAX_SENS_CNT) {
				ok = false;
				upCnt[i] = MIN_SENS_CNT;
			}
			if (downCnt[i]<MIN_SENS_CNT || downCnt[i]>MAX_SENS_CNT) {
				ok = false;
				downCnt[i] = MIN_SENS_CNT;
			}
		}

		if (!ok) {
			Menu.msg(Texte.notjust, Texte.secval);
			for (i=0; i<cnt; ++i) {
				maxCnt[i] = 0;
			}
		}

	}

/**
* only for info.
*/
	private static void dispVals() {

		int i;

		Display.cls();
		for (i=0; i<cnt; ++i) {
			Display.intVal(downCnt[i]);
			Display.data(' ');
		}
		Timer.sleepWd(1000);
		Display.cls();
		for (i=0; i<cnt; ++i) {
			Display.intVal(upCnt[i]);
			Display.data(' ');
		}
		Timer.sleepWd(1000);
		Display.cls();
		for (i=0; i<cnt; ++i) {
			Display.intVal(maxCnt[i]);
			Display.data(' ');
		}
		Timer.sleepWd(1000);
		Display.cls();

	}

//
//	some properties
//
	public static boolean isDown() { return ltg==LU; }
	public static boolean isUp() { return ltg==LO; }
	public static boolean isBetween() { return ltg==LZ; }

	public static boolean upOk() {

		return ltg==LU || ltg==LUZ || ltg==LZ;
	}

	public static boolean downOk() {

		return ltg==LO || ltg==LZ;
	}

	public static int getCnt() { return cnt; }
//
//	cmd handling
//
	private static int cmd(int nr, int val) { return cmd(nr, val, 0); }


	private static int cmd(int nr, int val, int data) {

		int ret = Msg.exchg(nr, val, data);

		if (ret>=0) {
			return ret;
		}

		// more tries
		int tryCnt = MAX_COMM_ERR-1;

		for (; ret<0 && tryCnt>0; --tryCnt) {
			ret = Msg.exchg(nr, val, data);
Timer.wd();	// bei zu hohem MAX_COMM_ERR
		}
		return ret;
	}

	private static boolean cmdAll(int val) {
		return cmdAll(val, 0);
	}
/**
*	send a command to all MS.
*/
	private static boolean cmdAll(int val, int data) {

		int i, j;
		boolean ok = true;

		// first try
		for (i=0; i<cnt; ++i) {
			j = Msg.exchg(i+1, val, data);
			ret[i] = j;
			if (j<0) ok = false;
		}

		// more tries
		int tryCnt = MAX_COMM_ERR-1;

		for (; !ok && tryCnt>0; --tryCnt) {
			ok = true;
			for (i=0; i<cnt; ++i) {
				j = ret[i];
				if (j<0) {
					j = Msg.exchg(i+1, val, data);
					ret[i] = j;
				}
				if (j<0) ok = false;
			}
Timer.wd();	// bei zu hohem MAX_COMM_ERR
		}
		return ok;
	}

	private static boolean cmdAll(int val, int[] data) {

		int i, j;
		boolean ok = true;

		// first try
		for (i=0; i<cnt; ++i) {
			j = Msg.exchg(i+1, val, data[i]);
			ret[i] = j;
			if (j<0) ok = false;
		}

		// more tries
		int tryCnt = MAX_COMM_ERR-1;

		for (; !ok && tryCnt>0; --tryCnt) {
			ok = true;
			for (i=0; i<cnt; ++i) {
				j = ret[i];
				if (j<0) {
					j = Msg.exchg(i+1, val, data[i]);
					ret[i] = j;
				}
				if (j<0) ok = false;
			}
Timer.wd();	// bei zu hohem MAX_COMM_ERR
		}
		return ok;
	}






	public static void powerOff() {

		JopSys.wr(0, BBSys.IO_TRIAC);
	}

	// be careful when using this in another program!
	public static void powerOn() {

		JopSys.wr(BBSys.BIT_TR_ON, BBSys.IO_TRIAC);
	}


	public static void error(int nr) { error(nr, 0); }

/* not used
	private static void errorFindMS(int nr) {

		int i, msnr;

		msnr = 0;

		for (i=0; i<cnt; ++i) {
			if (ret[i]<0) {
				msnr = i+1;
				break;
			}
		}
		error(nr, msnr);
	}
*/

/**
*	error display with endless loop.
*		allow Menu, but no 'real' function.
*		be WARNED: menu function 'download' (Debug.echo()) switches Power ON!!!
*/
	public static void error(int nr, int msnr) {

		powerOff();
		Zentrale.chkNot();

		if (msnr!=0) {
			Display.line1(Texte.mserr, msnr);
		} else {
			Display.line1(Texte.error);
		}
		Display.line2(Texte.errTxt(nr));

		Log.write(Log.ERROR, nr, msnr);

//
//	allow Menu cmd's for error correction
//	but no way out
		for (;;) {

			Keyboard.loop();			
											
			if (Keyboard.pressed) {
				if (Keyboard.rd()==Keyboard.B) {
					Menu.doit();
					Display.line1(Texte.errMode);
					Display.line2(Texte.startNew);
				}
			}
			Timer.waitForNextInterval();
			Timer.wd();
		}
	}

/**
*	communication error display with secial display!
*/
	private static void comErr() {

		int i;
		int msnr = 0;

		powerOff();
		Zentrale.chkNot();

		Display.cls();
		Display.line1(Texte.comErr);
		Display.line2();

		for (i=0; i<cnt; ++i) {
			Timer.wd();
			if (ret[i]<0) {
				msnr = i+1;
				Display.intVal(msnr);
				Display.data(' ');
			}
		}

		Log.write(Log.ERROR, Err.COMM, msnr);
		for (;;) {
			Timer.wd();
		}
	}


/*
*	Werte Sensoren der Masten aus.
*	Leitung unten nur wenn ALLE Sensoren unten anzeigen.
*	Das Freigaberelais wird aber nur in down() nach einer erfolgreichen Fahrt
*	gesetzt. Die LEDs in der Zentrale aber auch nach Einschalten und in LUZ.
*/
	private static void chkLtg() {

		int i, val;
		boolean u, o;
		int cntU, cntO, cntZ;
		cntU = 0;
		cntO = 0;
		cntZ = 0;

		if (!cmdAll(BBSys.CMD_INP)) {
			comErr();
		}

		for (i=0; i<cnt; ++i) {
			val = ret[i]>> 4;		// TASTER und Sensoren!
			o = (val & BBSys.BIT_SENSO) != 0;
			u = (val & BBSys.BIT_SENSU) != 0;
			if (o && u) {
				error(Err.SENS_UP_AND_DOWN, i+1);
			} else if (o) {
				++cntO;
			} else if (u) {
				++cntU;
			} else {
				++cntZ;
			}
		}

		if (cntU==0 && cntO==0) {
			ltg = LZ;
			JopSys.wr(BBSys.BIT_LED_U + BBSys.BIT_LED_O, BBSys.IO_LED);
		} else if (cntU==cnt && cntO==0) {
			ltg = LU;
			JopSys.wr(BBSys.BIT_LED_U, BBSys.IO_LED);
		} else if (cntU!=0 && cntO==0) {
			ltg = LUZ;
		} else if (cntU==0 && cntO!=0) {
			ltg = LO;
			JopSys.wr(BBSys.BIT_LED_O, BBSys.IO_LED);
		} else {
			JopSys.wr(0, BBSys.IO_LED);
			error(Err.SENS_UP_AND_DOWN);
		}

	}

/**
*	see if a new MS is at address 0 after boot timeout.
*	more than one MS can be changed, BUT only one at a time AND
*	lowest number first!
*/
	private static void chkNewMS() {

		int i, j, missing;

		i = 0;
		missing = -1;
		for (j=0; j<cnt; ++j) {
			if (ret[j]<0) {
				missing = j;
				break;				// stop on first found!
			}
		}
		if (missing!=-1 && cmd(0, BBSys.CMD_STATUS)>=0) {
			// found !
			++missing;				// base 1
			Display.line1(Texte.newms, missing);
			cmd(0, BBSys.CMD_SET_STATE, BBSys.MS_DBG);
			cmd(0, BBSys.CMD_SETAD, missing);

			Timer.sleepWd(2000);
			Display.line1(Texte.reboot);
			for(;;) ;
		}
	}

/**
*	something went wrong during boot.
*/
	private static void bootErr() {

		int i, j;

		i = 0;
		for (j=0; j<cnt; ++j) {
			if (ret[j]<0) ++i;
		}
		if (i==cnt) error(Err.NO_MS);
		for (j=0; j<cnt; ++j) {
			if (ret[j]<0) {
				error(Err.NO_ANSW_MS, j+1);	// endless loop
			}
		}
		error(Err.NO_ANSW_MS);	// endless loop
	}

	private static final int BOOT_TIME = 100;				// max. 10s
/**
*	boot all MS and check MS error
*/
	private static boolean boot() {

		int i, j;

		powerOn();
		Display.line1();
		Display.intVal(cnt);
		Display.data(' ');
		Display.data(Texte.chkms);

		// wait for boot
		for (i=0; i<BOOT_TIME; ++i) {
			if (cmdAll(BBSys.CMD_STATUS)) {
				break;
			}
			Timer.wd();
			Clock.loop();
			Timer.sleepWd(100);
			Msg.flush();		// flush the input buffer
		}
		if (i==BOOT_TIME) {
			chkNewMS();
			bootErr();
		}
		Timer.sleepWd(200);		// for shure

		if (!cmdAll(BBSys.CMD_SET_STATE, BBSys.MS_RDY)) {
			comErr();
		}
		Timer.wd();

		Timer.sleepWd(100);		// wait for error detection after set MS_RDY
		if (!cmdAll(BBSys.CMD_ERRNR)) {
			comErr();
		}
		for (i=0; i<cnt; ++i) {
			if (ret[i] != 0) {
				error(ret[i], i+1);	// endless loop
			}
		}
		Timer.wd();

		if (!cmdAll(BBSys.CMD_STATUS)) {
			comErr();
		}
		// not MS_RDY means some strange error !!!
		// should never happen
		for (i=0; i<cnt; ++i) {
			if (ret[i] != BBSys.MS_RDY) {
				error(Err.MS_ERR, i+1);	// endless loop
			}
		}
		Timer.wd();

		// see, if there are more MS than configured
		for (i=cnt+1; i<=16; ++i) {
			if (cmd(i, BBSys.CMD_STATUS)>=0) {
				error(Err.WRONG_MS_CNT);
			}
		}

		return true;
	}

/**
*	init all MS for real action.
*/
	private static boolean initMS() {

		getTemp();
		return setVals();
	}

	private static void getTemp() {

		int i;

		if (cmdAll(BBSys.CMD_TEMP)) {
			for (i=0; i<cnt; ++i) {
				temp[i] = Temp.calc((ret[i]<<3)+17000);
				Timer.wd();
			}
		}
	}

	private static boolean setVals() {
		
		boolean ok = true;
		int i;

		if (!cmdAll(BBSys.CMD_SET_DOWNCNT, downCnt)) {
			comErr();
		}
		if (!cmdAll(BBSys.CMD_SET_UPCNT, upCnt)) {
			comErr();
		}
		if (!cmdAll(BBSys.CMD_SET_MAXCNT, maxCnt)) {
			comErr();
		}

		Timer.wd();

		return true;
	}


/**
*	check all MS and state of Leitung.
*/
	public static void chkMS() {

		boot();
		chkLtg();
		Timer.wd();
		powerOff();
	}

/**
*	all MS in service mode
*/
	public static void serviceUp() {

		for (int i=0; i<cnt; ++i) {
			maxCnt[i] = 0;				// keine Ueberw. im Serv.mode
		}
		boot();
		initMS();
		if (!cmdAll(BBSys.CMD_SET_STATE, BBSys.MS_SERVICE)) {
			comErr();
		}
		Menu.msg(Texte.serviceUp, Texte.posein);
		powerOff();
	}
	public static void serviceDown() {

		for (int i=0; i<cnt; ++i) {
			maxCnt[i] = 0;				// keine Ueberw. im Serv.mode
		}
		boot();
		initMS();
		if (!cmdAll(BBSys.CMD_SET_STATE, BBSys.MS_SERVICE)) {
			comErr();
		}
		Menu.msg(Texte.serviceDown, Texte.posein);
		powerOff();
	}

/**
*	set values
*/
	public static void servAfterUp(int nr) {

		downCnt[nr] = servCnt[nr];
		maxCnt[nr] = lastCnt[nr];

		if (downCnt[nr]<MIN_SENS_CNT) {
			downCnt[nr] = MIN_SENS_CNT;
			maxCnt[nr] = 0;
			Menu.msgMast(nr+1, Texte.sukl);
		} else if (downCnt[nr]>MAX_SENS_CNT) {
			downCnt[nr] = MAX_SENS_CNT;
			maxCnt[nr] = 0;
			Menu.msgMast(nr+1, Texte.sugr);
		}

		Timer.wd();

		Config.setMSmaxCnt(nr+1, maxCnt[nr]);
		Config.setMSdwnCnt(nr+1, downCnt[nr]);

		Timer.wd();
	}

	public static void servAfterDown(int nr) {

		upCnt[nr] = servCnt[nr];
		maxCnt[nr] = maxCnt[nr]-lastCnt[nr];

		if (upCnt[nr]<MIN_SENS_CNT) {
			upCnt[nr] = MIN_SENS_CNT;
			maxCnt[nr] = 0;
			Menu.msgMast(nr+1, Texte.sokl);
		} else if (upCnt[nr]>MAX_SENS_CNT) {
			upCnt[nr] = MAX_SENS_CNT;
			maxCnt[nr] = 0;
			Menu.msgMast(nr+1, Texte.sogr);
		}

		Timer.wd();

		Config.setMSmaxCnt(nr+1, maxCnt[nr]);
		Config.setMSupCnt(nr+1, upCnt[nr]);

		Timer.wd();
	}

/**
*	save data on mast 1.
*/
	public static void backZs() {

		boot();
		if (cmd(1, BBSys.CMD_SET_STATE, BBSys.MS_DBG)<0) {
			comErr();
		}
		Display.cls();
		Display.line1(Texte.backZs);

		int startPage = Flash.MS_DATA>>7;
		int pages = (Flash.MS_DATA_LEN+127)/128;
		int data;

//
//	longer Msg timeout becaus CMD_FL_PROG takes long (about 15ms).
//	restart after program
//
		Msg.slow = true;					// longer Msg timeout

		for (int i=0; i<pages; ++i) {

			boolean error = false;
			if (cmd(1, BBSys.CMD_FL_PAGE, startPage+i)<0) { comErr(); }
			for (int j=0; j<128; ++j) {
				Timer.wd();
				data = Flash.read(Flash.MS_DATA+i*128+j) & 0xff;
				if ((data = Msg.exchg(1, BBSys.CMD_FL_DATA, data))<0) {		// only one try! addr autoincrement
					error = true;
					Timer.sleepWd(50);								// to ignore late replays
					--i;											// program pgae again
					break;
				}
			}
			if (!error) {
				if (Msg.exchg(1, BBSys.CMD_FL_PROG, 0)<0) { comErr(); }
			}
			
		}
		powerOff();
	}

/**
*	restore data from mast 1.
*/
	public static void restZs() {

		boot();
		if (cmd(1, BBSys.CMD_SET_STATE, BBSys.MS_DBG)<0) {
			comErr();
		}
		Display.cls();
		Display.line1(Texte.restZs);

		int startPage = Flash.MS_DATA>>7;
		int pages = (Flash.MS_DATA_LEN+127)/128;
		int data;

		Msg.slow = true;

		if (cmd(1, BBSys.CMD_FL_PAGE, startPage)<0) { comErr(); }

		for (int i=0; i<Flash.MS_DATA_LEN; ++i) {

			Timer.wd();
			data = Msg.exchg(1, BBSys.CMD_FL_READ, 0);
			if (data<0) { comErr(); }
			Flash.write(Flash.MS_DATA+i, data);
			
		}
		powerOff();
	}


	private static void getServCnt() {

		int i;

		if (!cmdAll(BBSys.CMD_SERVICECNT)) {
			comErr();
		}
		for (i=0; i<cnt; ++i) {
			servCnt[i] = ret[i];
		}
	}

/**
*	now go up!
*/
	public static void up() {

		int i, val;

		if (!boot()) return;
		if (!initMS()) return;

		ltg = LUNKNOWN;

		Display.line1(Texte.goesUp);
		Display.line2(Texte.empty);
		Relais.resLu();
		JopSys.wr(BBSys.BIT_LED_FO, BBSys.IO_LED);
		Log.write(Log.UP_STARTED);

		// reset cnt
// TODO last realy known value!!!
		if (!cmdAll(BBSys.CMD_SETCNT, 0)) {
			comErr();
		}

		if (!cmdAll(BBSys.CMD_UP)) {
			comErr();
		}
		Timer.wd();

		runningLoop(BBSys.MS_UP);

		getTemp();

		if (isUp()) {
			Display.line1(Texte.isUp);
			Relais.setLo();
			Log.write(Log.IS_UP);
		}
		Display.line2(Texte.empty);

		powerOff();
	}


/**
*	now go down!
*/
	public static void down() {

		int i, val;

		if (!boot()) return;
		if (!initMS()) return;

		ltg = LUNKNOWN;

		Display.line1(Texte.goesDown);
		Display.line2(Texte.empty);
		Relais.resLo();
		JopSys.wr(BBSys.BIT_LED_FU, BBSys.IO_LED);
		Log.write(Log.DOWN_STARTED);

		// set cnt
// TODO last realy known value!!!
		if (!cmdAll(BBSys.CMD_SETCNT, maxCnt)) {
			comErr();
		}

		if (!cmdAll(BBSys.CMD_DOWN)) {
			comErr();
		}
		Timer.wd();

		runningLoop(BBSys.MS_DOWN);

		getTemp();

		if (Station.isDown()) {
			Display.line1(Texte.isDown);
			Relais.setLu();
			Log.write(Log.IS_DOWN);
		}
		Display.line2(Texte.empty);

		powerOff();
	}

/**
*	Leitung faehrt.
*/
	private static void runningLoop(int dir) {

		int i, val, min, max;
		boolean running;
		int cntStopped = 0;

		// wait for autom stop of all
		do {

			cntStopped = 0;

			if (!cmdAll(BBSys.CMD_STATUS)) {
				comErr();
			}

			for (i=0; i<cnt; ++i) {
				val = ret[i];
				if (val==dir) {
					;						// still runnig ... OK
				} else if (val==BBSys.MS_RDY) {
					++cntStopped;			// MS stopped
				} else if (val==BBSys.MS_ERR) {
					val = cmd(i+1, BBSys.CMD_ERRNR);
					if (val>0) {
						error(val, i+1);
					} else {
						error(Err.MS_ERR, i+1);
					}
				} else {
					error(Err.MS_ERR, i+1);		// something wrong
				}
			}

			max = -9999;
			min = 9999;

			if (!cmdAll(BBSys.CMD_CNT)) {
				comErr();
			}
//Display.line2();
			for (i=0; i<cnt; ++i) {
				val = ret[i];
				val = (val<<20)>>20;		// sign
//Display.intVal(val);
//Display.data(' ');
				lastCnt[i] = val;
				if (val<min) min = val;
				if (val>max) max = val;
			}
			val = max-min;

			if (val>MAX_DIFF) {
				error(Err.MAX_DIFF);
			} else if (val>CONTROL_DIFF && cntStopped==0) {
				control(dir, min, max);
			}

// display maximum differenz for now
if (val<100 && val>0) {
Display.line2();
Display.data('0'+val/10);
Display.data('0'+val%10);
}

			Clock.loop();
			Timer.wd();

		} while (cntStopped!=cnt);

		getServCnt();
		chkLtg();

	}

/**
*	control of MS.
*	Send CMD_PAUSE to the 'fastest' stations.
*/
	private static void control(int dir, int min, int max) {

		int i;

		if (dir==BBSys.MS_UP) {
			for (i=0; i<cnt; ++i) {
				if (lastCnt[i] > min+CONTROL_DIFF) {
					cmd(i+1, BBSys.CMD_PAUSE);
				}
			}
		} else if (dir==BBSys.MS_DOWN) {
			for (i=0; i<cnt; ++i) {
				if (lastCnt[i] < max-CONTROL_DIFF) {
					cmd(i+1, BBSys.CMD_PAUSE);
				}
			}
		}
	}

}
