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
*	Send.java
*/

package sms;

import joprt.RtThread;
import util.Serial;

/**
*	Send functions for SMS.
*	@author  <a href="mailto:martin.schoeberl@chello.at">Martin Schoeberl</a>
*	@since   07/2002
*/

class Send {

/** PDU message in 'hex' int's. */
	private static int[] hexBuf;
/** length of message in hexBuf. */
	private static int hexLen;
/** cmd or msg in 'plain' ascii. */
	private static int[] buf;
/** Length of data in sender buffer */
	private static int sndLen;
/** Pointer in sender buffer */
	private static int sent;

/**
*	Must be called befor any other function or field is accessed.
*/
	static void init() {

		hexBuf = new int[Sms.SMS_MAX+Sms.NR_MAX+20];
		buf = new int[2*Sms.SMS_MAX+2*Sms.NR_MAX+20];
		sndLen = sent = 0;
	}
/**
*	This is the main loop!
*/
	static void loop() {

		if (sent<sndLen) {
			if (Sms.ser.txFreeCnt()>0) {
if (sent==0) util.Dbg.wr('s');
if (buf[sent]=='\r') util.Dbg.wr('c');
else util.Dbg.wr(buf[sent]);

RtThread.sleepMs(10);
/*
if (sent==0) System.out.print("send:");
System.out.print((char) buf[sent]);
if (buf[sent]=='\r') System.out.println();
*/
				Sms.ser.wr(buf[sent]);
				++sent;
			}
		} else if (sent==sndLen) {
			sent = sndLen = 0;				// all sent, buffer is free
		}
	}

/**
*	Send an ascii string (for modem control).
*/
	static void send(int[] s) {

		sndLen = Sms.strcpy(buf, s);
		sent = 0;					// should be redundant, but who knows
	}

/**
*	Send an ascii string with parameter (for modem control).
*/
	static void send(int[] s, int val) {

		sndLen = Sms.strcpy(buf, s);
		if (val>99) buf[sndLen++] = val/100+'0';
		if (val>9) buf[sndLen++] = val%100/10+'0';
		buf[sndLen++] = val%10+'0';
		buf[sndLen++] = '\r';
		sent = 0;					// should be redundant, but who knows
	}

/**
*	Send a PDU message.
*		Reads data from Sms, converts it and send.
*/
	static void sendPDU() {

		int pos = 0;
		for (int j=0; j<hexLen; ++j) {
			setHex(buf, pos, hexBuf[j]);
			pos += 2;
		}
		buf[pos++] = 26;		// Ctrl-Z
		sndLen = pos;
		sent = 0;					// should be redundant, but who knows

	}


/**
*	Generate the PDU and return length for CMGS.
*	Writes in hexBuf.
*/
	static int genPDU() {

		int i, j, k, pos;
		int len = Sms.sndTxtLen;

		hexBuf[0] = 0x00;					// use SMSC stored in phone
		hexBuf[1] = 0x11;					// first octet of SMS-SUBMIT
		hexBuf[2] = 0x00;					// msg. ref (let phone select)
		hexBuf[3] = Sms.sndNrLen;			// len (in nibbles) of dest. number
		hexBuf[4] = Sms.sndNrType;			// 0x91 for intl. numbers (43...)

		i = 0;
		for (pos = 5; i<Sms.sndNrLen; ++pos) {
			j = Sms.sndNr[i]-'0';
			++i;
			if (i<Sms.sndNrLen) {
				j |= (Sms.sndNr[i]-'0')<<4;
			} else {
				j |= 0xf0;
			}
			++i;
			hexBuf[pos] = j;
		}
			
		hexBuf[pos++] = 0x00;				// TP-PID
		hexBuf[pos++] = 0x00;				// TP-DCS
		hexBuf[pos++] = 0xa9;				// TP-Validity-Period

		hexBuf[pos++] = len;

		int cnt = 0;
		for (j=0; j<len; ++j) {

			k = (Sms.sndTxt[j]&0x7f)>>cnt;
			if (j+1 < len) {
				k |= (Sms.sndTxt[j+1]&0x7f)<<(7-cnt);
			}
			hexBuf[pos] = k&0xff;

			++cnt;
			if (cnt==8) {
				cnt = 0;
			} else {
				++pos;
			}
		}

		hexLen = pos;

		return pos;
	}


	private static void setHex(int[] s, int pos, int val) {

		int i = val >> 4;
		if (i<10) { i += '0'; } else { i = i-10+'A'; }
		s[pos] = i;
		i = val & 0x0f;
		if (i<10) { i += '0'; } else { i = i-10+'A'; }
		s[pos+1] = i;
	}

}
