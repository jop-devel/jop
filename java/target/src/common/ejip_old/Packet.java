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

/*
*   Changelog:
*		2002-11-11	use LinkLayer info to mix Slip and Ethernet packets
*/

package ejip_old;

import util.Dbg;

/**
*	Packet buffer handling for a minimalistic TCP/IP stack.
*
*/
public class Packet {

	public final static int MAX = 1500;		// maximum Packet length in bytes
//	public final static int MAX = StackParameters.PACKET_MTU_SIZE; 
	public final static int MAXW = 1500/4;	// maximum Packet length in word
	public final static int MAXLLH = 7;		// 7 16 bit words for ethernet
	private final static int CNT = 8;		// size of packet pool
//	private final static int CNT = StackParameters.PACKET_POOL_SIZE; 

	/** interface source/destination */
	public LinkLayer interf;
	/** place for link layer data */
	public int[] llh;
	/** Buffer for the ip datagram */
	public int[] buf;
	/** Packet length in bytes */
	public int len;
	/** Current status of the packet. */
	private int status;
	/** The packet is free to use. */
	public final static int FREE = 0;
	/** Allocated and either under interpretation or under construction. */
	public final static int ALLOC = 1;
	/** A datagram packet ready to be sent by the link layer. */
	public final static int SND_DGRAM = 2;
	/** Received packet ready to be processed by the network stack. */
	public final static int RCV = 3;
	/** A TCP packet ready to be sent. This will go to TCP_ONFLY after sending. */
	public final static int SND_TCP = 4;
	/** A sent and not acked TCP packet. Can be resent after a timeout. */
	public final static int TCP_ONFLY = 5;
	
	private static Object mutex;

	//	no direct construction
	private Packet() {
		llh = new int[MAXLLH];
		buf = new int[MAXW];
		len = 0;
		status = FREE;
		interf = null;
	}

	private static Packet[] packets;

	static {
		mutex = new Object();

		packets = new Packet[CNT];
		for (int i=0; i<CNT; ++i) { // @WCA loop=8
			packets[i] = new Packet();
		}
		
	}

private static void dbg() {

	synchronized (mutex) {
		Dbg.wr('|');
		for (int i=0; i<CNT; ++i) { // @WCA loop=8
			Dbg.wr('0'+packets[i].status);
		}
		Dbg.wr('|');
	}
}


/**
*	Request a packet of a given type from the pool and set new type.
*/
	public static Packet getPacket(int type, int newType) {

		int i;
		Packet p;

		synchronized (mutex) {
			for (i=0; i<CNT; ++i) { // @WCA loop=8
				if (packets[i].status==type) {
					break;
				}
			}
			if (i==CNT) {
if (type==FREE) Dbg.wr('!');
				return null;
			}
			packets[i].status = newType;
			p = packets[i];
		}
// dbg();
		return p;
	}

/**
*	Request a packet of a given type from the pool and set new type and source.
*/
	public static Packet getPacket(int type, int newType, LinkLayer s) {

		int i;
		Packet p;

		synchronized (mutex) {
			for (i=0; i<CNT; ++i) { // @WCA loop=8
				if (packets[i].status==type) {
					break;
				}
			}
			if (i==CNT) {
if (type==FREE) Dbg.wr('!');
				return null;
			}
			packets[i].status = newType;
			packets[i].interf = s;
			p = packets[i];
		}
// dbg();
		return p;
	}

/**
*	Request a packet of a given type and source from the pool and set new type.
*/
	public static Packet getPacket(LinkLayer s, int type, int newType) {

		int i;
		Packet p = null;

		synchronized (mutex) {
			for (i=0; i<CNT; ++i) { // @WCA loop=8
				p = packets[i];
				if (p.status==type && packets[i].interf==s) {
					p.status = newType;
					break;
				}
			}
		}
		if (i==CNT) {
			p = null;
		}
			
		return p;
	}
	/**
	 * Get a packet with either status SND or SND_TCP.
	 * Keep the status as it is needed in the link layer.
	 * @param s link layer for this packet.
	 * @return a Packet
	 */
	public static Packet getTxPacket(LinkLayer s) {
		int i;
		Packet p = null;

		synchronized (mutex) {
			for (i=0; i<CNT; ++i) { // @WCA loop=8
				p = packets[i];
				if ((p.status==SND_DGRAM || p.status==SND_TCP)
						&& packets[i].interf==s) {
					break;
				}
			}
		}
		if (i==CNT) {
			p = null;
		}
			
		return p;
	}

	public void setStatus(int v) {

		synchronized (mutex) {
			status = v;
		}
// dbg();
	}

	public int getStatus() {
		return status;
	}

	public LinkLayer getLinkLayer() {
		return interf;
	}


	/**
	 * Make a deep copy from Packet p. Used just for ARP requests
	 * with a TCP packet as the TCP packet is kept in the connection.
	 * @param p
	 */
	public void copy(Packet p) {

		int i;
		
		synchronized (mutex) {
			this.len = p.len;
			this.interf = p.interf;
			for (i=0; i<MAXLLH; ++i) {  // @WCA loop=7
				this.llh[i] = p.llh[i];
			}
			for (i=0; i<MAXW; ++i) {  // @WCA loop=375
				this.buf[i] = p.buf[i];
			}
		}
	}
}
