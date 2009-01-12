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


import com.jopdesign.io.IOFactory;
import com.jopdesign.io.SysDevice;
import com.jopdesign.sys.Startup;

import ejip_old.*;

/**
 * Test program for CMP version of ejip. This is the version that
 * does not scale very well. We have to check why at some time.
 * 
 * @author Martin Schoeberl
 * 
 */
public class EjipBenchCMPSlow {
	
	// Shared Variables
	static boolean finished;
	
	static Object monitor = new Object();
	static final int CNT = 10000;
	static final boolean CMP = false;
	static final int MAX_OUTSTANDING = 3;
		
	static Net net;
	static LinkLayer ipLink;
	
	static int sent;
	static int received;
	static int a, b;
	static int sum;

/**
*	Start network.
*/
	public EjipBenchCMPSlow() {

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
	
	private static void request() {
		
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

		
		EjipBenchCMPSlow ebench = new EjipBenchCMPSlow();

		SysDevice sys = IOFactory.getFactory().getSysDevice();
		int nrCpu = Runtime.getRuntime().availableProcessors();

		System.out.println("Ejip benchmark");

		Runnable rl = new Runnable() {
			public void run() {
				for (;;) {
					ipLink.loop();					
				}
			}
		};

		Runnable rn = new Runnable() {
			public void run() {
				for (;;) {
					net.loop();					
				}
			}
		};

		if (nrCpu>2) {
			Startup.setRunnable(rl, 0);
			Startup.setRunnable(rn, 1);
		} else if (nrCpu==2) {
			Startup.setRunnable(rn, 0);			
		}
		
		for (int i = 0; i < nrCpu; i++) {
//			Startup.setRunnable(r, i);
		}

		System.out.println("Start calculation");
		// Start of measurement
		// Start of all other CPUs
		sys.signal = 1;
		start = (int) System.currentTimeMillis();


		if (nrCpu==1) {
			for (received=0; received<CNT;) {
				request();
				ipLink.loop();
				net.loop();
			}
		} else if (nrCpu==2) {
			for (received=0; received<CNT;) {
				request();
				ipLink.loop();
			}
		} else {
			for (received=0; received<CNT;) {
				request();
			}			
		}

		// End of measurement
		stop = (int) System.currentTimeMillis();
		time = stop - start;
		System.out.println("TimeSpent: " + time);
	}
}



