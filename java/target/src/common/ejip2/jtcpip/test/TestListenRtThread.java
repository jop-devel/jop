/* 
 * Copyright  (c) 2006-2007 Graz University of Technology. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The names "Graz University of Technology" and "IAIK of Graz University of
 *    Technology" must not be used to endorse or promote products derived from
 *    this software without prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 *  PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE LICENSOR BE
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 *  OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 *  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 *  OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 *  OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *  POSSIBILITY  OF SUCH DAMAGE.
 */

package ejip2.jtcpip.test;

/**
 * Test passive TcpIP with RtThreads
 * 
 * @author Nelson Langkamp
 */

import ejip2.LinkLayer;
import ejip2.Net;
import ejip2.jtcpip.NwLoopThread;
import ejip2.jtcpip.TCP;
import ejip2.jtcpip.TCPConnection;

import joprt.RtThread;
import util.Dbg;
import util.Timer;



public class TestListenRtThread {

	static Net net;

	static LinkLayer ipLink;

	static NwLoopThread nwlt;

	static TCPConnection conn = null;

	
	public static void main(String[] args) {

		Dbg.initSerWait();
		// initialize jop to use ip and mac
		ipLink = Net.init("129.27.142.183", "00:01:02:03:04:05");
		nwlt = NwLoopThread.createInstance(ipLink);
		ejip2.jtcpip.UDPConnection.init();
		ejip2.jtcpip.TCPConnection.init();
		ejip2.jtcpip.Payload.init();
		ejip2.jtcpip.TCPOutputStream.init();
		ejip2.jtcpip.util.NumFunctions.init();
		ejip2.jtcpip.IP.init();
		ejip2.Arp.init();
		int time = 10000;
		
		new RtThread(10, time) {

			public void run() {
				TCPConnection tmp = null;
				int my_byte = 0;
				short port = 44;
				TCPConnection conn = TCPConnection.newConnection(port);
				conn.setState(TCPConnection.STATE_LISTEN);

				if (conn == null)
					return;

				for (;;) {
					waitForNextPeriod();
					if (tmp == null) {
						tmp = TCP.listen(port, conn);
						if (tmp != null)
							Dbg.wr("CONNECTION\n");
					} else {
						Dbg.wr("reading\n");
						my_byte = tmp.iStream.read();
						if (my_byte != -1) {
							Dbg.wr("print\n");
							Dbg.wr((char) my_byte);
						}
					}
				}
			}
		};

		new RtThread(10, time) {
			public void run() {
				for (;;) {
					waitForNextPeriod();
					nwlt.run();
				}
			}
		};

		new RtThread(10, time) {
			public void run() {
				for (;;) {
					waitForNextPeriod();
					ipLink.loop();
				}
			}
		};

	
		RtThread.startMission();

		// forever();
	}

	private static void forever() {

		//
		// just do the WD blink with lowest priority
		// => if the other threads take to long (*3) there will be a reset
		//
		for (;;) {
			for (int i = 0; i < 10; ++i) {
				RtThread.sleepMs(50);
				Timer.wd();
				/*-
				 int val = Native.rd(Const.IO_IN);
				 Native.wr(val, Const.IO_LED);
				 */
				Timer.loop();
			}
			Timer.wd();
		}
	}
}
