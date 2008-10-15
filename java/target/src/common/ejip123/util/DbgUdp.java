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

package ejip123.util;

import ejip123.Ip;
import ejip123.Packet;
import ejip123.PacketPool;
import ejip123.Udp;
import joprt.RtThread;

public class DbgUdp extends Dbg{

private Packet p;
private int idx;
private int ip, port;

DbgUdp(){

	idx = Udp.OFFSET<<2;
	ip = Ip.Ip(192, 168, 2, 1);
	port = 10000;
	p = PacketPool.getFreshPacket();
	new RtThread(1, 100000){
		public void run(){
			loop();
			waitForNextPeriod();
		}
	};

}

private void loop(){
	synchronized(this){
		if(p == null){
			p = PacketPool.getFreshPacket();
		}
	}

}

void dbgWr(int c){
	synchronized(this){
		if(p == null && (p = PacketPool.getFreshPacket()) == null){
			return;
		}

		p.setByte(c, idx);
		idx++;
		if(c == '\n' || idx >= PacketPool.PACKET_SIZE()){
			p.setLen(idx);
			p.padWithZeros();
			Udp.send(p, ip, port);
			idx = Udp.OFFSET<<2;
			p = PacketPool.getFreshPacket();
		}
	}
}
}
