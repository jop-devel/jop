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
*	Texte fuer KFL.
*/

public class Texte {

	private static final int ERR_OFFSET = 60;	// start for error messages
	private static final int LANG_OFFSET = 90;	// start for next language

	public static final int  ae = 0xe1;
	public static final int  oe = 0xef;
	public static final int  ue = 0xf5;
	public static final int  Ae = 0x80;
	public static final int  Oe = 0x86;
	public static final int  Ue = 0x8a;

	private static int[] errBuf;

	public static int[] echo1;
	public static int[] echo2;

	public static int[] goesUp;
	public static int[] isUp;
	public static int[] goesDown;
	public static int[] isDown;

	public static int[] stop;
	public static int[] error;

	public static int[] download;
	public static int[] empty;

	public static int[] notaus;
	public static int[] automatik;

	public static int[] ein;
	public static int[] aus;

	public static int[] bereit;
	public static int[] clock;

	public static int[] aktiv;
	public static int[] menu;

	public static int[] mserr;
	public static int[] chkms;

	public static int[] errMode;
	public static int[] startNew;

	public static int[] comTest;
	public static int[] comErr;

	public static int[] anzahl;
	public static int[] justageUnten;
	public static int[] reboot;
	public static int[] notjust;
	public static int[] secval;
	public static int[] posein;
	public static int[] best;
	public static int[] serviceUp;

	public static int[] sukl;
	public static int[] sugr;
	public static int[] sokl;
	public static int[] sogr;
	public static int[] mast;
	public static int[] notdown;

	public static int[] justageOben;
	public static int[] mastNr;
	public static int[] serviceDown;
	public static int[] notup;
	public static int[] waitMast;
	public static int[] selMast;
	public static int[] pin;

	public static int[] deutsch;
	public static int[] englisch;
	public static int[] franz;
	public static int[] sprache;
	public static int[] logbuch;

	public static int[] msshort;
	public static int[] newms;
	public static int[] backZs;
	public static int[] restZs;

	public static void init() {

		int[] s1 =  {' '};
		empty = s1;

		errBuf = new int[20];

		Timer.wd();
		init1();
		Timer.wd();
		init2();
		Timer.wd();
		init3();
		Timer.wd();
	}

	private static void init1() {

		echo1 = readText(1);
		echo2 = readText(2);
		goesUp = readText(3);
		isUp = readText(4);
		goesDown = readText(5);
		isDown = readText(6);
		stop = readText(7);
		error = readText(8);
		download = readText(9);
		notaus = readText(10);
		automatik = readText(11);
		ein = readText(12);
		aus = readText(13);
		bereit = readText(14);
		clock = readText(15);
		aktiv = readText(16);
		menu = readText(17);
		mserr = readText(18);
		chkms = readText(19);
		errMode = readText(20);
	}

	private static void init2() {

		startNew = readText(21);
		comTest = readText(22);
		comErr = readText(23);
		anzahl = readText(24);
		justageUnten = readText(25);
		reboot = readText(26);
		notjust = readText(27);
		secval = readText(28);
		posein = readText(29);
		best = readText(30);
		serviceUp = readText(31);
		sukl = readText(32);
		sugr = readText(33);
		sokl = readText(34);
		sogr = readText(35);
		mast = readText(36);
		notdown = readText(37);
		justageOben = readText(38);
		mastNr = readText(39);
		serviceDown = readText(40);
	}

	private static void init3() {

		notup = readText(41);
		waitMast = readText(42);
		selMast = readText(43);
		pin = readText(44);
		deutsch = readText(45);
		englisch = readText(46);
		sprache = readText(47);
		logbuch = readText(48);
		msshort = readText(49);
		newms = readText(50);
		backZs = readText(51);
		restZs = readText(52);
	}

	private static int[] readText(int nr) {

		int buf[];

		
		nr += Config.getLang()*LANG_OFFSET;
		int addr = Flash.TEXT_START + (nr<<5);		// 32 byte per text entry in flash
		int len = JopSys.rdMem(addr);

		if (len==0 || len>20) {
			buf = new int[8];
			buf[0] = 'T';
			buf[1] = 'e';
			buf[2] = 'x';
			buf[3] = 't';
			buf[4] = ' ';
			buf[5] = '0'+nr/100;
			nr %= 100;
			buf[6] = '0'+nr/10;
			buf[7] = '0'+nr%10;
		} else {
			buf = new int[len];
			for (int i=0; i<len; ++i) {
				buf[i] = JopSys.rdMem(addr+1+i);
			}
		}

		return buf;
	}

	public static int[] errTxt(int nr) {

		int i;

		for (i=0; i<20; ++i) {
			errBuf[i] = ' ';
		}

		nr += Config.getLang()*LANG_OFFSET;
		int addr = Flash.TEXT_START + ((nr+ERR_OFFSET)<<5);		// 32 byte per text entry in flash
		int len = JopSys.rdMem(addr);

		if (len==0 || len>20) {
			errBuf[0] = 'E';
			errBuf[1] = 'r';
			errBuf[2] = 'r';
			errBuf[3] = ' ';
			errBuf[4] = '0'+nr/100;
			nr %= 100;
			errBuf[5] = '0'+nr/10;
			errBuf[6] = '0'+nr%10;
		} else {
			for (i=0; i<len; ++i) {
				errBuf[i] = JopSys.rdMem(addr+1+i);
			}
		}

		return errBuf;
	}
}
