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
 A small telnet server. This example shows how a TCP server can be implemented with the packet orientated TcpHandler
 interface.
 */
public class Telnetd implements TcpHandler, PingReplyHandler{

private StringBuffer sb = new StringBuffer(32);
private TcpConnection tc;
private final int IDLE = -1;
private final int GREET = 0;
private final int ESTABLISHED = 1;
private final int PING = 2;
private final int PINGDONE = 3;
private static final int PROCESSING = 4;
private int state = IDLE;
private Packet curPacket;
private int curOff;
//private final Object this = new Object();

public static void main(String[] args){

	PacketPool.init(10, 1500);

//	Dbg.initSerWait(); // serial debug output
	DbgUdp.init(); // sends debug output over the network to 192.168.2.1:10000 (see init method)

	Router.init(3); // initializes a routing table with 3 routes
//	Serial ser = new Serial(10, 1000, Const.IO_UART_BG_MODEM_BASE); // simulator
	Serial ser = new Serial(10, 1000, Const.IO_UART1_BASE); // one byte every ~400us at 19200 baud
	LinkLayer slip = Slip.init(9, 10000, ser, Ip.Ip(192, 168, 2, 2), 1500);
	Ip.init(6, 50000); // ip (and therefore icmp and tcp) loop thread: period 50ms
	Router.addRoute(new Route(Ip.Ip(192, 168, 2, 0), Ip.Ip(255, 255, 255, 0), slip));
	Router.setDefaultInterface(slip); // where should packets go which are not matched by a route?
	Tcp.init(3, 4); // inits tcp with slots for 3 handlers and 4 connections

	Telnetd th = new Telnetd();
	Tcp.addHandler(23, th);

	RtThread.startMission();
	Router.print(); // like route print on windows or ip addr on linux
	th.forever();
}

/**
 @noinspection NonPrivateFieldAccessedInSynchronizedContext */
private void forever(){
	for(; ;){
		Timer.wd();
		synchronized(this){
			Packet p;
			switch(state){
				case PROCESSING:
					Dbg.wr("processing...\n");
					processCmd();
					break;
				case GREET:
					if(tc != null && (p = PacketPool.getFreshPacket()) != null){
						p.setData(Tcp.OFFSET<<2, "Welcome to JOP\r\n> ");
						if(tc.send(p, true))
							state = ESTABLISHED;
					}
					break;
				case PINGDONE:
					if(tc != null && (p = PacketPool.getFreshPacket()) != null){
						p.setData(Tcp.OFFSET<<2, sb);
						if(tc.send(p, true)){
							state = ESTABLISHED;
						}
					}
					break;
				default:
					break;
			}
		}
	}
}

private void processCmd(){
	int cmdlen = curPacket.getData(curOff, sb);
	int tmpState = ESTABLISHED;
	if(cmdlen > 0){
		Dbg.wr("cli: ");
		Dbg.wr(sb);
		if(Util.CharSequenceStartsWith(sb, "hello")){
			sb.setLength(0);
			sb.append("Hello from JOP\r\n> ");
		} else if(Util.CharSequenceStartsWith(sb, "ping")){
			int ip = Ip.parseIp(sb, 5);
			if(ip != 0){
				sb.setLength(0);
				sb.append("PING ");
				Util.appendIp(sb, ip);
				sb.append("\r\n");
				tmpState = PING;
				Icmp.ping(ip, this);
			} else{
				sb.setLength(0);
				sb.append("parsing cmd failed\r\n> ");
			}
		} else{
			sb.setLength(0);
			sb.append("unknown command\r\n> ");
		}
	}
	if(sb.length() > 0){
		curPacket.setLen(0);
		curPacket.setData(Tcp.OFFSET<<2, sb);
		tc.send(curPacket, true);
	}
	state = tmpState;
}

public boolean isBusy(TcpConnection newCon){
	synchronized(this){
		return state != IDLE;
	}
}

public boolean request(TcpConnection con, Packet p, int off){
	synchronized(this){
		if(state != ESTABLISHED)
			return false;

		curOff = off;
		curPacket = p;
		curPacket.setStatus(Packet.ALLOC);
		state = PROCESSING;
		return true;
	}
}

public void pingReply(int ms){
	synchronized(this){
		sb.setLength(0);
		sb.append("got reply in ");
		sb.append(ms);
		sb.append("ms\r\n> ");
		state = PINGDONE;
	}
}

public void pingTimeout(){
	synchronized(this){
		sb.setLength(0);
		sb.append("timed out\r\n> ");
		state = PINGDONE;
	}
}

public void established(TcpConnection newCon){
	synchronized(this){
		if(state == IDLE){
			tc = newCon;
			state = GREET;
		} else{
			newCon.close();
		}
	}
}

public void closed(TcpConnection closedCon){
	synchronized(this){
		if(tc == closedCon){
			tc = null;
			state = IDLE;
			Dbg.wr("connection closed, ready for more!\n");
		} else{
			Dbg.wr("Con ");
			Dbg.wr(closedCon.toString());
			Dbg.wr(" we support only one concurrent connection. this one was not the first and was therefore closed\n");
		}
	}
}

public void reset(TcpConnection closedCon){
	synchronized(this){
		if(tc == closedCon){
			tc = null;
			state = IDLE;
		}
	}
}
}
