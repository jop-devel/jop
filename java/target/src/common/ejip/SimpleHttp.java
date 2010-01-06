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
 * Simple HTTP server, based on Wolfgang's HTTPServer example.
 */
public class SimpleHttp extends TcpHandler {

	public static final int CMD = 0;
	public static final int CONT_CMD = 1;
	public static final int CONT_REPLY = 2;
	public static final int QUIT = 3;

	public static final int MAX_CMDLEN = 4*1024;

	private int state = CMD;
	private StringBuffer sb = new StringBuffer();
	private StringBuffer cmd = new StringBuffer();

	public Packet request(Packet p) {

		Ip.getData(p, Tcp.DATA, sb);
		
		if (Logging.LOG) {
			Logging.wr("\nHTTP request: ");
			Logging.wr(sb);
		}

		// ignore messages unless we continue to send the message
		if ((sb.length() == 0) && (state != CONT_REPLY)) {
			return null;
		}

		switch(state) {
		case CMD:
			cmd.setLength(0);
			state = CONT_CMD;
			/* fall through */
		case CONT_CMD:
			cmd.append(sb);

			if (cmd.indexOf("\r\n\r\n") >= 0) {
				// parse command and send first reply
				if (cmd.charAt(0)=='G' && cmd.charAt(1)=='E' && cmd.charAt(2)=='T' && cmd.charAt(3)==' ') {

					if (cmd.charAt(4)=='/') {
						sb.setLength(0);
						sb.append("HTTP/1.0 200 OK\r\n\r\n");
						Ip.setData(p, Tcp.DATA, sb);
						state = CONT_REPLY;
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
			// get the single page
			setContent(sb, cmd);
			Ip.setData(p, Tcp.DATA, sb);
			// we serve only a single page
			state = QUIT;
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
	
	private int hitCount;
	
	/**
	 * Serve a single HTTP packet. Overwrite for an application HTTP
	 * server.
	 * @param sb the HTML content will be set here.
	 */
	void setContent(StringBuffer sb, StringBuffer cmd) {
		sb.setLength(0);
		sb.append("<html><head></head><body><h2>Hello WWW World!</h2>");
		sb.append("<p>");
		sb.append(++hitCount);
		sb.append("</body></html>\r\n\r\n");
	}
}