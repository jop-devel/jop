/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2008, Martin Schoeberl

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package cmp;


import ejip.LinkLayer;
import ejip.Loopback;
import ejip.Net;
import ejip.Packet;
import ejip.Udp;
import ejip.UdpHandler;
import joprt.RtThread;

/**
 * Test program for CMP version of ejip.
 * 
 * @author Martin Schoeberl
 * 
 */
public class EjipBenchCMP {
	
	static class LinkThread extends RtThread {

		public LinkThread(int prio, int us) {
			super(prio, us);
			// TODO Auto-generated constructor stub
		}

		public void run() {
			while (!go) {
				waitForNextPeriod();
			}
		}
	}

	// Shared Variables
	static boolean finished;
	
	static Object monitor = new Object();
	static final int PRIORITY = 1;
	static final int PERIOD = 1000;
	static final int CNT = 10000;
	static final boolean CMP = false;
	static final int MAX_OUTSTANDING = 1;
	
	volatile static boolean go = false;
	
	static Net net;
	static LinkLayer ipLink;
	
	int sent;
	int received;
	int a, b;
	int sum;

/**
*	Start network.
*/
	public EjipBenchCMP() {

		net = Net.init();
		ipLink = Loopback.init();

		UdpHandler adder;
		adder = new UdpHandler() {
			public void request(Packet p) {
				if (p.len != ((Udp.DATA+1)<<2)) {
					p.setStatus(Packet.FREE);
				} else {
					p.buf[Udp.DATA] += p.buf[Udp.DATA+1];
					p.len = (Udp.DATA)<<2;
					Udp.build(p, (127<<24)+(0<<16)+(0<<8)+1, 5678);
				}
			}
		};
		Udp.addHandler(1234, adder);

		UdpHandler result;
		result = new UdpHandler() {
			public void request(Packet p) {
				if (p.len == ((Udp.DATA)<<2)) {
					sum = p.buf[Udp.DATA];
				}
				++received;
				p.setStatus(Packet.FREE);
			}
		};
		Udp.addHandler(5678, result);

		sent = 0;
		a = 0x1234;
		b = 0xabcd;
		sum = 1234;
	}

	public int test(int cnt) {

		for (received=0; received<cnt;) {
			request();
			ipLink.loop();
			net.loop();
		}
		return sum;
	}
	
	private void request() {
		
		if (sent-received<MAX_OUTSTANDING) {
			Packet p = Packet.getPacket(Packet.FREE, Packet.ALLOC, ipLink);
			if (p == null) {				// got no free buffer!
				return;
			}
			p.buf[Udp.DATA] = a;
			p.buf[Udp.DATA+1] = b;
			p.len = (Udp.DATA+1)<<2;
			Udp.build(p, (127<<24)+(0<<16)+(0<<8)+1, 1234);
			sent++;
			// just generate new 'funny' values and use sum
			a = (a<<1)^b;
			b = (b<<1)^sum;
		}
	}

	
	public static void main(String[] args) {

		// Initialization for benchmarking 
		int start = 0;
		int stop = 0;
		int time = 0;

		System.out.println("Ejip benchmark:");
		
		EjipBenchCMP ebench = new EjipBenchCMP();

		int nrCpu = Runtime.getRuntime().availableProcessors();

		for (int i = 0; i < nrCpu; i++) {
//			rtt.setProcessor(i);
		}

		// Start threads on this and other CPUs
		RtThread.startMission();

		// give threads time to setup their memory
		RtThread.sleepMs(100);
		System.out.println("Start calculation");
		// Start of measurement
		start = (int) System.currentTimeMillis();
		go = true;
		
		for (int i=0; i<CNT; ++i) {
		}
		ebench.test(CNT);

		// This main thread will be blocked till the
		// worker thread has finished

//		while (true) {
//			synchronized (monitor) {
//				if (finished) {
//					break;
//				}
//			}
//		}

		// End of measurement
		stop = (int) System.currentTimeMillis();

		System.out.println("StartTime: " + start);
		System.out.println("StopTime: " + stop);
		time = stop - start;
		System.out.println("TimeSpent: " + time);
	}
}



