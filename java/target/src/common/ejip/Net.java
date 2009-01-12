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

import rtlib.SRSWQueue;

/**
*	Start device driver threads and poll for packets.
*/

public class Net implements Runnable {
	
	/**
	 * Enable the experimental TCP implementation
	 */
	public static final boolean TCP_ENABLED = false;
	
	public static final int PROT_ICMP = 1;
	
	Ejip ejip;
	Ip ip;
	private Udp udp;
	private Tcp tcp;

	public Net(Ejip ejipRef) {
		ejip = ejipRef;
		ip = new Ip(ejip);
		udp = new Udp(ejip);
		tcp = new Tcp(ejip);
		TcpIp.init();
	}


/**
*	Look for received packets and invoke receive.
*	Mark them to be sent if returned with len!=0 from TcpIp layer.
*/
	public void run() {

		Packet p;
		SRSWQueue<Packet> rxQ = ejip.llRxQueue;
		if (rxQ==null) {
			if (Logging.LOG) Logging.wr("No link layer registered");
			return;
		}
		
		// get one received packet from the receive queue
		p = rxQ.deq();
		if (p!=null) {
			receive(p);
		} else {
			udp.run();
			if (TCP_ENABLED) tcp.run();
		}			
	}
	
	/**
	 * Process one IP packet. Change buffer and set length to get a packet sent
	 * back. called from Net.loop().
	 */
	public void receive(Packet p) {

		int i;
		int[] buf = p.buf;
		int len;

		i = buf[0];
		len = i & 0xffff; // length from IP header
		// NO options are assumed in ICMP/TCP/IP...
		// => copy if options present
		if (len > p.len || (i >>> 24 != 0x45)) {
			if (Logging.LOG) Logging.wr("IP options -> discard");
			ejip.returnPacket(p); // packet to short or ip options => drop it
			return;
		} else {
			p.len = len; // correct for to long packets
		}

		// TODO fragmentation
		if (Ip.chkSum(buf, 0, 20) != 0) {
			ejip.returnPacket(p);
			if (Logging.LOG) Logging.wr("wrong IP checksum ");
			return;
		}

		int prot = (buf[2] >> 16) & 0xff; // protocol
		if (prot == PROT_ICMP) {
			TcpIp.doICMP(p);
			ip.doIp(p, prot);
		} else if (prot == Tcp.PROTOCOL) {
			if ((buf[5] & 0xffff) == 80) {
				// still do our simple HTML server
				TcpIp.doTCP(p);
				ip.doIp(p, prot);
			} else {
				if (TCP_ENABLED) {
					// that's the new TCP processing
					tcp.process(p);					
				} else {
					ejip.returnPacket(p); // mark packet free					
				}
			}
		} else if (prot == Udp.PROTOCOL) {
			udp.process(p); // Udp generates the reply
		} else {
			ejip.returnPacket(p); // mark packet free
		}
	}


	public Tcp getTcp() {
		return tcp;
	}

	public Udp getUdp() {
		return udp;
	}

}
