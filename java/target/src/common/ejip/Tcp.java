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

/*
*   Changelog:
*/

import util.Dbg;

/**
 * TCP functions.
 * 
 * @author martin
 *
 */

public class Tcp {

	public static final int PROTOCOL = 6;

	static final int FL_URG = 0x20;

	static final int FL_ACK = 0x10;

	static final int FL_PSH = 0x8;

	static final int FL_RST = 0x4;

	static final int FL_SYN = 0x2;

	static final int FL_FIN = 0x1;

	/**
	 * Offset of TCP header in words.
	 */
	public static final int HEAD = 5;
	/**
	 * Offset of sequence number.
	 */
	public static final int SEQNR = 6;
	/**
	 * Offset of acknowledgment number.
	 */
	public static final int ACKNR = 7;
	public static final int FLAGS = 8;
	public static final int CHKSUM = 9;
	/**
	 * Offset of data in words when no options present
	 */
	public static final int DATA = 10;
	

	private static Object monitor;

	public static final int MAX_HANDLER = 8;
	private static TcpHandler[] list;
	private static int[] ports;
	private static int loopCnt;

	static {
		monitor = new Object();
		list = new TcpHandler[MAX_HANDLER];
		ports = new int[MAX_HANDLER];
		loopCnt = 0;		
	}
	/**
	*	add a handler for TCP requests.
	*	returns false if list is full.
	*/
	public static boolean addHandler(int port, TcpHandler h) {


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
	*	remove a handler for TCP requests.
	*	returns false if it was not in the list.
	*/
	public static boolean removeHandler(int port) {

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
	static void loop() {

		int i = loopCnt;

		if (list[i]!=null) {
			list[i].run();
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
	static void process(Packet p) {

		int i, j;
		int[] buf = p.buf;

		int port = buf[HEAD];
		int remport = port >>> 16;
		port &= 0xffff;

		// TODO add options on checksum
		
		buf[2] = (PROTOCOL<<16) + p.len - 20; 		// set protocol and TCP length in iph checksum for tcp checksum
		if (Ip.chkSum(buf, 2, p.len-8)!=0) {
			Dbg.intVal(p.len);
			Dbg.wr(" : ");
			for (int k = 0; k < (p.len+3)/4; k++) {
				Dbg.hexVal(buf[k]);
			}
			p.setStatus(Packet.FREE);	// mark packet free
Dbg.wr("wrong TCP checksum ");
			return;
		}

		TcpConnection tc = TcpConnection.findConnection(p);
		// connection pool is empty, drop the packet
		if (tc==null) {
			p.setStatus(Packet.FREE);
			return;
		}
		
		if (handleState(p, tc)==false) {
			return;
		}
		
		if (list!=null) {
			for (i=0; i<MAX_HANDLER; ++i) {
				if (list[i]!=null && ports[i]==tc.localPort) {
					list[i].request(p);
					break;
				}
			}
			if (i==MAX_HANDLER) {
				p.setStatus(Packet.FREE);	// mark packet free
Dbg.lf();
Dbg.wr('T');
Dbg.intVal(port);
			}
		} else {
			p.setStatus(Packet.FREE);
		}
	}
	
	/**
	 * Handle the TCP state machine
	 * @param p incomming packet
	 * @param tc connection
	 * @return false means nothing more to do
	 */
	private static boolean handleState(Packet p, TcpConnection tc) {
		
		int state = tc.state;
		
		System.out.print("TCP state: ");
		System.out.print(state);
		
		int buf[] = p.buf;
		
		int i = buf[8] >>> 16;
		int flags = i & 0xff;
		int hlen = i >>> 12;
		int datlen = p.len - 20 - (hlen << 2);

		System.out.print("TCP state: ");
		System.out.print(state);
		System.out.print(" len ");
		System.out.println(datlen);
		
		switch(state) {
		case TcpConnection.CLOSED:
			if ((flags&FL_SYN) == 0) {
				p.setStatus(Packet.FREE);
				tc.setStatus(TcpConnection.FREE);
				System.out.println("dropped non SYN packet");
				return false;
			}
			tc.rcvNxt = buf[SEQNR]+1;
			tc.sndNxt = 123;	// TODO: get time dependent initial seqnrs
			fillHeader(p, tc, FL_SYN|FL_ACK);
			tc.state = TcpConnection.LISTEN;
			break;
		case TcpConnection.LISTEN:
		case TcpConnection.SYN_RCVD:
		case TcpConnection.SYN_SENT:
		case TcpConnection.ESTABLISHED:
		case TcpConnection.CLOSE_WAIT:
		case TcpConnection.LAST_ACK:
		case TcpConnection.FIN_WAIT_1:
		case TcpConnection.FIN_WAIT_2:
		case TcpConnection.CLOSING:
		case TcpConnection.TIME_WAIT:
			break;
		}
		return false;
	}
	
	static void fillHeader(Packet p, TcpConnection tc, int fl) {

		int buf[] = p.buf;
		buf[HEAD] = (tc.localPort << 16) + tc.remotePort;
		buf[SEQNR] = tc.sndNxt;
		buf[ACKNR] = tc.rcvNxt;
		// TODO: set window according to buffer
		buf[FLAGS] = 0x50000000 + (fl << 16) + 512; // hlen = 20, no options
		buf[CHKSUM] = 0; // clear checksum field
		buf[2] = (PROTOCOL << 16) + p.len - 20; // set protocol and tcp length
												// in iph checksum for tcp
												// checksum
		buf[CHKSUM] = Ip.chkSum(buf, 2, p.len - 8) << 16;
		// TODO: set to 0xffff if 0, or is this only in UDP?
		p.len = CHKSUM*4;
		// fill in IP header, swap IP addresses and mark for send
		// TODO: use our own build to avoid method invokation
		// see also copy from UDP code
		Ip.doIp(p, PROTOCOL);
	}


/* ==================================================================== */
/* this code is a copy from Udp.java!!!!! TODO: adapt it                */
	
	public static void getData(Packet p, StringBuffer s) {
		
		int[] buf = p.buf;
		s.setLength(0);
		for (int i = Tcp.DATA*4; i < p.len; i++) {
			s.append((char) ((buf[i>>2]>>(24 - ((i&3)<<3))) & 0xff));
		}
	}
	
	public static void setData(Packet p, StringBuffer s) {
		
		int[] buf = p.buf;
		int cnt = s.length();
		// copy buffer
		int k = 0;
		for (int i=0; i<cnt; i+=4) {
			for (int j=0; j<4; ++j) {
				k <<= 8;
				if (i+j < cnt) k += s.charAt(i+j);
			}
			buf[Tcp.DATA + (i>>>2)] = k;
		}

		p.len = Tcp.DATA*4+cnt;
	}
	/**
	 * Generate a reply with IP src/dst exchanged.
	 * @param p
	 */
	public static void reply(Packet p) {
		
		int[] buf = p.buf;
		Tcp.build(p, buf[4], buf[3], buf[HEAD]>>>16);
	}

	/**
	*	Get source IP from interface and build IP/UDP header.
	*/
	public static void build(Packet p, int dstIp, int port) {

		int srcIp = p.interf.getIpAddress();
		if (srcIp==0) {						// interface is down
			p.setStatus(Packet.FREE);		// mark packet free
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
		p.setStatus(Packet.SND);	// mark packet ready to send
	}
}
