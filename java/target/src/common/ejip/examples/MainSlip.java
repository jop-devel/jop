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
*	Main.java: test main.
*
*	Author: Martin Schoeberl (martin.schoeberl@chello.at)
*
*/

import util.Serial;
import util.Timer;

import com.jopdesign.sys.Const;

import ejip.*;

/**
*	Test Main for ejip.
*/

public class MainSlip {

	static Net net;
	static LinkLayer ipLink;
	static Serial ser;

/**
*	Start network and enter forever loop.
*/
	public static void main(String[] args) {

		Ejip ejip = new Ejip();

		//
		//	start TCP/IP
		//
		net = new Net(ejip);
		
		//
		//	use second serial line for simulation
		//	with JopSim and on the project usbser
		//
		ser = new Serial(Const.IO_UART_BG_MODEM_BASE);
		int ip = Ejip.makeIp(192, 168, 1, 2);
		ipLink = new Slip(ejip, ser, ip);
		// register the simple HTTP server
		net.getTcp().addHandler(80, new HtmlBaseio());

		
		forever();
	}

	private static void forever() {

		for (;;) {
			for (int i=0; i<1000; ++i) {
				ser.loop();
				ipLink.run();
				ser.loop();
				net.run();
			}
			Timer.wd();
			System.out.print("*");
		}
	}
}
