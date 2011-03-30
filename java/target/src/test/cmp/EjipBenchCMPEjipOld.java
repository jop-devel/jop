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


import rtlib.SRSWQueue;

import com.jopdesign.io.IOFactory;
import com.jopdesign.io.SysDevice;
import com.jopdesign.sys.Startup;

import ejip_old.*;

/**
 * Test program for CMP version of ejip.
 * 
 * @author Martin Schoeberl
 * 
 */
public class EjipBenchCMPEjipOld {
	
	// Shared Variables
	static boolean finished;
	
	static Object monitor = new Object();
	static final int CNT = 1000;
	static final int VECTOR_LEN = 10;
	static final int MAX_OUTSTANDING = 4;
		
	static Net net;
	static LinkLayer ipLink;
	
	static volatile int sent;
	static volatile int received;
	static int a, b;
	static int sum;
	
	static SRSWQueue<Packet> macQueue, resultQueue;

/**
*	Start network.
*/
	public static void init() {

		net = Net.init();
		ipLink = Loopback.init();
		
		macQueue = new SRSWQueue<Packet>(Packet.MAX);
		resultQueue = new SRSWQueue<Packet>(Packet.MAX);

		UdpHandler mac;
		mac = new UdpHandler() {
			public void request(Packet p) {
				macQueue.enq(p);
			}
		};
		Udp.addHandler(1234, mac);

		UdpHandler result;
		result = new UdpHandler() {
			public void request(Packet p) {
				resultQueue.enq(p);
			}
		};
		Udp.addHandler(5678, result);

		sent = 0;
		a = 0x1234;
		b = 0xabcd;
		sum = 1234;
	}
	
	private static void macServer() {
		
		Packet p = macQueue.deq();
		if (p==null) return;
		
		if (p.len < ((Udp.DATA+1)<<2)) {
			p.setStatus(Packet.FREE);
		} else {
			int buf[] = p.buf;
			int len = buf[Udp.DATA];
			int result = 0;
			
			
			for (int i=0; i<len; ++i) { // @WCA loop=10
				result += buf[Udp.DATA+1+i*2] * buf[Udp.DATA+2+i*2];
			}
			
			buf[Udp.DATA] = result;
			p.len = (Udp.DATA+1)<<2;
			Udp.build(p, (127<<24)+(0<<16)+(0<<8)+1, 5678);
		}
		
	}
	
	private static void resultServer() {
		
		Packet p = resultQueue.deq();
		if (p==null) return;

		if (p.len == ((Udp.DATA)<<2)) {
			sum = p.buf[Udp.DATA];
		}
		++received;
		p.setStatus(Packet.FREE);
		if (received>=CNT) {
			Runner.stop();
		}		
	}
	private static void request() {

		if (sent-received<MAX_OUTSTANDING) {
			Packet p = Packet.getPacket(Packet.FREE, Packet.ALLOC, ipLink);
			if (p == null) {				// got no free buffer!
				return;
			}
			int buf[] = p.buf;
			buf[Udp.DATA] = VECTOR_LEN;			
			for (int i=0; i<VECTOR_LEN; ++i) { // @WCA loop=10
				buf[Udp.DATA+1+i*2] = a;
				buf[Udp.DATA+2+i*2] = b;				
				// just generate new 'funny' values and use sum
				a = (a<<1)^b;
				b = (b<<1)^sum;
			}
			
			p.len = (Udp.DATA+1+VECTOR_LEN*2)<<2;
			Udp.build(p, (127<<24)+(0<<16)+(0<<8)+1, 1234);
			sent++;
		}
//		System.out.print("request ");
//		System.out.print(sent);
//		System.out.print(" ");
//		System.out.println(received);
	}

	
	public static void main(String[] args) {

		// Initialization for benchmarking 
		int start = 0;
		int stop = 0;
		int time = 0;

		init();

		SysDevice sys = IOFactory.getFactory().getSysDevice();
		int nrCpu = Runtime.getRuntime().availableProcessors();

		System.out.println("Ejip benchmark");
		
		// our work list
		Runnable[] work = new Runnable[] {
			// first one (or more) can printout
			new Runnable() { public void run() { ipLink.loop(); } },
			new Runnable() { public void run() { resultServer(); } },
			new Runnable() { public void run() { request(); } },				
			new Runnable() { public void run() { macServer(); } },
			// more heavy tasks at the end
			new Runnable() { public void run() { net.loop(); } },
		};

		Runner[] runner = Runner.distributeWorklist(work, nrCpu);
		for (int i=0; i<nrCpu-1; i++) {
			Startup.setRunnable(runner[i+1], i);
		}

		received = 0;
		System.out.println("Start");
		// Start of measurement
		// Start of all other CPUs
		sys.signal = 1;
		start = (int) System.currentTimeMillis();

		runner[0].run();

		// End of measurement
		stop = (int) System.currentTimeMillis();
		time = stop - start;
		System.out.println("TimeSpent: " + time);
	}
}



