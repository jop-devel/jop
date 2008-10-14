package oebb;

import util.Timer;

import com.jopdesign.sys.Native;

import ejip.Ip;
import ejip.Packet;
import ejip.Udp;

public class Logging implements Runnable {
	
	final static int SND_PORT = 2007;
	final static int ACK_PORT = 2008;
	
	final static int MIN_TIMOUT = 4;
	
	boolean nandChecked = false;
	int timeOut;
	
	public Logging() {
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
		Packet p = Packet.getPacket(Packet.FREE, Packet.ALLOC, Main.ipLink);
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
		Udp.build(p, state.destIp, SND_PORT);
		lm.addToFreeList();
	}
	
	private void testNAND() {
		
		int i, j;
		
		LogMsg lm = LogMsg.getFreeMsg();
		if (lm==null) {
			return;
		}
		
		Native.wrMem(0x90, 0x100001);
		Native.wrMem(0x00, 0x100002);
//
//			should read 0x98 and 0x73
//
		i = Native.rdMem(0x100000);	// Manufacturer
		j = Native.rdMem(0x100000);	// Size
		System.out.print("NAND ");
		System.out.print(i);
		System.out.print(" ");
		System.out.print(j);
		System.out.print(" ");
		lm.msg.append("NAND ");
		if (i==0x198) {
			lm.msg.append("Toshiba ");
		} else if (i==0x120) {
			lm.msg.append("ST ");
		} else {
			lm.msg.append("Unknown manufacturer ");
		}
			
		if (j==0x173) {
			lm.msg.append("16 MB");
		} else if (j==0x175) {
			lm.msg.append("32 MB");
		} else if (j==0x176) {
			lm.msg.append("64 MB");
		} else if (j==0x179) {
			lm.msg.append("128 MB");
		} else {
			lm.msg.append("error reading NAND");
		}

//
//			read status, should be 0xc0
//
		Native.wrMem(0x70, 0x100001);
		i = Native.rdMem(0x100000)&0x1c1;
		j = Native.rdMem(0x100000)&0x1c1;
		if (i==0x1c0 && j==0x1c0) {
			lm.msg.append(" status OK");
		} else {
			lm.msg.append(" error reading NAND status");
		}
		System.out.println(lm.msg);
		lm.addToSendList();
		nandChecked = true;
	}
}
