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

package ejip.jtcpip.test;

/**
 * Test passive TcpIP with RtThreads
 * 
 * @author Nelson Langkamp
 */

import java.io.IOException;

import ejip_old.CS8900;
import ejip_old.LinkLayer;
import ejip_old.Net;
import ejip.jtcpip.NwLoopRtThread;
import ejip.jtcpip.TCP;
import ejip.jtcpip.TCPConnection;

import joprt.RtThread;
import util.Dbg;
import util.Timer;

public class TestListenRtThread {

	static Net net;

	static LinkLayer ipLink;

	static RtThread nwlt;

	static TCPConnection conn = null;

	public static void main(String[] args) {

		Dbg.initSerWait();
		// initialize jop to use ip and mac
		int[] eth = {0x00, 0x05, 0x02, 0x03, 0x04, 0x07};
		ipLink = CS8900.init(eth , (192 << 24) + (168 << 16) + (0 << 8) + 123);
		Net.linkLayer = ipLink;
		nwlt = NwLoopRtThread.createInstance(ipLink);
		ejip.jtcpip.UDPConnection.init();
		ejip.jtcpip.TCPConnection.init();
		ejip.jtcpip.Payload.init();
		ejip.jtcpip.TCPOutputStream.init();
		ejip.jtcpip.util.NumFunctions.init();
		ejip.jtcpip.IP.init();

		ListenThread listenThread = new ListenThread(8, 10000);
		//
		// new RtThread(9, 5000) {
		// public void run() {
		// for (;;) {
		// waitForNextPeriod();
		// nwlt.run();
		// }
		// }
		// };

		new RtThread(10, 2500) {
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

class ListenThread extends RtThread {

	public ListenThread(int prio, int us) {
		super(prio, us);
		// TODO Auto-generated constructor stub
	}

	public void run() {
		TCPConnection conn = null;
		short port = 44;
		// while is to be able to open up another connection 
		// after previous was closed
		while (true) {
			// TODO: only one connection for now
			conn = TCP.listen(port, this);

			// do something
			for (;;) {

				// do echo
				int tmp = 0;
				while ((tmp = conn.iStream.read()) != -1) {
					Dbg.wr("input: ");
					Dbg.wr((char) tmp);
					Dbg.wr('\n');
					// do Echo
					try {
						conn.oStream.write(tmp);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				if (conn.getState() == TCPConnection.STATE_CLOSE_WAIT
						|| conn.getState() == TCPConnection.STATE_CLOSED) {
					conn = null;
					break;
				}
				waitForNextPeriod();
			}
		}
	}
}
