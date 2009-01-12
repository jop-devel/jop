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
*	Ppp.java
*
*	communicate with jop via serial line.
*/

import joprt.RtThread;
import util.Serial;
import util.Timer;

/**
*	Ppp driver.
*/

public class Ppp extends LinkLayer {

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
	private static final int IP_SEND= 10000;		// Send timeout for IP for reconnect (in ms)
	private static final int PPP_HANDLING = 60000;	// Timeout for the PPP negotiation
	
	private static final int GPRS_TRY_CNT = 3;		// After that count connect via GSM

/**
*	receive buffer
*/
	private int[] rbuf;
/**
*	send buffer
*/
	private int[] sbuf;
/**
*	bytes received.
*/
	private int cnt;
/**
*	a ppp packet is in the receive buffer.
*/
	private boolean ready;
/**
*	bytes to be sent. 0 means txFree
*/
	private int scnt;
/**
*	allready sent bytes.
*/
	private int sent;

/**
*	flag (0x7e, ~) received
*/
	private boolean flag;
/**
*	number for lcp id
*/
	private int lcpId;
/**
*	state machine
*/
	private int state;
/**
*	reject counter
*/
	private int rejCnt;
	private static final int MAX_REJ = 5;

/**
*	remote IP address.
*/
	private int ipRemote;
/**
*	request for reconnect.
*/
	private boolean reconnectRequest;
/**
*	request for a disconnect.
*/
	private boolean disconnectRequest;
	
	// TODO: we run out of object fields....
	private static int connCount;
	private static boolean useGSM;

	private Serial ser;
	private RtThread rth;
	
	/**
*	private constructor. The singleton object is created in init().
*/
	/**
	 * Create a PPP connection.
	 */
	public Ppp(Ejip ejip, Serial serPort, RtThread pppThread) {

		super(ejip, 0);
		
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
		
		useGSM = true;

		initStr();

		ser = serPort;
		// new Serial(serAddr, 10, 3000);

		rth = pppThread;
	}

	/**
	*	Returns IP address, 0 if not connected.
	*/
	public int getIpAddress() {
		if (state==CONNECTED) {
			return ip;
		} else {
			return 0;
		}
	}

	/**
	*	Set connection strings and connect.
	*/
	public void startConnection(StringBuffer dialstr, StringBuffer connect, StringBuffer user, StringBuffer passwd) {

		int i, j;

		copyStr(dialstr, dial, gsm_dial);
		dial.append('\r');
		gsm_dial.append('\r');

		con.setLength(0);
		con.append("AT+CGDCONT=1,\"IP\",\"");
		i = connect.length();
		for (j=0; j<i; ++j) con.append(connect.charAt(j));
		con.append("\"\r");

		copyStr(user, uid, gsm_uid);
		copyStr(passwd, pwd, gsm_pwd);

		reconnectRequest = true;
		connCount = 0;
		ip = 0;
	}

	/**
	*	Forces the connection to be anew established.
	*/
	public void reconnect() {
		reconnectRequest = true;
		connCount = 0;
		ip = 0;
	}
	
	/**
	 * Force a disconnect
	 */
	public void disconnect() {
		disconnectRequest = true;
		ip = 0;
	}
/**
*	main loop. However this loop NEVER returns!
*	TODO: change to a loop based version to use PPP without threads.
*/
	public void run() {

		connect();
	}

	private StringBuffer dial;
	private StringBuffer con;
	private StringBuffer uid;
	private StringBuffer pwd;

	private StringBuffer gsm_dial;
	private StringBuffer gsm_uid;
	private StringBuffer gsm_pwd;


	private static String ok;
	private static String connect;
	private static String ath;
	private static String pin;
	private static String flow;

	private StringBuffer strBuf;

	/**
	*	a little helper:
	*/
	static void copyStr(StringBuffer src, StringBuffer dst,
			StringBuffer gsm_dst) {

		int i, j;
		dst.setLength(0);
		i = src.length();
		for (j=0; j<i; ++j) {
			char ch = src.charAt(j);
			if (ch=='|') {
				++j;	// skip it
				break;
			}
			dst.append(ch);
		}
		gsm_dst.setLength(0);
		for (; j<i; ++j) {
			gsm_dst.append(src.charAt(j));
		}
	}

	private void initStr() {

		con = new StringBuffer(40);
		dial = new StringBuffer(20);
		uid = new StringBuffer(20);
		pwd = new StringBuffer(20);

		gsm_dial = new StringBuffer(20);
		gsm_uid = new StringBuffer(20);
		gsm_pwd = new StringBuffer(20);
		
		ok = "OK";
		connect = "ECT";
		ath = "|+++|ATH\r";
		pin = "AT+CPIN=5644\r";

		flow = "AT\\Q3\r";

		strBuf = new StringBuffer(40);
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

		int i, j, val;

		i = ser.txFreeCnt();
		j = s.length();
		if (j>i) return false;
		if (Logging.LOG) Logging.wr('\'');
		for (i=0; i<j; ++i) {
			val = s.charAt(i);
			if (val=='|') {
				waitSec(2);			// for shure if send buffer is full
			} else {
				ser.wr(val);
				if (Logging.LOG) Logging.wr((char) val);
			}
		}
		if (Logging.LOG) Logging.wr('\'');
		if (Logging.LOG) Logging.lf();
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
				if (Logging.LOG) Logging.wr((char) val);
				if (val == rcv.charAt(ptr)) {
					++ptr;
					if (ptr==len) {
						if (Logging.LOG) Logging.wr('\n');
						waitSec(1);
						return true;			// we're done
					}
				} else {
					ptr = 0;					// reset match pointer
				}
			}
		}
		if (Logging.LOG) Logging.wr('?');
		if (Logging.LOG) Logging.lf();

		return false;							// timeout expired
	}

	private static int globTimer;					// negotion send and ip-restart timer
	private static boolean lcpAck;
	/**
	*	do the modem stuff till CONNECT
	*/

	void modemInit() {

		useGSM = false;
		++connCount;
		for (;;++connCount) {
System.out.print("Modem init ");
System.out.println(connCount);
//			Led.stopModem();
//			waitSec(1);
//			Led.startModem();
			waitSec(15);
			
			if (connCount>GPRS_TRY_CNT && gsm_uid.length()!=0) {
				useGSM = true;
			}

			if (sendWait(ath, ok, 3)) {
				if (sendWait(flow, ok, 3)) {
					if (!sendWait(con, ok, 2)) {	// when ERROR PIN is not set
						sendWait(pin, ok, 30);
						if (!sendWait(con, ok, 20)) {
							continue;				// something really strange happend!
						}
					}
					sendWait("ATD", null, 1);
					if (useGSM) {
						if (sendWait(gsm_dial, connect, 30)) {
							System.out.println("GSM connect ok");
							break;
						}
					} else {
						if (sendWait(dial, connect, 30)) {
							System.out.println("GPRS connect ok");
							break;						
						}
					}
				}
			}

//			waitSec(1);
		}

		state = MODEM_OK;
		globTimer = Timer.getTimeoutMs(NEG_SEND);
		lcpAck = false;
	}

	/**
	*	cancel connection.
	*/
	void modemHangUp() {

		ip = 0;								// stop sending ip data
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
	*	establish a connection.
	*/
	void connect() {

		state = INIT;
		rejCnt = 0;
		lcpAck = false;
		int timer = 0;

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
				// start timer for PPP negotiation
				timer = Timer.getTimeoutMs(PPP_HANDLING);
			}

			if ((rejCnt > MAX_REJ) || 
					(Timer.timeout(timer) && state!=CONNECTED)) {
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
							;
						}
					} else if (code == NAK) {	// with this NAK we will get our IP address
						ip= (rbuf[10]<<24) + (rbuf[11]<<16) +
							(rbuf[12]<<8) + rbuf[13];
						dbgIp(ip);
						makeIPCP();
						state = IPCP_SENT2;
					} else if (code == ACK) {
						state = IPCP_OK;
						// nothing more to do ?
						state = CONNECTED;
						if (Logging.LOG) Logging.wr('C');
						if (Logging.LOG) Logging.wr('\n');
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
			modemHangUp();		// start over
			modemInit();
		}
		
		if (disconnectRequest) {
			modemHangUp();		// stop the connection
		}

		if (state==CONNECTED) {			// send waiting IP packets
			if (scnt==0) {				// transmit buffer is free
				globTimer = Timer.getTimeoutMs(IP_SEND);	// use IP timeout
				//
				// get a ready to send packet with source from this driver.
				//
				Packet p = txQueue.deq();
				if (p!=null) {
					sendIp(p);			// send one packet
				}
			} else {					// check sendTimer;
				if (Timer.timeout(globTimer)) {
System.out.println("IP send timeout");
System.out.println(scnt);
					modemHangUp();		// start over
					modemInit();
				}
			}
		} else {						// do the negotiation stuff
			dropIp();
			if (Timer.timeout(globTimer)) {


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
						++rejCnt;		// increment counter to start over when no respond
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

		Packet p = txQueue.deq();
		if (p!=null && !p.isTcpOnFly) {
			ejip.returnPacket(p);
		}
	}

void dbgCon() {
if (state!=CONNECTED) {
	if (Logging.LOG) Logging.wr('>');
for (int i=0; i<cnt; ++i) {
	if (Logging.LOG) Logging.byteVal(rbuf[i]);
	if ((i&0x0f) ==0) rth.waitForNextPeriod();
}
if (Logging.LOG) Logging.wr('\n');
}
}
void dbgIp(int ip) {
	if (Logging.LOG) Logging.wr('I');
	if (Logging.LOG) Logging.wr(' ');
	if (Logging.LOG) Logging.intVal(ip>>>24);
	if (Logging.LOG) Logging.intVal((ip>>>16)&0xff);
	if (Logging.LOG) Logging.intVal((ip>>>8)&0xff);
	if (Logging.LOG) Logging.intVal(ip&0xff);
	if (Logging.LOG) Logging.lf();
}

	/**
	*	generate a PPP (negotiation) request.
	*/

	void makeLCP() {

		lcpId = 0x22;
		if (Logging.LOG) Logging.wr('L');
		if (Logging.LOG) Logging.intVal(lcpId);
		if (Logging.LOG) Logging.wr('\n');

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
		if (Logging.LOG) Logging.wr('P');
		if (Logging.LOG) Logging.intVal(lcpId);
		if (Logging.LOG) Logging.wr('\n');

/* compression
		sbuf[0] = 0xff;
		sbuf[1] = 0x03;
*/
		sbuf[0] = PAP>>8;
		sbuf[1] = PAP&0xff;
		sbuf[2] = REQ;
		sbuf[3] = lcpId;
		sbuf[4] = 0;
		
		StringBuffer u, p;
		if (useGSM) {
			u = gsm_uid;
			p = gsm_pwd;
		} else {
			u = uid;
			p = pwd;
		}

		int ulen = u.length();
		int plen = p.length();
		sbuf[5] = ulen + plen + 6;			// length including code, id and length field
		sbuf[6] = ulen;		// length of user id
		for (i=0; i<ulen; ++i) {
			sbuf[7+i] = u.charAt(i);
		}
		sbuf[7+ulen] = plen;
		for (i=0; i<plen; ++i) {
			sbuf[8+ulen+i] = p.charAt(i);
		}
		checksum(ulen + plen + 8);

/* A1.net
		sbuf[5] = 24-2;		// length including code, id and length field
		sbuf[6] = 13;		// length of user id
		sbuf[7] = 'p';
		sbuf[8] = 'p';
		sbuf[9] = 'p';
		sbuf[10] = '@';
		sbuf[11] = 'A';
		sbuf[12] = '1';
		sbuf[13] = 'p';
		sbuf[14] = 'l';
		sbuf[15] = 'u';
		sbuf[16] = 's';
		sbuf[17] = '.';
		sbuf[18] = 'a';
		sbuf[19] = 't';
		sbuf[20] = 3;		// length of password
		sbuf[21] = 'p';
		sbuf[22] = 'p';
		sbuf[23] = 'p';

		checksum(24);
*/



/* ONE
		sbuf[7] = 30-4;		// length including code, id and length field
		sbuf[8] = 14;		// length of user id
		sbuf[9] = '+';
		sbuf[10] = '4';
		sbuf[11] = '3';
		sbuf[12] = '6';
		sbuf[13] = '9';
		sbuf[14] = '9';
		sbuf[15] = '1';
		sbuf[16] = '9';
		sbuf[17] = '5';
		sbuf[18] = '2';
		sbuf[19] = '0';
		sbuf[20] = '2';
		sbuf[21] = '2';
		sbuf[22] = '0';
		sbuf[23] = 6;		// length of password
		sbuf[24] = 'N';
		sbuf[25] = '6';
		sbuf[26] = 'J';
		sbuf[27] = '8';
		sbuf[28] = 'N';
		sbuf[29] = '4';

		checksum(30);
*/

	}

	void makeIPCP() {

		lcpId = 0x44;
		if (Logging.LOG) Logging.wr('I');
		if (Logging.LOG) Logging.intVal(lcpId);
		if (Logging.LOG) Logging.wr('\n');

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
		sbuf[8] = ip>>>24;
		sbuf[9] = (ip>>16)&0xff;
		sbuf[10] = (ip>>8)&0xff;
		sbuf[11] = ip&0xff;

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

		if (Logging.LOG) Logging.wr('R');
		if (Logging.LOG) Logging.wr(' ');
		int resp = ACK;

		for (i=0; i<cnt; ++i) sbuf[i] = rbuf[i];	// assume ACK
		int slen = len + 4;

		while (len > 0) {
			int opt = rbuf[ptr];
			if (Logging.LOG) Logging.intVal(opt);
			if (type==LCP && opt==3) {				// auth. protocol
				if ((rbuf[ptr+2]<<8) + rbuf[ptr+3] != PAP) {
					resp = REJ;
					if (Logging.LOG) Logging.wr('!');
					if (Logging.LOG) Logging.wr('P');
					if (Logging.LOG) Logging.wr(' ');
				}
			} else if (type==IPCP) {
				if (opt==2) {						// IP-Compression
					resp = REJ;
				} else if (opt==3) {				// IP-address
					ipRemote = (rbuf[ptr+2]<<24) + (rbuf[ptr+3]<<16) +
						(rbuf[ptr+4]<<8) + rbuf[ptr+5];
					if (Logging.LOG) Logging.hexVal(ipRemote);
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
		if (Logging.LOG) Logging.lf();

		checksum(slen+4);

		return resp == ACK;
	}

/**
*	get a Packet buffer and copy from receive buffer.
*/
	void readIp() {

		int i, j, k;

		Packet p = ejip.getFreePacket(this);
		if (p==null) {
			if (Logging.LOG) Logging.wr('!');
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

		p.len = cnt;

		cnt = 0;
		ready = false;

		rxQueue.enq(p);		// inform upper layer
	}


/**
*	copy packet to send buffer.
*/
	void sendIp(Packet p) {

		int i, k;
		int[] pb = p.buf;

		sbuf[0] = 0xff;
		sbuf[1] = 0x03;
		sbuf[2] = IP>>8;
		sbuf[3] = IP&0xff;

		int slen = p.len;
		sent = 0;
		for (i=0; i<slen; i+=4) {
			k = pb[i>>>2];
			sbuf[i+4] = k>>>24;
			sbuf[i+4+1] = (k>>>16)&0xff;
			sbuf[i+4+2] = (k>>>8)&0xff;
			sbuf[i+4+3] = k&0xff;
		}
		if (!p.isTcpOnFly) {
			ejip.returnPacket(p);
		}

		checksum(slen+4);
	}

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

	private boolean escape;
	private int fcs;
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
				if (Logging.LOG) Logging.wr('d');
				continue;						// so don't worry about '~' escapes on cnt==0
			}

			if (!escape && val=='~') {
				flag = true;					// remember flag, because end flag and
				if (cnt!=0) {					// starting flag can be the same (see rfc1549)
					if (fcs==0xf0b8) {			// checksum ok?
						ready = true;
					} else {
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
			if (cnt==0 && val!=0xff) {
				rbuf[cnt++] = 0xff;
			}
			if (cnt==1 && val!=0x03) {
				rbuf[cnt++] = 0x03;
			}
			// rfc1548 6.6 Protocol-Field-Compression
			if (cnt==2 && (val&1)!=0) {
				rbuf[cnt++] = 0x00;
			}
			rbuf[cnt++] = val;

			fcs = check(val^fcs) ^ (fcs>>8);

		}
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
	if (Logging.LOG) Logging.wr('<');
for (i=0; i<scnt; ++i) {
	if (Logging.LOG) Logging.byteVal(sbuf[i]);
	if ((i&0x0f) ==0) rth.waitForNextPeriod();
}
if (Logging.LOG) Logging.wr('\n');
}
	}
	/**
	 * @return
	 */
	public int getConnCount() {
		return connCount;
	}
	
	/**
	 * GPRS or GSM
	 * @return 0=GPRS, 1=GSM
	 */
	public int getConnType() {
		if (useGSM) {
			return 1;
		} else {
			return 0;
		}
	}

}
