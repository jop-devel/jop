package tcpip_old;

/**
*	Slip.java
*
*	communicate with jopbb via serial line.
*/

import java.io.*;
import java.util.*;
import javax.comm.*;

public class Slip {

	private static final int TIMEOUT = 100;

/*
	private static CommPortIdentifier portId;
	private static InputStream is;
	private static OutputStream os;
	private static SerialPort serialPort;
*/

	private static final int END = 0xc0;
	private static final int ESC = 0xdb;
	private static final int ESC_END = 0xdc;
	private static final int ESC_ESC = 0xdd;

	private static int[] buf;
	private static int cnt;
	private static boolean esc;

	public static final int IO_PORT = 0;
	public static final int IO_STATUS = 1;
	public static final int IO_UART = 2;
	public static final int IO_CNT = 10;

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

	private static int[] text;
	private static int[] textend;
	private static int hits;

	public static void main(String[] args) {

		Timer.init();
		Display.init();
		Display.line1();
		Display.data('A');

		Html.init();

		cnt = 0;
		esc = false;

		hits = 0;

		id = 1;
		tcb_st = ST_LISTEN;		// select();

		buf = new int[1500];
		init();
		boolean blink = true;

		for (;;) {
			if (read()) {

//Display.data('R');
//Display.data('0'+cnt/10);
//Display.data('0'+cnt%10);
				doIP();
				cnt = 0;
			}

		}
	}

	static void set16(int pos, int val) {

		buf[pos] = val>>>8;
		buf[pos+1] = val&0xff;
	}

	static void set32(int pos, int val) {

		buf[pos] = val>>>24;
		buf[pos+1] = (val>>>16)&0xff;
		buf[pos+2] = (val>>>8)&0xff;
		buf[pos+3] = val&0xff;
	}

	static int val16(int i) {

		return buf[i]<<8 | buf[i+1];
	}

	static int val32(int i) {

		return buf[i]<<24 | buf[i+1]<<16 | buf[i+2]<<8 | buf[i+3];
	}

/*
	static void dumpIP() {

		int i;

		System.out.print("raw data: ");
		for (i=0; i<cnt; ++i) {
			System.out.print(Integer.toHexString(buf[i])+" ");
		}
		System.out.println();
		System.out.println("headlen: "+((buf[0]&0x0f)<<2));
		System.out.println("length: "+val16(2)+" slip cnt: "+cnt);
		System.out.println("protocol: "+buf[9]);
		System.out.println("chksum: "+val16(10));
		System.out.println("src addr: "+buf[12]+"."+buf[13]+"."+buf[14]+"."+buf[15]);
		System.out.println("dst addr: "+buf[16]+"."+buf[17]+"."+buf[18]+"."+buf[19]);
		int ix = (buf[0]&0x0f)<<2;
		if (buf[9]==PROT_ICMP) {
			System.out.println("ICMP");
			System.out.println("typ: "+buf[ix+0]);
			System.out.println("code: "+buf[ix+1]);
			if (buf[ix+0]==8 || buf[ix+0]==0) {
				System.out.println("ping id: "+val16(ix+4));
				System.out.println("ping seq: "+val16(ix+6));
				System.out.print("ping data: ");
				for (i=0; i<cnt-ix-8; ++i) {
					System.out.print(Integer.toHexString(buf[ix+8+i])+" ");
				}
				System.out.println();
			}
		} else if (buf[9]==PROT_TCP) {
			System.out.println("TCP");
			System.out.println("source port: "+val16(ix));
			System.out.println("dest port: "+val16(ix+2));
			System.out.println("sequence number: "+val32(ix+4));
			System.out.println("acknowledge number: "+val32(ix+8));
			int tcp_ix = (buf[ix+12]>>>2)&0x3c;
			System.out.println("tcp header lentgh: "+tcp_ix);
			i = buf[ix+13];
			System.out.print("flags: "+Integer.toHexString(i));
			if ((i&FL_URG)!=0) System.out.print(" URG");
			if ((i&FL_ACK)!=0) System.out.print(" ACK");
			if ((i&FL_PSH)!=0) System.out.print(" PSH");
			if ((i&FL_RST)!=0) System.out.print(" RST");
			if ((i&FL_SYN)!=0) System.out.print(" SYN");
			if ((i&FL_FIN)!=0) System.out.print(" FIN");
			System.out.println();
			System.out.println("window: "+val16(ix+14));
			System.out.println("checksum: "+val16(ix+16));
			System.out.println("urgent pointer: "+val16(ix+18));
			if (tcp_ix>20) {
				System.out.print("options: ");
				for (i=20; i<cnt-ix; ++i) {
					System.out.print(Integer.toHexString(buf[ix+i])+" ");
				}
				System.out.println();
			}
			System.out.print("tcp data: ");
			tcp_ix += ix;
			for (i=0; i<cnt-tcp_ix; ++i) {
				System.out.print(Integer.toHexString(buf[tcp_ix+i])+" ");
			}
			System.out.println();
			for (i=0; i<cnt-tcp_ix; ++i) {
				System.out.print((char) buf[tcp_ix+i]);
			}
			System.out.println();
		
		} else {
			System.out.println("unknwn protocol");
			for (i=0; i<10; ++i) {
				System.out.print(Integer.toHexString(buf[ix+i])+" ");
			}
			System.out.println();
		}
		System.out.println();

	}
*/

	static void doIP() {

		int i, j;
		int ret = 0;


//Display.data('C');
		int dataidx = (buf[0]&0x0f)<<2;
//Display.data('0'+buf[9]);
		if (buf[9]==PROT_ICMP) {
			ret = doICMP(dataidx, cnt-dataidx);
		} if (buf[9]==PROT_TCP) {
			ret = doTCP(dataidx, cnt-dataidx);
//Display.data('T');
			dataidx = 20;	// no ip options
		}

		if (ret != 0) {
			cnt = dataidx+ret;
//Display.data('S');
//Display.data('0'+cnt/10);
//Display.data('0'+cnt%10);
			buf[2] = cnt>>8;
			buf[3] = cnt&0xff;
			buf[10] = 0;
			buf[11] = 0;
			i = chkSum(0, 20);
			buf[10] = i >>> 8;
			buf[11] = i & 0xff;
			for (j=0; j<4; ++j) {
				i = buf[12+j];
				buf[12+j] = buf[16+j];
				buf[16+j] = i;
			}

			write();
		}
	}

	static int doICMP(int ix, int len) {

		if (buf[ix+0]==8) {
			buf[ix+0] = 0;	// return ping
			buf[ix+2] = 0;	// checksum
			buf[ix+3] = 0;	// checksum
			int sum = chkSum(ix, len);
			buf[ix+2] = sum >>> 8;
			buf[ix+3] = sum & 0xff;
//Display.data('I');
			return len;
		}
		return 0;
	}

	static int doTCP(int ix, int len) {

		int i, datlen;
		int rcvcnt, sndcnt;
		int fl;

		// Find the payload
		// offset = (buf[ix+12]>>>2)&0x3c;
		datlen = len - ((buf[ix+12]>>>2)&0x3c);

		int flags = buf[ix+13];


		// "TCB"
		// In a full tcp implementation we would keep track of this per connection.
		// This implementation only handles one connection at a time.
		// As a result, very little of this state is actually used after
		// the reply packet has been sent.

//		if (len < 20) return 0;

		// If it's not http, just drop it
//		if (val16(ix+2) != 80) return 0;

		// Get source port
		tcb_port = val16(ix);

		rcvcnt = val32(ix+4);		// sequence number
		sndcnt = val32(ix+8);		// acknowledge number
		// sndcnt has to be incremented for SYN!!!
	
	
		len = 20;
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
				len += Html.setText(buf, 40);
				++hits;
				Display.line1();
				Display.intVal(hits);
/*
				len += text.length;
				// Send reply packet
//				if (len > MTU) len = MTU;	// TODO MTU should be taken from tcp options
				// Read next segment of data into buffer
//				if (len > 20) {
					for (i=0; i<len-20; ++i) {
						buf[i+40] = text[i];
					}
//				}
*/
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
	
		doTcpHead(fl, sndcnt, rcvcnt, len);
		// Send return packet
		return len;
	}

	static void doTcpHead(int fl, int sndcnt, int rcvcnt, int len) {

		int i, ck;
		// clear rest of packet headers
		for (i = 0; i < 12; i++)
			buf[i] = 0;
		for (i = 20; i < 40; i++)
			buf[i] = 0;
	
		// Fill in IP header
//		Util.setShort(pkt, (short)4, id);	???
		buf[0] = 0x45;		// version, header len
	
		// Fill in TCP header
		buf[21] = 80; // Source port
		set16(22, tcb_port);
		set32(24, sndcnt);
		set32(28, rcvcnt);
		buf[32] = 0x50;		// data offset = 20 (no options)
		buf[33] = fl;		// flags
		set16(34, WINDOW);
	
		buf[9] = 6;			// protocol (tcp)
		set16(10, len);		// set tcp length in iph checksum for tcp checksum calculation
		buf[len+20] = 0;	// pad 0
		i = (len+1+12) & 0xfffe;	// make even
		ck = chkSum(8, i);
		buf[8] = 60;		// ttl
		set16(36, ck);
	
	}



	static int chkSum(int ix, int cnt) {

		int sum = 0;
		while (cnt>1) {
			sum += (buf[ix]<<8) | buf[ix+1];
			ix += 2;
			cnt -= 2;
		}
		if (cnt>0) sum += buf[ix];

		while ((sum>>16) != 0) sum = (sum & 0xffff) + (sum >> 16);

		return (~sum)&0xffff;
	}

	static void init() {
/*
		try {
			portId = CommPortIdentifier.getPortIdentifier("COM1");
			serialPort = (SerialPort) portId.open("SlipApp", 2000);
			is = serialPort.getInputStream();
			os = serialPort.getOutputStream();
			serialPort.setSerialPortParams(115200,
				SerialPort.DATABITS_8,
				SerialPort.STOPBITS_1,
				SerialPort.PARITY_NONE);
			serialPort.enableReceiveTimeout(TIMEOUT);

		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.exit(-1);
		}
*/
	}

	public static void putC(int c) {

/*
		try {
			os.write(c);
		} catch (IOException e) {}
*/
		while ((JopSys.rd(IO_STATUS)&1)==0) ;
//Display.data('0'+(c&0x0f));
		JopSys.wr(c, IO_UART);
	}

	private static void write() {

//Display.data('G');
		putC(END);
		for (int i=0; i<cnt; ++i) {
			int c = buf[i];
			if (c==END) {
				putC(ESC);
				putC(ESC_END);
			} else if (c==ESC) {
				putC(ESC);
				putC(ESC_ESC);
			} else {
				putC(c);
			}
		}
		putC(END);
	}

	private static int getC() {

/*
		int b = -1;

		try {
			b = is.read();
		} catch (IOException e) {}

		return b;
*/

		if ((JopSys.rd(IO_STATUS)&2)!=0) {
			int val = JopSys.rd(IO_UART);
			return val;
		} else {
			return -1;
		}
	}


	private static boolean read() {

/*
		for (;;) {		// as long as bytes are in read buffer (in JOP)
			int val = getC();
			if (val==-1) return false;
*/
		boolean blink = true;

		for (;;) {		// irgendwie klappt rts noch nicht!!!

			while ((JopSys.rd(IO_STATUS)&2)==0) {
				if (blink) {
					JopSys.wr(1, BBSys.IO_WD);
					blink = false;
				} else {
					JopSys.wr(0, BBSys.IO_WD);
					blink = true;
				}
			}
			int val = JopSys.rd(IO_UART);

			if (esc) {
				if (val == ESC_END) {
					buf[cnt++] = END;
				} else if (val == ESC_ESC) {
					buf[cnt++] = ESC;
				} else {
					buf[cnt++] = val;
				}
				esc = false;
				continue;
			}

			if (val == ESC) {
				esc = true;
			} else {
				esc = false;
				if (val==END) {
					if (cnt!=0) {
						if (cnt>=20) {		// ignore too short packages
JopSys.wr(0, BBSys.IO_WD);
							return true;
						} else {
							cnt = 0;
continue;
//							return false;
						}
					}
				} else {
					buf[cnt++] = val;
				}
			}
		}
//		return false;
	}

}
