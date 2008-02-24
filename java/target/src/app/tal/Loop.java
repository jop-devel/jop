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
 * Created on 22.04.2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package tal;

import joprt.RtThread;
import util.Dbg;
import util.Timer;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

/**
 * @author martin
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class Loop extends RtThread {

	Alarm[] alarm;
	static final int MEL_CNT = 8;
	Alarm cold;
	Alarm ort;
	Alarm accu;
	Alarm accuEmpty;
	
	int mask;
	boolean parOk;
	boolean startSend;
	int counter;
	
	boolean ortPushButton;
	int ortTimer;
	
	static final int ORT_MASK = 0x100;
	static final int ERASE_MASK = 0x200;

	static final int LED_TIME_MS = 200;
	static final int LED_ORT = 0x100;
	static final int LED_ACCU = 0x200;
	static final int LED_ACCU_EMPTY = 0x400;
	static final int LED_BLINK = 0x800;
	
	// accu threshholds in 0.1 V
	static final int VBAT_MAINS = 140;
	static final int VBAT_EMPTY = 110;
	static final int VBAT_OFF = 105;

	/**
	 * Call all 23 hours.
	 */
	static final int CALL_PERIOD_SEC = 23*3600;
	/**
	 * Remove ort after 8 hours.
	 */
	static final int ORT_TIME_SEC = 8*3600;
	/**
	 * Wait some time befor sending the Alarm.
	 * Perhaps it's a short one and we can transmit
	 * this in one call.
	 */
	static final int SEND_WAIT_MS = 1000;
	/**
	 * @param prio
	 * @param us
	 */
	public Loop(int prio, int us) {
		super(prio, us);
		Timer.loop();	// start the clock
		alarm = new Alarm[MEL_CNT];
		for (int i=0; i<MEL_CNT; ++i) {
			alarm[i] = new Alarm(Alarm.MB_OFF+i, Tal.par.time[i]);
		}
		mask = Tal.par.mask;
		parOk = Tal.par.ok;
		startSend = false;
		counter = 0;
		cold = new Alarm(Alarm.SB1_OFF+0, 0);
		// set cold to ON_OFF_PEND
		cold.setState(true);
		cold.setState(false);
		ort = new Alarm(Alarm.SB2_OFF+1, 0);
		ortPushButton = false;
		accu = new Alarm(Alarm.SB2_OFF+2, 0);
		accuEmpty = new Alarm(Alarm.SB2_OFF+4, 0);
	}



	public void run() {
		
		boolean changed = false;
		int ledTimer = Timer.getTimeoutMs(LED_TIME_MS);
		int transmitTimer = Timer.getSec()+CALL_PERIOD_SEC;
		
		for (;;) {
			
			changed = checkAlarm();
			
			if (changed && Tal.modem != null) {
				Tal.modem.startSend();
			} else if (Timer.secTimeout(transmitTimer)) {
				if (Tal.modem != null && Tal.modem.startSend()) {
					// only reschedule the timer if startSend() was ok.
Dbg.wr("Time to send a message\n");
					transmitTimer = Timer.getSec()+CALL_PERIOD_SEC;
				}
			}
			
			if (Timer.timeout(ledTimer)) {
				ledTimer = Timer.getTimeoutMs(200);
				setLeds();
			} else {
				// it's placed here to not
				// waste time slots
				Timer.loop();
				checkAccu();
				checkOrt();
			}
			waitForNextPeriod();
		}

	}



	/**
	 * 
	 */
	private void setLeds() {
		
		int i;
		boolean state;
		int led = 0;
		
		++counter;
		
		for (i=0; i<MEL_CNT; ++i) {
			if (alarm[i].getLedPattern(counter)) {
				led |= 1<<i;
			}
		}
		if (ort.getLedPattern(counter)) led |= LED_ORT;
		if (accu.getLedPattern(counter)) led |= LED_ACCU;
		if (accuEmpty.getLedPattern(counter)) led |= LED_ACCU_EMPTY;
		
		
		i = parOk ? counter&0x02 : counter &0x01;
		led |= (i!=0) ? LED_BLINK : 0;
		Native.wr(led, Const.IO_LED);

	}

	private boolean checkAlarm() {
		
		int i;
		int val = Native.rd(Const.IO_IN)^mask;
		for (i=0; i<MEL_CNT; ++i) {
			alarm[i].setState(((val>>i)&1)==1);
		}
		if ((val&ERASE_MASK)!=0) {
			Tal.par.erase();
			for (;;) {
				// wait till reset and flash the LEDs
				++i;
				if ((i&0x0f)==0) {
					Native.wr(0xfff, Const.IO_LED);
				} else { 
					Native.wr(0, Const.IO_LED);					
				}
				waitForNextPeriod();
			}
		}
		return Alarm.isPending();
	}	
	
	private void checkOrt() {

		boolean val = (Native.rd(Const.IO_IN) & ORT_MASK)!=0;
		boolean ortAlarm = ort.isOn();
		if (val && !ortPushButton) {
			// trigger on rising edge
			if (ortAlarm) {
				// switch off
				ort.setState(false);
			} else {
				ort.setState(true);
				ortTimer = Timer.getSec()+ORT_TIME_SEC;
			}
		} else {
			// check for timeout
			if (ortAlarm && Timer.secTimeout(ortTimer)) {
				// reset ort alarm
Dbg.wr("auto reset of ort\n");
				ort.setState(false);
			}
		}
		// thats the delay for edge detection
		ortPushButton = val;
	}
	/**
	 * 
	 */
	private void checkAccu() {
		
		int i;
		i = Native.rd(Const.IO_ADC3);	// U = 11 * ADCout * 3.3 / (2^16-1)
		i *= 100;
		i /= 18054;
		// value is now in 1/10 mA or 1/10 V
		if (i<VBAT_MAINS) { 
			accu.setState(true);
		} else {
			accu.setState(false); 
			accuEmpty.setState(false); 
		}
		if (i<VBAT_EMPTY) accuEmpty.setState(true);
		if (i<VBAT_OFF) {
Dbg.wr("switch off!!!\n");
			// battery switch is !d31 of LED port
			Native.wr(-1, Const.IO_LED);
			// no return, probably switching on again!
			for (;;) {
				waitForNextPeriod();
			}
		}
	}

}
