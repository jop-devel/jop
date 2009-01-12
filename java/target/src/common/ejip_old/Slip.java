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

//
//	Nullmodem cable (both female):
//
//		1	NC
//		2	3
//		3	2
//		4	6
//		5	5
//		6	4
//		7	8
//		8	7
//		9	NC
//
package ejip_old;

/**
*	Slip.java
*
*	communicate with jopbb via serial line.
*/

import util.Dbg;
import util.Serial;

/**
*	Slip driver.
*/

public class Slip extends LinkLayer {

	private static final int MAX_BUF = 1500;		// or should we use 1006

	private static final int END = 0xc0;
	private static final int ESC = 0xdb;
	private static final int ESC_END = 0xdc;
	private static final int ESC_ESC = 0xdd;

private static int simSendErr, simRcvErr;

/**
*	receive buffer
*/
	private static int[] rbuf;
/**
*	send buffer
*/
	private static int[] sbuf;
/**
*	bytes received.
*/
	private static int cnt;
/**
*	mark escape sequence.
*/
	private static boolean esc;
/**
*	an ip packet is in the receive buffer.
*/
	private static boolean ready;

/**
*	bytes to be sent. 0 means txFree
*/
	private static int scnt;
/**
*	allready sent bytes.
*/
	private static int sent;


/**
*	The one and only reference to this object.
*/
	private static Slip single;
	
	private static Serial ser;

/**
*	private constructor. The singleton object is created in init().
*/
	private Slip() {
	}

/**
*	allocate buffer, start serial buffer and slip Thread.
*/
	public static LinkLayer init(Serial serPort, int ipAddr) {


		if (single != null) return single;	// allready called init()

		rbuf = new int[MAX_BUF];
		sbuf = new int[MAX_BUF];
		cnt = 0;
		esc = false;
		ready = false;
		scnt = 0;
		sent = 0;

		ser = serPort;

		single = new Slip();
		single.ip = ipAddr;

		return single;
	}

	public int getIpAddress() {
		return ip;
	}

	/**
	*	Set connection strings and connect.
	*/
	public void startConnection(StringBuffer dialstr, StringBuffer connect, StringBuffer user, StringBuffer passwd) {
		// useless in Slip
	}
	/**
	*	Forces the connection to be new established.
	*	On Slip ignored.
	*/
	public void reconnect() {
	}
	static int timer;
	
/**
*	main loop.
*/
	public void loop() {

		Packet p;

		//
		//	read, write loop
		//
		if (ipLoop()) {
			timer = 0;
		} else {
			++timer;
			if (timer>100 && cnt>0) {		// flush buffer on timeout (100*10ms)
				Dbg.wr('t');
for (int i=0; i<cnt; ++i) {
int val = rbuf[i];
if (val=='\r') {
Dbg.wr('r');
} else {
	Dbg.wr(val);
}
}
					cnt = 0;
					esc = false;
					ready = false;
					timer = 0;
					// send anything back for windoz slip version
				if (ser.txFreeCnt()>0) {
					ser.wr('C');
					ser.wr('L');
					ser.wr('I');
					ser.wr('E');
					ser.wr('N');
					ser.wr('T');
					ser.wr('S');
					ser.wr('E');
					ser.wr('R');
					ser.wr('V');
					ser.wr('E');
					ser.wr('R');
					ser.wr('\r');
				}
			}
		}

		//
		//	copy packet to packet buffer.
		//
		if (ready) {				// we got a packet
			read();
		}
		if (scnt==0) {				// transmit buffer is free
			//
			// get a ready to send packet with source from this driver.
			//
			p = Packet.getTxPacket(single);
			if (p!=null) {
				send(p);				// send one packet
			}
		}
	}

/**
*	get a Packet buffer and copy from receive buffer.
*/
	private static void read() {

		int i, j, k;

		Packet p = Packet.getPacket(Packet.FREE, Packet.ALLOC, single);
		if (p==null) {
Dbg.wr('!');
			cnt = 0;						// drop it
			ready = false;					// don't block the receiver
			return;
		}

		int[] pb = p.buf;

		rbuf[cnt] = 0;						// fill remaining bytes to word
		rbuf[cnt+1] = 0;					// boundry with 0 for UDP
		rbuf[cnt+2] = 0;					// checksum

		// copy buffer
		k = 0;
		for (i=0; i<cnt; i+=4) {
			for (j=0; j<4; ++j) {
				k <<= 8;
				k += rbuf[i+j];
			}
			pb[i>>>2] = k;
		}

		p.len = cnt;

Dbg.wr('r');
Dbg.intVal(cnt);
		cnt = 0;
		ready = false;

/*
++simRcvErr;
if (simRcvErr%5==1) {
p.setStatus(Packet.FREE);
Dbg.wr(" rcv dropped");
Dbg.lf();
return;
}
*/
		p.setStatus(Packet.RCV);		// inform upper layer
	}


/**
*	copy packet to send buffer.
*/
	private static void send(Packet p) {

		int i, k;
		int[] pb = p.buf;

Dbg.wr('s');
Dbg.intVal(p.len);

/*
++simSendErr;
if (simSendErr%7==3) {
p.setStatus(Packet.FREE);
Dbg.wr(" send dropped");
Dbg.lf();
return;
}
*/
		scnt = p.len;
		sent = 0;
		for (i=0; i<scnt; i+=4) {
			k = pb[i>>>2];
			sbuf[i] = k>>>24;
			sbuf[i+1] = (k>>>16)&0xff;
			sbuf[i+2] = (k>>>8)&0xff;
			sbuf[i+3] = k&0xff;
		}
		if (p.getStatus()==Packet.SND_TCP) {
			p.setStatus(Packet.TCP_ONFLY);		// mark on the fly
		} else {
			p.setStatus(Packet.FREE);		// mark packet free			
		}
	}

/* warum geht das nicht !!!!!
	private void loop() {
*/
/**
*	read from serial buffer and build an ip packet.
*	send a packet if one is in our send buffer.
*/
	private static boolean ipLoop() {

		int i;
		boolean ret = false;

		i = ser.rxCnt();
		if (i!=0) {
			ret = true;
			rcv(i);
		}
		if (cnt==MAX_BUF && !ready) cnt = 0;	// buffer full, but not ready => drop it
		if (scnt!=0) {
			i = ser.txFreeCnt();
			if (i>2) {	
				snd(i);
			}
		}

		return ret;
	}

/**
*	copy from send buffer to serial buffer.
*/
	private static void snd(int free) {

		int i;

		if (sent==0) {
			ser.wr(END);
			--free;
		}

		for (i=sent; free>1 && i<scnt; ++i) {

			int c = sbuf[i];
			if (c==END) {
				ser.wr(ESC);
				ser.wr(ESC_END);
				free -= 2;
			} else if (c==ESC) {
				ser.wr(ESC);
				ser.wr(ESC_ESC);
				free -= 2;
			} else {
				ser.wr(c);
				--free;
			}
		}
		sent = i;

		if (sent==scnt && free!=0) {
			ser.wr(END);
			scnt = 0;
			sent = 0;
		}
	}

/**
*	copy from serial buffer to receive buffer.
*/
	private static void rcv(int len) {

		int i;

		// get all bytes from serial buffer
		for (i=0; i<len && cnt<MAX_BUF; ++i) {

			if (ready) break;			// wait till buffer is copied

			int val = ser.rd();

			if (esc) {
				if (val == ESC_END) {
					rbuf[cnt++] = END;
				} else if (val == ESC_ESC) {
					rbuf[cnt++] = ESC;
				} else {
					rbuf[cnt++] = val;
				}
				esc = false;
				continue;
			}

			if (val == ESC) {
				esc = true;
			} else {
				esc = false;
				if (val==END) {
					if (cnt>=20) {
						ready = true;	// we got one packet
						break;
					} else {			// ignore too short packages
						cnt = 0;
						continue;
					}
				} else {
					rbuf[cnt++] = val;
				}
			}
		}
	}

/* (non-Javadoc)
 * @see ejip.LinkLayer#getConnCount()
 */
public int getConnCount() {
	return 0;
}


}
