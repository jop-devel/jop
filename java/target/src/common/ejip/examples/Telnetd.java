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

package ejip.examples;


import ejip.*;
import util.Timer;

/**
 * Test main for TCP.
 */	
public class Telnetd extends TcpHandler {

	static Net net;
	static LinkLayer ipLink;
	
	StringBuffer sb = new StringBuffer();
	StringBuffer cmd = new StringBuffer();

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
		ipLink = new CS8900(ejip, eth, ip);
		
		// a telnet server
		net.getTcp().addHandler(23, new Telnetd());

		forever();
	}

	private static void forever() {

		for (;;) {
			for (int i=0; i<1000; ++i) {
				ipLink.run();
				net.run();
			}
			Timer.wd();
		}
	}

	public Packet request(Packet p) {

		StringBuffer hello = new StringBuffer("Hello from JOP\r\n");
		Ip.getData(p, Tcp.DATA, sb);
		StringBuffer resp = null;
		if (sb.length()!=0) {
			System.out.print("Telnet data:");
			System.out.println(sb);
			for (int i=0; i<sb.length(); ++i) {
				char ch = sb.charAt(i);
				if (ch!='\n' && ch!='\r') {
					cmd.append(ch);
				} else {
					String s = cmd.toString();
					if (s.equals("hello")) {
						resp = hello;
					}
					cmd.setLength(0);
				}
			}
		}
		if (resp!=null) {
			Ip.setData(p, Tcp.DATA, resp);
		} else {
			p.len = Tcp.DATA<<2;
		}
		return p;
	}

	public Packet established(Packet p) {
		Ip.setData(p, Tcp.DATA, "Welcome to JOP\r\n");
		return p;
	}

	public boolean finished() {
		// TODO Auto-generated method stub
		return false;
	}
}
