/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2009, Martin Schoeberl (martin@jopdesign.com)

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


/**
 * 
 */
package jembench.ejip;

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
