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

import java.io.IOException;

import util.Dbg;
import ejip_old.CS8900;
import ejip_old.LinkLayer;
import ejip_old.Net;
import ejip.jtcpip.JtcpipException;
import ejip.jtcpip.NwLoopThread;
import ejip.jtcpip.TCP;
import ejip.jtcpip.TCPConnection;


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
		int[] eth = {0x00, 0x05, 0x02, 0x03, 0x04, 0x07};
		ipLink = CS8900.init(eth , (192 << 24) + (168 << 16) + (0 << 8) + 123);
		Net.linkLayer = ipLink;
		nwlt = NwLoopThread.createInstance(ipLink);
		ejip.jtcpip.UDPConnection.init();
		ejip.jtcpip.TCPConnection.init();
		ejip.jtcpip.Payload.init();
		ejip.jtcpip.TCPOutputStream.init();
		ejip.jtcpip.util.NumFunctions.init();
		ejip.jtcpip.IP.init();

		TCPConnection tmp = null;
		short port = 1234;

		try {
			tmp = TCP.connect("192.168.0.5", port); // send syn flag
		} catch (JtcpipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (int i=0;;++i) {

			if (tmp.getState() == TCPConnection.STATE_ESTABLISHED) {
				Dbg.wr("writing");
				try {
					tmp.oStream.write(("Hello World"+i+"\n").getBytes());
				} catch (NullPointerException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			nwlt.run(); // "NwLoopThread"
			ipLink.loop(); // "CS8900 Thread"
		}

	}
}
