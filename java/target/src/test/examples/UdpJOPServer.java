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

package examples;


import joprt.RtThread;
import util.Dbg;
import util.Timer;
import ejip_old.*;

class MyServer extends UdpHandler {
	
	StringBuffer sb;

	// sorry, but the class initializer cannot
	// create objects (at the moment)
	public MyServer() {
		sb = new StringBuffer(100);
	}
	
	
	public void request(Packet p) {
		
		String answer = " Answer";
		
		System.out.print("reveived: ");
		// nasty manipulation of the int[] buffer
		// when dealing with byte characters :-(
		Ip.getData(p, Udp.DATA, sb);
		Dbg.wr(sb);
		System.out.println();
		sb.append(answer);
		Ip.setData(p, Udp.DATA, sb);
		// and return it to port 1234
		Udp.build(p, p.buf[3], 1234);
	};

}
/**
*	Simple UDP Server.
*/

	
public class UdpJOPServer {

	static Net net;
	static LinkLayer ipLink;

/**
*	Start network and enter forever loop.
*/
	public static void main(String[] args) {


		// use serial line for debugging
		Dbg.initSerWait();
		
		//
		//	create our simple UDP server
		//
		MyServer server = new MyServer();
		Udp.addHandler(1234, server);
		//
		//	start TCP/IP
		//
		net = Net.init();
		int[] eth = {0x00, 0xe0, 0x98, 0x33, 0xb0, 0xf8};
		int ip = (192<<24) + (168<<16) + (0<<8) + 123;
		ipLink = CS8900.init(eth, ip);

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
		new RtThread(5, 10000) {
			public void run() {
				for (;;) {
					waitForNextPeriod();
					ipLink.loop();
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
				Timer.wd();
				Timer.loop();
			}
			Timer.wd();
		}
	}
}
