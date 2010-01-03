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
*		2002-03-16	works with ethernet
*		2002-10-21	use Packet buffer, 4 bytes in one word
*
*
*/

/**
*	A minimalistic TCP/IP stack (with ICMP).
*
*	It's enough to handel a HTTP request (and nothing more)!
*/

public class UdpIpTcpIp {

	private static final int PROT_ICMP = 1;
	private static final int PROT_TCP = 6;

	static final int FL_URG = 0x20;
	static final int FL_ACK = 0x10;
	static final int FL_PSH = 0x8;
	static final int FL_RST = 0x4;
	static final int FL_SYN = 0x2;
	static final int FL_FIN = 0x1;

	static int ip_id, tcb_port;	// ip id, tcp port
	static int tcb_st;	// state

	static final int ST_LISTEN = 0;
	static final int ST_ESTAB = 2;
	static final int ST_FW1 = 3;
	static final int ST_FW2 = 4;

    static final int MTU = 1500-8;
    static final int WINDOW = 2680;

/**
*	calc ip check sum.
*	assume (32 bit) word boundries. rest of buffer is 0.
*	off offset in buffer (in words)
*	cnt length in bytes
*/
	static int chkSum(int[] buf, int off, int cnt) {


		int i;
		int sum = 0;
		cnt = (cnt+3)>>2;		// word count (max is 375)
		while (cnt != 0) { // @WCA loop<=375
			i = buf[off];
			sum += i & 0xffff;
			sum += i>>>16;
			++off;
			--cnt;
		}

		while ((sum>>16) != 0) { // @WCA loop<=2
			sum = (sum & 0xffff) + (sum >> 16);
		}

		sum = (~sum) & 0xffff;

		return sum;
	}

	public static void init() {

		tcb_st = ST_LISTEN;		// select();
		ip_id = 0x12340000;
//		Html.init();
	}

/**
*	return IP id in upper 16 bit.
*/
	static int getId() {

		ip_id += 0x10000;
		return ip_id;
	}

/**
*	process one ip packet.
*	change buffer and set length to get a packet sent back.
*	called from Net.run().
*/
	public static void receive(UdpIpPacket p) {

		int i, j;
		int ret = 0;
		int[] buf = p.buf;
		int len;

		i = buf[0];
		len = i & 0xffff;		// len from IP header
// NO options are assumed in ICMP/TCP/IP...
//		=> copy if options present
		if (len > p.len || (i>>>24!=0x45)) {
			p.setStatus(UdpIpPacket.FREE);	// packet to short or ip options => drop it
			return;
		} else {
			p.len = len;				// correct for to long packets
		}

		// TODO fragmentation
		if (chkSum(buf, 0, 20)!=0) {
			p.setStatus(UdpIpPacket.FREE);
UdpIpDbg.wr("wrong IP checksum ");
			return;
		}

		int prot = (buf[2]>>16) & 0xff;		// protocol
		if (prot==PROT_ICMP) {
			doICMP(p);
			doIp(p, prot);
		} else if (prot==PROT_TCP) {
			doTCP(p);
			doIp(p, prot);
		} else if (prot==UdpIpUdp.PROTOCOL) {
			UdpIpUdp.process(p);				// Udp generates the reply
		} else {
			p.setStatus(UdpIpPacket.FREE);	// mark packet free
		}
	}

/**
*	very simple generation of IP header.
*	just swap source and destination.
*/
	private static void doIp(UdpIpPacket p, int prot) {

		int[] buf = p.buf;
		int len = p.len;
		int i;

		if (len == 0) {
			p.setStatus(UdpIpPacket.FREE);	// mark packet free
		} else {
			buf[0] = 0x45000000 + len;			// ip length	(header without options)
			buf[1] = getId();					// identification, no fragmentation
			buf[2] = (0x20<<24) + (prot<<16);	// ttl, protocol, clear checksum
			i = buf[3];							// swap ip addresses
			buf[3] = buf[4];
			buf[4] = i;
			buf[2] |= chkSum(buf, 0, 20);

			// a VERY dummy arp/routing!
			// should this be in the cs8900 ?
			p.llh[0] = p.llh[3];
			p.llh[1] = p.llh[4];
			p.llh[2] = p.llh[5];
			p.llh[6] = 0x0800;
			p.setStatus(UdpIpPacket.SND);	// mark packet ready to send
		}
	}

/**
*	the famous ping.
*/
	private static void doICMP(UdpIpPacket p) {

UdpIpDbg.wr('P');
		if (p.buf[5]>>>16 == 0x0800) {
			// TODO check received ICMP checksum
			p.buf[5] = 0;							// echo replay plus clear checksu,
			p.buf[5] = chkSum(p.buf, 5, p.len-20);	// echo replay (0x0000) plus checksum
		} else {
			p.len = 0;
		}
	}


// TODO:!!!!!! do a real state machine,
// end is wrong (sending ack in fw1 !!!) makes remote site crazy
	static void doTCP(UdpIpPacket p) {

		int i;
		int datlen;
		int[] buf = p.buf;
		int rcvcnt, sndcnt;
		int fl;

UdpIpDbg.wr('T');

		// Find the payload
		i = buf[8]>>>16;
		int flags = i & 0xff;
		int hlen = i>>>12;
		datlen = p.len - 20 - (hlen<<2);

		// "TCB"
		// In a full tcp implementation we would keep track of this per connection.
		// This implementation only handles one connection at a time.
		// As a result, very little of this state is actually used after
		// the reply packet has been sent.

//		if (datlen < 0) return 0;

		// If it's not http, just drop it
		i = buf[5];
		if ((i & 0xffff) != 80) {
			p.len = 0;
			return;
		}
		// Get source port
		tcb_port = i>>>16;

		rcvcnt = buf[6];		// sequence number
		sndcnt = buf[7];		// acknowledge number
		// sndcnt has to be incremented for SYN!!!
	
	
		fl = FL_ACK;
		p.len = 40;
	
	
		// Figure out what kind of packet this is, and respond
		if ((flags & FL_SYN) != 0) {
	
			// SYN
			sndcnt = -1;		// start with -1 for SYN 
			rcvcnt++;
			fl |= FL_SYN;
//			tcb_st = ST_ESTAB;
	
		} else if (datlen > 0) {
	
			// incoming data
			rcvcnt += datlen;
	
			// TODO get url

			if (sndcnt==0) {
//				p.len += Html.setText(buf, 5+hlen, datlen, 10);
				// Send reply packet
//				if (len > MTU) len = MTU;	// TODO MTU should be taken from tcp options
				// Read next segment of data into buffer
			} else {
				fl |= FL_FIN;
//				tcb_st = ST_FW1;
			}
	

			fl |= FL_PSH;
	
		} else if ((flags & FL_FIN) != 0) {
	
			// FIN
			rcvcnt++;
			// Don't bother with FIN-WAIT-2, TIME-WAIT, or CLOSED; they just cause trouble
//			tcb_st = ST_LISTEN;
	
		} else if ((flags & FL_ACK) != 0) {
	
			// ack with no data
			if (sndcnt > 0) {
				// calculate no of bytes left to send
// i = len2send - sndnxt
i = 0;
				if (i == 0) {
					// EOF; send FIN
					fl |= FL_FIN;
//					tcb_st = ST_FW1;
				} else if (i > 0) {
					// not EOF; send next segment
//					len += i;
					fl |= FL_PSH;
				} else {						// ***** this is never used! thats bad
					// ack of FIN; no reply
					p.len = 0;
					return;
				}
			} else {
				p.len = 0;
				return;					// No reply packet
			}
	
		} else {
			p.len = 0;
			return;						// drop it
		}
	
		// Fill in TCP header
		buf[5] = (80<<16) + tcb_port;
		buf[6] = sndcnt;
		buf[7] = rcvcnt;
		buf[8] = 0x50000000 + (fl<<16) + WINDOW;	// hlen = 20, no options
		buf[9] = 0;									// clear checksum field
		buf[2] = (PROT_TCP<<16) + p.len - 20; 		// set protocol and tcp length in iph checksum for tcp checksum
		buf[9] = chkSum(buf, 2, p.len-8)<<16;
	
	}
}
