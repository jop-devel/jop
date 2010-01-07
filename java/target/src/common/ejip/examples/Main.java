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
*	Main.java: test main for the ejip stack
*
*	Author: Martin Schoeberl (martin@jopdesign.com)
*
*/

import util.Timer;

import ejip.*;

/**
*	Test Main for ejip.
*/

	
public class Main {

	static Net net;
	static LinkLayer ipLink;

/**
*	Start network and enter forever loop.
*/
	public static void main(String[] args) {

		//
		//	start TCP/IP
		//
		Ejip ejip = new Ejip();
		net = new Net(ejip);
// don't use CS8900 when simulating on PC or for BG263
		int[] eth = {0x00, 0xe0, 0x98, 0x33, 0xb0, 0xf8};
		int ip = Ejip.makeIp(192, 168, 0, 123); 
		ipLink = new CS8900(ejip, eth, ip);
		net.getUdp().addHandler(Tftp.PORT, new Tftp(ejip));


		forever();
	}

	private static void forever() {

		for (;;) {
			for (int i=0; i<1000; ++i) {
				ipLink.run();
				net.run();
			}
			Timer.wd();
		}
	}
}
