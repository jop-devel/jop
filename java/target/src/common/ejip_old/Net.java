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

import util.Dbg;

/**
*	Start device driver threads and poll for packets.
*/

public class Net {
	
	/**
	 * Enable the experimental TCP implementation
	 */
	public static final boolean TCP_ENABLE = false;
	
	public static final int PROT_ICMP = 1;
	
	/**
	 * Holds a reference to the actual LinkLayer to abstract the source of the
	 * IP address
	 * 
	 * TODO: should not be that global! We can have more link layers.
	 * FIXME: remove it!!!! - used by jtcpip
	 */
	public static LinkLayer linkLayer;


/**
*	The one and only reference to this object.
*/
	private static Net single;

/**
*	private because it's a singleton Thread.
*/
	private Net() {
	}

/**
*	Allocate buffer and create thread.
*/
	public static Net init() {

		if (single != null) return single;			// allready called init()

		Udp.init();
		TcpIp.init();

		single = new Net();
		
		return single;
	}


/**
*	Look for received packets and invoke receive.
*	Mark them to be sent if returned with len!=0 from TcpIp layer.
*/
	public void loop() {

		Packet p;

		// is a received packet in the pool?
		p = Packet.getPacket(Packet.RCV, Packet.ALLOC);
		if (p!=null) {					// got one received Packet from pool
			receive(p);
		} else {
			Udp.loop();
			if (TCP_ENABLE)	Tcp.loop();
		}
	}
	
	/**
	 * Process one IP packet. Change buffer and set length to get a packet sent
	 * back. called from Net.loop().
	 */
	public static void receive(Packet p) {

		int i, j;
		int ret = 0;
		int[] buf = p.buf;
		int len;

		i = buf[0];
		len = i & 0xffff; // len from IP header
		// NO options are assumed in ICMP/TCP/IP...
		// => copy if options present
		if (len > p.len || (i >>> 24 != 0x45)) {
			Dbg.wr("IP options -> discard");
			p.setStatus(Packet.FREE); // packet to short or ip options => drop
										// it
			return;
		} else {
			p.len = len; // correct for to long packets
		}

		// TODO fragmentation
		if (Ip.chkSum(buf, 0, 20) != 0) {
			p.setStatus(Packet.FREE);
			Dbg.wr("wrong IP checksum ");
			return;
		}

		int prot = (buf[2] >> 16) & 0xff; // protocol
		if (prot == PROT_ICMP) {
			TcpIp.doICMP(p);
			Ip.doIp(p, prot);
		} else if (prot == Tcp.PROTOCOL) {
			if ((buf[5] & 0xffff) == 80) {
				// still do our simple HTML server
				TcpIp.doTCP(p);
				Ip.doIp(p, prot);
			} else {
				if (TCP_ENABLE) {
					// that's the new upcomming TCP processing
					Tcp.process(p);					
				} else {
					p.setStatus(Packet.FREE); // mark packet free					
				}
			}
		} else if (prot == Udp.PROTOCOL) {
			Udp.process(p); // Udp generates the reply
		} else {
			p.setStatus(Packet.FREE); // mark packet free
		}
	}

}
