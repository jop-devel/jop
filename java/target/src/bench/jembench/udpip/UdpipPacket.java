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

/*
*   Changelog:
*		2002-11-11	use LinkLayer info to mix Slip and Ethernet packets
*/

package jembench.udpip;


/**
*	Packet buffer handling for a minimalistic TCP/IP stack.
*
*/
public class UdpipPacket {

	// smaller packet sizes for the benchmark
	public final static int MAX = 40;		// maximum Packet length in bytes
	public final static int MAXW = 40/4;	// maximum Packet length in word
	public final static int MAXLLH = 7;		// 7 16 bit words for ethernet
	private final static int CNT = 2;		// size of packet pool

	/** interface source/destination */
	public UdpipLinkLayer interf;
	/** place for link layer data */
	public int[] llh;
	/** ip datagram */
	public int[] buf;
	/** length in bytes */
	public int len;
	/** usage of packet */
	private int status;
	public final static int FREE = 0;
	public final static int ALLOC = 1;
	public final static int SND = 2;
	public final static int RCV = 3;

	private static Object monitor;

	//	no direct construction
	private UdpipPacket() {
		llh = new int[MAXLLH];
		buf = new int[MAXW];
		len = 0;
		status = FREE;
		interf = null;
	}

	private static boolean initOk;
	private static UdpipPacket[] packets;

	// a small race condition on init
	public static void init() {

		if (initOk) return;
		monitor = new Object();

		synchronized (monitor) {
			initOk = true;
			packets = new UdpipPacket[CNT];
			for (int i=0; i<CNT; ++i) { // @WCA loop=2
				packets[i] = new UdpipPacket();
			}
		}
	}

private static void dbg() {

	synchronized (monitor) {
		UdpipDbg.wr('|');
		for (int i=0; i<CNT; ++i) { // @WCA loop=2
			UdpipDbg.wr('0'+packets[i].status);
		}
		UdpipDbg.wr('|');
	}
}


/**
*	Request a packet of a given type from the pool and set new type.
*/
	public static UdpipPacket getPacket(int type, int newType) {

		int i;
		UdpipPacket p;

		if (!initOk) {
			init();
		}
		synchronized (monitor) {
			for (i=0; i<CNT; ++i) { // @WCA loop=2
				if (packets[i].status==type) {
					break;
				}
			}
			if (i==CNT) {
if (type==FREE) UdpipDbg.wr('!');
				return null;
			}
			packets[i].status = newType;
			p = packets[i];
		}
// dbg();
		return p;
	}

/**
*	Request a packet of a given type from the pool and set new type and source.
*/
	public static UdpipPacket getPacket(int type, int newType, UdpipLinkLayer s) {

		int i;
		UdpipPacket p;

		if (!initOk) {
			init();
		}
		synchronized (monitor) {
			for (i=0; i<CNT; ++i) { // @WCA loop=2
				if (packets[i].status==type) {
					break;
				}
			}
			if (i==CNT) {
if (type==FREE) UdpipDbg.wr('!');
				return null;
			}
			packets[i].status = newType;
			packets[i].interf = s;
			p = packets[i];
		}
// dbg();
		return p;
	}

/**
*	Request a packet of a given type and source from the pool and set new type.
*/
	public static UdpipPacket getPacket(UdpipLinkLayer s, int type, int newType) {

		int i;
		UdpipPacket p;

		if (!initOk) {
			init();
		}
		synchronized (monitor) {
			for (i=0; i<CNT; ++i) { // @WCA loop=2
				if (packets[i].status==type && packets[i].interf==s) {
					break;
				}
			}
			if (i==CNT) {
				return null;
			}
			packets[i].status = newType;
			p = packets[i];
		}
// dbg();
		return p;
	}

	public void setStatus(int v) {

		synchronized (monitor) {
			status = v;
		}
// dbg();
	}

	public int getStatus() {
		return status;
	}

	public UdpipLinkLayer getLinkLayer() {
		return interf;
	}
}
