/*
 * Copyright (c) Martin Schoeberl, martin@jopdesign.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *	This product includes software developed by Martin Schoeberl
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 */

package ejip;

/**
*	Main.java: test main.
*
*	Author: Martin Schoeberl (martin.schoeberl@chello.at)
*
*/

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
		Dbg.init();

		//
		//	start TCP/IP and all (four) threads
		//
		Net.init();
		//
		//	start device driver threads
		//
// don't use CS8900 when simulating on PC or for BG263
		// LinkLayer ipLink = CS8900.init(Net.eth, Net.ip);
// don't use PPP on my web server
		// Ppp.init(); 
		LinkLayer ipLink = Slip.init((192<<24) + (168<<16) + (1<<8) + 2); 


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
// Native.wr(-1, Native.IO_LED);
			}
			Timer.wd();
		}
	}
}
