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

import java.io.IOException;
import util.Serial;
import util.Timer;

import ejip.*;
import sdcard.*;

/**
 * Simple HTTP server. Uses the SD card file system. 
 */	
public class HTTPServer extends TcpHandler {

	public static final int CMD = 0;
	public static final int CONT_CMD = 1;
	public static final int CONT_REPLY = 2;
	public static final int QUIT = 3;

	public static final int MAX_CMDLEN = 4*1024;
	private static final int BUFFER_LEN = 1024;

	static Ejip ejip;
	static Net net;
	static LinkLayer ipLink;
	static Serial ser;

	private int state = CMD;
	private StringBuffer sb = new StringBuffer();
	private StringBuffer cmd = new StringBuffer();

	private FileInputStream currentFile;
	private byte[] buffer = new byte[BUFFER_LEN];

	public Packet request(Packet p) {

		Ip.getData(p, Tcp.DATA, sb);
		
		System.out.println("serving HTTP request");

		// ignore messages unless we continue to send the message
		if ((sb.length() == 0) && (state != CONT_REPLY)) {
			return null;
		}

		switch(state) {
		case CMD:
			cmd.delete(0, cmd.length());
			state = CONT_CMD;
			/* fall through */
		case CONT_CMD:
			cmd.append(sb);

			if (cmd.indexOf("\r\n\r\n") >= 0) {
				//System.out.println("--------------------------------");
				//System.out.print(cmd);
				//System.out.println("--------------------------------");
				// parse command and send first reply
				if (cmd.charAt(0)=='G' && cmd.charAt(1)=='E' && cmd.charAt(2)=='T' && cmd.charAt(3)==' ') {

					if (cmd.charAt(4)=='/') {

						sb.delete(0, sb.length());
						if (cmd.charAt(5)==' ') {
							sb.append("index.htm");
						} else {
							int i = 5;
							while (cmd.charAt(i) != ' ') {
								sb.append(cmd.charAt(i));
								i++;
							}
						}

						try {
							currentFile = new FileInputStream(sb);

							sb.delete(0, sb.length());
							sb.append("HTTP/1.0 200 OK\r\nContent-Length: ");
							sb.append(currentFile.available());
							sb.append("\r\n\r\n");
							Ip.setData(p, Tcp.DATA, sb);
							state = CONT_REPLY;

						} catch (IOException exc) {
							Ip.setData(p, Tcp.DATA, "HTTP/1.0 404 File not found\r\nContent-Length: 3\r\n\r\n404");
							state = QUIT;
						}
					} else {
						Ip.setData(p, Tcp.DATA, "HTTP/1.0 404 File not found\r\nContent-Length: 3\r\n\r\n404");
						state = QUIT;
					}
				} else {
					Ip.setData(p, Tcp.DATA, "HTTP/1.0 501 I can only handle GET\r\nContent-Length: 3\r\n\r\n501");
					state = QUIT;
				}
			} else {
				if (cmd.length() > MAX_CMDLEN) {
					state = QUIT;
				}
				// just ack what we have
				return null;
			}
			break;
		case CONT_REPLY:
			// read from file
			int r = currentFile.read(buffer);
			// hand over packet
			Ip.setData(p, Tcp.DATA, buffer, r);
			// check for EOF
			if (r == buffer.length) {
				state = CONT_REPLY;
			} else {
				currentFile.close();
				state = QUIT;
			}
			break;
		case QUIT:
			// ignore any messages when idle
			return null;
		}

		return p;
	}

	public Packet established(Packet p) {
		state = CMD;
		return null;
	}

	public boolean finished() {
		return state == QUIT;
	}

	/**
	*	Start network and enter forever loop.
	*/
	public static void main(String[] args) {

		ejip = new Ejip(100, 1500);

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
		
		// create http server
		HTTPServer server = new HTTPServer();

		// register http server
		net.getTcp().addHandler(80, server);

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