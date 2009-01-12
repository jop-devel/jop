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

	/**
	 * TCP protocl number
	 */
	public static final int PROTOCOL = 6;

	// TCP connection states
	public final static int FREE = -1;

	public final static int CLOSED = 0;

	public final static int LISTEN = 1;

	public final static int SYN_RCVD = 2;

	public final static int SYN_SENT = 3;

	public final static int ESTABLISHED = 4;

	public final static int CLOSE_WAIT = 5;

	public final static int LAST_ACK = 6;

	public final static int FIN_WAIT_1 = 7;

	public final static int FIN_WAIT_2 = 8;

	public final static int CLOSING = 9;

	public final static int TIME_WAIT = 10;

	// TCP flags
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
	public static final int OPTION = 10;
	/**
	 * Offset of data in words when no options present
	 */
	public static final int DATA = 10;
	
	/**
	 * 500 ms timer tick for the timout handlers
	 */
	public static final int TIMER_TICK = 500;
	/**
	 * Timout for retransmit in timer ticks.
	 */
	public static final int TIMEOUT = 4;
	/**
	 * The timer.
	 */
	private static int timer;

	private static Object mutex;

	public static final int MAX_HANDLER = 8;
	private static TcpHandler[] list;
	private static int[] ports;
	private static int loopCnt;

	static {
		mutex = new Object();
		list = new TcpHandler[MAX_HANDLER];
		ports = new int[MAX_HANDLER];
		loopCnt = 0;
		timer = ((int) System.currentTimeMillis()) + TIMER_TICK;
	}
	/**
	*	add a handler for TCP requests.
	*	returns false if list is full.
	*/
	public static boolean addHandler(int port, TcpHandler h) {


		synchronized(mutex) {
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

		synchronized(mutex) {
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

		int i;

		if (timer - ((int) System.currentTimeMillis())<0) {
			// do the TCP timeout
			timer = ((int) System.currentTimeMillis()) + TIMER_TICK;
			for (i=0; i<TcpConnection.CNT; ++i) {
				synchronized (mutex) {
					TcpConnection tc = TcpConnection.connections[i];
					if (tc.outStanding!=null) {
						tc.timeout--;
						if (tc.timeout==0) {
							// TODO: exponential backoff and cancel connection
							// after some time (2-7 minutes)
							tc.timeout = TIMEOUT;
							// let it retransmit
							System.out.println("retransmit");
							tc.outStanding.setStatus(Packet.SND_TCP);
						}
					}					
				}
			}
		} else {
			// do an application poll
			i = loopCnt;
			synchronized (mutex) {
				if (list[i]!=null) {
					list[i].run();
					++i;
					if (i==MAX_HANDLER) i=0;
				} else {
					i = 0;
				}				
			}
			loopCnt = i;
		}
	}
	/**
	*	process packet and generate reply if necessary.
	*/
	static void process(Packet p) {

		int[] buf = p.buf;

		// TODO add options on checksum
		
		buf[Ip.CHKSUM] = (PROTOCOL<<16) + p.len - 20; 		// set protocol and TCP length in iph checksum for tcp checksum
		if (Ip.chkSum(buf, 2, p.len-8)!=0) {
			p.setStatus(Packet.FREE);	// mark packet free
			return;
		}

		TcpConnection tc = TcpConnection.findConnection(p);
		// connection pool is empty, drop the packet
		if (tc==null) {
			p.setStatus(Packet.FREE);
			return;
		}
		// We could add the handler to the connection to find
		// it faster
		TcpHandler th = null;
		// is a handler registered for that port?
		for (int i=0; i<MAX_HANDLER; ++i) {
			synchronized (mutex) {
				if (list[i]!=null && ports[i]==tc.localPort) {
					th = list[i];
					break;
				}			
			}
		}
		// no handler found
		if (th==null) {
			p.setStatus(Packet.FREE);
			tc.close();
Dbg.lf();
Dbg.wr('T');
Dbg.intVal(buf[HEAD] & 0xffff);
			return;
		}
		
		// do the TCP state machine
		handleState(p, th, tc);
	}
	
	/**
	 * Handle the TCP state machine
	 * @param p incomming packet
	 * @param tc connection
	 * @return false means nothing more to do
	 */
	private static void handleState(Packet p, TcpHandler th, TcpConnection tc) {
		
		// TODO: do we need synchronized for handle state?
		// or synchronized handling of connection change?
		
		// we only come into this when a handler is registered
		// so there is no use of state closed
		if (tc.state==Tcp.CLOSED) {
			tc.state = Tcp.LISTEN;
		}
		int state = tc.state;
		
		int buf[] = p.buf;
		
		int i = buf[8] >>> 16;
		int flags = i & 0xff;
		int hlen = i >>> 12;
		int datlen = p.len - 20 - (hlen << 2);

		System.out.print("TCP state: ");
		System.out.print(state);
		System.out.print(" len ");
		System.out.println(datlen);

		synchronized (mutex) {
			if ((flags&FL_ACK) !=0 && tc.outStanding!=null) {
				if (buf[ACKNR]==tc.sndNxt) {
					System.out.println("ACK received");
					Packet os = tc.outStanding;
					tc.outStanding = null;
					os.setStatus(Packet.FREE);
					if (flags==FL_ACK && p.len==DATA<<2 &&
							state == ESTABLISHED) {
						// only ack - no more action
						System.out.println("just an ACK");
						p.setStatus(Packet.FREE);
						return;
					}
				} else {
					// not the correct ACK - drop it
					p.setStatus(Packet.FREE);
					return;
				}
			}			
		}
		
		if (tc.outStanding!=null) {
			// we handle only one packet at a time
			// so we have to drop it.
			System.out.println("waiting on ACK - dropped");
			p.setStatus(Packet.FREE);
			return;
		}
		
		switch(state) {
		case Tcp.CLOSED:
			// we should not receive a packet in state CLOSED
			// that means no one is listening
			// shall we send a RST?
			p.setStatus(Packet.FREE);
			tc.close();
			return;
		case Tcp.LISTEN:
			if ((flags&FL_SYN) == 0) {
				p.setStatus(Packet.FREE);
				tc.close();
				System.out.println("dropped non SYN packet");
				return;
			}
			// TODO: read options (MSS)
			tc.rcvNxt = buf[SEQNR]+1;
			tc.sndNxt = 123;	// TODO: get time dependent initial seqnrs
			buf[OPTION] = 0x02040000 + 512;	// set MSS to 512
			p.len = (OPTION+1)<<2;	// len in bytes
			fillHeader(p, tc, FL_SYN|FL_ACK);
			tc.sndNxt++;		// SYN send counts for one
			tc.state = Tcp.SYN_RCVD;
			break;
		case Tcp.SYN_RCVD:
			if ((flags&FL_ACK) != 0 && buf[ACKNR]==tc.sndNxt) {
				System.out.println("SYN acked");
				tc.state = Tcp.ESTABLISHED;
				th.established(p);
				fillHeader(p, tc, FL_ACK);
				tc.sndNxt += p.len-(DATA<<2);
			} else {
				p.setStatus(Packet.FREE);				
			}
			break;
		case Tcp.SYN_SENT:
			break;
		case Tcp.ESTABLISHED:
			if ((flags&FL_FIN)!=0) {
				System.out.println("do FIN");
				// TODO check SEQNR
				tc.rcvNxt = buf[SEQNR]+1;
				p.len = DATA<<2;
				fillHeader(p, tc, FL_ACK|FL_FIN);
				tc.sndNxt++;		// FIN send counts for one
				tc.state = LAST_ACK;
				break;
			}
			// again ignored any options
			int len = p.len-(DATA<<2);
			if (buf[SEQNR]==tc.rcvNxt) {
				tc.rcvNxt += len;
				System.out.println("do request");
				th.request(p);
				fillHeader(p, tc, FL_ACK);
				tc.sndNxt += p.len-(DATA<<2);
			} else {
				System.out.println("dropped wrong SEQNR");
				p.setStatus(Packet.FREE);
				// TODO ack last segment
			}
			break;
		case Tcp.CLOSE_WAIT:
			// we do not need this state as we are not interested
			// in half open connetions
			System.out.println("CLOSE_WAIT");
			break;
		case Tcp.LAST_ACK:
			p.setStatus(Packet.FREE);
			System.out.println("LAST_ACK");
			if (tc.outStanding==null) {
				System.out.println("we received the last ACK");
				tc.close();
			}
			break;
		case Tcp.FIN_WAIT_1:
			break;
		case Tcp.FIN_WAIT_2:
			break;
		case Tcp.CLOSING:
			break;
		case Tcp.TIME_WAIT:
			break;
		}
	}
	
	static void fillHeader(Packet p, TcpConnection tc, int fl) {

		// Do we really free it here?
		if (p.len==0) {
			p.setStatus(Packet.FREE); // mark packet free
			return;
		}
		int buf[] = p.buf;
		// IP header (without checksum)
		buf[0] = 0x45000000 + p.len; // ip length (header without options)
		buf[1] = Ip.getId(); // identification, no fragmentation
		buf[Ip.SOURCE] = tc.localIP;
		buf[Ip.DESTINATION] = tc.remoteIP;

		buf[HEAD] = (tc.localPort << 16) + tc.remotePort;
		buf[SEQNR] = tc.sndNxt;
		buf[ACKNR] = tc.rcvNxt;
		// TODO: set window according to buffer
		if ((fl&FL_SYN)!=0) {
			buf[FLAGS] = 0x60000000 + (fl << 16) + 512; // hlen = 24, mss option						
		} else {
			buf[FLAGS] = 0x50000000 + (fl << 16) + 512; // hlen = 20, no options			
		}
		buf[CHKSUM] = 0; // clear checksum field
		buf[Ip.CHKSUM] = (PROTOCOL << 16) + p.len - 20; // set protocol and tcp length
												// in iph checksum for tcp
												// checksum
		buf[CHKSUM] = Ip.chkSum(buf, 2, p.len - 8) << 16;
		// TODO: set to 0xffff if 0, or is this only in UDP?
		// fill in IP header, swap IP addresses and mark for send
		
		// for TCP checksum used field of IP header
		buf[Ip.CHKSUM] = (0x20 << 24) + (PROTOCOL << 16); // ttl, protocol, clear checksum
		buf[Ip.CHKSUM] |= Ip.chkSum(buf, 0, 20);
	
		p.llh[6] = 0x0800;
	
		// packets with data or the SYN/FIN set
		// need to be retransmitted
		if (p.len>(DATA<<2) || (fl & (FL_SYN|FL_FIN))!=0) {
			synchronized (mutex) {
				tc.outStanding = p;
				tc.timeout = TIMEOUT;				
			}
			p.setStatus(Packet.SND_TCP); // mark packet ready to send			
		} else {
			// probably just an ACK
			p.setStatus(Packet.SND_DGRAM); // mark packet ready to send			
		}
		
	}
}
