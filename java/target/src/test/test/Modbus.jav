package test;

/**
*	Test Modbus.
*/

import com.jopdesign.sys.*;
import util.*;

class Modbus {

	private static final int BUF_MAX = 530;
/** response or msg in 'plain' ascii. */
	static int[] asciiBuf;
	static int[] buf;
/** Pointer in receiver asciiBuffer */
	private static int rcvd;

	// public static int outVal;  use test.SmsTest.outVal!!!

	public static void main( String s[] ) {


		asciiBuf = new int[BUF_MAX];
		buf = new int[BUF_MAX/2];
		rcvd = 0;
		test.SmsTest.outVal = 0;
		Serial.init(2);
		Timer.init(20000000, 10);
		tcpip.Eth.init();

		loop();
	}


	private static void loop() {

		for (;;) {

			Serial.loop();

			while (!Serial.rxEmpty() && rcvd<BUF_MAX) {
				asciiBuf[rcvd] = Serial.rd();
				rcvd++;
				if (asciiBuf[rcvd-1]==0x0a) break;
				Serial.loop();
			}

			tcpip.Eth.loop();
			Serial.loop();

			if (rcvd==BUF_MAX) rcvd = 0;						// flush on full asciiBuffer

			if (rcvd!=0 && asciiBuf[rcvd-1]==0x0a) {
				doMsg();
				rcvd = 0;
			}

			Timer.wd();
		}

	}

// TODO: parity, timeout
	private static void doMsg() {

		int pos, i, j, k;


		if (asciiBuf[0] != (int) ':') return;

		pos = 1;
		for (i=0; asciiBuf[pos]!='\r' && asciiBuf[pos]!='\n' && pos<rcvd-1; ++i) {
			j = readHex(asciiBuf, pos);
			if (j<0) return;			// somthing really wrong!
			buf[i] = j;
			pos += 2;
		}

		j = buf[1];		// cmd

		int len = 5;
		if (j==0x02) {				// Read Input Status
			i = Native.rd(Native.IO_INOUT);
			buf[2] = 0x02;				// two bytes
			buf[3] = i&0xff;
			buf[4] = i>>8;
		} else if (j==0x04) {		// Read Input Registers
			i = Native.rd(Native.IO_ADC);
			if (buf[3]==0) {
				i >>>= 16;
			} else {
				i &= 0xffff;
			}
			buf[2] = 0x02;				// two bytes
			buf[4] = i&0xff;		// high byte first
			buf[3] = i>>8;

		} else if (j==0x05) {		// Force Single Coil
			
			len = 6;
			k = buf[3];
			k = 1<<k;
			if (buf[4] == 0xff) {
				test.SmsTest.outVal |= k;
			} else {
				test.SmsTest.outVal &= ~k;
			}
			Native.wr(test.SmsTest.outVal, Native.IO_INOUT);

		} else {					// the rest is zero
			buf[2] = 0x02;				// two bytes
			buf[3] = 0;
			buf[4] = 0;
		}

		j = 0;
		for (i=0; i<len; ++i) {
			j += buf[i];
		}
		buf[len] = (-j)&0xff;

		pos = 1;
		for (j=0; j<len+1; ++j) {
			setHex(asciiBuf, pos, buf[j]);
			pos += 2;
		}
		asciiBuf[pos++] = '\r';
		asciiBuf[pos++] = '\n';

		for (i=0; i<pos; ++i) {

			while (Serial.txFull()) {
				Serial.loop();
			}
			Serial.wr(asciiBuf[i]);
			Serial.loop();
		}
	}

	private static void setHex(int[] s, int pos, int val) {

		int i = val >> 4;
		if (i<10) { i += '0'; } else { i = i-10+'A'; }
		s[pos] = i;
		i = val & 0x0f;
		if (i<10) { i += '0'; } else { i = i-10+'A'; }
		s[pos+1] = i;
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
}
