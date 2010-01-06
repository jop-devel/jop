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

package ejip;

/**
*	Packet buffer handling for a minimalistic TCP/IP stack.
*
*/
public class Packet {

	public final static int MAXLLH = 7;		// 7 16 bit words for ethernet

	/** interface source/destination */
	public LinkLayer interf;
	/** place for link layer data */
	public int[] llh;
	/** Buffer for the ip datagram */
	public int[] buf;
	/** Packet length in bytes */
	public int len;
	/** Mark as TCP packet on the fly. Don't free it in the link layer. */
	public boolean isTcpOnFly;
		
	/**
	 * Create a packet with maximum length. 
	 */
	Packet(int pktSize) {
		llh = new int[MAXLLH];
		buf = new int[(pktSize+3)>>2]; // DFA likes shift, but not division
		len = 0;
		interf = null;
	}


	public LinkLayer getLinkLayer() {
		return interf;
	}
	
	public void setLinkLayer(LinkLayer ll) {
		interf = ll;
	}


	/**
	 * Make a deep copy from Packet p. Used just for ARP requests
	 * with a TCP packet as the TCP packet is kept in the connection.
	 * @param p
	 */
	synchronized public void copy(Packet p) {

		int i;
		
		this.len = p.len;
		this.interf = p.interf;
		for (i=0; i<MAXLLH; ++i) {
			this.llh[i] = p.llh[i];
		}
		for (i=0; i<buf.length; ++i) {
			this.buf[i] = p.buf[i];
		}
	}
}
