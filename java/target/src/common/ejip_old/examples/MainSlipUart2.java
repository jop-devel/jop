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

package ejip_old.examples;

/**
*	Main.java: test main.
*
*	Author: Martin Schoeberl (martin.schoeberl@chello.at)
*
*/

import joprt.RtThread;
import util.Dbg;
import util.Serial;
import util.Timer;

import com.jopdesign.sys.Const;

import ejip_old.*;

/**
*	Test Main for ejip.
*/

public class MainSlipUart2 {

	static Net net;
	static LinkLayer ipLink;
	static Serial ser;

/**
*	Start network and enter forever loop.
*/
	public static void main(String[] args) {

		Dbg.initSerWait();
		
		//
		//	start TCP/IP and all (four) threads
		//
		net = Net.init();
// don't use CS8900 when simulating on PC or for BG263
		// LinkLayer ipLink = CS8900.init(Net.eth, Net.ip);
// don't use PPP on my web server
		// Ppp.init(Const.IO_UART_BG_MODEM_BASE); 

		//
		//	use second serial line for simulation
		//	with JopSim
		//
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
		new RtThread(10, 10000) {
			public void run() {
				for (;;) {
					waitForNextPeriod();
					ser.loop();
				}
			}
		};

		//
		//	WD thread has lowest priority to see if every timing will be met
		//

		RtThread.startMission();

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

			}
			System.out.print("*");
			Timer.wd();
		}
	}
}
