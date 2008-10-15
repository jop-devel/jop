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

package ejip123;

import ejip123.util.Dbg;

/** see RFC 826. */
class Arp{
private final static int ENTRY_CNT = 4;
private static final Entry[] list;
public static final int ETHER_PROT = 0x0806;

private Arp(){
}

/**
 Adds an entry into the ARP table.

 @param p A received ARP request or reply */
private static void add(Packet p){

	int ip_src = (p.buf[3] << 16) + (p.buf[4] >>> 16);

	int nr = -1;
	int oldest = list[0].age;
	int youngest = oldest;
	for(int i = 0; i < ENTRY_CNT; ++i){
		if(list[i].ip == ip_src){
			// we have an entry for this IP address
			nr = i;
		}
		if(list[i].age < oldest){
			oldest = list[i].age;
		} else if(list[i].age > youngest){
			youngest = list[i].age;
		}
	}
	// if there is no previous entry for that IP, replace the oldest entry
	Entry e = list[(nr == -1) ? oldest : nr];
	e.ip = ip_src;

	e.mac[0] = p.buf[2] >>> 24;
	e.mac[1] = (p.buf[2] >>> 16)&0xff;
	e.mac[2] = (p.buf[2] >>> 8)&0xff;
	e.mac[3] = (p.buf[2])&0xff;
	e.mac[4] = (p.buf[3] >>> 24);
	e.mac[5] = (p.buf[3] >>> 16)&0xff;

	e.valid = true;
	e.age = ++youngest;
	dump(nr);
}

private static Entry find(int ip){
	for(int i = 0; i < ENTRY_CNT; ++i){
		if(list[i].ip == ip && list[i].valid){
			return list[i];
		}
	}
	return null;
}

private static void dump(int nr){

	Dbg.wr("add ARP IP=");
	Dbg.hexVal(list[nr].ip);
	for(int i = 0; i < 6; ++i){
		Dbg.hexVal(list[nr].mac[i]);
	}
}

private static class Entry{
	private int ip;
	private final int[] mac;	// could be optimized to use 16-bit words
	// in intel byte order for CS8900
	private boolean valid;
	private int age;
//		TODO: not used - age wraps around after 4 billion requests
//		static int ageCnt;

	Entry(){
		mac = new int[6];
		ip = 0;
		valid = false;
		age = 0;
	}

}

static{

	list = new Entry[ENTRY_CNT];
	for(int i = 0; i < ENTRY_CNT; ++i)
		list[i] = new Entry();
	// Static ARP entry: IP Broadcast -> Ethernet Broadcast
	// this is just one possibility, should be resolved algorithmically not with the table
	// solved with LinkLayer.isLocalBroadcast(ip)
/*
	list[0].ip = 0xFFFFFFFF; // 255.255.255.255
	list[0].mac[0] = 0xFF; // -> resolves to FF:FF:FF:FF:FF:FF
	list[0].mac[1] = 0xFF;
	list[0].mac[2] = 0xFF;
	list[0].mac[3] = 0xFF;
	list[0].mac[4] = 0xFF;
	list[0].mac[5] = 0xFF;
	list[0].valid = true;
	list[0].age = 1;
*/
}

/** process an ARP packet. */
static void receive(Packet p, int[] eth, int ip, int[] ethdr){

//		Dbg.wr('A');

	if(p.buf[6] == ip){
//			System.out.println("ARP receive");
		int arp_op = p.buf[1]&0xffff;

		add(p);	// Add the entry anyway

		if(arp_op == 1){
			// System.out.println("request");

			/* Set the ar$op field to ares_op$REPLY */
			p.buf[1] = 0x06040002;	// hw-len, sw-len, opcode reply

			/* Swap hardware and protocol fields, putting the local hardware and protocol addresses in the sender fields. */
			int ip_src = (p.buf[3] << 16) + (p.buf[4] >>> 16);
			p.buf[2] = (eth[0] << 24) + (eth[1] << 16) + (eth[2] << 8) + eth[3];
			p.buf[3] = (eth[4] << 24) + (eth[5] << 16) + (ip >>> 16);
			// llh
//			p.buf[4] = (ip << 16) + p.llh[3];
//			p.buf[5] = (p.llh[4] << 16) + p.llh[5];
			p.buf[4] = (ip << 16) + ethdr[3];
			p.buf[5] = (ethdr[4] << 16) + ethdr[5];
			p.buf[6] = ip_src;

			/* Send the packet to the (new) target hardware address on the same hardware on which the request was received. */
			p.setLen(46);
			// llh
/*
			p.llh[0] = p.llh[3];
			p.llh[1] = p.llh[4];
			p.llh[2] = p.llh[5];
			p.llh[6] = ETHER_PROT;			// ARP frame
*/
			ethdr[0] = ethdr[3];
			ethdr[1] = ethdr[4];
			ethdr[2] = ethdr[5];
			ethdr[6] = ETHER_PROT;			// ARP frame
			p.setStatus(Packet.DGRAM_RDY);	// mark packet ready to send
			return;
		}
	}
	p.free();			// mark packet free
}

/** Send an ARP request from LinkLayer to the source address from the IP packet. The original IP packet gets thrown away. */
private static void sendRequest(Packet p){

	int ip_dest = p.buf[4];

	p.buf[0] = 0x00010800;	// hw addr. space 1, Protocol add. space IP
	p.buf[1] = 0x06040001;	// hw-len, sw-len, opcode request

	int eth[] = CS8900.eth;	// we have only the static field in CS8900

	p.buf[2] = (eth[0] << 24) + (eth[1] << 16) + (eth[2] << 8) + eth[3];

	p.buf[3] = (eth[4] << 24) + (eth[5] << 16) + (CS8900.single.getIp()>>> 16);
	p.buf[4] = (CS8900.single.getIp()<< 16);	// we don't know the dest. eth. addr.
	p.buf[5] = 0;
	p.buf[6] = ip_dest;			// destination IP address
	p.setLen(46);				// why 46?
	// llh
//	p.llh[0] = 0xffff;		// Ethernet broadcast
//	p.llh[1] = 0xffff;
//	p.llh[2] = 0xffff;
	CS8900.ethdr[0] = 0xffff;		// Ethernet broadcast
	CS8900.ethdr[1] = 0xffff;
	CS8900.ethdr[2] = 0xffff;
	// own Ethernet address is filled by CS8900
//	p.llh[6] = ETHER_PROT;		// ARP frame
	CS8900.ethdr[6] = ETHER_PROT;

}

/** Fill in the destination MAC address. If not in the cache use this packet for a ARP request.
 The IP packet gets lost when not a TCP packet.

 @return the original or created copy for the link layer to send. Can be null! - nah
 */
static Packet fillMAC(Packet p, int[] ethdr){

	Entry e;
	// IP destination address (without gateway) is
	// at position 4 for IP packets and at 6 for ARP packets
	// FIXME do we really need to find() the dest mac for arp packets?
	// if we answer a request, we know the mac of the sender
	// if we request a mac, we need to broadcast
	// are there any other cases?
	if(p.prot() == ETHER_PROT){
		e = find(p.buf[6]);
	} else{
		// TODO check for local broadcast...
//		if(CS8900.single.isLocalBroadcast(ip))


		e = find(p.buf[4]);
	}
	// TODO: for dhcp
	// int firstHopDest = CS8900.isSameSubnet(p.buf[addrPos]) ?
	// p.buf[addrPos] : Ip.linkLayer.gateway;
//		int firstHopDest = p.buf[addrPos];
//		Entry e = Entry.find(firstHopDest);


	if(e == null){
		// If it's a TCP packet we need to make a copy,
		// set the status to on-the-fly and rely on the
		// TCP timeout retransmission TODO: avoid the need to copy
		if(p.status() == Packet.CON_RDY){
			Packet ap = PacketPool.getFreshPacket();
			if(ap != null){
				ap.copy(p);
				sendRequest(ap);
				// avoid transmit from the link layer again
				// before the ARP reply comes in
				p.setStatus(Packet.CON_ONFLY);
				return ap;
			}
		} else{
			sendRequest(p);
		}
	} else{
		int[] mac = e.mac;
		// intel byte order !!!
//		p.llh[0] = (mac[0] << 8)|mac[1];
//		p.llh[1] = (mac[2] << 8)|mac[3];
//		p.llh[2] = (mac[4] << 8)|mac[5];
		ethdr[0] = (mac[0] << 8)|mac[1];
		ethdr[1] = (mac[2] << 8)|mac[3];
		ethdr[2] = (mac[4] << 8)|mac[5];

	}

	return p;
}

/**
 Returns true if the MAC address of a given IP address is already in the cache

 @return boolean */
public static boolean inCache(int ip){
	// TODO dhcp
	// return Entry.find(CS8900.isSameSubnet(ip) ? ip :
	// Ip.linkLayer.gateway) != null;
	return find(ip) != null;

}
}
