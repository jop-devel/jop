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

package ejip;

public class Ip {

	public final static int CHKSUM = 2;
	public final static int SOURCE = 3;
	public final static int DESTINATION = 4;
	
	// TODO: this should not be static and it should be a synch. access
	static int ip_id = 0x12340000;
	
	Ejip ejip;
	
	public Ip(Ejip ejipRef) {
		ejip = ejipRef;
	}

	/**
	 * Very simple generation of IP header. Just swap source and destination.
	 * Still used by ICMP and TCP.
	 */
	void doIp(Packet p, int prot) {
	
		int[] buf = p.buf;
		int len = p.len;
		int i;
	
		if (len == 0) {
			ejip.returnPacket(p); // mark packet free
		} else {
			buf[0] = 0x45000000 + len; // ip length (header without options)
			buf[1] = Ip.getId(); // identification, no fragmentation
			buf[2] = (0x20 << 24) + (prot << 16); // ttl, protocol, clear
													// checksum
			i = buf[3]; // swap ip addresses
			buf[3] = buf[4];
			buf[4] = i;
			buf[2] |= Ip.chkSum(buf, 0, 20);
	
			p.llh[6] = 0x0800;
	
			// send packet to the link layer for the packet
			p.interf.txQueue.enq(p);	
		}
	}

	/**
	 * return IP id in upper 16 bit.
	 */
	public static int getId() {
	
		Ip.ip_id += 0x10000;
		return Ip.ip_id;
	}

	/**
	 * calc IP check sum. assume (32 bit) word boundaries. rest of buffer is 0.
	 * off offset in buffer (in words) cnt length in bytes
	 */
	public static int chkSum(int[] buf, int off, int cnt) {
	
		int i;
		int sum = 0;
		int max = buf.length;
		cnt = (cnt + 3) >> 2; // word count
		// add max condition for DFA loop bound analysis
		for (int j=0; j<cnt && j<max; ++j) {
			i = buf[off];
			sum += i & 0xffff;
			sum += i >>> 16;
			++off;
		}
// that's the original ejip code
//		while (cnt != 0) {
//			i = buf[off];
//			sum += i & 0xffff;
//			sum += i >>> 16;
//			++off;
//			--cnt;
//		}
	
		while ((sum >> 16) != 0) // @WCA loop<=2
			sum = (sum & 0xffff) + (sum >> 16);
	
		sum = (~sum) & 0xffff;
	
		return sum;
	}

	/**
	 * Copy packet data into a StringBuffer
	 * @param p packet
	 * @param off offset in 32-bit words
	 * @param s StringBuffer destination
	 */
	public static void getData(Packet p, int off, StringBuffer s) {
		
		int[] buf = p.buf;
		int len = p.len;
		s.setLength(0);
		for (int i = off<<2; i < len; i++) {
			s.append((char) ((buf[i>>2]>>(24 - ((i&3)<<3))) & 0xff));
		}
	}

	/**
	 * Set data from StringBuffer into the packet
	 * @param p packet
	 * @param off offset in 32-bit words
	 * @param s StringBuffer source
	 */
	public static void setData(Packet p, int off, StringBuffer s) {
		
		int[] buf = p.buf;
		int cnt = s.length();
		// copy buffer
		int k = 0;
		for (int i=0; i<cnt; i+=4) {
			for (int j=0; j<4; ++j) {
				k <<= 8;
				if (i+j < cnt) k += s.charAt(i+j);
			}
			buf[off + (i>>>2)] = k;
		}
	
		p.len = (off<<2)+cnt;
	}

	/**
	 * Set data from StringBuffer into the packet
	 * @param p packet
	 * @param off offset in 32-bit words
	 * @param s StringBuffer source
	 * @param start offset into s
	 * @return offset up to which the data has been written
	 */
	public static int setData(Packet p, int off, StringBuffer s, int start) {
		
		int[] buf = p.buf;
		int cnt = s.length()-start;

		if (cnt > ((buf.length-off)<<2)) {
			cnt = (buf.length-off)<<2;
		}

		// copy buffer
		int k = 0;
		for (int i=0; i<cnt; i+=4) {
			for (int j=0; j<4; ++j) {
				k <<= 8;
				if (i+j < cnt) k += s.charAt(start+i+j);
			}
			buf[off + (i>>>2)] = k;
		}
	
		p.len = (off<<2)+cnt;

		return cnt+start;
	}

	/**
	 * Set data from String into the packet
	 * @param p packet
	 * @param off offset in 32-bit words
	 * @param s String source
	 */
	public static void setData(Packet p, int off, String s) {
		
		int[] buf = p.buf;
		int cnt = s.length();
		// copy buffer
		int k = 0;
		for (int i=0; i<cnt; i+=4) {
			for (int j=0; j<4; ++j) {
				k <<= 8;
				if (i+j < cnt) k += s.charAt(i+j);
			}
			buf[off + (i>>>2)] = k;
		}
	
		p.len = (off<<2)+cnt;
	}

	/**
	 * Set data from byte array into the packet
	 * @param p packet
	 * @param off offset in 32-bit words
	 * @param b byte array
	 */
	public static void setData(Packet p, int off, byte[] b) {
		setData(p, off, b, b.length);
	}

	/**
	 * Set data from byte array into the packet
	 * @param p packet
	 * @param off offset in 32-bit words
	 * @param b byte array
	 * @param cnt length of payload data
	 */
	public static void setData(Packet p, int off, byte[] b, int cnt) {

		int[] buf = p.buf;
		// copy buffer
		int k = 0;
		for (int i=0; i<cnt; i+=4) {
			for (int j=0; j<4; ++j) {
				k <<= 8;
				if (i+j < cnt) k += (int)b[i+j] & 0xff;
			}
			buf[off + (i>>>2)] = k;
		}

		p.len = (off<<2)+cnt;
	}

}
