package tcpip_old;
/**
*	TcpIp.java: A minimalistic TCP/IP stack (with ICMP).
*
*	Copyright Martin Schoeberl.
*
*   This software may be used and distributed according to the terms
*   of the GNU General Public License, incorporated herein by reference.
*
*   The author may be reached as martin.schoeberl@chello.at
*
*	It's enough to handel a HTTP request (and nothing more)!
*
*
*   Changelog:
*		2002-03-16	works with ethernet
*
*
*/

import util.*;

public class TcpIp {

	private static final int PROT_ICMP = 1;
	private static final int PROT_TCP = 6;

	static final int FL_URG = 0x20;
	static final int FL_ACK = 0x10;
	static final int FL_PSH = 0x8;
	static final int FL_RST = 0x4;
	static final int FL_SYN = 0x2;
	static final int FL_FIN = 0x1;

	static int id, tcb_port;	// ip id, tcp port
	static int tcb_st;	// state

	static final int ST_LISTEN = 0;
	static final int ST_ESTAB = 2;
	static final int ST_FW1 = 3;
	static final int ST_FW2 = 4;

    static final int MTU = 1500-8;
    static final int WINDOW = 2680;

	static void set16(int[] buf, int pos, int val) {

		buf[pos] = val>>>8;
		buf[pos+1] = val&0xff;
	}

	static void set32(int[] buf, int pos, int val) {

		buf[pos] = val>>>24;
		buf[pos+1] = (val>>>16)&0xff;
		buf[pos+2] = (val>>>8)&0xff;
		buf[pos+3] = val&0xff;
	}

	static int val16(int[] buf, int i) {

		return buf[i]<<8 | buf[i+1];
	}

	static int val32(int[] buf, int i) {

		return buf[i]<<24 | buf[i+1]<<16 | buf[i+2]<<8 | buf[i+3];
	}

/**
*	calc ip header check sum.
*/
	static int chkSum(int[] buf, int ix, int cnt) {

		int sum = 0;
		while (cnt>1) {
			sum += (buf[ix]<<8) | buf[ix+1];
			ix += 2;
			cnt -= 2;
		}
		if (cnt>0) sum += buf[ix];		// oder doch += buf[ix]<<8?????

		while ((sum>>16) != 0) sum = (sum & 0xffff) + (sum >> 16);

		return (~sum)&0xffff;
	}

	public static void init() {

		tcb_st = ST_LISTEN;		// select();
		Html.init();
	}
/**
*	process one ip packet.
*	change buffer and return length to get a packet sent back.
*	called from Eth.process().
*/
	public static int receive(int[] buf, int pos, int len) {

		int i, j;
		int ret = 0;


/*
Eth.wrSer('l');
Eth.intVal(len);
Eth.intVal(val16(buf, pos+2));
Eth.wrSer('\n');
*/

		len = val16(buf, pos+2);		// len from IP header - TODO check with eth len

		int dataidx = (buf[pos+0]&0x0f)<<2;
		if (buf[pos+9]==PROT_ICMP) {
			ret = doICMP(buf, pos+dataidx, len-dataidx);
		} if (buf[pos+9]==PROT_TCP) {
			ret = doTCP(buf, pos+dataidx, len-dataidx);
			dataidx = 20;	// no ip options
		}

		if (ret != 0) {
			len = dataidx+ret;
			set16(buf, pos+2, len);				// ip length
			buf[pos+10] = 0;
			buf[pos+11] = 0;
			for (j=0; j<4; ++j) {				// swap ip addresses
				i = buf[pos+12+j];
				buf[pos+12+j] = buf[pos+16+j];
				buf[pos+16+j] = i;
			}
			set16(buf, pos+10, chkSum(buf, pos+0, 20));
			return len;
		}

		return 0;		// no response
	}

/**
*	the famous ping.
*/
	private static int doICMP(int[] buf, int ix, int len) {

Dbg.wr('P');
		if (buf[ix+0]==8) {
			buf[ix+0] = 0;	// return ping
			buf[ix+2] = 0;	// checksum
			buf[ix+3] = 0;	// checksum
			set16(buf, ix+2, chkSum(buf, ix, len));
			return len;
		}
		return 0;
	}


	static int doTCP(int[] buf, int ix, int len) {

		int i, datlen;
		int rcvcnt, sndcnt;
		int fl;

		// Find the payload
		int offset = (buf[ix+12]>>>2)&0x3c;
		datlen = len - offset;

		int flags = buf[ix+13];

		// "TCB"
		// In a full tcp implementation we would keep track of this per connection.
		// This implementation only handles one connection at a time.
		// As a result, very little of this state is actually used after
		// the reply packet has been sent.

//		if (len < 20) return 0;

		// If it's not http, just drop it
//		if (val16(buf, ix+2) != 80) return 0;

		// Get source port
		tcb_port = val16(buf, ix);

		rcvcnt = val32(buf, ix+4);		// sequence number
		sndcnt = val32(buf, ix+8);		// acknowledge number
		// sndcnt has to be incremented for SYN!!!
	
	
		len = 20;						// no options ?
		fl = FL_ACK;
	
	
		// Figure out what kind of packet this is, and respond
		if ((flags & FL_SYN) != 0) {
	
			// SYN
			sndcnt = -1;		// start with -1 for SYN 
			rcvcnt++;
			fl |= FL_SYN;
//			tcb_st = ST_ESTAB;
	
		} else if (datlen > 0) {
	
			// incoming data
			rcvcnt += datlen;
	
			// TODO get url

			if (sndcnt==0) {
				len += Html.setText(buf, ix+offset, datlen, ix+20);	// was 40 ???
				// Send reply packet
//				if (len > MTU) len = MTU;	// TODO MTU should be taken from tcp options
				// Read next segment of data into buffer
			} else {
				fl |= FL_FIN;
//				tcb_st = ST_FW1;
			}
	

			fl |= FL_PSH;
	
		} else if ((flags & FL_FIN) != 0) {
	
			// FIN
			rcvcnt++;
			// Don't bother with FIN-WAIT-2, TIME-WAIT, or CLOSED; they just cause trouble
//			tcb_st = ST_LISTEN;
	
		} else if ((flags & FL_ACK) != 0) {
	
			// ack with no data
			if (sndcnt > 0) {
				// calculate no of bytes left to send
// i = len2send - sndnxt
i = 0;
				if (i == 0) {
					// EOF; send FIN
					fl |= FL_FIN;
//					tcb_st = ST_FW1;
				} else if (i > 0) {
					// not EOF; send next segment
					len += i;
					fl |= FL_PSH;
				} else {
					// ack of FIN; no reply
					return 0;
				}
			} else {
				return 0;				// No reply packet
			}
	
		} else {
			return 0;					// drop it
		}
	
		doTcpHead(buf, ix-20, fl, sndcnt, rcvcnt, len);
		// Send return packet
		return len;
	}

/**
*	pos is position of IP packet!!!
*/
	static void doTcpHead(int[] buf, int pos, int fl, int sndcnt, int rcvcnt, int len) {

		int i, ck;
		// clear rest of packet headers
		for (i = 0; i < 12; i++)
			buf[pos+i] = 0;
		for (i = 20; i < 40; i++)
			buf[pos+i] = 0;
	
		// Fill in IP header
//		Util.setShort(pkt, (short)4, id);	???
		buf[pos+0] = 0x45;		// version, header len
	
		// Fill in TCP header
		buf[pos+21] = 80; // Source port
		set16(buf, pos+22, tcb_port);
		set32(buf, pos+24, sndcnt);
		set32(buf, pos+28, rcvcnt);
		buf[pos+32] = 0x50;		// data offset = 20 (no options)
		buf[pos+33] = fl;		// flags
		set16(buf, pos+34, WINDOW);
	
		buf[pos+9] = 6;			// protocol (tcp)
		set16(buf, pos+10, len);		// set tcp length in iph checksum for tcp checksum calculation
		buf[pos+len+20] = 0;	// pad 0
		i = (len+1+12) & 0xfffe;	// make even
		ck = chkSum(buf, pos+8, i);
		buf[pos+8] = 60;		// ttl
		set16(buf, pos+36, ck);
	
	}
}

