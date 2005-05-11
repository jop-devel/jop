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

package ejip;

/**
*	Arp.java: does ARP.
*
*	Author: Martin Schoeberl (martin.schoeberl@chello.at)
*
*   Changelog:
*		2002-03-15	ARP works!
*		2002-10-21	use Packet buffer, 4 bytes in one word
*		2002-11-11	ARP in own class (called from LinkLayer driver).
*
*
*/

import util.Dbg;

/**
*	handle ARP request.
*/

public class Arp {

/**
*	handle ARP request.
*/
	protected static void request(Packet p, int[] eth, int ip) {

		Dbg.wr('A');

		if (p.buf[6]==ip) {


/*
    Ethernet packet data:
	16.bit: (ar$hrd) Hardware address space (e.g., Ethernet,
			 Packet Radio Net.)
	16.bit: (ar$pro) Protocol address space.  For Ethernet
			 hardware, this is from the set of type
			 fields ether_typ$<protocol>.
	 8.bit: (ar$hln) byte length of each hardware address
	 8.bit: (ar$pln) byte length of each protocol address
	16.bit: (ar$op)  opcode (ares_op$REQUEST | ares_op$REPLY)
	nbytes: (ar$sha) Hardware address of sender of this
			 packet, n from the ar$hln field.
	mbytes: (ar$spa) Protocol address of sender of this
			 packet, m from the ar$pln field.
	nbytes: (ar$tha) Hardware address of target of this
			 packet (if known).
	mbytes: (ar$tpa) Protocol address of target.
*/
			/*
			Set the ar$op field to ares_op$REPLY
			*/
			p.buf[1] = 0x06040002;	// hw-len, sw-len, opcode replay

			/*
			Swap hardware and protocol fields, putting the local
	    	hardware and protocol addresses in the sender fields.
			*/
			int ip_src = (p.buf[3]<<16) + (p.buf[4]>>>16);
			p.buf[2] = (eth[0]<<24) + (eth[1]<<16) + (eth[2]<<8) + eth[3];
			p.buf[3] = (eth[4]<<24) + (eth[5]<<16) + (ip>>>16);
			p.buf[4] = (ip<<16) + p.llh[3];
			p.buf[5] = (p.llh[4]<<16) + p.llh[5];
			p.buf[6] = ip_src;

			/*
			Send the packet to the (new) target hardware address on
	    	the same hardware on which the request was received.
			*/

			p.len = 46;
			p.llh[0] = p.llh[3];
			p.llh[1] = p.llh[4];
			p.llh[2] = p.llh[5];
			p.llh[6] = 0x0806;			// ARP frame
			p.setStatus(Packet.SND);	// mark packet ready to send
		} else {
			p.setStatus(Packet.FREE);			// mark packet free
		}
	}

}
