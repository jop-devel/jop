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

class Entry {

	final static int ENTRY_CNT = 4;
	static Entry[] list;
	static int ageCnt;

	int ip;
	int[] mac;		// could be optimized to use 16-bit words
					// in intel byte oreder for CS8900
	boolean valid;
	int age;

	static void init() {
		if (list!=null) return;
		
		list = new Entry[ENTRY_CNT];
		for (int i=0; i<ENTRY_CNT; ++i) list[i] = new Entry();
		ageCnt = 0;
	}
	
	Entry() {
		mac = new int[6];
		ip = 0;
		valid = false;
		age = 0;
	}
	
	/**
	 * Add en entry into the ARP table
	 * @param p A received ARP request or reply
	 */
	static void add(Packet p) {

		int ip_src = (p.buf[3]<<16) + (p.buf[4]>>>16);

		int nr = 0;
		int oldest = ageCnt-list[0].age;
		for (int i=0; i<ENTRY_CNT; ++i) {
			if (list[i].ip==ip_src) {
				// we have an entry for this IP
				// address
				nr = i;
				break;
			}
			if (ageCnt-list[i].age>oldest) {
				oldest = ageCnt-list[i].age;
				nr = i;
			}
		}
		ageCnt++;
		// replace the oldest entry

		Entry e = list[nr];
		e.ip = ip_src;
		
		e.mac[0] = p.buf[2]>>>24;
		e.mac[1] = (p.buf[2]>>>16)&0xff;
		e.mac[2] = (p.buf[2]>>>8)&0xff;
		e.mac[3] = (p.buf[2])&0xff;
		e.mac[4] = (p.buf[3]>>>24);
		e.mac[5] = (p.buf[3]>>>16)&0xff;
		
		e.valid = true;
		e.age = ageCnt;
		dump(nr);
	}
	
	static Entry find(int ip) {
		
		for (int i=0; i<ENTRY_CNT; ++i) {
			if (list[i].ip==ip && list[i].valid) {
				return list[i];
			}
		}
		return null;
	}
	
	static void dump(int nr) {
		
		Dbg.wr("add ARP IP=");
		Dbg.hexVal(list[nr].ip);
		for (int i=0; i<6; ++i) {
			Dbg.hexVal(list[nr].mac[i]);
		}
	}
}

/**
*	handle ARP request.
*/

public class Arp {
	
	
	
	protected static void init() {
		Entry.init();
	}

/**
*	handle ARP request.
*/
	protected static void receive(Packet p, int[] eth, int ip) {

//		Dbg.wr('A');

		if (p.buf[6]==ip) {


/*
    Ethernet transmission layer (not necessarily accessible to the user):
	 	48.bit: Ethernet address of destination
		48.bit: Ethernet address of sender
		16.bit: Protocol type = ether_type$ADDRESS_RESOLUTION
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
			int arp_op = p.buf[1] & 0xffff;

			Entry.add(p);	// Add the entry anyway
			
			if (arp_op==1) {
//				Dbg.wr("ARP request");
			} else if (arp_op==2) {
//				Dbg.wr("ARP reply");
				p.setStatus(Packet.FREE);			// mark packet free
				return;
			} else {
				Dbg.wr("ARP unknown op: ");
				Dbg.intVal(arp_op);
			}

			/*
			Set the ar$op field to ares_op$REPLY
			*/
			p.buf[1] = 0x06040002;	// hw-len, sw-len, opcode reply

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

	/**
	 * Send an ARP request from LinkLayer to the source
	 * address from the IP packet. The original IP packet
	 * gets thrown away.
	 * @param ll
	 * @param ip
	 */
	protected static void sendRequest(Packet p) {
		
		int ip_dest = p.buf[4];
		
		p.buf[0] = 0x00010800;	// hw addr. space 1, Protocol add. space IP 
		p.buf[1] = 0x06040001;	// hw-len, sw-len, opcode request
		
		int eth[] = CS8900.eth;	// we have only the static filed in CS8900
		
		p.buf[2] = (eth[0]<<24) + (eth[1]<<16) + (eth[2]<<8) + eth[3];
		p.buf[3] = (eth[4]<<24) + (eth[5]<<16) + (CS8900.ip>>>16);
		p.buf[4] = (CS8900.ip<<16);	// we don't know the dest. eth. addr.
		p.buf[5] = 0;
		p.buf[6] = ip_dest;			// destination IP address
		p.len = 46;				// why 46?
		p.llh[0] = 0xffff;		// Ethernet broadcast
		p.llh[1] = 0xffff;
		p.llh[2] = 0xffff;
		// own Etherner address is filled by CS8900
		p.llh[6] = 0x0806;		// ARP frame

	}
	
	/**
	 * Fill in the destination MAC address. If not in
	 * the cache use this packet for a ARP request.
	 * The IP packet get's lost.
	 * @param p
	 */
	static void fillETH(Packet p) {
		
		// IP destination address (without gateway) is
		// at position 4 for IP packets and at 6 for ARP packets
		Entry e = Entry.find(p.buf[4]);
		if (p.llh[6] == 0x0806) e = Entry.find(p.buf[6]);
		if (e==null) {
			sendRequest(p);
		} else {
			int[] mac = e.mac;
			// intel byte order !!!
			p.llh[0] = mac[0]<<8 | mac[1];
			p.llh[1] = mac[2]<<8 | mac[3];
			p.llh[2] = mac[4]<<8 | mac[5];

		}
	}
	
}
