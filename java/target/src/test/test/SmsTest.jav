/**
*	SmsTest.java
*/

package test;

import com.jopdesign.sys.*;
import util.*;
import sms.*;

public class SmsTest {

	private static int inVal;
	public static int outVal;			// share this with Htlm
	private static int a1Val;
	private static int a2Val;

	private static int[] txtHelp;
	private static int[] txtStatus;
	private static int[] txtIn;
	private static int[] txtOut;
	private static int[] txtA1;
	private static int[] txtA2;
	private static int[] txtInfoOn;
	private static int[] txtInfoOff;

	private static int[] txtBuf;

	private static int[] regNr;
	private static int regNrType;
	private static boolean isReg;

	private static boolean confOut;
	private static final int CONF_FIRST_TIME = 2*60;	// first retry in seconds
	private static int confTimeout;
	private static int confTimer;

	private static int[] gotNr;
	private static int gotNrType;


	private static int timer;
	private static int sec;
	private static int oldIn;

	private static int append(int[] d, int[] s) {

		int i, j;
		for (i=0; d[i]!=0; ++i) {
			;
		}
		for (j=0; j<s.length; ++j) {
			if (i>=d.length-1) break;
			d[i] = s[j];
			++i;
		}
		d[i] = 0;
		return i;
	}

	private static void setHelp() {

		int[] s1 = {'H','e','l','l','o',' ','f','r','o','m',' ','T','A','L','!','\n',};
		int[] s2 = {'c','o','m','m','a','n','d','s',':','\n',};
		int[] s3 = {'q',' ','-',' ','s','t','a','t','u','s','\n',};
		int[] s4 = {'s','2',' ','-',' ','s','e','t',' ','o','u','t','2','\n',};

		txtHelp[0] = 0;
		append(txtHelp, s1);
		append(txtHelp, s2);
		append(txtHelp, s3);
		append(txtHelp, s4);

		setHelp2();
	}

	private static void setHelp2() {

		int[] s5 = {'r','2',' ','-',' ','r','e','s','e','t',' ','o','u','t','2','\n',};
		int[] s6 = {'e',' ','-',' ','e','n','a','b','l','e',' ','i','n','f','o','\n',};
		int[] s7 = {'d',' ','-',' ','d','i','s','a','b','l','e',' ','i','n','f','o','\n',};
		int[] s8 = {'c',' ','-',' ','c','o','n','f','i','r','m',' ','i','n','f','o','\n',};
		append(txtHelp, s5);
		append(txtHelp, s6);
		append(txtHelp, s7);
		append(txtHelp, s8);
	}

	private static void setTxt() {

		int[] s1 = {'T','A','L',' ','S','t','a','t','u','s','\n',};
		int[] s2 = {'i','n',' ',' ',};
		int[] s3 = {'o','u','t',' ',};
		int[] s4 = {'a','1',' ',' ',};
		int[] s5 = {'a','2',' ',' ',};
		int[] s6 = {'a','u','t','o',' ','i','n','f','o',' ','o','n',};
		int[] s7 = {'a','u','t','o',' ','i','n','f','o',' ','o','f','f',};

		txtStatus = s1;
		txtIn = s2;
		txtOut = s3;
		txtA1 = s4;
		txtA2 = s5;
		txtInfoOn = s6;
		txtInfoOff = s7;

		setHelp();
	}

	private static void doIo() {

		int i = Native.rd(Native.IO_ADC);
		a1Val = (i>>>16);
		i &= 0xffff;
		a2Val = i;
		inVal = Native.rd(Native.IO_INOUT);
	}

	private static void status(int[] nr, int nrType) {

		int i, j, pos;

		txtBuf[0] = 0;

		doIo();							// read IO values

		pos = append(txtBuf, txtStatus);
		pos = append(txtBuf, txtIn);
		for (i=0; i<8; ++i) {
			j = ((inVal) & (1<<i)) != 0 ? '0' : '1';
			txtBuf[pos++] = j;
		}
		txtBuf[pos++] = '\n';
		txtBuf[pos++] = 0;
		pos = append(txtBuf, txtOut);
		for (i=0; i<4; ++i) {
			j = ((outVal) & (1<<i)) != 0 ? '1' : '0';
			txtBuf[pos++] = j;
		}
		txtBuf[pos++] = '\n';
		txtBuf[pos++] = 0;
		pos = append(txtBuf, txtA1);

/*
		txtBuf[pos++] = '0'+a1Val/10;
		txtBuf[pos++] = '0'+a1Val%10;
		txtBuf[pos++] = '\n';
		txtBuf[pos++] = 0;
*/
		i = a1Val+100;
		i /= 201;
		txtBuf[pos+3] = '0'+i%10;
		txtBuf[pos+2] = '.';
		i /= 10;
		txtBuf[pos+1] = '0'+i%10;
		txtBuf[pos] = '0'+i/10;
		pos += 4;
		txtBuf[pos++] = '\n';
		txtBuf[pos++] = 0;

		pos = append(txtBuf, txtA2);
/*
		txtBuf[pos++] = '0'+a2Val/10;
		txtBuf[pos++] = '0'+a2Val%10;
		txtBuf[pos++] = '\n';
		txtBuf[pos++] = 0;
*/
		i = a2Val+100;
		i /= 201;
		txtBuf[pos+3] = '0'+i%10;
		txtBuf[pos+2] = '.';
		i /= 10;
		txtBuf[pos+1] = '0'+i%10;
		txtBuf[pos] = '0'+i/10;
		pos += 4;
		txtBuf[pos++] = '\n';
		txtBuf[pos++] = 0;


		if (isReg) {
			append(txtBuf, txtInfoOn);
		} else {
			append(txtBuf, txtInfoOff);
		}

		if (Sms.isFree()) {
//			System.out.println("send replay");
			Sms.send(txtBuf, nr, nrType);
		} else {
//			System.out.println("send buffer full!");
		}

/*
		for (i=0; i<txtBuf.length; ++i) {
			j = txtBuf[i];
			if (j==0) break;
			System.out.print((char) j);
		}
		System.out.println(i);
*/
	}


	private static void doCmd(int[] s) {

		int i, j, cmd;
		cmd = s[0];
		if (cmd=='q' || cmd=='Q') {		
			status(gotNr, gotNrType);
		} else if (cmd=='s' || cmd=='S') {		
			i = s[1]-'1';
			if (i>=0 && i<=3) outVal |= 1<<i;
			Native.wr(outVal, Native.IO_INOUT);
			status(gotNr, gotNrType);
		} else if (cmd=='r' || cmd=='R') {		
			i = s[1]-'1';
			if (i>=0 && i<=3) outVal &= 0xfffffff7>>(3-i);
			Native.wr(outVal, Native.IO_INOUT);
			status(gotNr, gotNrType);
		} else if (cmd=='e' || cmd=='E') {		
			Sms.strcpy(regNr, Sms.rcvNr);
			regNrType = Sms.rcvNrType;
			isReg = true;
			status(gotNr, gotNrType);
		} else if (cmd=='d' || cmd=='D') {		
			isReg = false;
			confOut = false;
			status(gotNr, gotNrType);
		} else if (cmd=='c' || cmd=='C') {		
			// TODO: compare number
Dbg.wr('\n');
Dbg.wr('c');
			confOut = false;
// } else if (Sms.rcvTxt[0]=='A') {				// should only be else! is for my test with same handy
		} else {
			if (Sms.isFree()) {
//				System.out.println("send replay");
				Sms.send(txtHelp, gotNr, gotNrType);
			} else {
//				System.out.println("send buffer full!");
			}
		}
	}


	private static void init() {


		txtHelp = new int[Sms.SMS_MAX];
		txtBuf = new int[Sms.SMS_MAX];
		setTxt();
		regNr = new int[Sms.NR_MAX];
		isReg = false;

		confOut = false;
		confTimeout = 0;
		confTimer = 0;

		gotNr = new int[Sms.NR_MAX];

		inVal = 0;
		outVal = 0;
		a1Val = 0;
		a2Val = 0;

		timer = Timer.getNextCnt(1000);
		sec = 0;
		oldIn = inVal;

		Sms.init();
	}

	public static void loop() {

		Sms.loop();
//
//	handle received SMS
//
		if (Sms.gotSms) {

			print();

			Sms.strcpy(gotNr, Sms.rcvNr);
			gotNrType = Sms.rcvNrType;

			doCmd(Sms.rcvTxt);
Sms.rcvTxt[0] = '*';
Sms.rcvTxt[1] = 0;

			Sms.gotSms = false;
		}

		if (Timer.timeout(timer)) {
//			timer = Timer.getNextCnt(timer, 1000);
timer = Timer.getNextCnt(1000);
			++sec;

//
//	this is the second loop part!
//
			inVal = Native.rd(Native.IO_INOUT);	// read in val to see if something changed
			if (oldIn!=inVal) {				// something has changed!

				if (isReg) {
					confOut = true;			// wait on confirm
					confTimeout = CONF_FIRST_TIME;
					confTimer = sec + confTimeout;

					if (Sms.isFree()) {
						status(regNr, regNrType);
					}
				}

				oldIn = inVal;
			}
//
//	resend if waiting on confirm
//
			if (isReg && confOut && (sec-confTimer > 0)) {

Dbg.wr('\n');
Dbg.intVal(confTimeout);
Dbg.wr('r');
				if (Sms.isFree()) {
					status(regNr, regNrType);
					confTimeout <<= 1;			// exponential backoff
				}
				confTimer = sec + confTimeout;
				if (confTimeout > 24*3600) {	// forget it after one day
					confOut = false;
				}
					
			}

		}
	}

/**
*	Test main.
*/
	public static void main(String[] args) {

		int i, j;
		int max = 0;
		int timer = 10;

		Timer.init(20000000, 5);
		Dbg.init();

		init();
		tcpip.Eth.init();
		for (;;) {
			i = Timer.cnt();			// measure max time!
Serial.loop();
			loop();
Serial.loop();
			tcpip.Eth.loop();
Serial.loop();
			j = Timer.cnt();

			j -= i;
			if (j>max) max = j;

if (sec-timer > 0) {
	Dbg.wr(':');
	Dbg.intVal(max/2000);
	Dbg.wr(':');
	timer = sec+3;
	max = 0;
}
		}
	}

	private static void print() {

		int i, j;

		Dbg.wr('\n');
		Dbg.wr('s');
		Dbg.wr('m');
		Dbg.wr('s');
		Dbg.wr(':');
		for (i=0; i<Sms.rcvNr.length; ++i) {
			j = Sms.rcvNr[i];
			if (j==0) break;
			Dbg.wr(j);
		}
		Dbg.wr('\n');
		for (i=0; i<Sms.rcvTxt.length; ++i) {
			j = Sms.rcvTxt[i];
			if (j==0) break;
			Dbg.wr(j);
		}
		Dbg.wr('\n');
/*
		System.out.println();
		System.out.print("got SMS:");
		for (i=0; i<Sms.rcvTxt.length; ++i) {
			j = Sms.rcvTxt[i];
			if (j==0) break;
			System.out.print((char) j);
		}
		System.out.println();
		System.out.print("from ");
		for (i=0; i<Sms.rcvNr.length; ++i) {
			j = Sms.rcvNr[i];
			if (j==0) break;
			System.out.print((char) j);
		}
		System.out.print(" type: "+Sms.rcvNrType);
		System.out.println();

*/
	}
}
