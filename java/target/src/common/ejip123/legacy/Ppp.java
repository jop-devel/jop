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

package ejip123.legacy;

/**
*	Ppp.java
*
*	communicate with jop via serial line.
*/

import joprt.RtThread;
import ejip123.util.Dbg;
import ejip123.util.Serial;
import util.Timer;
import ejip123.LinkLayer;
import ejip123.Packet;
import ejip123.PacketPool;

/**
*	Ppp driver.
*/

public class Ppp extends LinkLayer{

	private static final int MAX_BUF = 1500+4;		// 1500 is PPP information field
/**
*	period for thread in us.
*/

	private static final int IP = 0x0021;			// Internet Protocol packet
	private static final int IPCP = 0x8021;			// Internet Protocol Configuration Protocol packet
	private static final int CCP = 0x80fd;			// Compression Configuration Protocol packet
	private static final int LCP = 0xc021;			// Link Configuration Protocol packet
	private static final int PAP = 0xc023;			// Password Authentication Protocol packet

	private static final int REQ = 1;				// Request options list
	private static final int ACK = 2;				// Acknowledge options list
	private static final int NAK = 3;				// Not acknowledge options list
	private static final int REJ = 4;				// Reject options list
	private static final int TERM = 5;				// Termination

	private static final int NEG_SEND= 3000;		// Period of send negotiation (in ms)
	private static final int IP_SEND= 10000;		// Send timout for ip for reconnect (in ms)

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
*	a ppp packet is in the receive buffer.
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
*	flag (0x7e, ~) received
*/
	private static boolean flag;
/**
*	number for lcp id
*/
	private static int lcpId;
/**
*	state machine
*/
	private static int state;
/**
*	reject counter
*/
	private static int rejCnt;
	private static final int MAX_REJ = 5;

/**
*	remote ip address.
*/
	private static int ipRemote;

/**
*	request for reconnect.
*/
	private static boolean reconnectRequest;
/**
*	request for a disconnect.
*/
	private static boolean disconnectRequest;

	private static int connCount;

/**
*	The one and only reference to this object.
*/
	private static Ppp single;

	private static Serial ser;
	private static RtThread rth;

/**
*	private constructor. The singleton object is created in init().
*/
	private Ppp() {
	}

/**
*	allocate buffer, start serial buffer and slip Thread.
*/
	public static LinkLayer init(int prio, int us, Serial serPort) {

		if (single != null) return single;		// already called init()

		rbuf = new int[MAX_BUF];
		sbuf = new int[MAX_BUF];
		cnt = 0;
		ready = false;
		flag = false;
		scnt = 0;
		sent = 0;
		connCount = 0;

		lcpId = 0x11;
		ipRemote = 0;
		reconnectRequest = false;
		disconnectRequest = false;

		initStr();

		ser = serPort;
		// new Serial(serAddr, 10, 3000);

		single = new Ppp();
		single.setIp(0);
		rth =  new RtThread(prio, us) {
			public void run() {
				for (;;) {
					waitForNextPeriod();
					single.loop();
				}
			}
		};

		return single;
	}

	/**
	*	Returns IP address, 0 if not connected.
	*/
	public int getIp() {
		if (state==CONNECTED) {
			return super.getIp();
		} else {
			return 0;
		}
	}

	/**
	*	Set connection strings and connect.
	*/
	public void startConnection(StringBuffer dialstr, StringBuffer connect, StringBuffer user, StringBuffer passwd) {

		int i, j;
// System.out.println("start Conn");

		// TODO correct dial string in Strecken data
		// copyStr(dialstr, dial);

		con.setLength(0);
		con.append("AT+CGDCONT=1,\"IP\",\"");
		i = connect.length();
		for (j=0; j<i; ++j) con.append(connect.charAt(j));
		con.append("\"\r");

//		Util.copyStr(user, uid);
//		Util.copyStr(passwd, pwd);

		reconnectRequest = true;
		connCount = 0;
		setIp(0);
	}

	/**
	*	Forces the connection to be anew established.
	*/
	public void reconnect() {
// System.out.println("reconnect");
		reconnectRequest = true;
		connCount = 0;
		setIp(0);
	}

	/**
	 * Force a disconnect
	 */
	public void disconnect() {
		disconnectRequest = true;
		setIp(0);
	}
/**
*	main loop. However this loop NEVER returns!
*	TODO: change to a loop based version to use PPP without threads.
*/
	protected void loop() {

		connect();
	}

	private static StringBuffer dial;
	private static StringBuffer con;
	private static StringBuffer uid;
	private static StringBuffer pwd;

	private static String ok;
	private static String connect;
	private static String ath;
	private static String pin;
	private static String flow;

	private static StringBuffer strBuf;

private static void initStr() {

		con = new StringBuffer(40);
		dial = new StringBuffer(20);
		uid = new StringBuffer(20);
		pwd = new StringBuffer(20);

/* we get the information from startConnection
		con.append("AT+CGDCONT=1,\"IP\",\"A1.net\"\r");
		uid.append("ppp@A1plus.at");
		pwd.append("ppp");
*/


		dial.append("ATD*99***1#\r");

		String client= "CLIENT";
		String ver= "VER";

		ok = "OK";
		connect = "ECT";
		ath = "|+++|ATH\r";
		pin = "AT+CPIN=5644\r";

		flow = "AT\\Q3\r";

		strBuf = new StringBuffer(40);

		// int [] s7 = { 'A', 'T', '+', 'C', 'G', 'D', 'C', 'O', 'N', 'T', '=', '1', ',', '"', 'I', 'P', '"', ',', '"', 'w', 'e', 'b', '.', 'o', 'n', 'e', '.', 'a', 't', '"', '\r' };
		/* A1 */
		// int [] s7 = { 'A', 'T', '+', 'C', 'G', 'D', 'C', 'O', 'N', 'T', '=', '1', ',', '"', 'I', 'P', '"', ',', '"', 'A', '1', '.', 'n', 'e', 't', '"', '\r' };
		/* OEBB VPN */
		/*
		int [] s7 = { 'A', 'T', '+', 'C', 'G', 'D', 'C', 'O', 'N', 'T', '=', '1', ',', '"', 'I', 'P', '"', ',',
			'"', 'o', 'e', 'b', 'b', '.', 'A', '1', '.', 'n', 'e', 't', '"', '\r' };
		*/
		/*
		int [] s8 = { 'A', 'T', 'D', '*', '9', '9', '*', '*', '*', '1', '#', '\r' };
		int [] s9 = { 'A', 'T', '\\', 'Q', '3', '\r' };
		*/
	}

	/**
	*	wait seconds and drop IP packets
	*/
	void waitSec(int t) {

		int timer = Timer.getTimeoutMs(1000);

		for (int i=0; i<t; ++i) {
			while (!Timer.timeout(timer)) {
				rth.waitForNextPeriod();
			}
			timer = Timer.getTimeoutMs(1000);
			dropIp();
		}
	}

	/**
	*	send a string to serial line buffer.
	*	'|' has special meaning: wait one second.
	*/
	boolean wrString(StringBuffer s) {

		int i, j, k, val;

		i = ser.txFreeCnt();
		j = s.length();
		if (j>i) return false;
Dbg.wr('\'');
		for (i=0; i<j; ++i) {
			val = s.charAt(i);
			if (val=='|') {
				waitSec(2);			// for shure if send buffer is full
			} else {
				ser.wr(val);
Dbg.wr(val);
			}
		}
Dbg.wr('\'');
Dbg.wr('\n');
		return true;
	}

	boolean sendWait(String snd, String rcv, int timeout) {

		strBuf.setLength(0);
		strBuf.append(snd);
		return sendWait(strBuf, rcv, timeout);
	}
	/**
	*	send a string and loop until rcv string arrives or it times out
	*	timeout in seconds for receive string, is also used for send (if handshake lines are not set)
	*	return false means timeout.
	*/
	boolean sendWait(StringBuffer snd, String rcv, int timeout) {

		timeout *= 1000;


		//
		//	send string
		//
		int timer = Timer.getTimeoutMs(timeout);	// use same timeout for send
		while (!wrString(snd)) {
			rth.waitForNextPeriod();			// wait till send buffer is free
			dropIp();
			if (Timer.timeout(timer)) return false;	// timeout on send means problem with handshake lines
		}

		if (rcv==null) return true;			// no wait string, we're done

		int ptr = 0;
		int len = rcv.length();
		//
		//	now wait on response string
		//
		for (timer = Timer.getTimeoutMs(timeout); !Timer.timeout(timer); ) {

			rth.waitForNextPeriod();
			dropIp();

			for (int i = ser.rxCnt(); i>0; --i) {

				int val = ser.rd();
Dbg.wr(val);
				if (val == rcv.charAt(ptr)) {
					++ptr;
					if (ptr==len) {
Dbg.wr('\n');
						waitSec(1);
						return true;			// we're done
					}
				} else {
					ptr = 0;					// reset match pointer
				}
			}
		}
Dbg.wr('?');
Dbg.wr('\n');

		return false;							// timeout expired
	}

	private static int globTimer;					// negotion send and ip-restart timer
	private static boolean lcpAck;
	private static boolean ipcpAck;

	/**
	*	do the modem stuff till CONNECT
	*/

	void modemInit() {

		++connCount;
		for (;;++connCount) {
 System.out.print("Modem init ");
 System.out.println(connCount);

			if (sendWait(ath, ok, 3)) {
				if (sendWait(flow, ok, 3)) {
					if (!sendWait(con, ok, 2)) {	// when ERROR PIN is not set
						sendWait(pin, ok, 30);
						if (!sendWait(con, ok, 20)) {
							continue;				// something really strange happend!
						}
					}
					if (sendWait(dial, connect, 10)) {
						break;
					}
				}
			}

			waitSec(1);
		}

		state = MODEM_OK;
		globTimer = Timer.getTimeoutMs(NEG_SEND);
		lcpAck = false;
		ipcpAck = false;
	}

	/**
	*	cancel connection.
	*/
	void modemHangUp() {

// System.out.print("Modem hangup ");
// System.out.println(connCount);
		setIp(0);								// stop sending ip data
		reconnectRequest = false;
		disconnectRequest = false;
		state = INIT;
		rejCnt = 0;
		// flush buffer
		for (int i = ser.rxCnt(); i>0; --i) {
			ser.rd();
		}
		for (;;) {
			if (sendWait(ath, ok, 3)) {
				break;
			}
			waitSec(3);
		}
	}

	private static final int INIT = 0;
	private static final int MODEM_OK = 1;
	private static final int LCP_SENT = 2;
	private static final int LCP_OK = 3;
	private static final int PAP_SENT = 4;
	private static final int PAP_OK = 5;
	private static final int IPCP_SENT = 6;
	private static final int IPCP_SENT2 = 7;
	private static final int IPCP_OK = 8;
	private static final int CONNECTED = 9;

	/**
	*	establish a connetcion.
	*/
	void connect() {

		state = INIT;
		rejCnt = 0;
		lcpAck = false;
		ipcpAck = false;

		//
		//	wait for startConnection(...)
		//
		while (!reconnectRequest) {
			rth.waitForNextPeriod();
		}
		reconnectRequest = false;

		//
		//	start the modem
		//
		modemInit();

		//
		//	now LCP negotiation
		//

		for (;;) {
			rth.waitForNextPeriod();
			pppLoop();

			if (state==MODEM_OK) {
			}

			if (rejCnt > MAX_REJ) {
// System.out.print("1");
				modemHangUp();		// start over
				modemInit();
			}

			if (ready && scnt==0) {				// one packet is read and send buffer is free

dbgCon();

				int prot = (rbuf[2]<<8)+rbuf[3];
				int code = rbuf[4];

				if (prot == LCP) {

					if (code == REQ) {
						if (checkOptions(LCP)) {
							lcpAck = true;
						} else {
							++rejCnt;
						}
					} else if (code==ACK && rbuf[5]==lcpId) {
						state = LCP_OK;
					} else if (code==TERM) {
// System.out.print("2");
						modemHangUp();		// start over
						modemInit();
					}

				} else if (prot == PAP) {

					if (rbuf[4]==ACK && rbuf[5]==lcpId) {
						state = PAP_OK;
					}

				} else if (prot == IPCP) {

					if (code == REQ) {
						if (checkOptions(IPCP)) {
							ipcpAck = true;
						}
					} else if (code == NAK) {	// with this NAK we will get our IP address
						setIp((rbuf[10]<<24) + (rbuf[11]<<16) +
							(rbuf[12]<<8) + rbuf[13]);
						dbgIp(super.getIp());
						makeIPCP();
						state = IPCP_SENT2;
					} else if (code == ACK) {
						state = IPCP_OK;
						// nothing more to do ?
						state = CONNECTED;
Dbg.wr('C');
Dbg.wr('\n');
					}

//
//	des is net guat: kommt nur hierher, wenn der Sendbuffer frei ist!!!
//
				} else if (prot == IP) {		// we finally got an ip packet :-)

					readIp();

				}

				cnt = 0;
				ready = false;
			}

			doSend();
		}
	}

	void doSend() {

		if (reconnectRequest) {
// System.out.print("3");
			modemHangUp();		// start over
			modemInit();
		}

		if (disconnectRequest) {
			modemHangUp();		// stop the connection
		}

		if (state==CONNECTED) {			// send waiting ip packets
			if (scnt==0) {				// transmit buffer is free
				globTimer = Timer.getTimeoutMs(IP_SEND);	// use IP timeout
				//
				// get a ready to send packet with source from this driver.
				//
				Packet p = PacketPool.getTxPacket(single);
				if (p!=null) {
					sendIp(p);			// send one packet
				}
			} else {					// check sendTimer;
				if (Timer.timeout(globTimer)) {
// System.out.print("4");
					modemHangUp();		// start over
					modemInit();
				}
			}
		} else {						// do the negotiation stuff
			dropIp();
			if (Timer.timeout(globTimer)) {
/*
Dbg.intVal(state);
if (lcpAck) Dbg.wr('t'); else Dbg.wr('f');
*/
				if (scnt==0) {			// once every three seconds send a REQ
					if (state == MODEM_OK) {
						makeLCP();
						state = LCP_SENT;
					} else if (state == LCP_OK && lcpAck) {
						makePAP();
						state = PAP_SENT;
//					} else if (state == PAP_OK && ipcpAck) { // wait for remote ipcp and ACK first on Linux
					} else if (state>=PAP_OK && state<CONNECTED) {	// ONE
						makeIPCP();
						state = IPCP_SENT;
						++rejCnt;		// incremenet counter to start over when no respond
					}
					globTimer = Timer.getTimeoutMs(NEG_SEND);	// use negotiation timeout
				}
			}
		}
	}

	/**
	*	drop waiting packet to prevent packet buffer overrun
	*/
	void dropIp() {

		// TODO: what shall we do with CON_RDY packets?
		Packet p = PacketPool.getTxPacket(single);
		if (p!=null) {
			p.free();		// mark packet free
		}
	}

void dbgCon() {
if (state!=CONNECTED) {
Dbg.wr('>');
for (int i=0; i<cnt; ++i) {
	Dbg.byteVal(rbuf[i]);
	if ((i&0x0f) ==0) rth.waitForNextPeriod();
}
Dbg.wr('\n');
}
}
void dbgIp(int ip) {
Dbg.wr('I');
Dbg.wr(' ');
Dbg.intVal(ip>>>24);
Dbg.intVal((ip>>>16)&0xff);
Dbg.intVal((ip>>>8)&0xff);
Dbg.intVal(ip&0xff);
Dbg.wr('\n');
}

	/**
	*	generate a PPP (negotiation) request.
	*/

	void makeLCP() {

		lcpId = 0x22;
Dbg.wr('L');
Dbg.intVal(lcpId);
Dbg.wr('\n');

		sbuf[0] = 0xff;
		sbuf[1] = 0x03;
		//	REQ LCP options 2, 7, 8
		sbuf[2] = LCP>>8;
		sbuf[3] = LCP&0xff;
		sbuf[4] = REQ;
		sbuf[5] = lcpId;
		sbuf[6] = 0;
		sbuf[7] = 18-4;		// length including code, id and length field
		sbuf[8] = 0x02;		// async-map
		sbuf[9] = 0x06;
		sbuf[10] = 0x00;
		sbuf[11] = 0x0a;
// sbuf[11] = 0x00;			// one does not like this
		sbuf[12] = 0x00;
		sbuf[13] = 0x00;
		sbuf[14] = 0x07;	// protocol field compression
		sbuf[15] = 0x02;
		sbuf[16] = 0x08;	// addr., contr. field compression
		sbuf[17] = 0x02;

		checksum(18);
	}

	void makePAP() {

		int i;

		lcpId = 0x33;
Dbg.wr('P');
Dbg.intVal(lcpId);
Dbg.wr('\n');

/* compression
		sbuf[0] = 0xff;
		sbuf[1] = 0x03;
*/
		sbuf[0] = PAP>>8;
		sbuf[1] = PAP&0xff;
		sbuf[2] = REQ;
		sbuf[3] = lcpId;
		sbuf[4] = 0;

		int ulen = uid.length();
		int plen = pwd.length();
		sbuf[5] = ulen + plen + 6;			// length including code, id and length field
		sbuf[6] = ulen;		// length of user id
		for (i=0; i<ulen; ++i) {
			sbuf[7+i] = uid.charAt(i);
		}
		sbuf[7+ulen] = plen;
		for (i=0; i<plen; ++i) {
			sbuf[8+ulen+i] = pwd.charAt(i);
		}
		checksum(ulen + plen + 8);


	}

	void makeIPCP() {

		lcpId = 0x44;
Dbg.wr('I');
Dbg.intVal(lcpId);
Dbg.wr('\n');

/* compression
		sbuf[0] = 0xff;
		sbuf[1] = 0x03;
*/
		sbuf[0] = IPCP>>8;
		sbuf[1] = IPCP&0xff;
		sbuf[2] = REQ;
		sbuf[3] = lcpId;
		sbuf[4] = 0;
		sbuf[5] = 14-4;		// length including code, id and length field
		sbuf[6] = 0x03;		// ip-address 0.0.0.0
		sbuf[7] = 0x06;
		sbuf[8] = super.getIp()>>>24;
		sbuf[9] = (super.getIp()>>16)&0xff;
		sbuf[10] = (super.getIp()>>8)&0xff;
		sbuf[11] = super.getIp()&0xff;

		// checksum(14);
		checksum(12);
	}

	/**
	*	process a LCP, IPCP request
	*/
	boolean checkOptions(int type) {

		int i;
		int len = (rbuf[6]<<8) + rbuf[7] - 4;		// including code, id and lentgh

		int ptr = 8;

Dbg.wr('R');
Dbg.wr(' ');
		int resp = ACK;

		for (i=0; i<cnt; ++i) sbuf[i] = rbuf[i];	// assume ACK
		int slen = len + 4;

		while (len > 0) {
			int opt = rbuf[ptr];
Dbg.intVal(opt);
			if (type==LCP && opt==3) {				// auth. protocol
				if ((rbuf[ptr+2]<<8) + rbuf[ptr+3] != PAP) {
					resp = REJ;
Dbg.wr('!');
Dbg.wr('P');
Dbg.wr(' ');
				}
			} else if (type==IPCP) {
				if (opt==2) {						// IP-Compression
					resp = REJ;
				} else if (opt==3) {				// IP-address
					ipRemote = (rbuf[ptr+2]<<24) + (rbuf[ptr+3]<<16) +
						(rbuf[ptr+4]<<8) + rbuf[ptr+5];
Dbg.hexVal(ipRemote);
dbgIp(ipRemote);
				}
			}
			// } else if (opt==xx}
			if (resp==REJ) {
				int optlen = rbuf[ptr+1];
				slen = 4 + optlen;
				for (i=0; i<optlen; ++i) {
					sbuf[8+i] = rbuf[ptr+i];
				}
				break;								// end check options
			}
			i = rbuf[ptr+1];						// check next option
			ptr += i;
			len -= i;
		}
		sbuf[4] = resp;
		sbuf[6] = slen>>>8;
		sbuf[7] = slen&0xff;
Dbg.wr('\n');

		checksum(slen+4);

		return resp == ACK;
	}

/**
*	get a Packet buffer and copy from receive buffer.
*/
	void readIp() {

		int i, j, k;

		Packet p = null;//PacketPool.getFreshRcvPacket(single);
		if (p==null) {
Dbg.wr('!');
			return;							// try again later
		}									// buf blocks receive buffer :-<

		int[] pb = p.buf;

		cnt -= 6;							// minus ppp header and checksum

		rbuf[cnt+4] = 0;
		rbuf[cnt+4+1] = 0;
		rbuf[cnt+4+2] = 0;

		// copy buffer
		k = 0;
		for (i=0; i<cnt; i+=4) {
			for (j=0; j<4; ++j) {
				k <<= 8;
				k += rbuf[i+j+4];			// after header
			}
			pb[i>>>2] = k;
		}

		p.setLen(cnt);

Dbg.wr('r');
Dbg.intVal(cnt);
/*
dbgIp(pb[3]);
dbgIp(pb[4]);
for (i=0; i<(cnt+4)>>2; ++i) Dbg.hexVal(pb[i]);
Dbg.wr('\n');
*/
		cnt = 0;
		ready = false;

		p.setStatus(Packet.RCV);		// inform upper layer
	}


/**
*	copy packet to send buffer.
*/
	void sendIp(Packet p) {

		int i, k;
		int[] pb = p.buf;

Dbg.wr('s');
Dbg.intVal(p.len());

		sbuf[0] = 0xff;
		sbuf[1] = 0x03;
		sbuf[2] = IP>>8;
		sbuf[3] = IP&0xff;

		int slen = p.len();
		sent = 0;
		for (i=0; i<slen; i+=4) {
			k = pb[i>>>2];
			sbuf[i+4] = k>>>24;
			sbuf[i+4+1] = (k>>>16)&0xff;
			sbuf[i+4+2] = (k>>>8)&0xff;
			sbuf[i+4+3] = k&0xff;
		}
		if (p.status()== Packet.CON_RDY) {
			p.setStatus(Packet.CON_ONFLY);		// mark on the fly
		} else {
			p.free();		// mark packet free
		}

		checksum(slen+4);
	}

/* warum geht das nicht !!!!!
	private void loop() {
*/
/**
*	read from serial buffer and build a ppp packet.
*	send a packet if one is in our send buffer.
*/
	boolean pppLoop() {

		int i;
		boolean ret = false;

		i = ser.rxCnt();
		if (i!=0 && !ready) {
			ret = true;
			rcv(i);
		}
		if (scnt!=0) {
			i = ser.txFreeCnt();
			if (i>2) {
				snd(i);
			}
		}

		return ret;
	}

/**
*	copy from send buffer to serial buffer with flags and escapes.
*/
	void snd(int free) {

		int i;

		if (sent==0) {
			ser.wr('~');
			--free;
		}

		for (i=sent; free>1 && i<scnt; ++i) {

			int c = sbuf[i];
/* no hard code
if (state >= LCP_OK) { 			// hard code async map
	if (c=='~' || c=='}' || c==17 || c==19) { // 0x000a0000 async map
		ser.wr('}');
		ser.wr(c ^ ' ');
		free -= 2;
	} else {
		ser.wr(c);
		--free;
	}
} else {
*/
			if (c=='~' || c=='}' || c<0x20) {			// c<0x20 could be omitted after LCP async map
				ser.wr('}');
				ser.wr(c ^ ' ');
				free -= 2;
			} else {
				ser.wr(c);
				--free;
			}
/*
}
*/
		}
		sent = i;

		if (sent==scnt && free!=0) {
			ser.wr('~');
			scnt = 0;
			sent = 0;
		}
	}

	private static boolean escape;
	private static int fcs;
/**
*	copy from serial buffer to receive buffer.
*	calc CRC on the fly.
*/
	void rcv(int len) {

		int i;

		if (cnt==0) fcs = 0xffff;

		// get all bytes from serial buffer
		for (i=0; i<len && cnt<MAX_BUF; ++i) {

			int val = ser.rd();
			if (cnt==0 && !flag && val!='~') {	// wait for a packet start
				escape = false;					// first data byte is not an escape
Dbg.wr('d');
				continue;						// so don't worry about '~' escapes on cnt==0
			}

			if (!escape && val=='~') {
				flag = true;					// remember flag, because end flag and
				if (cnt!=0) {					// starting flag can be the same (see rfc1549)
					if (fcs==0xf0b8) {			// checksum ok?
						ready = true;
					} else {
Dbg.wr('d');
Dbg.intVal(cnt);
Dbg.wr('\n');
						cnt = 0;				// just drop it
					}
					break;
				}
				continue;
			} else {
				flag = false;
			}


			if (!escape && val=='}') {
				escape = true;
				continue;
			}

			if (escape) {
				val ^= ' ';
				escape = false;
			}

			// rfc1549 3.2 Address-and-Control-Field-Compression
			if (cnt==0 && val!=0xff) rbuf[cnt++] = 0xff;
			if (cnt==1 && val!=0x03) rbuf[cnt++] = 0x03;
			// rfc1548 6.6 Protocol-Field-Compression
			if (cnt==2 && (val&1)!=0) rbuf[cnt++] = 0x00;
			rbuf[cnt++] = val;

			fcs = check(val^fcs) ^ (fcs>>8);

		}
/*
Dbg.wr('r');
Dbg.intVal(cnt);
Dbg.wr('\n');
*/
	}

/**
*	calculate CRC of byte c for checksum
*/
	int check(int c) {

		c &= 0xff;
		for (int i=0; i<8; ++i) {
			if ((c&1) != 0) {
				c >>= 1;
				c ^= 0x8408;
			} else {
				c >>= 1;
			}
		}
		return c;
	}

/**
*	calculate CRC for send packet and mark it ready to send.
*/
	void checksum(int len) {

		int k, j, i;
		int fcs = 0xffff;

		for (i=0; i<len; ++i) {
			j = sbuf[i];
			j ^= fcs & 0xff;	// only low order byte
			for (k=0; k<8; ++k) {
				if ((j&1) != 0) {
					j >>= 1;
					j ^= 0x8408;
				} else {
					j >>= 1;
				}
			}
			fcs = j ^ (fcs>>8);
		}
		fcs = fcs ^ 0xffff;
		sbuf[len] = fcs & 0xff;		// LSB first !
		sbuf[len+1] = fcs >> 8;
		scnt = len+2;

if (state!=CONNECTED) {
Dbg.wr('<');
for (i=0; i<scnt; ++i) {
	Dbg.byteVal(sbuf[i]);
	if ((i&0x0f) ==0) rth.waitForNextPeriod();
}
Dbg.wr('\n');
}
	}
	public int getConnCount() {
		return connCount;
	}

}
