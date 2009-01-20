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

import com.jopdesign.sys.Const;

import util.Serial;
import util.Timer;

import ejip.*;

/**
 * Test main for TCP.
 */	
public class SMTPClient extends TcpHandler {

	public static final int IDLE = 0;
	public static final int HELO = 1;
	public static final int FROM = 2;
	public static final int RCPT = 3;
	public static final int DATA = 4;
	public static final int MSG = 5;
	public static final int CONT = 6;
	public static final int QUIT = 7;

	static Ejip ejip;
	static Net net;
	static LinkLayer ipLink;
	static Serial ser;
	
	private int state = IDLE;
	private StringBuffer sb = new StringBuffer();

	private StringBuffer helo;
	private StringBuffer from;
	private StringBuffer rcpt;
	private StringBuffer msg;

	private int msgOffset;

	public SMTPClient(String domain, String sender, String receiver, String message) {
		helo = new StringBuffer("HELO ");
		helo.append(domain);
		helo.append("\r\n");

		from = new StringBuffer("MAIL FROM:<");
		from.append(sender);
		from.append(">\r\n");

		rcpt = new StringBuffer("RCPT TO:<");
		rcpt.append(receiver);
		rcpt.append(">\r\n");

		msg = new StringBuffer(message);
	}

	public Packet request(Packet p) {

		Ip.getData(p, Tcp.DATA, sb);

		// ignore messages unless we continue to send the message
		if ((sb.length() == 0) && (state != CONT)) {
			return null;
		}

		switch(state) {
		case HELO:
			if (sb.charAt(0)=='2' && sb.charAt(1)=='2' && sb.charAt(2)=='0') {
				Ip.setData(p, Tcp.DATA, helo);
				state = FROM;
			} else {
				Ip.setData(p, Tcp.DATA, "QUIT\r\n");
				state = IDLE;
			}
			break;
		case FROM:
			if (sb.charAt(0)=='2' && sb.charAt(1)=='5' && sb.charAt(2)=='0') {
				Ip.setData(p, Tcp.DATA, from);
				state = RCPT;
			} else {
				Ip.setData(p, Tcp.DATA, "QUIT\r\n");
				state = IDLE;
			}
			break;
		case RCPT:
			if (sb.charAt(0)=='2' && sb.charAt(1)=='5' && sb.charAt(2)=='0') {
				Ip.setData(p, Tcp.DATA, rcpt);
				state = DATA;
			} else {
				Ip.setData(p, Tcp.DATA, "QUIT\r\n");
				state = IDLE;
			}
			break;
		case DATA:
			if (sb.charAt(0)=='2' && sb.charAt(1)=='5' && sb.charAt(2)=='0') {
				Ip.setData(p, Tcp.DATA, "DATA\r\n");
				state = MSG;
			} else {
				Ip.setData(p, Tcp.DATA, "QUIT\r\n");
				state = IDLE;
			}
			break;
		case MSG:
			if (sb.charAt(0)=='3' && sb.charAt(1)=='5' && sb.charAt(2)=='4') {				
				msgOffset = Ip.setData(p, Tcp.DATA, msg, 0);				
				state = CONT;
			} else {
				Ip.setData(p, Tcp.DATA, "QUIT\r\n");
				state = IDLE;
			}
			break;
		case CONT:
			if (msgOffset < msg.length()) {
				msgOffset = Ip.setData(p, Tcp.DATA, msg, msgOffset);				
				state = CONT;
			} else {
				Ip.setData(p, Tcp.DATA, "\r\n.\r\n");
				state = QUIT;
			}
			break;
		case QUIT:
			Ip.setData(p, Tcp.DATA, "QUIT\r\n");
			state = IDLE;
			break;
		case IDLE:
			// ignore any messages when idle
			return null;
		}
		
		return p;
	}

	public Packet established(Packet p) {
		state = HELO;
		return p;
	}

	public boolean finished() {
		return false; // let the server shut down the connection
	}

	/**
	*	Start network and enter forever loop.
	*/
	public static void main(String[] args) {
		
		ejip = new Ejip(8, 256);

		//
		//	start TCP/IP
		//
		net = new Net(ejip);

		//
		//	use second serial line for simulation
		//	with JopSim and on the project usbser
		//
		ser = new Serial(Const.IO_UART_BG_MODEM_BASE);
		int ip = Ejip.makeIp(192, 168, 1, 2); 
		ipLink = new Slip(ejip, ser, ip);
		
		// create smtp client
		String message = ("From: <jop@jopdesign.com>\r\n"
						  +"To: <user@host>\r\n"
						  +"Subject: Hello, world!\r\n"
						  +"Happy happy, joy joy :-)");
		SMTPClient client = new SMTPClient("jopdesign.com", "jop@jopdesign.com", "user@host", message);

		// register smtp client
		net.getTcp().addHandler(10025, client);
		// get connection to smtp server 192.168.1.1 (postfix running on local PC)
		net.getTcp().startConnection(ipLink, Ejip.makeIp(192, 168, 1, 1), 25);

		forever();
	}

	private static void forever() {

		for (;;) {
			for (int i=0; i<1000; ++i) {
				ser.loop();
				// timeout in slip depends on loop time!
				ipLink.run();
				ser.loop();
				net.run();
			}
			Timer.wd();
			System.out.print("*");
		}
	}
}
