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


/**
 * 
 */
package ejip;

/**
 * The Embedded Java IP stack.
 * 
 * @author Martin Schoeberl
 * 
 * TODO list:
 *		TcpConnection: outstanding packet shall be returned to the free pool.
 *			remove static array
 *		Merge Net and Ejip -> remove Net reference in OEBB Logging and other apps
 *		There are still some statics around
 *		Merge TFTP implementations and test it with OEBB
 */
public class Ejip {

	/**
	 * Enable the experimental TCP implementation
	 */
	public static final boolean TCP_ENABLED = true;

	// default values
	private final static int MAX = 1500;		// maximum Packet length in bytes
	public final static int CNT = 8;			// size of packet pool

	private PacketQueue freePool;
	private int maxLength;
	private int maxPackets;
	
	// TODO: annoying that arrays of generics don't work
	// So we have only one link layer at the moment - we would need
	// a list...
	PacketQueue llRxQueue;
	
	/**
	 * Initialize the Ejip stack.
	 * @param nrPkt number of IP packets to use.
	 * @param pktSize maximum size of an IP packet.
	 */
	public Ejip(int nrPkt, int pktSize) {
		freePool = new PacketQueue(nrPkt);
		maxLength = pktSize;
		maxPackets = nrPkt;
		for (int i=0; i<nrPkt; ++i) {
			freePool.enq(new Packet(pktSize));
		}
	}

	public Ejip() {
		this(CNT, MAX);
	}

	synchronized public void registerLinkLayer(LinkLayer ll) {
//		int cnt = llRxQueues==null ? 1 : llRxQueues.length+1;
//		// this does not work
//		SRSWQueue<Packet> tmp[] = (SRSWQueue<Packet> []) new Object[cnt];
//		for (int i=0; i<cnt-1; ++i) {
//			tmp[i] = llRxQueues[i];
//		}
//		tmp[cnt-1] = ll.rxQueue;
//		llRxQueues = tmp;
		
		llRxQueue = ll.rxQueue;
	}
	/**
	 * Get a free packet, return null if the free pool is empty.
	 * We could use two mutexes for enqueue and dequeue.
	 * @return
	 */
	synchronized public Packet getFreePacket(LinkLayer link) {
		Packet p = freePool.deq();
		if (Logging.LOG) {
			Logging.wr("get: free packets: ");
			Logging.intVal(freePool.cnt());
			Logging.lf();
		}
		if (p!=null) p.interf = link;
		return p;
	}
	
	/**
	 * Return a packet into the free pool.
	 * @param p
	 */
	synchronized public void returnPacket(Packet p) {
		freePool.enq(p);
	}

	/**
	 * Maximum length of an IP packet.
	 * @return
	 */
	public int getMaxLength() {
		return maxLength;
	}

	public int getMaxPackets() {
		return maxPackets;
	}
	
	public static int makeIp(int a, int b, int c, int d) {
		return (a<<24) + (b<<16) + (c<<8) + d;
	}
}
