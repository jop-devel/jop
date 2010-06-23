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

package jembench.udpip;

/**
*	Start device driver threads and poll for packets.
*/

public class UdpipNet {

	public static int[] eth;				// own ethernet address
	public static int ip;					// own ip address

/**
*	The one and only reference to this object.
*/
	private static UdpipNet single;

/**
*	private because it's a singleton Thread.
*/
	private UdpipNet() {
	}

/**
*	Allocate buffer and create thread.
*/
	public static UdpipNet init() {

		if (single != null) return single;			// allready called init()

		eth = new int[6];
		eth[0] = 0x00;
		eth[1] = 0xe0;
		eth[2] = 0x98;
		eth[3] = 0x33;
		eth[4] = 0xb0;
		eth[5] = 0xf7;		// this is eth card for chello
		eth[5] = 0xf8;
		ip = (192<<24) + (168<<16) + (0<<8) + 123;
		// ip = (192<<24) + (168<<16) + (0<<8) + 4;

		UdpipUdp.init();
		UdpipPacket.init();
		UdpipTcpIp.init();

		//
		//	start my own thread
		//
		single = new UdpipNet();
		
		return single;
	}


/**
*	Look for received packets and call TcpIp.
*	Mark them to be sent if returned with len!=0 from TcpIp layer.
*/
	public void loop() {

		UdpipPacket p;

		// is a received packet in the pool?
		p = UdpipPacket.getPacket(UdpipPacket.RCV, UdpipPacket.ALLOC);
		if (p!=null) {					// got one received Packet from pool
			UdpipTcpIp.receive(p);
		} else {
			UdpipUdp.loop();
		}
	}
}
