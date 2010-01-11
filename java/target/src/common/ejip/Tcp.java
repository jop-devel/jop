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

/**
 * TCP functions.
 * 
 * @author martin
 * 
 * TODO: timer shall be an int with the correct subtraction calculation
 * 		see Timer.java
 *
 */

public class Tcp implements Runnable {

	/**
	 * TCP protocol number
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
	 * 500 ms timer tick for the timeout handlers
	 */
	public static final int TIMER_TICK = 500;

	/**
	 * 8 seconds until a connection is timed out for TIME_WAIT (very short!)
	 */
	public static final int TIME_WAIT_TIMEOUT = 8000/TIMER_TICK;

	/**
	 * 60 seconds until a connection is timed out when being idle
	 */
	public static final int USER_TIMEOUT = 60000/TIMER_TICK;


	/**
	 * Timout for retransmit in timer ticks.
	 */
	public static final int TIMEOUT = 2000/TIMER_TICK;
	
	/**
	 * Maximum retransmissions.
	 */
	public static final int MAX_RETRANSMIT = 10;
	
	/**
	 * The timer.
	 */
	private static int timer;

	private static Object mutex;

	public static final int MAX_HANDLER = 8;
	private TcpHandler[] list;
	private int[] ports;
	private int loopCnt;
	private int conLoopCnt;
	
	private Ejip ejip;

	public Tcp(Ejip ejipRef) {
		ejip = ejipRef;
		mutex = new Object();
		list = new TcpHandler[MAX_HANDLER];
		ports = new int[MAX_HANDLER];
		loopCnt = 0;
		conLoopCnt = 0;
		// TODO: why is this a (char)?
		timer = (char)System.currentTimeMillis() + TIMER_TICK;
	}
	/**
	*	add a handler for TCP requests.
	*	returns false if list is full.
	*/
	public boolean addHandler(int port, TcpHandler h) {


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
	public boolean removeHandler(int port) {

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
	*	Called periodically from Net for timeout processing.
	*/
	public void run() {

		int i;

		if ((short)(timer - System.currentTimeMillis()) < 0) {

			// this is probably a quite big synchronized block
			// for a SLIP connection
			synchronized (mutex) {
				TcpConnection tc = TcpConnection.connections[conLoopCnt];

//				if (Logging.LOG) {
//					Logging.intVal(tc.idleTime);
//					Logging.wr("; ");
//				}
				
				tc.idleTime--;
				if (tc.state != FREE && tc.idleTime < 0) {
					if (Logging.LOG) {
						Logging.wr("Forced shutdown");
						Logging.lf();
					}
					tc.close(ejip);
					return;
				}

				if (tc.outStanding!=null) {
					tc.timeout--;
					if (tc.timeout==0) {
						// TODO: exponential backoff 
						tc.retryCnt++;
						if (tc.retryCnt==MAX_RETRANSMIT) {
							Logging.wr("maximum retransmit - close");
							tc.close(ejip);
							tc.retryCnt=0;
						} else {
							tc.timeout = TIMEOUT;
							// let it retransmit
							Logging.wr("retransmit");
							tc.outStanding.interf.txQueue.enq(tc.outStanding);								
						}
					}
				}
			}
			
			++conLoopCnt;
			// All connection done, restart timer
			if (conLoopCnt==TcpConnection.CNT) {
				conLoopCnt = 0;				
				timer = (short)(System.currentTimeMillis() + TIMER_TICK);
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
	void process(Packet p) {

		int[] buf = p.buf;

		// TODO add options on checksum
		
		buf[Ip.CHKSUM] = (PROTOCOL<<16) + p.len - 20; 		// set protocol and TCP length in iph checksum for tcp checksum
		if (Ip.chkSum(buf, 2, p.len-8)!=0) {
			ejip.returnPacket(p);	// mark packet free
			return;
		}

		TcpConnection tc = TcpConnection.findConnection(p);
		// connection pool is empty, drop the packet
		if (tc==null) {
			ejip.returnPacket(p);
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
			ejip.returnPacket(p);
			tc.close(ejip);
			if (Logging.LOG) Logging.lf();
			if (Logging.LOG) Logging.wr('T');
			if (Logging.LOG) Logging.intVal(buf[HEAD] & 0xffff);
			return;
		}

		// do the TCP state machine
		handleState(p, th, tc);
	}
	
	/**
	 * Handle the TCP state machine
	 * @param p incoming packet
	 * @param tc connection
	 * @return false means nothing more to do
	 */
	private void handleState(Packet p, TcpHandler th, TcpConnection tc) {

		// TODO: do we need synchronized for handle state?
		// or synchronized handling of connection change?
		
		// we only come into this when a handler is registered
		// so there is no use of state closed
		if (tc.state==CLOSED) {
			tc.state = LISTEN;
		}
		int state = tc.state;
		
		int buf[] = p.buf;
		
		int i = buf[8] >>> 16;
		int flags = i & 0xff;
		int hlen = i >>> 12;
		int datlen = p.len - 20 - (hlen << 2);

		Packet h;

		if (Logging.LOG) {
			Logging.wr("TCP state: ");
			Logging.intVal(state);
//			Logging.wr(" len ");
//			Logging.intVal(datlen);
			Logging.lf();
		}

		if (!checkConnection(p, th, tc, flags, state)) {
			return;
		}

		// we received _something_ on this connection
		synchronized (mutex) {
			tc.idleTime = USER_TIMEOUT;
		}

		if (!checkAck(p, th, tc, flags)) {
			return;
		}
		
		switch(state) {
		case Tcp.CLOSED:
			// we should not receive a packet in state CLOSED
			// that means no one is listening
			// shall we send a RST?
			// we don't ever get here!
			if (Logging.LOG) {
				Logging.wr("shutdown from CLOSED ");
			}
			ejip.returnPacket(p);
			tc.close(ejip);
			return;
		case Tcp.LISTEN:
			// set the connection for the handler
			th.connection = tc;
			// TODO: read options (MSS)
			tc.rcvNxt = buf[SEQNR]+1;
			tc.sndNxt = (int)System.currentTimeMillis();	// TODO: get time dependent initial seqnrs
			buf[OPTION] = 0x02040000 + 512;	// set MSS to 512
			p.len = (OPTION+1)<<2;	// len in bytes
			fillHeader(p, tc, FL_SYN|FL_ACK);
			tc.sndNxt++;		// SYN send counts for one
			tc.state = SYN_RCVD;
			break;
		case Tcp.SYN_RCVD:
			if ((flags&FL_ACK) != 0 && buf[ACKNR]==tc.sndNxt) {
				if (Logging.LOG) {
					Logging.wr("SYN acked ");
				}
				tc.state = ESTABLISHED;
				h = th.established(p);
				if (h != null) {
					// only reply if necessary
					fillHeader(h, tc, FL_ACK);
					tc.sndNxt += p.len-(DATA<<2);
				} else {
					ejip.returnPacket(p);
				}
			} else {
				ejip.returnPacket(p);				
			}
			break;
		case Tcp.SYN_SENT:
			if ((flags&FL_SYN) == 0) {
				ejip.returnPacket(p);
				if (Logging.LOG) {
					Logging.wr("dropped non SYN packet in SYN_SENT ");
				}
				return;
			}
			tc.rcvNxt = buf[SEQNR]+1;
			p.len = DATA<<2;	// len in bytes
			tc.state = ESTABLISHED;
			h = th.established(p);
			if (h == null) {
				// return plain ack
				Ip.setData(p, Tcp.DATA, "");
				fillHeader(p, tc, FL_ACK);
				tc.sndNxt += p.len-(DATA<<2);
			} else {
				fillHeader(h, tc, FL_ACK);
				tc.sndNxt += h.len-(DATA<<2);
			}
			break;

		case Tcp.ESTABLISHED:
			if ((flags&FL_FIN)!=0) {
				if (Logging.LOG) {
					Logging.wr("FIN received ");
				}
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

				if (Logging.LOG) {
					Logging.wr("do request ");
				}
				h = th.request(p);
				tc.rcvNxt += len;

				if (h==null) {
					if (flags==FL_ACK && len==0 && !th.finished()) {
						// nothing to send and nothing to ack
					} else {
						flags = FL_ACK | (th.finished() ? FL_FIN : 0);
						// return plain ack
						Ip.setData(p, Tcp.DATA, "");
						fillHeader(p, tc, flags);
						tc.sndNxt += p.len-(DATA<<2);
					}
				} else {
					flags = FL_ACK | (th.finished() ? FL_FIN : 0);
					fillHeader(h, tc, flags);
					tc.sndNxt += h.len-(DATA<<2);
				}

				if (th.finished()) {
					if (Logging.LOG) {
						Logging.wr("send FIN ");
					}
					tc.sndNxt++;
					tc.state = FIN_WAIT_1;
				}

			} else {
				ejip.returnPacket(p);
				// TODO ack last segment
				if (Logging.LOG) {
					Logging.wr("dropped wrong SEQNR ");
				}
			}
			break;
		case Tcp.CLOSE_WAIT:
			// we do not need this state as we are not interested
			// in half open connections
			if (Logging.LOG) {
				Logging.wr("spurious state CLOSE_WAIT ");
			}			
			ejip.returnPacket(p);
			break;
		case Tcp.LAST_ACK:
			if (Logging.LOG) {
				Logging.wr("shutdown from LAST_ACK ");
			}
			// reset the connection for the handler
			th.connection = null;			
			ejip.returnPacket(p);
			tc.close(ejip);
			break;
		case Tcp.FIN_WAIT_1:
			// reset the connection for the handler
			th.connection = null;

			tc.rcvNxt = buf[SEQNR]+1;			
			if ((flags&FL_FIN) != 0) {
				fillHeader(p, tc, FL_ACK);
				tc.sndNxt++;		// FIN send counts for one
				if ((flags&FL_ACK) != 0) {
					if (Logging.LOG) {
						Logging.wr("wait from FIN_WAIT_1 ");
					}
					tc.state = TIME_WAIT;
					tc.idleTime = TIME_WAIT_TIMEOUT;
				} else {
					tc.state = CLOSING;
				}
			} else {
				ejip.returnPacket(p);
				tc.state = FIN_WAIT_2;
			}
			break;
		case Tcp.FIN_WAIT_2:
			tc.rcvNxt = buf[SEQNR]+1;			
			if ((flags&FL_FIN) != 0) {
				fillHeader(p, tc, FL_ACK);
				tc.sndNxt++;		// FIN send counts for one
				if (Logging.LOG) {
					Logging.wr("wait from FIN_WAIT_2 ");
				}
				tc.state = TIME_WAIT;
				tc.idleTime = TIME_WAIT_TIMEOUT;
			} else {
				ejip.returnPacket(p);
			}
			break;
		case Tcp.CLOSING:
			if (Logging.LOG) {
				Logging.wr("shutdown from CLOSING ");
			}
			// drop packet, but keep connection alive
			ejip.returnPacket(p);
			tc.state = TIME_WAIT;
			tc.idleTime = TIME_WAIT_TIMEOUT;
			break;
		case Tcp.TIME_WAIT:
			if (Logging.LOG) {
				Logging.wr("dropped packet in TIME_WAIT ");
			}
			// just discard packet
			ejip.returnPacket(p);
			break;
		}
	}

	/**
	 * TODO: explain ... at least packet handling...
	 * 
	 * Consumed the packet (either returned or sent) when returning false.
	 * On true return the packet still needs to be handeled.
	 * @param p
	 * @param th
	 * @param tc
	 * @param flags
	 * @param state
	 * @return
	 */
	private boolean checkConnection(Packet p, TcpHandler th, TcpConnection tc, int flags, int state) {

		int buf[] = p.buf;

		// reset the connection
		synchronized (mutex) {
			if ((flags&FL_RST)!=0) {
				if (Logging.LOG) {
					Logging.wr("RST received");
					Logging.lf();
				}
				// reset the connection for the handler
				th.connection = null;

				ejip.returnPacket(p);
				tc.close(ejip);
				return false;
			}
		}
		// reset if what we received does not match our state
		synchronized (mutex) {
			if (state == LISTEN && (flags & FL_SYN) == 0) {
				//ejip.returnPacket(p);
				// reset the connection
				tc.rcvNxt = buf[SEQNR]+p.len-(DATA<<2);
				tc.sndNxt = buf[ACKNR];
				Ip.setData(p, Tcp.DATA, "");
				fillHeader(p, tc, FL_RST|FL_ACK);
				if (Logging.LOG) {
					Logging.wr("dropped non SYN packet in LISTEN");
					Logging.lf();
				}
				return false;
			}
		}

		// check if the handler is busy
		synchronized (mutex) {
			if (th.connection != null
				&& th.connection != tc
				&& th.connection.state != Tcp.FREE) {
// 				System.out.print("wrong connection: ");
// 				System.out.print(tc);
// 				System.out.print(" got already ");
// 				System.out.print(th.connection);
// 				System.out.print(" state ");
// 				System.out.println(th.connection.state);
				ejip.returnPacket(p);
				return false;
			}
		}

		return true;
	}


	/**
	 * TODO: explain...
	 * 
	 * Packet consumed on false return.
	 * 
	 * @param p
	 * @param th
	 * @param tc
	 * @param flags
	 * @return
	 */
	private boolean checkAck(Packet p, TcpHandler th, TcpConnection tc, int flags) {

		int buf[] = p.buf;

		// check for ACK
		synchronized (mutex) {
			if ((flags&FL_ACK)!=0 && tc.outStanding!=null) {
				if (buf[ACKNR]==tc.sndNxt) {
					if (Logging.LOG) {
						Logging.wr("ACK received");
						Logging.lf();
					}
					Packet os = tc.outStanding;
					// recycle the outstanding packet and reset isTcpOnFly
					tc.outStanding = null;
					os.isTcpOnFly = false;
					ejip.returnPacket(os);
				} else {
					// not the correct ACK - drop it
					ejip.returnPacket(p);
					if (Logging.LOG) {
						Logging.wr("dropped wrong ACKNR");
						Logging.lf();
					}
					return false;
				}
			}			
		}		
		if (tc.outStanding!=null) {
			// we handle only one packet at a time
			// so we have to drop it.
			ejip.returnPacket(p);
			if (Logging.LOG) {
				Logging.wr("waiting on ACK - dropped");
				Logging.lf();
			}
			return false;
		}

		return true;
	}

	public void startConnection(LinkLayer ll, int ip, int port) {
		Packet p = ejip.getFreePacket(ll);
		p.buf[OPTION] = 0x02040000 + 512;	// set MSS to 512
		p.len = (OPTION+1)<<2;	// len in bytes
		TcpConnection tc = TcpConnection.findConnection(ip, port, ll.getIpAddress(), 10000+port);
		tc.sndNxt = (int)System.currentTimeMillis();	// TODO: get time dependent initial seqnrs
		fillHeader(p, tc, FL_SYN);
		tc.sndNxt++;		// SYN send counts for one
		tc.state = SYN_SENT;
	}

	/**
	 * Fill in the header and enqueue the packet into the send queue.
	 * 
	 * @param p
	 * @param tc
	 * @param fl
	 */
	void fillHeader(Packet p, TcpConnection tc, int fl) {

		// Do we really free it here?
		if (p.len==0) {
			ejip.returnPacket(p); // mark packet free
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
		buf[ACKNR] = (fl & FL_ACK) != 0 ? tc.rcvNxt : 0;
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
			p.isTcpOnFly = true;
		}

		// we send _something_ on this connection
		synchronized (mutex) {
			tc.idleTime = USER_TIMEOUT;
		}

		p.interf.txQueue.enq(p);
		
	}
}
