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
*	Receive.java
*/

package sms;

/**
*	Receive functions for SMS.
*	@author  <a href="mailto:martin.schoeberl@chello.at">Martin Schoeberl</a>
*	@since   07/2002
*/

// class Receive {
public class Receive {

/** PDU message in 'hex' int's. */
	static int[] hexBuf;
/** response or msg in 'plain' ascii. */
	static int[] buf;
/** Pointer in receiver buffer */
	private static int rcvd;

/** signal 'OK' received to Sms. */
	static boolean okRcvd;
/** signal a message to be deleted to Sms. */
	static int delNr;

	private static final int BUF_MAX = 1000;

/** Receive timeout in seconds. */
	private static final int RCV_TIMEOUT = 2;
/** Timer for receiver */
	private static int timer;
/** State of receiver */
	private static int state;
	private static final int IDLE = 0;
	private static final int READ = 1;
	private static final int INTER = 2;

/** "+CMGL" */
	private static int[] cmgl;
	private static int[] ok;


/**
*	Must be called befor any other function or field is accessed.
*/
	static void init() {

		hexBuf = new int[Sms.SMS_MAX+Sms.NR_MAX+20];
		buf = new int[BUF_MAX+10];		// can be long!!!
		rcvd = 0;
		for (int i=0; i<BUF_MAX; ++i) buf[i] = ' ';	// for easier scan ?

		okRcvd = false;
		delNr = -1;
		timer = 0;			// some value
		state = IDLE;

		int[] s1 = {'+','C','M','G','L',':',' '};
		cmgl = s1;
		int[] s2 = {'O','K'};
		ok = s2;
	}
/**
*	This is the main loop!
*	Fast loop to read Serial buffer.
*/
	static void loop() {

		while (Sms.ser.rxCnt()>0 && rcvd<BUF_MAX) {
				buf[rcvd] = Sms.ser.rd();
				timer = Sms.sec + RCV_TIMEOUT;					// restart Timer 
if (rcvd==0) util.Dbg.wr('r');
if (buf[rcvd]=='\r') util.Dbg.wr('c');
else util.Dbg.wr(buf[rcvd]);
/*
if (rcvd==0) System.out.print("receive:");
System.out.print((char) buf[rcvd]);
if (buf[rcvd]=='\r') System.out.println();
*/
				rcvd++;
		}
	}


/**
*	This loop is called once every second too handle receive state machine.
*/
	static void loopSec() {

		if (state == IDLE) {

			if (rcvd != 0) state = READ;

		} else if (state == READ) {

			if (Sms.sec-timer >= 0) {
				state = INTER;
			}

		} else if (state == INTER) {

			interpret();
			state = IDLE;
		}
	}

	private static boolean strncmp(int[] s1, int[] s2, int pos, int len) {

		int i;
		for (i=0; i<s1.length && i+pos<len; ++i) {
			if (s2[pos+i] != s1[i]) break;
		}
		return (i==s1.length);
	}

	private static int nextLine(int[] s, int pos, int len) {

		for (; pos<len; ++pos) {
			if (s[pos]=='\r') break;
			if (s[pos]=='\n') break;
		}
		++pos;
		if (pos>=len) return -1;
		if (s[pos]=='\n') ++pos;
		if (pos>=len) return -1;

		return pos;
	}

	private static int readInt(int[] s, int pos) {

		int val;
		boolean found = false;

		for (val=0; s[pos]>='0' && s[pos]<='9'; ++pos) {
			found = true;
			val *= 10;
			val += s[pos]-'0';
		}
		if (!found) return -1;
		return val;
	}
	private static int overInt(int[] s, int pos) {

		for (; s[pos]>='0' && s[pos]<='9'; ++pos) {
			;
		}
		return pos;
	}

	private static int readHex(int[] s, int pos) {

		int i, j;

		i = s[pos];
		if (i>='0' && i<='9') { i -= '0'; }
		else if (i>='A' && i<='F') { i = i-'A'+10; }
		else return -1;

		j = i*16;
		i = s[pos+1];
		if (i>='0' && i<='9') { i -= '0'; }
		else if (i>='A' && i<='F') { i = i-'A'+10; }
		else return -1;
		return j+i;
	}

/*
+CMGL: 3,2,,12
07913496090091991100038121F30000A902CB30
+CMGL: 1,1,,67
07913496090091990407A1286697F900002070927183410839E8329BFD4697D9EC37880745BFE9EF39FA4D9F835AA0245A5E0631D365313BED3ECFC56936B92C0785EB66D0B4397505A93E

OK

0: received unread 
1: received read; 
2: stored unsent; 
3: stored sent; 
4: all;
*/

/**
*	Convert number to plain ascii.
*	@return	number type.
*/
	private static int getNumber(int[] s, int pos, int len, int[] d) {

		int i, j, k;

		int ret = s[pos];

		++pos;

		k = 0;
		for (i=0; i<len; ++i) {
			j = s[pos];
			d[k++] = (j & 0x0f) + '0';
			j >>>= 4;
			if (j!=0x0f) {
				d[k++] = j + '0';
			}
			++pos;
		}
		d[k] = 0;	// EOS

		return ret;
	}

/**
*	Convert strange coding to ascii.
*/
	private static void getText(int[] s, int pos, int len, int[] d) {

		int j, k;
		int cnt = 0;
		for (j=0; j<len; ++j) {
			k = s[pos]<<cnt;
			k |= s[pos-1]>>(8-cnt);
			d[j] = k & 0x7f;

			++cnt;
			if (cnt==8) {
				cnt = 0;
			} else {
				++pos;
			}
		}
		d[j] = 0;	// EOS
	}

/**
*	Read a PDU message from receive buffer and fill buffers in Sms.
*	Should be called only when Sms.gotSms is false;
*/
	private static void getMsg(int type, int[] s, int len) {

		if (type==2 || type==3) {	// only rcvd msg are interesting
			return;
		}

		int i;
		int pos = 0;

		i = s[pos]; ++pos;		// SMSC len
		if (i>Sms.NR_MAX) return;
		pos += i;
		++pos;					// some type

		i = s[pos]; ++pos;
		//
		// TODO store senders Number (and nr type) for replay
		// now len is in nibbles and without type field!!!
		//
		if (i>Sms.NR_MAX) return;
		Sms.rcvNrType = getNumber(s, pos, (i+1)>>1, Sms.rcvNr);

		pos += 1+((i+1)>>1);
		pos += 9;					// rcv msg.

// TODO: read TP-DCS to see if 7, 8 or 16 bit data!

		i = s[pos]; ++pos;				// msg len in 'character' !!!
		if (i>Sms.SMS_MAX) return;
		getText(s, pos, i, Sms.rcvTxt);

		Sms.gotSms = true;				// Now message is ready for upper level application.
	}


/**
*	Interprete received data. This function is called when the receive buffer
*	contains data but no more data arrived one the serial line for more than 
*	RCV_TIMEOUT seconds.
*	<p>interpret() has three functions:<br>
*		Empty buffer for unused echo...<br>
*		Signal 'OK' to Sms for modem handling.<br>
*		Read received SMS and mark them for delete.<br>
*
*/
	private static void interpret() {

		int i, j;
		int pos = 0;
		int msgNr, msgTyp;

		while (pos!=-1 && pos<BUF_MAX) {

//
//	handle CMGL messages:
//		only first msg will be copied to Sms.rcvTxt (in getMsg) and deleted.
//
			if (strncmp(cmgl, buf, pos, rcvd)) {

				pos += cmgl.length;
				msgNr = readInt(buf, pos); pos = overInt(buf, pos)+1;
				msgTyp = readInt(buf, pos);
				pos = nextLine(buf, pos, rcvd);
				if (pos==-1) break;

				for (i=0; buf[pos]!='\r' && buf[pos]!='\n' && pos<rcvd-1; ++i) {
					j = readHex(buf, pos);
					if (j<0)	break;			// somthing really wrong!
					hexBuf[i] = j;
					pos += 2;
				}

				if (Sms.gotSms) {
					break;						// buffer in Sms is not free -> forget it!
				}
				getMsg(msgTyp, hexBuf, i);
				delNr = msgNr;

				break;							// only one msg per call
			}

			if (strncmp(ok, buf, pos, rcvd)) {
				okRcvd = true;
				break;							// forget the rest on 'OK'
			}

			pos = nextLine(buf, pos, rcvd);
		}

//
//	clear rcv buffer
//
		for (i=0; i<BUF_MAX; ++i) buf[i] = ' ';
		rcvd = 0;	// empty buffer
	}


/*
	private static void printMsg(int type, int[] s, int len) {

		int i, j, k;

		int pos = 0;
		i = s[pos]; ++pos;
		if (i>0) {				// len in octets
			System.out.print("SMSC=");
//			printNumber(s, pos, i-1);
			System.out.println();
			pos += i;
		}
		++pos;					// some type
		if (type==2 || type==3) {		// send msg.
			++pos;
		}

		i = s[pos]; ++pos;
		if (type==2 || type==3) {
			System.out.print("Destination Number=");
		} else {
			System.out.print("Sender Number=");
		}
//		printNumber(s, pos, (i+1)/2);	// now len is in nibbles and without type field!!!
		System.out.println();
		pos += 1+(i+1)/2;

		if (type==2 || type==3) {
			pos += 3;					// send msg.
		} else {
			pos += 9;					// rcv msg.
		}

// TODO: read TP-DCS to see if 7, 8 or 16 bit data!

		i = s[pos]; ++pos;				// msg len in 'character' !!!
		System.out.println("msg len="+i);

		getText(s, pos, i, Sms.rcvTxt);

		System.out.print("msg='");
		for (j=0; j<i; ++j) {
			System.out.print((char) Sms.rcvTxt[j]);
		}
		Sms.rcvTxt[j] = 0;
		System.out.println("'");
		System.out.println();
	}
*/
}
