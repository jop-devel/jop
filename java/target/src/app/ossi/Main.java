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

import ejip.*;
import util.*;
import joprt.*;

import com.jopdesign.sys.Native;

/**
*	Test Main for ejip.
*/

public class Main {

/**
*	Start network and enter forever loop.
*/
	public static void main(String[] args) {

		Timer.init(20000000, 5);	// just for the watch dog or some usleep (where ?)
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
				int val = Native.rd(Native.IO_IN);
				Native.wr(val, Native.IO_LED);
			}
			Timer.wd();
		}
	}
}
