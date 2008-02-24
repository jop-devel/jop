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
*	Zentrale Steuerung.
*	TODO: Automatik ausbauen.
*/

public class Zentrale {

	public static final int VER_MAJ = 0;
	public static final int VER_MIN = 9;


	private static int[] kflver;

public static boolean auto;		// fuer Testbetrieb
private static int autoTimer;	// next Time for automatic
private static final int AUTO_TIME = 8*60;

	public static void main(String[] args) {

		Timer.init();
		Timer.wd();

// wait to make shure that no wd reset apears and 
// make diplay dark again
Timer.sleepWd(1000);	// only for development with dl from MAX+PLUS

		Display.init();
		Keyboard.init();

		Flash.init();
		Log.init();
		Texte.init();
		Temp.init();
		Timer.wd();
//		Menu.init();		net notwendig
		Msg.init(0);
		Clock.init();
		Timer.wd();

		chkNot();			// zuerst auf Notaus ueberpruefen


auto = false;
autoTimer = 0;

Timer.start();
Menu.setDate();

		init();

		Timer.start();

		Station.init();

		Station.chkMS();

		Display.line1(kflver);
		Display.line2(Texte.bereit);

		forever();
	}



	private static void init() {

		int[] str =  {'K', 'F', 'L', ' ',
					'V', '0'+VER_MAJ, '.', '0'+VER_MIN};

		kflver = str;

		Display.line1(kflver);
		Display.line2(Texte.empty);

	}



	public static void chkNot() {

		if ((JopSys.rd(BBSys.IO_TAST)&BBSys.BIT_TNOT) != 0) {
			JopSys.wr(0, BBSys.IO_TRIAC);	// Schuetz off
			Display.line1(Texte.notaus);
			Log.write(Log.NOTSTOP);
			for (;;) {						// do nothing after Not Stop
				Timer.wd();
			}
		}
	}


	private static void forever() {

		int blinkCnt = 0;
		int val;
		int[] buf = new int[20];

		int old_tast = 0;

		for (;;) {


			Keyboard.loop();

			if (Keyboard.pressed) {
				if (Keyboard.rd()==Keyboard.B) {
					Menu.doit();
					Display.line1(kflver);
					Display.line2(Texte.bereit);
					if (autoTimer<Clock.getSec()) {		// a little delay after Menu
						autoTimer = Clock.getSec()+3;
					}
				}
			}

			chkNot();


			// automatischer Testbetrieb
			if (auto) {

				if (autoTimer<Clock.getSec()) {
					if (Station.isUp()) {
						Station.down();
					} else if (Station.isDown()) {
						Station.up();
					}
					autoTimer = Clock.getSec()+AUTO_TIME;
				}

			// 'manueller' Betrieb
			} else {

				val = JopSys.rd(BBSys.IO_TAST);
				if (val == old_tast) {
					if ((val&BBSys.BIT_TAUF)!=0 && Station.upOk()) {
						Station.up();
						autoTimer = Clock.getSec()+AUTO_TIME;
					} else if ((val&BBSys.BIT_TAB)!=0 && Station.downOk()) {
						Station.down();
						autoTimer = Clock.getSec()+AUTO_TIME;
					}
				} else {
					old_tast = val;
				}
				autoTimer = Clock.getSec();

			}

			if (blinkCnt==100) {
				Timer.wd();
				blinkCnt = 0;
			}
			++blinkCnt;

			if (Clock.loop()) {
				if (auto) {
					Clock.getDate(buf);
					Display.line2(buf);
					Display.line1(Texte.automatik);
				}
			}

			Timer.waitForNextInterval();
		}
	}
}
