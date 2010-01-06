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

/**
 * Represents a single TCP connection.
 * 
 * 
 * 
 * @author Martin
 * 
 */

public class TcpConnection {
	
	/**
	 * State of the TCP connection.
	 */
	int state;
	int localIP; // do we need it?
	int remoteIP;
	int localPort;
	int remotePort;
	
	/**
	 * The next expected receive sequence number.
	 * Without the length of the incoming package.
	 */
	int rcvNxt;
	/**
	 * The last sent sequence number.
	 */
	int sndNxt;
	/**
	 * The outstandig packet. We only allow one packet on the fly.
	 */
	Packet outStanding;
	/**
	 * Timeout for retransmit of the outstanding packet. Will be
	 * decremented and retransmit on 0
	 */
	int timeout;
	
	/**
	 * Retransmission counter.
	 */
	int retryCnt;

	/**
	 * We shut down the connection after being idle for too long.
	 */	
	int idleTime;
	
	/**
	 * Maximum number of active TCP connections
	 */
	final static int CNT = 10;
	static TcpConnection[] connections;
	
	private static Object mutex = new Object();
	static {
		connections = new TcpConnection[CNT];
		for (int i=0; i<CNT; ++i) {
			connections[i] = new TcpConnection();
		}
	}
	
	
	private TcpConnection() {
		state = Tcp.FREE;
		// not needed, right?
//		Packet os = outStanding;
//		outStanding = null;
//		if (os!=null) {
//			os.setStatus(Packet.FREE);			
//		}
	}
	
	public static TcpConnection findConnection(Packet p) {
		
		int[] buf = p.buf;

		int dstPort = buf[Tcp.HEAD];
		int srcPort = dstPort >>> 16;
		dstPort &= 0xffff;
		int src = buf[Ip.SOURCE];
		int dest = buf[Ip.DESTINATION];
		
		return findConnection(src, srcPort, dest, dstPort);
	}

	public static TcpConnection findConnection(int src, int srcPort, int dest, int dstPort) {
				
		TcpConnection free = null;
		TcpConnection conn = null;
				
		synchronized (mutex) {
			for (int i=0; i<CNT; ++i) {
				TcpConnection tc = connections[i];
				if (tc.state!=Tcp.FREE) {
					if (dstPort==tc.localPort &&
						srcPort==tc.remotePort &&
						src==tc.remoteIP &&
						dest==tc.localIP) {
						
						conn = tc;
						break;
					}
				} else {
					if (free==null) {
						free = tc;
					}
				}
			}
			// if not found get a new one when possible
			if (conn==null) {
				conn = free;
				if (free!=null) {
					free.state = Tcp.CLOSED;
					free.localPort = dstPort;
					free.remotePort = srcPort;
					free.remoteIP = src;
					free.localIP = dest;
				}
			}

			if (conn != null) {
				// we use it, so we're not idle
				conn.idleTime = 0;
			}
		}
		
		int cnt=0;
		for (int i=0; i<CNT; ++i ) {
			if (connections[i].state!=Tcp.FREE) {
				++cnt;
			}
		}

		if (Logging.LOG) {
			Logging.wr("getCon: con in use: ");
			Logging.intVal(cnt);
			Logging.lf();
		}

		return conn;
	}
	
	public static TcpConnection getFreeConnection() {
		
		// local port number is the connection number
		// tc.localPort = 1024+i;
		return null;
	}

	/**
	 * Close the connection and return any outstanding packet to the pool.
	 *
	 */
	public void close(Ejip ejip) {

		synchronized (mutex) {
			if (outStanding != null) {
				Packet os = outStanding;
				// recycle the outstanding packet and reset isTcpOnFly
				outStanding = null;
				os.isTcpOnFly = false;
				ejip.returnPacket(os);
			}
			state = Tcp.FREE;
			outStanding = null;
		}
	}
}
