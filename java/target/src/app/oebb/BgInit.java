/*
 * Copyright (c) Martin Schoeberl, martin@jopdesign.com
 * All rights reserved.
 *
 */

package oebb;

/**
*	BgInit.java: A main program with SLIP on modem line for Flash
*	programming of BG.
*
*
*/

import ejip.*;
import joprt.RtThread;
import util.Dbg;
import util.Serial;
import util.Timer;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;


/**
 * @author Martin Schoeberl (martin@jopdesign.com)
 * 
 *	A main program with SLIP on modem line for Flash
 *	programming of BG.
 *
 */
public class BgInit {

	static Net net;
	static LinkLayer ipLink;
	static Serial ser;

/**
*	Start network and enter forever loop.
*/
	public static void main(String[] args) {

		Dbg.initSerWait();

		//
		//	start TCP/IP and all threads
		//
		net = Net.init();

		ser = new Serial(Const.IO_UART_BG_MODEM_BASE);
		ipLink = Slip.init(ser,	(192<<24) + (168<<16) + (1<<8) + 2);
		
		//
		//	start device driver threads
		//
		
		new RtThread(5, 10000) {
			public void run() {
				for (;;) {
					waitForNextPeriod();
					net.loop();
				}
			}
		};
		// Slip timeout (for windoz slip reply) depends on
		// period (=100*period) !
		new RtThread(9, 10000) {
			public void run() {
				for (;;) {
					waitForNextPeriod();
					ipLink.loop();
				}
			}
		};
		new RtThread(10, 3000) {
			public void run() {
				for (;;) {
					waitForNextPeriod();
					ser.loop();
				}
			}
		};


		System.out.println("Bginit");

		RtThread.startMission();

		//
		//	WD thread has lowest priority to see if every timing will be met
		//
		forever();
	}

	private static void forever() {

		//
		//	just do the WD blink with lowest priority
		//	=> if the other threads take to long (*3) there will be a reset
		//
		for (;;) {
			for (int i=0; i<10; ++i) {
				int val = Native.rd(Const.IO_IN);
				Native.wr(val, Const.IO_LED);
				RtThread.sleepMs(50);
			}
			Timer.wd();
		}
	}
}
