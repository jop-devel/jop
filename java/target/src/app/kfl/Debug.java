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
*	Downloadfunktion fuer Zentrale
*/

public class Debug {

	private static final int IO_UART = 2;
	private static final int IO_RS485 = 15;
	private static final int IO_STATUS = 1;

	public static void echo() {

		int val;

		Display.line1(Texte.echo1);
		Display.line2(Texte.echo2);
		Station.powerOn();

		for (;;) {

			Clock.loop();
			Keyboard.loop();

			if (Keyboard.pressed) {
				val = Keyboard.rd();
				if (val==Keyboard.C) {
					Station.powerOff();
					return;
				}
			}

			if ((JopSys.rd(IO_STATUS)&2)!=0) JopSys.wr(JopSys.rd(IO_UART), IO_RS485);
			if ((JopSys.rd(IO_STATUS)&32)!=0) JopSys.wr(JopSys.rd(IO_RS485), IO_UART);

			Timer.wd();
		}
	}


	private static void handleMsg() {

		int data;

		int val = Msg.readCmd();
		if (val==BBSys.CMD_STATUS) {
			Msg.write(0);
		} else if (val==BBSys.CMD_FL_PAGE) {
			data = Msg.readData();
			Flash.setPage(data);
			Msg.write(0);
		} else if (val==BBSys.CMD_FL_DATA) {
			data = Msg.readData();
			Flash.setData(data);
			Msg.write(0);
		} else if (val==BBSys.CMD_FL_PROG) {
			Flash.program();
			Msg.write(0);
		} else if (val==BBSys.CMD_FL_READ) {
			Msg.write(Flash.read());
		} else if (val==BBSys.CMD_RESET) {
			Msg.write(0);
			for(;;)
				;				// wait for WD

		} else if (val==BBSys.CMD_VERSION) {
			Msg.write((Zentrale.VER_MAJ<<6)+Zentrale.VER_MIN);
		}
	}


/**
*	main download loop.
*/
	public static void download() {

		Display.line1(Texte.download);
		Display.line2(Texte.aktiv);

		int blinkCnt = 0;

		for (;;) {

			Clock.loop();
			Keyboard.loop();

			if (Keyboard.pressed) {
				if (Keyboard.rd()==Keyboard.C) {
					return;
				}
			}

			Msg.loop();
			if (Msg.available) {
				handleMsg();
			}

			if (blinkCnt==100) {
				Timer.wd();
				blinkCnt = 0;
			}
			++blinkCnt;

//			Timer.waitForNextInterval();		net wirklich notwendig! => faster download ?
		}
	}

	public final static int TESTCNT = 50;

	public static void testStatus() {

// zum Test der Bootproblematik!!
		for (;;) {
			Station.chkMS();
			Display.cls();
			Timer.sleepWd(5000);
		}
	}

private static void reseved() {

		int i, j, t;

		Display.line1(Texte.empty);
		Display.line2(Texte.empty);
		int min = 9999999;
		int max = 0;
		int err = 0;
		int cnt = 0;
		int stCnt = Station.getCnt();

		Station.powerOn();

		for (;;) {

			for (i=0;i<TESTCNT;++i) {
				t = JopSys.rd(JopSys.IO_CNT);
				for (j=1; j<=stCnt; ++j) {
JopSys.wr(-1, BBSys.IO_EXP);
					if (Msg.exchg(j, BBSys.CMD_STATUS, 0)<0) ++err;
JopSys.wr(0, BBSys.IO_EXP);
				}
				t = JopSys.rd(JopSys.IO_CNT)-t;


				if (t<min) min = t;
				if (t>max) max = t;

				JopSys.wr(0, BBSys.IO_WD);		// no time for function call
				JopSys.wr(1, BBSys.IO_WD);
				for (int k=0; k<(i&0xf); ++k) Timer.sleep(1);	// don't sync on Mast cycles
			}

			cnt += TESTCNT;
			Display.cls();
			Display.intVal(min/JopSys.MS);
			Display.data('.');
			Display.intVal((min*10/JopSys.MS)%10);
			Display.data(' ');
			Display.intVal(cnt);
			Display.line2();
			Display.intVal(max/JopSys.MS);
			Display.data('.');
			Display.intVal((max*10/JopSys.MS)%10);
			Display.data(' ');
			Display.intVal(err);
			min = 9999999;
			max = 0;
		}
	}
}
