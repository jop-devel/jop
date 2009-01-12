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

/**
*	Sms.java
*/

package sms;

import joprt.RtThread;

import com.jopdesign.sys.Const;

import ejip.CS8900;
import ejip.Ejip;
import ejip.LinkLayer;
import ejip.Net;

import util.Dbg;
import util.Serial;
import util.Timer;

/**
*	Send a SMS via a mobile phone.
*
*	All functions are static and simple to be used with <a href="http://www.jopdesign.com/">Jop</a>.<br>
*
*	All 'strings' (buffer) are int arrays with C-type length (0).
*
*	@author	<a href="mailto:martin.schoeberl@chello.at">Martin Schoeberl</a>
*	@since	07/2002
*	@see	<a href="http://www.dreamfabric.com/sms/">SMS messages and the PDU format</a>
*	@see	<a href="http://www.fastlogic.co.za/faq59.htm">Hayes AT command parameters
*			for sending and receiving SMS messages</a>
*/

public class Sms {

	static Serial ser;
	
/** maximum length of 7 bit (default) messages */
	public static final int SMS_MAX = 160;
/** maximum length of a telefon number */
	public static final int NR_MAX = 20;

/** new message if ready for send */
	private static boolean sendIt;
/** 'plain' text of SMS to be sent */
	static int[] sndTxt;
/** length of msg in sndTxt. */
	static int sndTxtLen;
/** destination number of SMS to be sent */
	static int[] sndNr;
/** len of destination number */
	static int sndNrLen;
/** type of destination number */
	static int sndNrType;

/**
*	Is the sender free?
*/
	public static boolean isFree() { return !sendIt; }
/**
*	Send a message.
*	copies data and tries to deliver it.
*	No error on problems: Unreliable as the SMS service by itself.
*	End to End check on higher level (ack SMS from human).
*
*	@param	txt		Text of message.
*	@param	nr		Destination telefon number
*	@param	nrType	Type of number: 0x91 indicates international format
*/
	public static void send(int[] txt, int[] nr, int nrType) {

		if (sendIt) return;			// just drop it

Dbg.wr('\n');
Dbg.wr('s');
int i, j;
for (i=0; i<nr.length; ++i) {
	j = nr[i];
	if (j==0) break;
	Dbg.wr(j);
}
Dbg.wr(':');
for (i=0; i<txt.length; ++i) {
	j = txt[i];
	if (j==0) break;
	Dbg.wr(j);
}
Dbg.wr(':');
Dbg.wr('\n');

		sndTxtLen = strcpy(sndTxt, txt);
		sndNrLen = strcpy(sndNr, nr);
		sndNrType = nrType;
		sendIt = true;				// 'triggers' send event
	}

/**
*	Buffer is filled with a new SMS. This flag must be cleared (from the uper level)
*	after reading the message from the public buffers ({@link #rcvTxt}, {@link #rcvNr} and
*	{@link #rcvNrType}).
*/
	public static boolean gotSms;
/** 'plain' text of received SMS */
	public static int[] rcvTxt;
/** senders number of received SMS */
	public static int[] rcvNr;
/** type of senders number */
	public static int rcvNrType;

/**
*	Must be called befor any other function or field is accessed.
*/
	public static void init() {

		sndTxt = new int[SMS_MAX];
		sndNr = new int[NR_MAX];
		rcvTxt = new int[SMS_MAX];
		rcvNr = new int[NR_MAX];
		gotSms = false;
		sendIt = false;

		strInit();
		Receive.init();
		Send.init();



		timer = Timer.getTimeoutMs(WD_TIME);
		sec = 0;

		state = INIT;
		timEvent = STATE_TIME;
	}

	private static final int WD_TIME = 500;	// every half second
	private static int timer;				// ms Timer used to set sec
	private static int tick;				// half second tick
	static int sec;							// local clock for other timers (used in Receive)
/**
*	This is the main loop!
*/
	public static void loop() {

		Receive.loop();
		Send.loop();

//
//	half second timer
//		alternate between Receive and Sms second loop.
//
		if (Timer.timeout(timer)) {
			timer = Timer.getTimeoutMs(WD_TIME);
			++tick;
			if ((tick & 1)!=0) {		// one half
				loopSec();
			} else {
				Receive.loopSec();		// and the other
			}
		}
	}

/** timer for next Event ... state change or what else */
	private static int timEvent;
/** do the init sequence some time to be able a cange of mobile phone... */
	private static int timInit;
	private static final int INIT_TIME = 5*60;

/** timer to change state or check result */
	private static final int STATE_TIME = 10;
//	private static final int ERROR_TIME = 60;
private static final int ERROR_TIME = 10;
	private static final int PIN_TIME = 60;


/** master states */
	private static int state;
	private static final int INIT = 0;
	private static final int INIT0 = 1;
	private static final int INIT1 = 2;
	private static final int INIT2 = 3;
	private static final int INIT3 = 4;	
	private static final int NO_HANDY = 5;
	private static final int READY = 6;
	private static final int SEND_HEAD = 7;
	private static final int SEND_PDU = 8;

/**
*	This loop is called once every second too handle the state machine.
*/
	private static void loopSec() {

		Timer.wd();
		++sec;

//
//	time for state change
//
		if (sec-timEvent >= 0) {
			timEvent = sec + STATE_TIME;		// restart timer

			boolean ok = Receive.okRcvd;
			Receive.okRcvd = false;

			if (state==INIT) {
				Send.send(ath);
				state = INIT0;
			} else if (state==INIT0) {
				if (ok) {
					Send.send(cpin);
					// a longer time for the pin
					timEvent = sec + PIN_TIME;
					state = INIT1;
				} else {
					state = NO_HANDY;
					timEvent = sec + ERROR_TIME;
				}
			} else if (state==INIT1) {
				if (ok) {
					Send.send(cmgf);
					state = INIT2;
				} else {
					state = NO_HANDY;
					timEvent = sec + ERROR_TIME;
				}
				
			} else if (state==INIT2) {
				state = INIT3;
				if (ok) {
					Send.send(csms);
					state = INIT3;
				} else {
					state = NO_HANDY;
					timEvent = sec + ERROR_TIME;
				}
			} else if (state==INIT3) {
				if (ok) {
					state = READY;
					timInit = sec + INIT_TIME;	// next new init sequence
				} else {
					state = NO_HANDY;
					timEvent = sec + ERROR_TIME;
				}
			} else if (state==NO_HANDY) {
				state = INIT;					// now try again

//
//	now we are in the ready/poll... loop
//
			} else if (state==READY) {
				doReady();
//
//	we've sent CMGS, now send PDU and wait for OK
//
			} else if (state==SEND_HEAD) {
				Send.sendPDU();
				state = SEND_PDU;

//
//	PDU is sent, did we get OK?
//
			} else if (state==SEND_PDU) {
				if (ok) {
					Sms.sendIt = false;				// Sender is free again
					state = READY;
				} else {
					state = INIT;					// something wrong, start an init sequence
				}

			} else {
			}

		}

	}

/**
*	The READY state in its own funtion.
*/
	private static void doReady() {

		int i = Receive.delNr;
//
//	delete a msg
//
		if (i!=-1) {
			Send.send(cmgd, i);
			Receive.delNr = -1;
//
//	send a message
//
		} else if(sendIt) {
			i = Send.genPDU();
			Send.send(cmgs, i);
			state = SEND_HEAD;
//
//	restart (INIT)
//
		} else if (sec-timInit >= 0) {				// it's time to initialze again :-)
			state = INIT;
//
//	poll SMS list
//
		} else {
			Send.send(cmgl);				// poll list
		}
	}

/**
*	A C like string copy :-)
*	@return	resulting strlen.
*/
	public static int strcpy(int[] d, int[] s) {

		int i, max;
		i = d.length;
		max = s.length;
		if (i<max) max = i;
		for (i=0; i<max; ++i) {
			d[i] = s[i];
			if (d[i]==0) break;
		}
		if (i==max && i<d.length) d[i] = 0;

		return i;
	}

	private static int[] ath;
	private static int[] cmgf;
	private static int[] csms;
	private static int[] cmgs;
	private static int[] cmgl;
	private static int[] cmgd;
	private static int[] cpin;

	private static void strInit() {
//
//	some strings
//
		int[] s1 = {'A','T','H','\r'};
		ath = s1;
		int[] s2 = {'A','T','+','C','M','G','F','=','0','\r'};
		cmgf = s2;
		int[] s3 = {'A','T','+','C','S','M','S','=','0','\r'};
		csms = s3;
		int[] s4 = {'A','T','+','C','M','G','S','=',};
		cmgs = s4;
		int[] s5 = {'A','T','+','C','M','G','L','=','4','\r'};
		cmgl = s5;
		int[] s6 = {'A','T','+','C','M','G','D','=',};
		cmgd = s6;
//		int[] s7 = {'A','T','+','C','P','I','N','=','5','6','4','4','\r'};
		int[] s7 = {'A','T','+','C','P','I','N','=','9','1','7','4','\r'};
		cpin = s7;
	}


	static Net net;
	static LinkLayer ipLink;


/**
*	Test main.
*/
	public static void main(String[] args) {

		Dbg.init();	// that's the UDP version
		init();
		ser = new Serial(Const.IO_UART1_BASE);
		new RtThread(1, 10000) {
			public void run() {
				for (;;) {
					waitForNextPeriod();
					ser.loop();
				}
			}
		};

		//
		//	start TCP/IP
		//
		Ejip ejip = new Ejip();
		net = new Net(ejip);
// don't use CS8900 when simulating on PC or for BG263
		int[] eth = {0x00, 0xe0, 0x98, 0x33, 0xb0, 0xf8};
		int ip = Ejip.makeIp(192, 168, 0, 123); 
		ipLink = new CS8900(ejip, eth, ip);

		//
		//	start device driver threads
		//

		new RtThread(5, 10000) {
			public void run() {
				for (;;) {
					waitForNextPeriod();
					net.run();
				}
			}
		};
		new RtThread(5, 10000) {
			public void run() {
				for (;;) {
					waitForNextPeriod();
					ipLink.run();
				}
			}
		};

		RtThread.startMission();

		int i, j;
		int[] text = {'H','e','l','l','o',' ','f','r','o','m',' ','J','O','P','!'};
		int[] nr = {'4','3','6','9','9','1','9','5','2','0','2','2','0'};

		int[] gotNr = new int[Sms.NR_MAX];
		int gotNrType;

		int timer = Timer.getTimeoutMs(1000);
		int sec = 0;
		boolean doit = true;

		for (;;) {
			loop();
			if (Timer.timeout(timer)) {
				timer = Timer.getTimeoutMs(1000);
				++sec;
			}

			if (state==READY && doit) {
				Dbg.wr("ready to send");
				//	0x91 for intl. numbers (43...)
				Sms.send(text, nr, 0x91);
				doit = false;
			}
			if (gotSms) {
//				tim();
				Dbg.wr("got SMS:");
				for (i=0; i<rcvTxt.length; ++i) {
					j = rcvTxt[i];
					if (j==0) break;
					Dbg.wr((char) j);
				}
				Dbg.lf();
				Dbg.wr("from ");
				for (i=0; i<rcvNr.length; ++i) {
					j = rcvNr[i];
					if (j==0) break;
					Dbg.wr((char) j);
				}
				Dbg.wr(" type: "+Sms.rcvNrType);
				Dbg.lf();


				Sms.strcpy(gotNr, Sms.rcvNr);
				gotNrType = Sms.rcvNrType;
				if (Sms.isFree()) {
					Dbg.wr("send replay");
					Dbg.lf();
					Sms.send(text, gotNr, gotNrType);
				} else {
					Dbg.wr("send buffer full!");
					Dbg.lf();
				}

				Sms.gotSms = false;
			}
		}
	}

}
