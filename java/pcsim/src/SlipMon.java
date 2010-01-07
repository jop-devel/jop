/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2001-2008, Martin Schoeberl (martin@jopdesign.com)

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/


/**
*	SlipMon.java: 
*
*	Author: Martin Schoeberl (martin.schoeberl@chello.at)
*
*/

import com.jopdesign.sys.Const;

import util.*;
import ejip.*;
import joprt.*;

// TODO; needs to be updated for changed ejip API.

public class SlipMon {

//	static Serial ser;
//	static LinkLayer ipLink;
//
//	static boolean reset;
//
//	public static void main(String[] args) {
//
//		if (args!=null) {
//			ser = new Serial(Const.IO_UART_BG_MODEM_BASE);
//		} else {
//			ser = new Serial(Const.IO_UART1_BASE);
//		}
//
//		Dbg.initSer();
//		//
//		//	start TCP/IP without the Net thread
//		//	we want to get all packets
//		//
//		Udp.init();
//		Packet.init();
//		TcpIp.init();
//		//
//		//	start device driver threads
//		//
//		ipLink = Slip.init(ser, (192<<24) + (168<<16) + (1<<8) + 2); 
//
//
//		RtThread.startMission();
//
//		for (;;) {
//
//			// is a received packet in the pool?
//			Packet p = Packet.getPacket(Packet.RCV, Packet.ALLOC);
//			if (p!=null) {					// got one received Packet from pool
//				printPacket(p);
//				p.setStatus(Packet.FREE);	// mark packet free
//			}
//			RtThread.sleepMs(20);
//		}
//	}
//
//	static void printPacket(Packet p) {
//
//		Dbg.wr("Packet! ");
//		int cmd = p.buf[Udp.DATA];
//		if (cmd==12) Dbg.wr("DL_RPL ");
//		if (cmd==1) Dbg.wr("Ping ");
//		if (cmd==5) Dbg.wr("Connect ");
///*
//		for (int i=0; i<p.len/4; ++i) {		// p.len is in bytes
//			Dbg.intVal(p.buf[i]);
//		}
//*/
//		Dbg.lf();
//	}
}
