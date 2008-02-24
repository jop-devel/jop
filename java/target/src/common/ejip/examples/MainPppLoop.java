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

package ejip.examples;

/**
*	MainPppLoop.java: PPP/Modem test with a single thread.
*
*	Author: Martin Schoeberl (martin@jopdesign.com)
*
*/

import joprt.RtThread;
import util.Dbg;
import util.Serial;
import util.Timer;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

import ejip.*;

/**
*	Test Main for ejip.
*/

public class MainPppLoop {

	static Net net;
	static LinkLayer ipLink;
	static Serial ser;
	
	private static final int IPLINK_PRIO = 9;
	private static final int IPLINK_PERIOD = 100000;
	private static final int IPSER_PRIO = 10;
	private static final int IPSER_PERIOD = 3000;


/**
*	Start network and enter forever loop.
*/
	public static void main(String[] args) {

		Dbg.initSerWait();
		
//		for (int i=0; i<100; ++i) {
//			System.out.println(i+" "+Native.rd(Const.IO_STATUS2)+" "+Native.rd(Const.IO_UART2));
////			if ((Native.rd(Const.IO_STATUS2) & Const.MSK_UA_TDRE)!=0) {
//				Native.wr(i+'0', Const.IO_UART2);
////			}
//		}
//		
//		for (;;) {
//			if ((Native.rd(Const.IO_STATUS2) & Const.MSK_UA_RDRF)!=0) {
//				System.out.println("char: "+((char) Native.rd(Const.IO_UART2)));
//			}
//		}


		//
		//	initialize TCP/IP
		//
		net = Net.init();
		
		ser = new Serial(Const.IO_UART_BG_MODEM_BASE);
		

		//
		//	Create serial and PPP
		//

		new RtThread(IPSER_PRIO, IPSER_PERIOD) {
			public void run() {
				for (;;) {
					if (!waitForNextPeriod()) {
						System.out.print("missed");
					}
					ser.loop();
				}
			}
		};

		RtThread pppThre = new RtThread(IPLINK_PRIO, IPLINK_PERIOD) {
			public void run() {
				for (;;) {
					waitForNextPeriod();
					ipLink.loop();
				}
			}
		};

		ipLink = Ppp.init(ser, pppThre); 
		
		new RtThread(20, 100000) {
			public void run() {
				int i = 0;
				for (;;) {
					waitForNextPeriod();
					net.loop();
					++i;
					if (i==10) {
						Timer.wd();
						System.out.print('*');
						i = 0;
					}
					Timer.loop();
				}
			}
			
		};

		ipLink.startConnection(new StringBuffer("*99***1#"),
				new StringBuffer("A1.net"),
				new StringBuffer("ppp@A1plus.at"),
				new StringBuffer("ppp"));

		RtThread.startMission();
				
//		forever();
		// TODO: there is an issue when this loop is not
		// executed and the main thread terminates.
		for (;;) {
			int t = Timer.getTimeoutMs(800);
			while (!Timer.timeout(t)) {
				;
			}
			System.out.print("M");
		}
	}

	private static void forever() {

		for (;;) {
			for (int i=0; i<1000; ++i) {
//				ser.loop();
//				ipLink.loop();
//				ser.loop();
				net.loop();
			}
			Timer.wd();
		}
	}
}
