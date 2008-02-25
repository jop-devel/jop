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
 *
 */

package ossi;

/**
*	Main.java: test main.
*
*	Author: Martin Schoeberl (martin.schoeberl@chello.at)
*
*/

import joprt.RtThread;
import util.Dbg;
import util.Timer;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

/**
*	Test Main for ejip.
*/

public class Main {

/**
*	Start network and enter forever loop.
*/
	public static void main(String[] args) {

		// Dbg.init();
		Dbg.initSer();

		//
		//	start TCP/IP and all (four) threads
		//
		// Net.init();
		//
		//	start device driver threads
		//
// don't use CS8900 when simulating on PC or for BG263
		// LinkLayer ipLink = CS8900.init(Net.eth, Net.ip);
// don't use PPP on my web server
		// Ppp.init(); 
		// LinkLayer ipLink = Slip.init((192<<24) + (168<<16) + (1<<8) + 2); 


		//
		//	Start RST and Pwm thread
		//
		RST.init();
		Pwm.init();

		//
		//	WD thread has lowest priority to see if every timing will be met
		//

		RtThread.startMission();

/* read ID  and status from NAND
Dbg.wr('\n');
// Native.wrMem(0xff, 0x100001);
Native.wrMem(0x90, 0x100001);
Native.wrMem(0x00, 0x100002);
//
//	should read 0x98 and 0x73
//
Dbg.hexVal(Native.rdMem(0x100000));
Dbg.hexVal(Native.rdMem(0x100000));

//
//	read status, should be 0xc0
//
Native.wrMem(0x70, 0x100001);
Dbg.hexVal(Native.rdMem(0x100000));
Dbg.hexVal(Native.rdMem(0x100000));
*/

/* Read ID from Flash
Native.wrMem(0xaa, 0x80555);
Native.wrMem(0x55, 0x802aa);
Native.wrMem(0x90, 0x80555);
Dbg.hexVal(Native.rdMem(0x80000));
Native.wrMem(0xaa, 0x80555);
Native.wrMem(0x55, 0x802aa);
Native.wrMem(0x90, 0x80555);
Dbg.hexVal(Native.rdMem(0x80001));
*/

		forever();
	}

	private static void forever() {

		//
		//	just do the WD blink with lowest priority
		//	=> if the other threads take to long (*3) there will be a reset
		//
		for (;;) {
			for (int i=0; i<10; ++i) {
				RtThread.sleepMs(50);
				int val = Native.rd(Const.IO_IN);
				Native.wr(val, Const.IO_LED);
			}
			Timer.wd();
		}
	}
}
