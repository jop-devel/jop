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

import java.io.IOException;

import util.Dbg;
import ejip2.LinkLayer;
import ejip2.Net;
import ejip2.jtcpip.JtcpipException;
import ejip2.jtcpip.NwLoopThread;
import ejip2.jtcpip.TCP;
import ejip2.jtcpip.TCPConnection;


/**
 * Single Thread version of active TcpIp protocol.
 * jop is connecting to a server
 * 
 * @author Nelson Langkamp
 */

public class TestConnectSingleThread {
	static Net net;

	static LinkLayer ipLink;

	static NwLoopThread nwlt;

	static TCPConnection conn = null;

	public static void main(String[] args) {

		// init. stuff
		Dbg.initSerWait();
		ipLink = Net.init("192.168.0.123", "00:01:02:03:04:05");
		nwlt = NwLoopThread.createInstance(ipLink);
		ejip2.jtcpip.UDPConnection.init();
		ejip2.jtcpip.TCPConnection.init();
		ejip2.jtcpip.Payload.init();
		ejip2.jtcpip.TCPOutputStream.init();
		ejip2.jtcpip.util.NumFunctions.init();
		ejip2.jtcpip.IP.init();
		ejip2.Arp.init();

		TCPConnection tmp = null;
		short port = 4444;

		try {
			tmp = TCP.connect("129.27.142.211", port); // send syn flag
		} catch (JtcpipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (;;) {

			if (tmp.getState() == TCPConnection.STATE_ESTABLISHED) {
				Dbg.wr("writing");
				try {
					tmp.oStream.write("Hello World\n".getBytes());
				} catch (NullPointerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			nwlt.run(); // "NwLoopThread"
			ipLink.loop(); // "CS8900 Thread"
		}

	}
}
