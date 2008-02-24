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


package jbe;

import jbe.ejip.LinkLayer;
import jbe.ejip.Loopback;
import jbe.ejip.Net;
import jbe.ejip.Packet;
import jbe.ejip.Udp;
import jbe.ejip.UdpHandler;

/**
*	Ejip.java: Benchmark with ejip TCP/IP stack.
*
*	Author: Martin Schoeberl (martin@jopdesign.com)
*
*/

	
public class BenchUdpIp extends BenchMark {

	static Net net;
	static LinkLayer ipLink;
	
	static boolean sent;
	static int received;
	static int a, b;
	static int sum;

/**
*	Start network.
*/
	public BenchUdpIp() {

		net = Net.init();
		ipLink = Loopback.init();

		UdpHandler adder;
		adder = new UdpHandler() {
			public void request(Packet p) {
				if (p.len != ((Udp.DATA+1)<<2)) {
					p.setStatus(Packet.FREE);
				} else {
					p.buf[Udp.DATA] += p.buf[Udp.DATA+1];
					p.len = (Udp.DATA)<<2;
					Udp.build(p, (127<<24)+(0<<16)+(0<<8)+1, 5678);
				}
			}
		};
		Udp.addHandler(1234, adder);

		UdpHandler result;
		result = new UdpHandler() {
			public void request(Packet p) {
				if (p.len == ((Udp.DATA)<<2)) {
					sum = p.buf[Udp.DATA];
				}
				sent = false;
				++received;
				p.setStatus(Packet.FREE);
			}
		};
		Udp.addHandler(5678, result);

		sent = false;
		a = 0x1234;
		b = 0xabcd;
		sum = 1234;
	}

	public int test(int cnt) {

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
			Packet p = Packet.getPacket(Packet.FREE, Packet.ALLOC, ipLink);
			if (p == null) {				// got no free buffer!
				return;
			}
			p.buf[Udp.DATA] = a;
			p.buf[Udp.DATA+1] = b;
			p.len = (Udp.DATA+1)<<2;
			Udp.build(p, (127<<24)+(0<<16)+(0<<8)+1, 1234);
			sent = true;
			// just generate new 'funny' values and use sum
			a = (a<<1)^b;
			b = (b<<1)^sum;
		}
	}


	public String getName() {

		return "UdpIp";
	}

	public static void main(String[] args) {

		BenchMark bm = new BenchUdpIp();

		Execute.perform(bm);
	}
}
