
/**
*	SlipMon.java: 
*
*	Author: Martin Schoeberl (martin.schoeberl@chello.at)
*
*/

import util.*;
import ejip.*;
import joprt.*;

import com.jopdesign.sys.Native;

public class SlipMon {

	static LinkLayer ipLink;

	static boolean reset;

	public static void main(String[] args) {

		Dbg.initSer();
		//
		//	start TCP/IP without the Net thread
		//	we want to get all packets
		//
		Udp.init();
		Packet.init();
		TcpIp.init();
		//
		//	start device driver threads
		//
		ipLink = Slip.init((192<<24) + (168<<16) + (1<<8) + 2); 


		RtThread.startMission();

		for (;;) {

			// is a received packet in the pool?
			Packet p = Packet.getPacket(Packet.RCV, Packet.ALLOC);
			if (p!=null) {					// got one received Packet from pool
				printPacket(p);
				p.setStatus(Packet.FREE);	// mark packet free
			}
			RtThread.sleepMs(20);
		}
	}

	static void printPacket(Packet p) {

		Dbg.wr("Packet! ");
		int cmd = p.buf[Udp.DATA];
		if (cmd==12) Dbg.wr("DL_RPL ");
		if (cmd==1) Dbg.wr("Ping ");
		if (cmd==5) Dbg.wr("Connect ");
/*
		for (int i=0; i<p.len/4; ++i) {		// p.len is in bytes
			Dbg.intVal(p.buf[i]);
		}
*/
		Dbg.lf();
	}
}
