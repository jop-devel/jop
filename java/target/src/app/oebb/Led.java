package oebb;

/**
*	Led.java: Handle LED and Alarm beep.
*
*	Author: Martin Schoeberl (martin.schoeberl@chello.at)
*
*   Changelog:
*
*/

import util.Timer;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

public class Led {

	private static boolean blink;
	private static int ledTimeout;
	private static boolean ledOn;
	private static final int LED_TIME = 500;

	private static boolean beep;
	private static boolean bp;		// one short beep
	private static int beepTimeout;
	private static boolean beepOn;
	private static boolean modemOff;
	private static final int BEEP_TIME = 500;
	private static final int SHORT_BEEP_TIME = 500;

	// regular called from Logic.loop().

	static void loop() {

		if (blink) {
			if (Timer.timeout(ledTimeout)) {
				ledOn = !ledOn;
				ledTimeout = Timer.getTimeoutMs(LED_TIME);
			}
		}
		if (beep) {
			if (Timer.timeout(beepTimeout)) {
				beepOn = !beepOn;
				beepTimeout = Timer.getTimeoutMs(BEEP_TIME);
			}
		}
		if (bp) {
			if (Timer.timeout(beepTimeout)) {
				beepOn = false;					// switch off
				bp = false;
			}
		}

		// set output value

		int val = 8;		// DTR ready
		if (ledOn) {
			val |= 1;		// LED
		}
		if (beepOn) {
			val |= 2;		// relais a
		}
		// der Ruhekontakt wird verwendet
		if (modemOff) {
			val |= 4;		// relais b			
		}
		Native.wr(val, Const.IO_BG+1);
	}

	static void startBlinking() {

		if (!blink) {
			ledTimeout = Timer.getTimeoutMs(LED_TIME);
			blink = true;
			ledOn = true;
		}
	}

	static void stopBlinking() {

		blink = false;
		ledOn = false;
	}

	static void startBeeping() {

		if (!beep) {
			beepTimeout = Timer.getTimeoutMs(BEEP_TIME);
			beep = true;
			beepOn = true;
		}
	}

	static void stopBeeping() {

		beep = false;
		beepOn = false;
	}

	static void alarm() {

		startBlinking();
		startBeeping();
	}

	static void alarmOff() {

		stopBlinking();
		stopBeeping();
	}

	static void shortBeep() {

		if (!bp) {
			beepTimeout = Timer.getTimeoutMs(SHORT_BEEP_TIME);
			bp = true;
			beepOn = true;
		}
	}
	
	static void startModem() {
		modemOff = false;
	}
	
	static void stopModem() {
		modemOff = true;
	}
}
