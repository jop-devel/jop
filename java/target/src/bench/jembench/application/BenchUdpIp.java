/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2001-2008, Martin Schoeberl (martin@jopdesign.com)

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/


package jembench.application;


import jembench.SerialBenchmark;
import jembench.udpip.UdpipLinkLayer;
import jembench.udpip.UdpipLoopback;
import jembench.udpip.UdpipNet;
import jembench.udpip.UdpipPacket;
import jembench.udpip.UdpipUdp;
import jembench.udpip.UdpipUdpHandler;

/**
*	Ejip.java: SerialBenchmark with ejip TCP/IP stack.
*
*	Author: Martin Schoeberl (martin@jopdesign.com)
*
*/

	
public class BenchUdpIp extends SerialBenchmark {

	static UdpipNet net;
	static UdpipLinkLayer ipLink;
	
	static boolean sent;
	static int received;
	static int a, b;
	static int sum;

/**
*	Start network.
*/
	public BenchUdpIp() {

		net = UdpipNet.init();
		ipLink = UdpipLoopback.init();

		UdpipUdpHandler adder;
		adder = new UdpipUdpHandler() {
			public void request(UdpipPacket p) {
				if (p.len != ((UdpipUdp.DATA+1)<<2)) {
					p.setStatus(UdpipPacket.FREE);
				} else {
					p.buf[UdpipUdp.DATA] += p.buf[UdpipUdp.DATA+1];
					p.len = (UdpipUdp.DATA)<<2;
					UdpipUdp.build(p, (127<<24)+(0<<16)+(0<<8)+1, 5678);
				}
			}
		};
		UdpipUdp.addHandler(1234, adder);

		UdpipUdpHandler result;
		result = new UdpipUdpHandler() {
			public void request(UdpipPacket p) {
				if (p.len == ((UdpipUdp.DATA)<<2)) {
					sum = p.buf[UdpipUdp.DATA];
				}
				sent = false;
				++received;
				p.setStatus(UdpipPacket.FREE);
			}
		};
		UdpipUdp.addHandler(5678, result);

		sent = false;
		a = 0x1234;
		b = 0xabcd;
		sum = 1234;
	}

	public int perform(int cnt) {

		for (received=0; received<cnt;) {
			request();
			ipLink.loop();
			net.loop();
		}
		return sum;
	}
	
	public void loop() {
		request();
		ipLink.loop();
		net.loop();
	}
	private static void request() {
		
		if (!sent) {
			UdpipPacket p = UdpipPacket.getPacket(UdpipPacket.FREE, UdpipPacket.ALLOC, ipLink);
			if (p == null) {				// got no free buffer!
				return;
			}
			p.buf[UdpipUdp.DATA] = a;
			p.buf[UdpipUdp.DATA+1] = b;
			p.len = (UdpipUdp.DATA+1)<<2;
			UdpipUdp.build(p, (127<<24)+(0<<16)+(0<<8)+1, 1234);
			sent = true;
			// just generate new 'funny' values and use sum
			a = (a<<1)^b;
			b = (b<<1)^sum;
		}
	}


	public String toString() {

		return "UdpIp";
	}

}
