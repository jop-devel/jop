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

package ejip_old;

/*
 * Changelog: 2002-03-16 works with ethernet 2002-10-21 use Packet buffer, 4
 * bytes in one word
 * 
 * 
 */

import util.Dbg;

/**
 * A minimalistic TCP/IP stack (with ICMP).
 * 
 * It's enough to handel a HTTP request (and nothing more)!
 */

public class TcpIp {

	public static final int PROTOCOL = 6;

	static final int FL_URG = 0x20;

	static final int FL_ACK = 0x10;

	static final int FL_PSH = 0x8;

	static final int FL_RST = 0x4;

	static final int FL_SYN = 0x2;

	static final int FL_FIN = 0x1;

	static int tcb_port; //  tcp port

	static int tcb_st; // state

	static final int ST_LISTEN = 0;

	static final int ST_ESTAB = 2;

	static final int ST_FW1 = 3;

	static final int ST_FW2 = 4;

	static final int MTU = 1500 - 8;

	static final int WINDOW = 2680;

	public static void init() {

		tcb_st = ST_LISTEN; // select();
		Html.init();
	}

	/**
	 * the famous ping.
	 */
	static void doICMP(Packet p) {

		int type_code = p.buf[5] >>> 16;
		Dbg.wr('P');
		Dbg.hexVal(type_code);
		if (type_code == 0x0800) {
			// TODO check received ICMP checksum
			p.buf[5] = 0; // echo replay plus clear checksu,
			p.buf[5] = Ip.chkSum(p.buf, 5, p.len - 20); // echo replay (0x0000)
														// plus checksum
		} else {
			p.len = 0;
		}
	}

	// TODO:!!!!!! do a real state machine,
	// end is wrong (sending ack in fw1 !!!) makes remote site crazy
	static void doTCP(Packet p) {

		int i;
		int datlen;
		int[] buf = p.buf;
		int rcvcnt, sndcnt;
		int fl;

		Dbg.wr('T');

		// Find the payload
		i = buf[8] >>> 16;
		int flags = i & 0xff;
		int hlen = i >>> 12;
		datlen = p.len - 20 - (hlen << 2);

		// "TCB"
		// In a full tcp implementation we would keep track of this per
		// connection.
		// This implementation only handles one connection at a time.
		// As a result, very little of this state is actually used after
		// the reply packet has been sent.

		// if (datlen < 0) return 0;

		// If it's not http, just drop it
		i = buf[5];
		if ((i & 0xffff) != 80) {
			Dbg.lf();
			Dbg.wr('T');
			Dbg.intVal(i & 0xffff);
			p.len = 0;
			return;
		}
		// Get source port
		tcb_port = i >>> 16;

		rcvcnt = buf[6]; // sequence number
		sndcnt = buf[7]; // acknowledge number
		// sndcnt has to be incremented for SYN!!!

		fl = FL_ACK;
		p.len = 40;

		// Figure out what kind of packet this is, and respond
		if ((flags & FL_SYN) != 0) {

			// SYN
			sndcnt = -1; // start with -1 for SYN
			rcvcnt++;
			fl |= FL_SYN;
			// tcb_st = ST_ESTAB;

		} else if (datlen > 0) {

			// incoming data
			rcvcnt += datlen;

			// TODO get url

			if (sndcnt == 0) {
				p.len += Html.setText(buf, 5 + hlen, datlen, 10);
				// Send reply packet
				// if (len > MTU) len = MTU; // TODO MTU should be taken from
				// tcp options
				// Read next segment of data into buffer
			} else {
				fl |= FL_FIN;
				// tcb_st = ST_FW1;
			}

			fl |= FL_PSH;

		} else if ((flags & FL_FIN) != 0) {

			// FIN
			rcvcnt++;
			// Don't bother with FIN-WAIT-2, TIME-WAIT, or CLOSED; they just
			// cause trouble
			// tcb_st = ST_LISTEN;

		} else if ((flags & FL_ACK) != 0) {

			// ack with no data
			if (sndcnt > 0) {
				// calculate no of bytes left to send
				// i = len2send - sndnxt
				i = 0;
				if (i == 0) {
					// EOF; send FIN
					fl |= FL_FIN;
					// tcb_st = ST_FW1;
				} else if (i > 0) {
					// not EOF; send next segment
					// len += i;
					fl |= FL_PSH;
				} else { // ***** this is never used! thats bad
					// ack of FIN; no reply
					p.len = 0;
					return;
				}
			} else {
				p.len = 0;
				return; // No reply packet
			}

		} else {
			p.len = 0;
			return; // drop it
		}

		// Fill in TCP header
		buf[5] = (80 << 16) + tcb_port;
		buf[6] = sndcnt;
		buf[7] = rcvcnt;
		buf[8] = 0x50000000 + (fl << 16) + WINDOW; // hlen = 20, no options
		buf[9] = 0; // clear checksum field
		buf[2] = (PROTOCOL << 16) + p.len - 20; // set protocol and tcp length
												// in iph checksum for tcp
												// checksum
		buf[9] = Ip.chkSum(buf, 2, p.len - 8) << 16;

	}
}
