package oebb;

import util.Timer;

import com.jopdesign.sys.Native;

import ejip.Ejip;
import ejip.Ip;
import ejip.Net;
import ejip.Packet;
import ejip.Udp;

public class Logging implements Runnable {
	
	final static int SND_PORT = 2007;
	final static int ACK_PORT = 2008;
	
	final static int MIN_TIMOUT = 4;
	
	boolean nandChecked = false;
	int timeOut;
	private Ejip ejip;
	private Net net;
	
	public Logging(Ejip ejipRef, Net netRef) {
		ejip = ejipRef;
		net = netRef;
		timeOut = Timer.getTimeoutSec(MIN_TIMOUT);
	}
	
	public void run() {
		if (Gps.ok() && !nandChecked) {
			testNAND();			
		}
		// Do a send check all MIN_TIMOUT seconds
		if (Timer.timeout(timeOut)) {
			if (Status.connOk) {
				sendMsg();
			}
			timeOut = Timer.getTimeoutSec(MIN_TIMOUT);			
		}
	}
	
	void print(String s) {
	
		LogMsg lm = LogMsg.getFreeMsg();
		if (lm==null) {
			return;
		}
		lm.msg.append(s);
		lm.addToSendList();
	}
	
	void printHex(String s, int val) {
		
		LogMsg lm = LogMsg.getFreeMsg();
		if (lm==null) {
			return;
		}
		lm.msg.append(s);
		lm.msg.append(" 0x");
		for (int i=0; i<8; ++i) {
			int j = (val>>(4*(7-i))) & 0x0f;
			if (j<10) {
				j += '0';
			} else {
				j += 'a'-10;
			}
			lm.msg.append((char) j);
		}

		lm.addToSendList();
	}
	
	void printSmall(String s, int val) {
		
		LogMsg lm = LogMsg.getFreeMsg();
		if (lm==null) {
			return;
		}
		lm.msg.append(s);
		lm.msg.append((char) ('0'+val/100000%10));
		lm.msg.append((char) ('0'+val/10000%10));
		lm.msg.append((char) ('0'+val/1000%10));
		lm.msg.append((char) ('0'+val/100%10));
		lm.msg.append((char) ('0'+val/10%10));
		lm.msg.append((char) ('0'+val%10));
		lm.addToSendList();
	}


	private void sendMsg() {
		
		State state = Main.state;
		LogMsg lm = LogMsg.getSendMsg();
		if (lm==null) {
			return;
		}
		Packet p = ejip.getFreePacket(Main.ipLink);

		if (p==null) {
			// requeue it again
			lm.addToSendList();
			return;
		}
		
		p.buf[Udp.DATA+0] = state.bgid;
		p.buf[Udp.DATA+1] = lm.date;
		p.buf[Udp.DATA+2] = lm.time;
		p.buf[Udp.DATA+3] = Main.ipLink.getIpAddress();
		Ip.setData(p, Udp.DATA+4, lm.msg);
		
		// and send it
		net.getUdp().build(p, state.destIp, SND_PORT);
		lm.addToFreeList();
	}
	
	private void testNAND() {
		
		int i, j;
		
		LogMsg lm = LogMsg.getFreeMsg();
		if (lm==null) {
			return;
		}
		if (Main.fs.isAvailable()) {
			lm.msg.append("NAND Flash available");
		} else {
			lm.msg.append("NAND Flash NOT available");			
		}
		System.out.println(lm.msg);
		lm.addToSendList();
		nandChecked = true;
	}
}
