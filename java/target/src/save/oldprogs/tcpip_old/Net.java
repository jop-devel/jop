package tcpip;

/**
*	Net.java: starts device driver threads and polls for packets.
*
*	Author: Martin Schoeberl (martin.schoeberl@chello.at)
*
*   Changelog:
*		2002-03-15	ARP works!
*		2002-10-21	use Packet buffer, 4 bytes in one word
*		2002-11-11	runs in its own thread
*
*/

import util.*;

public class Net extends Thread {

	private static int[] eth;				// own ethernet address
	private static int ip;					// own ip address

/**
*	The one and only reference to this object.
*/
	private static Net single;

/**
*	private because it's a singleton Thread.
*/
	private Net() {
	}

	public static void init() {

		if (single != null) return;			// allready called init()

		eth = new int[6];
		eth[0] = 0x00;
		eth[1] = 0xe0;
		eth[2] = 0x98;
		eth[3] = 0x33;
		eth[4] = 0xb0;
		eth[5] = 0xf7;		// this is eth card for chello
		eth[5] = 0xf8;
		ip = (192<<24) + (168<<16) + (0<<8) + 4;

		TcpIp.init();

		//
		//	start device driver threads
		//
// don't use CS8900 when simulating on PC
		CS8900.init(eth, ip);
// don't use PPP on my web server
		// Ppp.init(); 

		//
		//	start my own thread
		//
		single = new Net();
		single.start();
	}


/**
*	Look for received packets and call TcpIp.
*	Mark them to be sent if returned with len!=0 from TcpIp layer.
*/
	public void run() {

		Packet p;

		for (;;) {
			// is a received packet in the pool?
			p = Packet.getPacket(Packet.RCV, Packet.ALLOC);
			if (p!=null) {					// got one received Packet from pool
				yield();
				TcpIp.receive(p);
				yield();
				if (p.len!=0) {
					// a VERY dummy arp/routing!
					// should this be in the cs8900 ?
					p.llh[0] = p.llh[3];
					p.llh[1] = p.llh[4];
					p.llh[2] = p.llh[5];
					p.llh[6] = 0x0800;
					p.setStatus(Packet.SND);	// mark packet ready to send
				} else {
					p.setStatus(Packet.FREE);	// mark packet free
				}
			}
			// give a chance to Main thread for WD
			try {
				Thread.sleep(1);
			} catch (Exception e) {};
		}
	}
}
