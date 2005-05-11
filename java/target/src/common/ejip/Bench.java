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
*	Ejip.java: Benchmark with ejip TCP/IP stack.
*
*	Author: Martin Schoeberl (martin.schoeberl@chello.at)
*
*/

	
public class Bench {

	static Net net;
	static LinkLayer ipLink;
	
	static boolean sent;
	static int received;
	static int a, b;
	static int sum;

/**
*	Start network and enter forever loop.
*/
	public static void main(String[] args) {

		net = Net.init();
		ipLink = Loopback.init();

		UdpHandler adder;
		adder = new UdpHandler() {
			public void request(Packet p) {
				if (p.len != ((Udp.DATA+1)<<2)) {
					p.setStatus(Packet.FREE);
				} else {
System.out.print("adder rcv ");
System.out.print(p.buf[Udp.DATA]);
System.out.print(p.buf[Udp.DATA+1]);
					p.buf[Udp.DATA] += p.buf[Udp.DATA+1];
System.out.print(p.buf[Udp.DATA]);
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
System.out.print("result rcv ");
System.out.println(sum);
				}
				sent = false;
				++received;
				p.setStatus(Packet.FREE);
			}
		};
		Udp.addHandler(5678, result);

		sent = false;
		a = 0x1234;
		b = 0xabcd;
		sum = 1234;

		test(5000);
	}

	private static void test(int cnt) {

		for (received=0; received<cnt;) {
			request();
			ipLink.loop();
			net.loop();
		}
	}
	
	private static void request() {
		
		if (!sent) {
			Packet p = Packet.getPacket(Packet.FREE, Packet.ALLOC, ipLink);
			if (p == null) {								// got no free buffer!
				return;
			}
			p.buf[Udp.DATA] = a;
			p.buf[Udp.DATA+1] = b;
			p.len = (Udp.DATA+1)<<2;
			Udp.build(p, (127<<24)+(0<<16)+(0<<8)+1, 1234);
			sent = true;
			// just generate new 'funny' values and use sum
			a = (a<<1)^b;
			b = (b<<1)^sum;
		}
	}
}
