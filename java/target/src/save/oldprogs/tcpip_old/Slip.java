package tcpip;

/**
*	Slip.java
*
*	communicate with jopbb via serial line.
*/

import util.*;

/**
*	Slip driver.
*/

public class Slip extends LinkLayer {

	private static final int MAX_BUF = 1500;		// or should we use 1006
/**
*	period for thread in ms.
*/
	private static final int PERIOD = 10;

	private static final int END = 0xc0;
	private static final int ESC = 0xdb;
	private static final int ESC_END = 0xdc;
	private static final int ESC_ESC = 0xdd;


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

/**
*	private constructor. The singleton object is created in init().
*/
	private Slip() {
		super(PERIOD);						// thats the period.
	}

/**
*	allocate buffer, start serial buffer and slip Thread.
*/
	public static void init() {

		if (single != null) return;			// allready called init()

		rbuf = new int[MAX_BUF];
		sbuf = new int[MAX_BUF];
		cnt = 0;
		esc = false;
		ready = false;
		scnt = 0;
		sent = 0;

		Serial.init();						// start serial buffer thread

		single = new Slip();
		single.start();						// kick off the thread
	}

/**
*	main loop.
*/
	public void run() {

		int timer = 0;; 
		Packet p;

		for (;;) {
			waitForNextPeriod();

			//
			//	read, write loop
			//
			if (loop()) {
				timer = 0;
			} else {
				++timer;
				if (timer>1000/PERIOD && cnt>0) {		// flush buffer on timeout
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
					if (Serial.txFreeCnt()>0) {
						Serial.wr('C');
						Serial.wr('L');
						Serial.wr('I');
						Serial.wr('E');
						Serial.wr('N');
						Serial.wr('T');
						Serial.wr('S');
						Serial.wr('E');
						Serial.wr('R');
						Serial.wr('V');
						Serial.wr('E');
						Serial.wr('R');
						Serial.wr('\r');
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
				p = Packet.getPacket(single, Packet.SND, Packet.ALLOC);
				if (p!=null) {
					send(p);				// send one packet
				}
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
			return;							// try again later
		}									// buf blocks receive buffer :-< 

		int[] pb = p.buf;

		rbuf[cnt] = 0;
		rbuf[cnt+1] = 0;
		rbuf[cnt+2] = 0;

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

Dbg.wr('s');
Dbg.intVal(cnt);
		cnt = 0;
		ready = false;

		p.setStatus(Packet.RCV);		// inform upper layer
	}


/**
*	copy packet to send buffer.
*/
	private static void send(Packet p) {

		int i, k;
		int[] pb = p.buf;

Dbg.wr('S');
Dbg.intVal(p.len);

		scnt = p.len;
		sent = 0;
		for (i=0; i<scnt; i+=4) {
			k = pb[i>>>2];
			sbuf[i] = k>>>24;
			sbuf[i+1] = (k>>>16)&0xff;
			sbuf[i+2] = (k>>>8)&0xff;
			sbuf[i+3] = k&0xff;
		}
		p.setStatus(Packet.FREE);		// mark packet free
	}

/* warum geht das nicht !!!!!
	private void loop() {
*/
/**
*	read from serial buffer and build an ip packet.
*	send a packet if one is in our send buffer.
*/
	private static boolean loop() {

		int i;
		boolean ret = false;

		i = Serial.rxCnt();
		if (i!=0) {
			ret = true;
			rcv(i);
		}
		if (scnt!=0) {
			i = Serial.txFreeCnt();
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
			Serial.wr(END);
			--free;
		}

		for (i=sent; free>1 && i<scnt; ++i) {

			int c = sbuf[i];
			if (c==END) {
				Serial.wr(ESC);
				Serial.wr(ESC_END);
				free -= 2;
			} else if (c==ESC) {
				Serial.wr(ESC);
				Serial.wr(ESC_ESC);
				free -= 2;
			} else {
				Serial.wr(c);
				--free;
			}
		}
		sent = i;

		if (sent==scnt && free!=0) {
			Serial.wr(END);
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

			int val = Serial.rd();

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


}
