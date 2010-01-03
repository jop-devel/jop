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

package jbe.udpip;

/*
*   Changelog:
*		2002-10-24	creation.
*
*	TODO: in Udp, TcpIp.... when to use the packet with automatic reply?
*/

/**
 * @author martin
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
/**
*	UDP functions.
*/

public class UdpIpUdp {

	public static final int PROTOCOL = 17;

	public static final int HEAD = 5;	// offset of udp header in words
	public static final int DATA = 7;	// offset of data in words

	private static Object monitor;

	public static final int MAX_HANDLER = 8;
	private static UdpIpUdpHandler[] list;
	private static int[] ports;
	private static int loopCnt;

	public static void init() {

		if (monitor!=null) return;
		monitor = new Object();
		list = new UdpIpUdpHandler[MAX_HANDLER];
		ports = new int[MAX_HANDLER];
		loopCnt = 0;

//		addHandler(Tftp.PORT, new Tftp());
	}
	/**
	*	add a handler for UDP requests.
	*	returns false if list is full.
	*/
	public static boolean addHandler(int port, UdpIpUdpHandler h) {

		if (monitor==null) init();

		synchronized(monitor) {
			for (int i=0; i<MAX_HANDLER; ++i) {
				if (list[i]==null) {
					ports[i] = port;
					list[i] = h;
					return true;
				}
			}
		}
		return false;
	}

	/**
	*	Called periodic from Net for timeout processing.
	*/
	static void loop() {

		int i = loopCnt;

		if (list[i]!=null) {
			list[i].loop();
			++i;
			if (i==MAX_HANDLER) i=0;
		} else {
			i = 0;
		}
		loopCnt = i;
	}
	/**
	*	process packet and generate reply if necessary.
	*/
	static void process(UdpIpPacket p) {

		int i, j;
		int[] buf = p.buf;

		int port = buf[HEAD];
		int remport = port >>> 16;
		port &= 0xffff;

		buf[2] = (PROTOCOL<<16) + p.len - 20; 		// set protocol and udp length in iph checksum for tcp checksum
		if (UdpIpTcpIp.chkSum(buf, 2, p.len-8)!=0) {
//			Dbg.intVal(p.len);
//			Dbg.wr(" : ");
//			for (int k = 0; k < (p.len+3)/4; k++) {
//				Dbg.hexVal(buf[k]);
//			}
			p.setStatus(UdpIpPacket.FREE);	// mark packet free
UdpIpDbg.wr("wrong UDP checksum ");
			return;
		}

		if (port == 1625) {

			// do the Dgb thing!
			i = UdpIpDbg.readBuffer(buf, 7);
			p.len = 28+i;
			// generate a reply with IP src/dst exchanged
			UdpIpUdp.build(p, buf[4], buf[3], remport);

		} else {

			if (list!=null) {
				for (i=0; i<MAX_HANDLER; ++i) { // @WCA loop=8
					if (list[i]!=null && ports[i]==port) {
						list[i].request(p);
						break;
					}
				}
				if (i==MAX_HANDLER) {
					p.setStatus(UdpIpPacket.FREE);	// mark packet free
UdpIpDbg.lf();
UdpIpDbg.wr('U');
UdpIpDbg.intVal(port);
				}
			} else {
				p.setStatus(UdpIpPacket.FREE);
			}
		}
	}
	

	public static void getData(UdpIpPacket p, StringBuffer s) {
		
		int[] buf = p.buf;
		s.setLength(0);
		for (int i = UdpIpUdp.DATA*4; i < p.len; i++) { // @WCA loop<=1500
			s.append((char) ((buf[i>>2]>>(24 - ((i&3)<<3))) & 0xff));
		}
	}
	
	public static void setData(UdpIpPacket p, StringBuffer s) {
		
		int[] buf = p.buf;
		int cnt = s.length();
		// copy buffer
		int k = 0;
		for (int i=0; i<cnt; i+=4) {  // @WCA loop<=1500
			for (int j=0; j<4; ++j) { // @WCA loop=4
				k <<= 8;
				if (i+j < cnt) k += s.charAt(i+j);
			}
			buf[UdpIpUdp.DATA + (i>>>2)] = k;
		}

		p.len = UdpIpUdp.DATA*4+cnt;
	}
	/**
	 * Generate a reply with IP src/dst exchanged.
	 * @param p
	 */
	public static void reply(UdpIpPacket p) {
		
		int[] buf = p.buf;
		UdpIpUdp.build(p, buf[4], buf[3], buf[HEAD]>>>16);
	}

	/**
	*	Get source IP from interface and build IP/UDP header.
	*/
	public static void build(UdpIpPacket p, int dstIp, int port) {

		int srcIp = p.interf.getIpAddress();
		if (srcIp==0) {						// interface is down
			p.setStatus(UdpIpPacket.FREE);		// mark packet free
		} else {
			build(p, srcIp, dstIp, port);
		}
	}

	/**
	*	Fill UDP and IP header and mark packet ready to send.
	*/
	public static void build(UdpIpPacket p, int srcIp, int dstIp, int port) {

		int i;
		int[] buf = p.buf;

		// read ethernet header from CS8900 driver

//		for (i=0; i<7; ++i) {
//			p.llh[i] = CS8900.llh[i];
//		}

		// IP header
		// TODO unique id for sent packet
		buf[0] = 0x45000000 + p.len;		// ip length	(header without options)
		buf[1] = UdpIpTcpIp.getId();				// identification, no fragmentation
		buf[3] = srcIp;
		buf[4] = dstIp;

		// UDP header
		// 'set' port numbers
		buf[HEAD] = ((port+10000)<<16) + port;		// src port = dst port + 10000
		// Fill in UDP header
		buf[HEAD+1] = (p.len-20)<<16;
		buf[2] = (PROTOCOL<<16) + p.len - 20; 		// set protocol and udp length in iph checksum for tcp checksum
		i = UdpIpTcpIp.chkSum(buf, 2, p.len-8);
		if (i==0) i = 0xffff;
		buf[HEAD+1] |= i;

		// for UDP checksum used field of IP header
		buf[2] = (0x20<<24) + (PROTOCOL<<16);	// ttl, protocol, clear checksum
		buf[2] |= UdpIpTcpIp.chkSum(buf, 0, 20);

		// a VERY dummy arp/routing!
		// should this be in the cs8900 ?
// TODO: This works ONLY if this packet was a RECEIVED packet!!!!!
//		so it's more ore less useless here
// PLEASE add at minimum a simple ARP cache in the ethernet driver.
		p.llh[0] = p.llh[3];
		p.llh[1] = p.llh[4];
		p.llh[2] = p.llh[5];
		p.llh[6] = 0x0800;
		p.setStatus(UdpIpPacket.SND);	// mark packet ready to send
	}
}
