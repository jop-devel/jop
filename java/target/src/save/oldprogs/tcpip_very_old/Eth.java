package tcpip_old;

/**
*	Eth.java: does ARP and calls TcpIp.
*
*	Author: Martin Schoeberl (martin.schoeberl@chello.at)
*
*   Changelog:
*		2002-03-15	ARP works!
*
*
*/

import util.*;

public class Eth {

	private static final int BUF_LEN = 6*256;
	private static int[] buf;				// send and receive buffer
	private static int rcv_len;

	private static int[] eth;				// own ethernet address
	private static int[] ip;				// own ip address


	public static final int ETH_HLEN = 14;		/* Total octets in header.	 */

/**
*	test main.
*/
	public static void main(String[] args) {

		Timer.init(20000000, 5);	// just for the watch dog
		Dbg.init();

		init();
		for (;;) {
			loop();
			Timer.wd();
		}
	}

	public static void init() {

		buf = new int[BUF_LEN];

		eth = new int[6];
		eth[0] = 0x00;
		eth[1] = 0xe0;
		eth[2] = 0x98;
		eth[3] = 0x33;
		eth[4] = 0xb0;
		eth[5] = 0xf7;		// this is eth card for chello
		eth[5] = 0xf8;
		ip = new int[4];
		ip[0] = 192;
		ip[1] = 168;
		ip[2] = 0;
		ip[3] = 4;


		rcv_len = 0;

		TcpIp.init();
		CS8900.init(eth);		// no return value???
	}


	public static void loop() {

		int i;

		CS8900.poll();

		if (CS8900.rxCnt!=0) {
			rcv_len = CS8900.rxCnt;
			for (i=0; i<rcv_len; ++i) buf[i] = CS8900.rxBuf[i];
			CS8900.rxCnt = 0;

			process();
		}

	}

	private static void process() {

		int i;
		int len;

		i = (buf[12]<<8) + buf[13];
		if (i == 0x0806) {					// ARP type
			arp();
		} else if (i == 0x0800) {			// IP type
			len = TcpIp.receive(buf, ETH_HLEN, rcv_len-ETH_HLEN);
			if (len!=0) {
				for (i=0; i<6; ++i) {
					buf[0+i] = buf[6+i];	// old src->dest
					buf[6+i] = eth[i];		// src
				}
//Dbg.wr('I');
//Dbg.wr(' ');
//for (i=0; i<ETH_HLEN+len; ++i) Dbg.hexVal(buf[i]);
//Dbg.wr('\n');
				CS8900.send(buf, ETH_HLEN+len);		// send back changed packet
			}
		}
		// ignore
		rcv_len = 0;
	}

	private static void arp() {

		int i;

		//Dbg.wr('A');

		//Dbg.intVal(buf[38]);
		//Dbg.intVal(buf[39]);
		//Dbg.intVal(buf[40]);
		//Dbg.intVal(buf[41]);

		if (buf[38]==ip[0] &&
			buf[39]==ip[1] &&
			buf[40]==ip[2] &&
			buf[41]==ip[3]) {


/*
    Ethernet packet data:
	16.bit: (ar$hrd) Hardware address space (e.g., Ethernet,
			 Packet Radio Net.)
	16.bit: (ar$pro) Protocol address space.  For Ethernet
			 hardware, this is from the set of type
			 fields ether_typ$<protocol>.
	 8.bit: (ar$hln) byte length of each hardware address
	 8.bit: (ar$pln) byte length of each protocol address
	16.bit: (ar$op)  opcode (ares_op$REQUEST | ares_op$REPLY)
	nbytes: (ar$sha) Hardware address of sender of this
			 packet, n from the ar$hln field.
	mbytes: (ar$spa) Protocol address of sender of this
			 packet, m from the ar$pln field.
	nbytes: (ar$tha) Hardware address of target of this
			 packet (if known).
	mbytes: (ar$tpa) Protocol address of target.
*/
			/*
			Swap hardware and protocol fields, putting the local
	    	hardware and protocol addresses in the sender fields.
			*/
			swapAdr();

			/*
			Set the ar$op field to ares_op$REPLY
			*/
			buf[21] = 0x02;		// ARP replay

			/*
			Send the packet to the (new) target hardware address on
	    	the same hardware on which the request was received.
			*/

			//Dbg.wr(' ');
			//Dbg.wr('r');
			//Dbg.wr('\n');

			CS8900.send(buf, 60);
		}

		//Dbg.wr('\n');
	}

	private static void swapAdr() {

		int i;

		for (i=0; i<6; ++i) {
			buf[0+i] = buf[6+i];	// old src->dest
			buf[32+i] = buf[6+i];	// arp dest addr
			buf[6+i] = eth[i];		// src
			buf[22+i] = eth[i];		// arp src addr
		}
		for (i=0; i<4; ++i) {		// set ip fields
			buf[38+i] = buf[28+i];
			buf[28+i] = ip[i];
		}
	}

	private static void ip() {

		//Dbg.wr('I');
	}

			

}
