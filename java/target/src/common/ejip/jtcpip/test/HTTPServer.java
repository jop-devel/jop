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
 * Single Thread version of passive TcpIp. jop is listening for connections
 * 
 * @author Nelson Langkamp
 */

import java.io.IOException;

import util.Dbg;
import ejip_old.CS8900;
import ejip_old.LinkLayer;
import ejip_old.Net;
import ejip.jtcpip.NwLoopThread;
import ejip.jtcpip.TCP;
import ejip.jtcpip.TCPConnection;

public class HTTPServer {
	static Net net;

	static LinkLayer ipLink;

	static NwLoopThread nwlt;

	static TCPConnection conn = null;

	static String page = 
		"HTTP/1.0 200 OK\r\n\r\n" +
		"<html><head></head><body>"+
		"<h2>HTML hello from JOP</h2>"+
		"</body></html>" +
		"\r\n\r\n";


	public static void main(String[] args) {

		// init stuff
		Dbg.initSerWait();
		Dbg.wr("Main2");
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

		boolean once = true; // only write once
		short port = 80;
		// FIXME: newConnection sets state to closed?!
		TCPConnection conn = TCPConnection.newConnection(port);
		conn.setState(TCPConnection.STATE_LISTEN);

		if (conn == null) {
			Dbg.wr("Error Main2: Could not init Connection\n");
			return;
		}

		for (;;) {
			if (conn.getState() == TCPConnection.STATE_CLOSED) {
				conn = TCPConnection.newConnection(port);
				conn.setState(TCPConnection.STATE_LISTEN);
			}
			// TODO: why does the connection go to state close wait?
			if (conn.getState() == TCPConnection.STATE_CLOSE_WAIT) {
				conn.close();
				conn = TCPConnection.newConnection(port);
				conn.setState(TCPConnection.STATE_LISTEN);
			}

			// reading from input stream
			int tmp = 0;
			while ((tmp = conn.iStream.read()) != -1) {
				Dbg.wr("input: ");
				Dbg.wr((char) tmp);
				Dbg.wr('\n');
				// do Echo
				if ((char) tmp == '/') {
					try {
						conn.oStream.write(page.getBytes());
					} catch (IOException e) {
						e.printStackTrace();
					}					
				}

			}

//			if (conn.getState() == TCPConnection.STATE_ESTABLISHED) {
//				if (once) {
//					try {
//						conn.oStream.write("Hello\r\n".getBytes());
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
//					once = false;
//				}
//			}
			nwlt.run();
			ipLink.loop();
		}

	}
}
