/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2009, Martin Schoeberl (martin@jopdesign.com)

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


/**
 * 
 */
package jeopard;

import joprt.RtThread;

import com.jopdesign.sys.Const;

import util.Serial;
import ejip.Ejip;
import ejip.LinkLayer;
import ejip.Loopback;
import ejip.Net;
import ejip.Packet;
import ejip.Slip;
import ejip.Udp;
import ejip.UdpHandler;

/**
 * UDP based control object for JOP/PC JVM communication.
 * 
 * @author Martin Schoeberl (martin@jopdesign.com)
 *
 */
public class UdpControl extends Control implements UdpHandler{

	Ejip ejip;
	Net net;
	LinkLayer ipLink;
	Serial ser;

	final static int UDP_PORT = 8888;

	
	public UdpControl() {
		
		ejip = new Ejip();
		net = new Net(ejip);
		ser = new Serial(Const.IO_UART_BG_MODEM_BASE);
		int ip = Ejip.makeIp(192, 168, 1, 2);
		ipLink = new Slip(ejip, ser, ip);
		
		// run the network stuff in a periodic thread
		new RtThread(10, 3000) {
			public void run() {
				for (;;) {
					ser.loop();
					ipLink.run();
					ser.loop();
					net.run();
					waitForNextPeriod();
				}
			}
		};

		net.getUdp().addHandler(UDP_PORT, this);
		
		// polling thread
	}
	
	public void loop() {
		// nothing to do
	}

	public void request(Packet p) {
		int len = p.len/4-Udp.DATA;
		if (len!=data.length) {
			throw new Error("Wrong packet length");
		}
		for (int i=0; i<data.length; ++i) {
			data[i] = p.buf[Udp.DATA+i];
		}
		ejip.returnPacket(p);
		avail = true;
	}
	
	public void send() {
		pack();
		Packet p = ejip.getFreePacket(ipLink);
		if (p==null) {
			System.out.println("Got no free packet");
			return;
		}
		for (int i=0; i<data.length; ++i) {
			p.buf[Udp.DATA+i] = data[i];
		}
		p.len = (Udp.DATA+data.length)*4;
		Udp.build(p, ipLink.getIpAddress(), ipLink.getIpAddress(), UDP_PORT);
	}
}
