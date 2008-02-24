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

package kfl.test;

import kfl.*;
/**
*	Test Main.
*/

public class Main {

	private static boolean auto;				// autom. Motortest
	private static int autoCnt;

	private static int maxTime;

	public static void main(String[] args) {

		Timer.init();			// wd
		maxTime = 0;
		Triac.init();
		Keyboard.init();
		Display.init();
		Msg.init(1);		// address
		init();

		Timer.start();

		forever();

	}

	private static void init() {

		int[] str =  {' ', 'K', 'F', 'L', ' ', 'T', 'e', 's', 't', ' ', 'V', ' ', '0', '.', '6'};
		int[] str2 = {' ', ' ', 'M', 'a', 's', 't', ' ', 'Z', 'e', 'n', 't', 'r', 'a', 'l', 'e'};

		for (int i=0; i<str.length; ++i) {
			Display.data(str[i]);
		}
		Display.line2();
		for (int i=0; i<str2.length; ++i) {
			Display.data(str2[i]);
		}
	}

	private static int in() {

		Display.line2();
		int val = (JopSys.rd(BBSys.IO_SENSOR)<<4) + JopSys.rd(BBSys.IO_TAST);
		int dir = Triac.getDir();
		if (dir==1) {
			val |= 1<<7;
		} else if (dir==-1) {
			val |= 3<<7;
		}
		val <<= 7;
		val |= Triac.getOpto();
		int ret = val;
		for (int i=0; i<16; ++i) {
			if ((val&0x01)==1) {
				Display.data('1');
			} else {
				Display.data('0');
			}
			val >>>= 1;
		}

		return ret;
	}
		

	static void handleMsg() {

		Msg.loop();
		if (Msg.available) {
			int val = Msg.readCmd();
			if (val==BBSys.CMD_STATUS) {
				int dir = Triac.getDir();
				if (dir==1) {
					Msg.write(1);
				} else if (dir==-1) {
					Msg.write(3);
				} else {
					Msg.write(0);
				}
			} else if (val==BBSys.CMD_UP) {
				Triac.rauf();
				Msg.write(0);
			} else if (val==BBSys.CMD_DOWN) {
				Triac.runter();
				Msg.write(0);
			} else if (val==BBSys.CMD_STOP) {
				Triac.stop();
				Msg.write(0);
			} else if (val==BBSys.CMD_TIME) {
				Msg.write(maxTime>>>8);			// div. by 256 => 34.7 us per tick
			} else if (val==BBSys.CMD_RESTIM) {
				maxTime = 0;
				Msg.write(0);
			} else if (val==BBSys.CMD_INP) {
				Msg.write((JopSys.rd(BBSys.IO_SENSOR)<<4) + JopSys.rd(BBSys.IO_TAST));
			} else if (val==BBSys.CMD_OPTO) {
				Msg.write(Triac.getOpto());
/* don't remeber what this was for?
			} else if (val==BBSys.CMD_RESCNT) {
				Triac.resetCnt();
				Msg.write(0);
*/
			} else if (val==BBSys.CMD_CNT) {
				Msg.write(Triac.getCnt());
			}
		}
	}


	static void dispAuto() {

		Display.line2();
		Display.data('A');
	}
	static void dispNoAuto() {

		Display.line2();
		Display.data(' ');
	}
	static void doAuto() {

		++autoCnt;
		if (autoCnt==200) {
			Triac.rauf();
		} else if (autoCnt==36200) {
			Triac.stop();
		} else if (autoCnt==120200) {
			Triac.runter();
		} else if (autoCnt==156200) {
			Triac.stop();
		} else if (autoCnt==240000) {
			autoCnt = 0;
		}
	}


/**
*	main loop.
*/
	private static void forever() {

		int blinkCnt = 0;
		int val;

		for (;;) {

			Triac.loop();

			handleMsg();

			Keyboard.loop();

			if (Keyboard.pressed) {
				val = Keyboard.rd();

				auto = false;
				dispNoAuto();
				if (val==40) {
					Triac.rauf();
				} else if (val==24) {
					Triac.runter();
				} else if (val==1) {
					Triac.stop();
				} else if (val==4) {
					in();
				} else if (val==56) {	// 'B'
					auto = true;
					autoCnt = 0;
					Triac.stop();
					dispAuto();
				}
			}

			if (auto) {
				doAuto();
			}

			if (blinkCnt==100) {
				Timer.wd();
				blinkCnt = 0;
			}
			++blinkCnt;

			val = JopSys.rd(BBSys.IO_TAST);
			if ((val & BBSys.BIT_TAB) != 0) {
				JopSys.wr(0x05, BBSys.IO_RELAIS);
			} else if ((val & BBSys.BIT_TAUF) != 0) {
				JopSys.wr(0x0a, BBSys.IO_RELAIS);
			} else {
				JopSys.wr(0x00, BBSys.IO_RELAIS);
			}
			JopSys.wr(val, BBSys.IO_LED);


			int used = Timer.usedTime();
			if (maxTime<used) maxTime = used;
			Timer.waitForNextInterval();
		}
	}
}
