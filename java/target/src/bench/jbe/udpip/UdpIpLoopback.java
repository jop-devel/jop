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

package jbe.udpip;

/**
*	Loopback.java
*
*	Loopback link layer driver.
*/

/**
*	Loopback driver.
*/

public class UdpIpLoopback extends UdpIpLinkLayer {

	private static final int MAX_BUF = 1500;		// or should we use 1006

/**
*	ip address.
*/
	private static int ip;

/**
*	The one and only reference to this object.
*/
	private static UdpIpLoopback single;
	
/**
*	private constructor. The singleton object is created in init().
*/
	private UdpIpLoopback() {
	}

/**
*	allocate buffer, start serial buffer and slip Thread.
*/
	public static UdpIpLinkLayer init() {

		ip = (127<<24) + (0<<16) + (0<<8) + 1;

		if (single != null) return single;	// allready called init()

		single = new UdpIpLoopback();
		return single;
	}

	public int getIpAddress() {
		return ip;
	}

	/**
	*	Set connection strings and connect.
	*/
	public void startConnection(StringBuffer dialstr, StringBuffer connect, StringBuffer user, StringBuffer passwd) {
		// useless for loopback driver
	}
	/**
	*	Forces the connection to be new established.
	*	On Slip ignored.
	*/
	public void reconnect() {
	}
	static int timer;
	
/**
*	main loop.
*/
	public void loop() {

		UdpIpPacket p;

		//
		// get a ready to send packet with source from this driver.
		//
		p = UdpIpPacket.getPacket(single, UdpIpPacket.SND, UdpIpPacket.ALLOC);
		if (p!=null) {
			//
			// and simple mark it as received packet.
			//
			p.setStatus(UdpIpPacket.RCV);		// inform upper layer
		}
	}

/* (non-Javadoc)
 * @see ejip.LinkLayer#getConnCount()
 */
public int getConnCount() {
	return 0;
}


}
