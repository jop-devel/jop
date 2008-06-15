package oebb;

import com.jopdesign.sys.Native;

import ejip.Ip;
import ejip.Packet;
import ejip.Udp;

public class Logging implements Runnable {
	
	final static int SND_PORT = 2007;
	final static int ACK_PORT = 2008;
	
	StringBuffer sb = new StringBuffer();
	boolean nandSent = false;

	public Logging() {
		System.out.println("Logging - check NAND");
		testNAND();
	}
	
	public void run() {
		if (Status.connOk && !nandSent) {
			sendMsg();
		}
	}

	private void sendMsg() {
		
		State state = Main.state;
		Packet p = Packet.getPacket(Packet.FREE, Packet.ALLOC, Main.ipLink);
		if (p==null) {
			return;
		}
		
		p.buf[Udp.DATA+0] = state.bgid;
		p.buf[Udp.DATA+1] = state.date;
		p.buf[Udp.DATA+2] = state.time;
		p.buf[Udp.DATA+3] = Main.ipLink.getIpAddress();
		Ip.setData(p, Udp.DATA+4, sb);
		
		// and send it
		Udp.build(p, state.destIp, SND_PORT);
		nandSent = true;
	}
	
	private void testNAND() {
		
		int i, j;
		sb.setLength(0);
		
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
		sb.append("NAND ");
		if (i==0x198) {
			sb.append("Toshiba ");
		} else if (i==0x120) {
			sb.append("ST ");
		} else {
			sb.append("Unknown manufacturer ");
		}
			
		if (j==0x173) {
			sb.append("16 MB");
		} else if (j==0x175) {
			sb.append("32 MB");
		} else if (j==0x176) {
			sb.append("64 MB");
		} else if (j==0x179) {
			sb.append("128 MB");
		} else {
			sb.append("error reading NAND");
		}

//
//			read status, should be 0xc0
//
		Native.wrMem(0x70, 0x100001);
		i = Native.rdMem(0x100000)&0x1c1;
		j = Native.rdMem(0x100000)&0x1c1;
		if (i==0x1c0 && j==0x1c0) {
			sb.append(" status OK");
		} else {
			sb.append(" error reading NAND status");
		}
		System.out.println(sb);

	}
}
