/*
 * Copyright (c) Martin Schoeberl, martin@jopdesign.com
 * All rights reserved.
 * 
 * Ping example with ejip.
 *
 */

package ejip.examples;

import joprt.RtThread;
import util.Timer;
import ejip.*;


/**
*	Ping program with ejip.
*/
	
public class Pinger {

	static Net net;
	static LinkLayer ipLink;
	
	static int target;

/**
*	Start network and enter forever loop.
*/
	public static void main(String[] args) {
		
		Ejip ejip = new Ejip();

		//
		//	start TCP/IP
		//
		net = new Net(ejip);
// don't use CS8900 when simulating on PC or for BG263
		int[] eth = {0x00, 0xe0, 0x98, 0x33, 0xb0, 0xf8};
		int ip = Ejip.makeIp(192, 168, 0, 123); 
		target = Ejip.makeIp(192, 168, 0, 1);
		ipLink = new CS8900(ejip, eth, ip);


		//
		//	start device driver threads
		//

		new RtThread(5, 10000) {
			public void run() {
				for (;;) {
					waitForNextPeriod();
					net.run();
				}
			}
		};
		new RtThread(5, 10000) {
			public void run() {
				for (;;) {
					waitForNextPeriod();
					ipLink.run();
				}
			}
		};

		new Worker(ejip);
		
		//
		//	WD thread has lowest priority to see if every timing will be met
		//

		RtThread.startMission();

		forever();
	}

	private static void forever() {

		//
		//	just do the WD blink with lowest priority
		//	=> if the other threads take to long (*3) there will be a reset
		//
		for (;;) {
			for (int i=0; i<10; ++i) {
				RtThread.sleepMs(50);
				Timer.wd();
				Timer.loop();
			}
			Timer.wd();
		}
	}
}

class Worker extends RtThread {
	
	Ejip ejip;
	
	Worker(Ejip ejipRef) {
		super(1, 5*1000000);
		ejip = ejipRef;
	}
	public void run() {
		
		int seqnr = 0;	// Ping sequence number
		
		for (;;) {
			waitForNextPeriod();
			System.out.println("Ping");
			Packet p = ejip.getFreePacket(Pinger.ipLink); 
			if (p==null) {
				System.out.println("no free packet");
				continue;
			}

			// to learn as much as possible
			// we generate the whole IP packet here,
			// but use the checksum calculation from TcpIP
			
			int[] buf = p.buf;
			p.len = 20+3*4;						// len is in bytes
			int len = p.len;
			int prot = 0x0001;					// ICMP

			// this is the IP header
			
			buf[0] = 0x45000000 + len;			// ip length	(header without options)
			buf[1] = Ip.getId();				// identification, no fragmentation
			buf[2] = (0x20<<24) + (prot<<16);	// ttl, protocol, clear checksum
			// source ip address
			buf[3] = Pinger.ipLink.getIpAddress();
			// destination ip address
			buf[4] = Pinger.target;
			buf[2] |= Ip.chkSum(buf, 0, 20);

			
			// this is the ICMP header
			int type = 0x0800;	// type 8 is echo request
			int id = 0x1234;	// id is in Unix the process IO
			
			buf[5] = type<<16;
			buf[6] = (id<<16) + seqnr;
			++seqnr;			// increment sequence number
			buf[7] = 0x20202030 + 1;	// data: '   1'
			// checksum covers whole ICMP packet
			int check = Ip.chkSum(buf, 5, 3*4);
			buf[5] |= check;
			
			p.llh[6] = 0x0800;

			Pinger.ipLink.enqTxPacket(p);
		}
	}
}
