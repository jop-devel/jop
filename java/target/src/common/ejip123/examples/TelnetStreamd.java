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

package ejip123.examples;

import com.jopdesign.sys.Const;
import ejip123.*;
import joprt.RtThread;
import ejip123.util.Dbg;
import ejip123.util.DbgUdp;
import ejip123.util.Serial;
import util.Timer;

/**
 A small telnet server. This example shows how a TCP server can be implemented with the non-blocking socket orientated
 TcpHandler interface.
 */
public class TelnetStreamd implements PingReplyHandler{
private TcpStreamConnection con;

private final int GREET = 0;
private final int ESTABLISHED = 1;
private final int PING = 2;
private final int PINGDONE = 3;
private int state = GREET;
private StringBuffer rcv = new StringBuffer(32);
private StringBuffer tmp = new StringBuffer(32);

public static void main(String[] args){

	PacketPool.init(8, 1500);

//	Dbg.initSerWait();
	DbgUdp.init();

	Router.init(3);
//	Serial ser = new Serial(10, 1000, Const.IO_UART_BG_MODEM_BASE); // simulator
	Serial ser = new Serial(10, 1000, Const.IO_UART1_BASE);
	LinkLayer slip = Slip.init(9, 1000, ser, Ip.Ip(192, 168, 2, 2), 1500);
	Ip.init(5, 5000);
	Router.addRoute(new Route(Ip.Ip(192, 168, 2, 0), Ip.Ip(255, 255, 255, 0), slip));
	Router.setDefaultInterface(slip);
	Tcp.init(3, 4);

	TelnetStreamd th = new TelnetStreamd();
	th.con = new TcpStreamConnection(5, 1000);
	Tcp.addHandler(23, th.con);

	RtThread.startMission();
	Router.print();
	th.forever();
}

/**
 @noinspection NonPrivateFieldAccessedInSynchronizedContext */
private void forever(){

	for(; ;){
		Timer.wd();
		synchronized(this){
			if(con.getStatus() == TcpStreamConnection.CLOSED)
				state = GREET;
			switch(state){
				case ESTABLISHED:
					processCmd();
					break;
				case GREET:
					String welcome = "Welcome to JOP\r\n> ";
					if(con.freeToWrite() >= welcome.length()){
						con.write(welcome);
						con.flush();
						state = ESTABLISHED;
					}
					break;
				case PINGDONE:
					if(con.freeToWrite() >= tmp.length()){
						con.write(tmp);
						con.flush();
						tmp.setLength(0);
						state = ESTABLISHED;
					}
					break;

				default:
					break;
			}
		}
	}
}

private void processCmd(){
	int readCnt = con.freeToRead();
	if(readCnt > 0){
		for(int i = 0; i < readCnt; i++){
			rcv.append((char)con.read());
		}
		Dbg.wr("cli=");
		Dbg.wr(rcv);
		Dbg.lf();
	}

	int cmdlen = rcv.length();
	int tmpState = ESTABLISHED;
	if(cmdlen > 0){
		if(Util.CharSequenceStartsWith(rcv, "hello")){
			rcv.setLength(0);
			con.write("Hello from JOP\r\n> ");
			con.flush();
		} else if(Util.CharSequenceStartsWith(rcv, "ping")){
			int ip = Ip.parseIp(rcv, 5);
			rcv.setLength(0);
			if(ip != 0){
				con.write("PING ");
				tmp.setLength(0);
				Util.appendIp(tmp, ip);
				con.write(tmp);
				con.write("\r\n");
				con.flush();
				tmpState = PING;
				Icmp.ping(ip, this);
			} else{
				con.write("parsing cmd failed\r\n> ");
				con.flush();
			}
		} else{
			rcv.setLength(0);
			con.write("unknown command\r\n> ");
			con.flush();
		}
	}
	state = tmpState;
}

public void pingReply(int ms){
	synchronized(this){
		tmp.setLength(0);
		tmp.append("got reply in ");
		tmp.append(ms);
		tmp.append("ms\r\n> ");

		state = PINGDONE;
	}
}

public void pingTimeout(){
	synchronized(this){

		tmp.setLength(0);
		tmp.append("timed out\r\n> ");

		state = PINGDONE;
	}
}

}