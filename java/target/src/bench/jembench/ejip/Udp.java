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

package jembench.ejip;

// UDP Debug output disabled

/*
*   Changelog:
*		2002-10-24	creation.
*		2010-01-06	No Tftp handler as default.
*
*	TODO: in Udp, TcpIp.... when to use the packet with automatic reply?
*/


/**
 * @author martin
 *
 *	UDP functions.
*/

public class Udp implements Runnable {

	public static final int PROTOCOL = 17;

	public static final int HEAD = 5;	// offset of udp header in words
	public static final int DATA = 7;	// offset of data in words

	private Object monitor;

	public static final int MAX_HANDLER = 8;
	private UdpHandler[] list;
	private int[] ports;
	private int loopCnt;
	
	private Ejip ejip;

	public Udp(Ejip ejipRef) {
		ejip = ejipRef;
		if (monitor!=null) return;
		monitor = new Object();
		list = new UdpHandler[MAX_HANDLER];
		ports = new int[MAX_HANDLER];
		loopCnt = 0;
	}
	/**
	*	add a handler for UDP requests.
	*	returns false if list is full.
	*/
	public boolean addHandler(int port, UdpHandler h) {

		synchronized(monitor) {
			for (int i=0; i<MAX_HANDLER; ++i) {
				if (list[i]==null) {
					ports[i] = port;
					list[i] = h;
					return true;
				}
			}
		}
		return false;
	}

	/**
	*	remove a handler for UDP requests.
	*	returns false if it was not in the list.
	*/
	public boolean removeHandler(int port) {

		synchronized(monitor) {
			for (int i=0; i<MAX_HANDLER; ++i) {
				if (list[i]!=null) {
					if (ports[i] == port) {
						list[i] = null;
						return true;
					}
				}
			}
		}
		return false;
	}
	/**
	*	Called periodic from Net for timeout processing.
	*/
	public void run() {

		int i = loopCnt;

		if (list[i]!=null) {
			list[i].loop();
			++i;
			if (i==MAX_HANDLER) i=0;
		} else {
			i = 0;
		}
		loopCnt = i;
	}
	/**
	*	process packet and generate reply if necessary.
	*/
	void process(Packet p) {

		int i, j;
		int[] buf = p.buf;

		int port = buf[HEAD];
		int remport = port >>> 16;
		port &= 0xffff;

		buf[2] = (PROTOCOL<<16) + p.len - 20; 		// set protocol and udp length in iph checksum for tcp checksum
		if (Ip.chkSum(buf, 2, p.len-8)!=0) {
			if (Logging.LOG) Logging.wr("wrong UDP checksum ");
			ejip.returnPacket(p);	// mark packet free
			return;
		}

		if (port == 1625) {

			// do the Dgb thing!
//			i = Dbg.readBuffer(buf, 7);
//			p.len = 28+i;
			// generate a reply with IP src/dst exchanged
			Udp.build(p, buf[4], buf[3], remport);

		} else {

			if (list!=null) {
				for (i=0; i<MAX_HANDLER; ++i) {
					if (list[i]!=null && ports[i]==port) {
						list[i].request(p);
						break;
					}
				}
				if (i==MAX_HANDLER) {
					ejip.returnPacket(p);	// mark packet free
					if (Logging.LOG) Logging.lf();
					if (Logging.LOG) Logging.wr('U');
					if (Logging.LOG) Logging.intVal(port);
				}
			} else {
				ejip.returnPacket(p);
			}
		}
	}
	
	/**
	 * Generate a reply with IP src/dst exchanged.
	 * @param p
	 */
	public static void reply(Packet p) {
		
		int[] buf = p.buf;
		Udp.build(p, buf[4], buf[3], buf[HEAD]>>>16);
	}

	/**
	*	Get source IP from interface and build IP/UDP header.
	*/
	public void build(Packet p, int dstIp, int port) {

		int srcIp = p.interf.getIpAddress();
		if (srcIp==0) {						// interface is down
			ejip.returnPacket(p);			// mark packet free
		} else {
			build(p, srcIp, dstIp, port);
		}
	}

	/**
	*	Fill UDP and IP header and mark packet ready to send.
	*/
	public static void build(Packet p, int srcIp, int dstIp, int port) {

		int i;
		int[] buf = p.buf;

		// IP header
		// TODO unique id for sent packet
		buf[0] = 0x45000000 + p.len;		// ip length	(header without options)
		buf[1] = Ip.getId();				// identification, no fragmentation
		buf[3] = srcIp;
		buf[4] = dstIp;

		// UDP header
		// 'set' port numbers
		buf[HEAD] = ((port+10000)<<16) + port;		// src port = dst port + 10000
		// Fill in UDP header
		buf[HEAD+1] = (p.len-20)<<16;
		buf[2] = (PROTOCOL<<16) + p.len - 20; 		// set protocol and udp length in iph checksum for tcp checksum
		i = Ip.chkSum(buf, 2, p.len-8);
		if (i==0) i = 0xffff;
		buf[HEAD+1] |= i;

		// for UDP checksum used field of IP header
		buf[2] = (0x20<<24) + (PROTOCOL<<16);	// ttl, protocol, clear checksum
		buf[2] |= Ip.chkSum(buf, 0, 20);

		p.llh[6] = 0x0800;
		p.interf.txQueue.enq(p);	// mark packet ready to send
	}
}
