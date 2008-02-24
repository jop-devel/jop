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
 * Created on 15.04.2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package tal;

import joprt.RtThread;
import util.Dbg;
import util.Serial;
import util.Timer;

/**
 * @author martin
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
/**
 * @author martin
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
/**
 * @author martin
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class Modem extends RtThread {

	private final static int MAX_MSG_LEN = 300;
	private final static int MAX_TRY = 5;

	private static volatile Modem single;
	private Serial ser;
	
	/**
	 * Generate a singleton for a modem.
	 * @return the instance of Modem.
	 */
	public static Modem getInstance(int prio, int us, Serial ser) { 
		if (single==null) {
			single = new Modem(prio, us, ser);
		}
		return single;
	}

	private StringBuffer strBuf;
	private volatile int state;
	private volatile boolean fwpDone;
	private int connCnt;
	Alarm fail3;
	Alarm fail12;
	
	static final int INIT = 0;
	static final int TEST = 123;
	static final int READY = 1;
	static final int RING = 2;
	static final int CONNECTED = 3;
	static final int SEND_FWP = 4;
	
	class Init {
		String cmd;
		String answ;
		int time;
		
		public Init(String c, String a, int t) {
			cmd =c;
			answ = a;
			time = t;
		}
	}

	private Init[] init;


	/*
	WARTEN_TIMEOUT(SEC5);       / dient f?r UNFŽHIGE HARDWARE /
	watch |= 0x01;
	OUT_B(ADR_TEST_LED,watch);
	WARTEN_TIMEOUT(SEC2);
	watch &= ~0x01;
	OUT_B(ADR_TEST_LED,watch);

	WARTEN_TIMEOUT(SEC2);       / ab hier beginnt Init f?r normale Hardware /
	modem_abort ();				/ disable DTR for 500 ms

	*/


	/**
	 * @param prio
	 * @param us
	 */
	private Modem(int prio, int us, Serial serial) {
		
		super(prio, us);
		ser = serial;
		strBuf = new StringBuffer(40);
		connCnt = 0;
		fail3 = new Alarm(Alarm.SB1_OFF+1, 0);
		fail12 = new Alarm(Alarm.SB1_OFF+2, 0);
		state = INIT;
		init = new Init[6];
		// TODO <clinit> in JOP
		// Initialization  string for the modem.
//		init[0] = new Init("|+++|", "", 3);			// does not work for auto baud rate
//		init[0] = new Init("ATH\r", "OK", 1);
		init[0] = new Init("AT&F\r", "", 1);		// default values
		init[1] = new Init("ATV1\r", "OK", 1);		// long, verbose result code (for ec 24)
		init[2] = new Init("ATH\r", "OK", 1);		// 
		init[3] = new Init("AT\\Q3\r", "", 1);		// RTS/CTS hardware flow control
		init[4] = new Init("ATS0=1\r", "OK", 1);	// automatic answer after 1 ring (0 is default)
		init[5] = new Init("ATS8=8\r", "OK", 1);	// number of seconds for a comma dial modifier

/*
		from fw.c in tal220
		init[1] = new Init("ATE0\r", "OK", 1);		// no echo in command mode
		init[1] = new Init("ATQ0\r", "OK", 1);		// return 'OK' (is default)
		init[1] = new Init("AT&B1\r", "OK", 1);		// a special cmd for robotics (fix baudrate)
		init[1] = new Init("AT&W\r", "OK", 1);		// store user profile (we don't need this)
*/
	}
	
	public void run() {

		for (;;) {
			if (state==INIT) {
				if (init(Tal.par.modem)) {
					state = READY;
				} else {
					waitSec(5);
				}
			} else if (state==READY || state==RING) {
				checkModem();
			} else if (state==TEST) {
				connect(Tal.par.telnr[0], null);
				sendWait(Tal.fwp.getXXX(), "", 5);
				answerFwp();
				state = READY;
			} else if (state==CONNECTED) {
				answerFwp();
				connCnt = 0;
				state = READY;
			} else if (state==SEND_FWP) {
				if (Tal.par.ok) sendFwp();
				state = READY;
			} else {
				// we don't have a state for this
				waitForNextPeriod();
			}
		}

	}

	public boolean startSend() {
			
		// A minimum race condition if the phone rings,
		// but this is not a major problem.
		// We do the retry one level higher.
		if (state==READY) {
			fwpDone = false;
			state = SEND_FWP;
			return true;
		} else {
			return false;
		}
	}

	private void sendFwp() {

		int telNr = 0;
		int oldVal = 0;
		int newVal = 0;

		while (connCnt<12 && !fwpDone) {
			
			if (connCnt>=3) {
				// set to ON_OFF_PEND
				fail3.setState(true);
				fail3.setState(false);
			}
Dbg.wr("sendFwp: "); Dbg.intVal(connCnt); Dbg.lf();
			if (connect(Tal.par.telnr[telNr], Tal.par.modem)) {
				for (int i=0; i<MAX_TRY; ++i) {
					// get values to send from Alarm
					oldVal = Alarm.getOldMsg();
					newVal = Alarm.getNewMsg();
					if (sendWait(Tal.fwp.getFwpString(oldVal), "*\r\n", 5) 
						&& sendWait(Tal.fwp.getFwpString(newVal), "*\r\n", 5)) {
							
						Alarm.stateTransmitted(oldVal, newVal);
						connCnt = 0;
						fwpDone = true;
						break;
					}
				}
			}
			if (!fwpDone) ++connCnt;
			++telNr;
			if (telNr>=Tal.par.cntTel) telNr = 0;
			waitSec(Tal.par.disconn);
			disconnect();
		}
		if (!fwpDone){
			// set to ON_OFF_PEND
			fail3.setState(true);
			fail3.setState(false);
		}
	}


	private void checkModem() {
		Dbg.wr('.');
		readLine(strBuf, 1);
		if (strBuf.length()!=0) {
			Dbg.wr('^');
			Dbg.wr(strBuf);
			Dbg.wr('^');
			if (startsWith(strBuf, "CONNECT")) {
				Dbg.lf();
				Dbg.wr("connect!");
				Dbg.lf();
				state = CONNECTED;
			} else if (startsWith(strBuf, "RING")) {
				Dbg.wr("ring!");
				state = RING;
			}
		}
	}


	/**
	 * @param strBuf
	 * @param string
	 * @return
	 */
	private boolean startsWith(StringBuffer tmp, String string) {
		int max = tmp.length()-1;
		for (int i = 0; i < string.length(); i++) {
			if (i>max || tmp.charAt(i)!=string.charAt(i)) {
				return false;
			}
		}
		return true;
	}

	private void answerFwp() {
		
		// Wait for other modem after CONNECT!
		// over-read all garbage characters
		// Because this modem is dumb!
		// readWait(10);	
		// walter will das nicht	
		
		StringBuffer in = Tal.fwp.getIn();
		StringBuffer out = Tal.fwp.getOut();
		for (;;) {
			readLine(in, '*', 10);
			if (in.length()==0) break;
			if (startsWith(in, "*\r\n")) break;
			Dbg.wr("handle");
			Tal.fwp.handle();
			sendWait(out, "", 3);
		}
		/*
		// send data again for short 
		for (int i=0; i<MAX_TRY; ++i) {
			// get values to send from Alarm
			int oldVal = Alarm.getOldMsg();
			int newVal = Alarm.getNewMsg();
			if (sendWait(Tal.fwp.getFwpString(oldVal), "*\r\n", 5) 
				&& sendWait(Tal.fwp.getFwpString(newVal), "*\r\n", 5)) {
							
				Alarm.stateTransmitted(oldVal, newVal);
				connCnt = 0;
				fwpDone = true;
				break;
			}
		}
		*/
//		waitSec(Tal.par.disconn);
// don't wait, with new parameter we got a reset
		disconnect();
	}
	/**
	 * Read one line until '\n' or timeout expired.
	 * @param in
	 * @param i
	 */
	private void readLine(StringBuffer in, int timeout) {
		readLine(in, -1, timeout);
	}
	
	/**
	 * Throw awai all character till startChar and than read
	 * one line until '\n' ot timeout expired.
	 * @param in
	 * @param startChar
	 * @param timeout
	 */
	private void readLine(StringBuffer in, int startChar, int timeout) {
		
		boolean start = startChar==-1;
		in.setLength(0);
		int timer = Timer.getTimeoutSec(timeout);
		int val = 0;
		while (!Timer.timeout(timer)) {
			for (int i = ser.rxCnt(); i>0; --i) {
				val = ser.rd();
Dbg.wr(val);
				if (!start && val==startChar) start = true;
				if (start) in.append((char) val);
				if (in.length()==MAX_MSG_LEN) {
Dbg.wr("line too long");
					return;
				}
				// we got a line and are ready to reutrn
				if (start & val=='\n') return;
				timer = Timer.getTimeoutSec(timeout);
			}
			waitForNextPeriod();
		}
		
	}

	/**
	 * 
	 */
	private void disconnect() {
		// this is a simple dissconnect
		ser.setDTR(false);
		waitSec(1);
		ser.setDTR(true);
		waitSec(1);
		sendWait("|+++|", "OK", 3);
		sendWait("ATH\r", "OK", 10);
		waitSec(1);
	}

	/**
	 * Start a connection.
	 */
	public boolean connect(StringBuffer telNr, StringBuffer modemStr) {
		
		if (!init(modemStr)) return false;
		strBuf.setLength(0);
		strBuf.append("ATD");
		strBuf.append(telNr);
		strBuf.append('\r');
		if (!sendWait(strBuf, "CONNECT", 30)) {
			return false;
		}
		readWait(1);		// read rest of CONNECT XXX string
		// because first two telegrams get lost,
		// other modem is not yet connected!
		waitSec(5);		

		return true;
	}


	private boolean init(StringBuffer modemStr) {
		
		ser.setDTR(false);
		waitSec(2);
		ser.setDTR(true);
		waitSec(1);
		for (int i = 0; i < init.length; i++) {
			if (!sendWait(init[i].cmd, init[i].answ, init[i].time)) {
				return false;
			}
			// read the answer when we are not interested
			if (init[i].answ.length()==0) {
				readWait(init[i].time);
			}
		}
		
		// special handling for PIN
		/*
		if (sendWait("AT+CPIN?\r", "SIM PIN", 3)) {
			readWait(1);	// flush input ('OK')
			sendWait("AT+CPIN=5644\r", "OK", 60);	// pin number for GSM modem
		}
		*/
		
		// do we have a modem string?
		if (modemStr!=null && modemStr.length()!=0) {
			strBuf.setLength(0);
			strBuf.append(modemStr);
			strBuf.append('\r');
			sendWait(strBuf, "OK", 1);
			readWait(1);					// perhaps this is a compound string
		}
		return true;
	}


	/**
	*	wait seconds
	*/
	private void waitSec(int t) {

		int timer = Timer.getTimeoutSec(t);

		while (!Timer.timeout(timer)) {
			waitForNextPeriod();
		}
	}

	/**
	*	send a string to serial line buffer.
	*	'|' has special meaning: wait one second.
	*/
	private boolean wrString(StringBuffer s) {

		int i, j, k, val;

		i = ser.txFreeCnt();
		j = s.length();
		if (j>i) return false;
Dbg.wr('\'');
		for (i=0; i<j; ++i) {
			val = s.charAt(i);
			if (val=='|') {
				waitSec(2);			// for shure if send buffer is full
			} else {
				ser.wr(val);
Dbg.wr(val);
			}
		}
Dbg.wr('\'');
		return true;
	}

	private boolean sendWait(String snd, String rcv, int timeout) {

		strBuf.setLength(0);
		strBuf.append(snd);
		return sendWait(strBuf, rcv, timeout);
	}
	
	/**
	*	send a string and loop until rcv string arrives or it times out
	*	timeout in seconds for receive string, is also used for send (if handshake lines are not set)
	*	return false means timeout.
	*/
	private boolean sendWait(StringBuffer snd, String rcv, int timeout) {

		//
		//	send string
		//
		int timer = Timer.getTimeoutSec(timeout);

		while (!wrString(snd)) {
			waitForNextPeriod();			// wait till send buffer is free
			if (Timer.timeout(timer)) return false;	// timeout on send means problem with handshake lines
		}
		
// return true; // for test without a modem


		if (rcv==null || rcv.length()==0) {
			return true; 			// no wait string, we're done
		}
		int ptr = 0;
		int len = rcv.length();
		//
		//	now wait on response string
		//
		timer = Timer.getTimeoutSec(timeout);
		while (!Timer.timeout(timer)) {

			waitForNextPeriod();
			for (int i = ser.rxCnt(); i>0; --i) {
				int val = ser.rd();
Dbg.wr(val);
				if (val == rcv.charAt(ptr)) {
					++ptr;
					if (ptr==len) {
						return true;			// we're done
					}
				} else {
					ptr = 0;					// reset match pointer
				}
			}
		}
Dbg.wr('?');
Dbg.wr('\n');

		return false;							// timeout expired

	}

	private void readWait(int timeout) {

		int timer = Timer.getTimeoutSec(timeout);
		while (!Timer.timeout(timer)) {
			waitForNextPeriod();
			for (int i = ser.rxCnt(); i>0; --i) {
				int val = ser.rd();
Dbg.wr(val);
			}
		}
	}

	/**
	 * @return
	 */
	public boolean isFwpDone() {
		return fwpDone;
	}

	/**
	 * @return
	 */
	public int getState() {
		return state;
	}

}
