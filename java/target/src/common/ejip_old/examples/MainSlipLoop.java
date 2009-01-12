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
*	MainSlipLoop.java: SLIP test main without threads
*
*	Author: Martin Schoeberl (martin.schoeberl@chello.at)
*
*/

import util.Dbg;
import util.Serial;
import util.Timer;

import com.jopdesign.sys.Const;

import ejip_old.*;

/**
*	Test Main for ejip.
*/

public class MainSlipLoop {

	static Net net;
	static LinkLayer ipLink;
	static Serial ser;

/**
*	Start network and enter forever loop.
*/
	public static void main(String[] args) {

		Dbg.init();

		//
		//	start TCP/IP and all (four) threads
		//
		net = Net.init();
// don't use CS8900 when simulating on PC or for BG263
		// LinkLayer ipLink = CS8900.init(Net.eth, Net.ip);
// don't use PPP on my web server
		// Ppp.init(Const.IO_UART_BG_MODEM_BASE); 

		ser = new Serial(Const.IO_UART1_BASE);
		ipLink = Slip.init(ser,	(192<<24) + (168<<16) + (1<<8) + 2);
		
		forever();
	}

	private static void forever() {

		for (;;) {
			for (int i=0; i<1000; ++i) {
				ser.loop();
				// timeout in slip depends on loop time!
				ipLink.loop();
				ser.loop();
				net.loop();
			}
			Timer.wd();
		}
	}
}
