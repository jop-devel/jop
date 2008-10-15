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

package ejip123;

import ejip123.legacy.Html;
import ejip123.util.Dbg;
import joprt.RtThread;

/** The internet protocol (see RFC 791). */
public class Ip{
public final static int OFFSET = 5;
public final static int ETHER_PROT = 0x0800;
private final static int ttl = (64<<24);
private static int ip_id = 0x12340000;

//TODO? provide a default mtu for upper layers
//is Router.getIf(dstIp).getMtu() enough?

private Ip(){
}

/**
 Creates a thread for receiving packets, sending fragments and other periodic work.

 @param prio Priority
 @param us   Period in microseconds */
public static void init(int prio, int us){
	new RtThread(prio, us){
		public void run(){
			for(; ;){
				waitForNextPeriod();
				loop();
			}
		}
	};
}

private static void loop(){
	// is a received packet in the pool?
	Packet p = PacketPool.getReceivedPacket();
	if(p != null){
		receive(p);
	}
	int cur = (int)System.currentTimeMillis();
	Icmp.loop(cur);
	Reassembler.loop(cur);
	Tcp.loop(cur);
	Fragmenter.loop();
}

private static void receive(Packet p){
	int[] buf = p.buf;
	int i = buf[0];

	if((i&0xF0000000) != 0x40000000){ // !ipv4?
		p.free();
		return;
	}

	int dstIp = buf[4];
	LinkLayer linkLayer = p.linkLayer();
	// we drop all packets that are not addressed to us, aren't a broadcast and aren't from ourselves (loopback).
	if(((linkLayer != null) || (dstIp&0xFF000000) != 0x7F000000) && linkLayer.getIp() != dstIp && (!linkLayer.isLocalBroadcast(
			dstIp))){
		p.free();
		return;
	}

	if(chkSum(buf, 0, 20) != 0){
		p.free();
		return;
	}

	int len = i&0xffff;
	if(len < p.len()) // if ip.len < linklayer.len truncate it, maybe upper layer can use it anyway.
		p.setLen(len);

	int off = (i&0x0F000000)>>>24;

	// fragmentation reassembly
	if((p = Reassembler.process(p, off)) == null)
		return;

	int prot = (buf[2]>>>16)&0xff; // protocol
	switch(prot){
		case Icmp.PROTOCOL:
//			p.print(0);
			Icmp.receive(p, off);
			break;
		case Tcp.PROTOCOL:
			if((buf[5]&0xffff) == 80){
				// still do our simple HTML server
				Html.doTCP(p);
				send(p, dstIp, buf[3], prot);
			} else{
				// simulate receive packet loss
//				if((System.currentTimeMillis()&0x1000) != 0){
//					Dbg.wr("packet lost\n");
//					p.free();
//				}else
				Tcp.process(p, off);
			}
			break;
		case Udp.PROTOCOL:
			Udp.process(p, off);
			break;
		default:
			Icmp.sendProtocolUnreachable(buf, off);
			p.free();
			break;
	}
}

/**
 Sends an IP packet. The IP of the sending interface will be used as source IP.

 @param p     The packet to be sent.
 @param dstIp Destination IP.
 @param prot  Protocol (e.g. udp, tcp, icmp).
 @return true if a packet was sent to a link layer */
static boolean send(Packet p, int dstIp, int prot){
	return send(p, 0, dstIp, prot);
}

/**
 Sends an IP packet. If the source IP is set to 0, the IP of the sending interface will be used.

 @param p     The packet to be sent.
 @param srcIp Source IP. If 0, the IP of the used interface will be used.
 @param dstIp Destination IP.
 @param prot  Protocol (e.g. udp, tcp, icmp).
 @return true if a packet was sent to a link layer. */
static boolean send(Packet p, int srcIp, int dstIp, int prot){

	int[] buf = p.buf;

	int len = p.len();
	if(len == 0){
		p.free(); // mark packet free
		return false;
	}
	buf[0] = 0x45000000 + len; // ip length (header without options)
	buf[1] = getId(); // identification, no fragmentation (yet)
	buf[2] = ttl + (prot<<16); // ttl, protocol, clear checksum
	buf[4] = dstIp;
	// test for loopback destination
	if((dstIp&0xFF000000) == 0x7F000000){
		// check and set source ip
		if((srcIp&0xFF000000) != 0x7F000000){
			if(srcIp == 0)
				buf[3] = 0x7F000001;
			else{
				p.free();
				return false;
			}
		} else
			buf[3] = srcIp;
		buf[2] |= chkSum(buf, 0, 20);
		p.setLinkLayer(null);
		p.setStatus(Packet.RCV); // sending == setting as received for loopback connections
		return true;
	}

	LinkLayer ll = p.linkLayer();
	if(ll == null){
		ll = Router.getIf(dstIp);
		if(ll == null){
			p.free();
			return false;
		} else
			p.setLinkLayer(ll);
	}
	// check if source ip is ok rfc1122 3.2.1.3
	if(srcIp == 0)
		srcIp = ll.getIp(); // TODO if the interface is down, this could be 0.
	else{
		// not allowed as source ip: limited bc, local bc, loopback addresses
		if(srcIp == 0xFFFFFFFF || ((srcIp&0xFF000000) == 0x7F000000) || ll.isLocalBroadcast(srcIp)){
			p.free();
			return false;
		}
	}

	buf[3] = srcIp;
	p.setProt(ETHER_PROT);
	if(len > ll.getMtu()){
		if(p.isConPrep()){
			p.free();
			return false;
		}
		Fragmenter.frag(p);
	} else{
		buf[2] |= chkSum(buf, 0, 20);
		p.setRdy();
	}
	return true;
}


/**
 calcs ip check sum. assumes (32 bit) word boundaries. rest of buffer needs to be 0-padded.

 @param buf buffer to be checked
 @param off offset in buffer (in words)
 @param cnt length in bytes
 @return checksum */
public static int chkSum(int[] buf, int off, int cnt){ // TODO make package visible when Html is ported
	if(off < 0 || cnt < 0)
		return 0;

	cnt = (cnt + 3)>>2; // word count
	int sum = 0;
	while(cnt != 0){
		int i = buf[off];
		sum += i&0xffff;
		sum += i>>>16;
		++off;
		--cnt;
	}

	while((sum>>16) != 0)
		sum = (sum&0xffff) + (sum>>16);

	sum = (~sum)&0xffff;

	return sum;
}

/**
 Returns a number (in the upper 16 bit) to be used as IP id.

 @return An integer different than the calls before (wraps after 2^16 calls). */
private static int getId(){
	ip_id += 0x10000;
	return ip_id;
}

/**
 Converts 4 unsigned bytes (like in dot format) to an IP address integer.

 @param msb  most significant byte.
 @param smsb second most significant byte.
 @param tmsb third most significant byte.
 @param lsb  least significant byte.
 @return An integer to be used as IP address in related classes. */
public static int Ip(int msb, int smsb, int tmsb, int lsb){
	return (msb<<24) + (smsb<<16) + (tmsb<<8) + lsb;
}

static int getMaxPayload(int dstIp){
	return Router.getIf(dstIp).getMtu() - (OFFSET<<2);
}

static int getSrcIp(int dstIp){
	return Router.getIf(dstIp).getIp();
}

public static int parseIp(CharSequence cmd, int off){
	int len = cmd.length();
	while(off < len && cmd.charAt(off) == ' ')
		off++;

	if(off >= len || !Character.isDigit(cmd.charAt(off)))
		return 0;
	int start = off;
	off++;
	int curOctet = 3;
	int ip = 0;
	int digitsDone = 1;
	// ping 192.168.0.
	// 01234567890123456
	while(off < len){
		char c = cmd.charAt(off);
//		Dbg.wr(c);
		boolean last = off == (len - 1);
		int end;
		if(c == '.' || (curOctet == 0 && (digitsDone > 0 && !Character.isDigit(c)) || digitsDone == 3)){
//			Dbg.wr("digits done=");
//			Dbg.intVal(digitsDone);
			if(digitsDone == 0)
				return 0;

			end = off;
		} else if(last){
//			Dbg.wr("last ");
			if(!Character.isDigit(c) || curOctet != 0)
				return 0;
			end = off + 1;
		} else if(Character.isDigit(c)){
			digitsDone++;
			off++;
			continue;
		} else
			return 0;

//		Dbg.lf();
//		Dbg.intVal(off);
//		Dbg.intVal(start);
//		Dbg.intVal(end);
		int octet = 0;
		int j = start;
		while(j < end){
			int digit = Character.digit(cmd.charAt(j), 10);
			if(digit < 0 || digit > 9)
				return 0;

			octet *= 10;
			octet += digit;
			j++;
		}
//		Dbg.intVal(octet);
//		Dbg.lf();
		if(octet > 255)
			return 0;
		ip += octet<<(curOctet<<3);
		curOctet--;
		if(curOctet < 0)
			break;
		digitsDone = 0;
		off++;
		start = off;
	}
	if(curOctet >= 0)
		return 0;
//	Dbg.lf();
	return ip;
}

/**
 Fragments packets into smaller junks. If the underlying linklayer can't send the given packet as a whole, this class
 splits it up.
 */
private static class Fragmenter{
	private static Packet bigP = null;
	private static int todo = 0;
	private static int done = 0;
	private static final int NetOffB = OFFSET<<2;
	private static int maxPayload = 0;

	private Fragmenter(){
	}

	static void loop(){
		if(bigP == null)
			return;

		Packet p;
		if((p = PacketPool.getFreshPacket()) == null)
			return;

		int[] buf = p.buf;
		int[] Bbuf = bigP.buf;
		buf[1] = Bbuf[1]|(done>>3); // set offset

		int byteCnt; // how much can we transfer with this segment
		if(todo <= maxPayload){ // last fragment?
			byteCnt = todo;
			todo = 0;
		} else{
			byteCnt = maxPayload&0xFFFFFFF8; // round down to next 8B boundary
			todo -= byteCnt;
			buf[1] |= 0x2000; // set more fragments flag
		}
		int lenIP = byteCnt + NetOffB;
		p.setLen(lenIP);
		buf[0] = 0x45000000 + lenIP; // ip length (header without options)

		lenIP = lenIP>>2;
		int i;
		for(i = 2; i <= 4; i++){ // copy IPs; ttl, prot, clear checksum
			buf[i] = Bbuf[i];
		}
		int j;
		for(i = OFFSET, j = i + (done>>2); i <= lenIP; i++, j++){ // copy payload
			buf[i] = Bbuf[j];
		}

		buf[2] |= chkSum(buf, 0, 20);
		p.setLinkLayer(bigP.linkLayer());
		p.setProt(ETHER_PROT);
		p.setStatus(Packet.DGRAM_RDY); // mark packet ready to send
		if(todo <= 0){
			// TODO what about CON packets?
			Bbuf[1] |= 0x2000;
			Bbuf[2] |= chkSum(Bbuf, 0, 20);
			bigP.setStatus(Packet.DGRAM_RDY);
			bigP = null;
		} else
			done += byteCnt;
	}

	public static void frag(Packet p){
		if(bigP != null){// we support one concurrent big packet only atm
			p.free();
		} else{
			bigP = p;
			maxPayload = p.linkLayer().getMtu() - NetOffB;

			done = maxPayload&0xFFFFFFF8;
			todo = p.len() - NetOffB - done;
			int len = done + NetOffB;
			p.buf[0] = 0x45000000 + len;
			p.setLen(len);
		}
	}
}

/** packet reassembly as suggested by http://tools.ietf.org/html/rfc815 . */
private static class Reassembler{
	/* layout of a hole descriptor:
	  0                   1                   2                   3
	  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
	 +-hole--+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	 |                               | next pointer                  |
	 | hole.first                    | hole.last                     |
	 +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 */
	private static Packet asm = null;
	private static int[] asmbuf;
	private static int off;
	private static int timestamp = 0;
	private static int asmTimeout = 3000;
	private static final Object mutex = new Object();

	private Reassembler(){
	}

	static void loop(int cur){
		if(asm != null && timestamp + asmTimeout - cur < 0){
			if(getHead() > off + 2){
				asmbuf[1] = (asmbuf[1]&(0xffff0000))|0x2000; // restore original flags and offset
				Icmp.sendTimeExceeded(asmbuf, off);
			}
			asm.free();
			asm = null;
			timestamp = 0;
		}
	}

	private static boolean reassemble(int[] buf){
		boolean mf = (buf[1]&0x2000) == 0x2000;
		int bytesFilled = 0;
		int cur = getHead();
		int ffirst = (buf[1]&0x1fff)<<3; // offset in bytes relative to the beginning of the ip payload
		int b0 = buf[0];
		// the last byte of this fragment is ffirst + total length - hdr length - 1
		int flast = ffirst + ((b0&0xffff) - (((b0>>>24)&0xf)<<2) - 1);
//	Dbg.wr("ffirst=" + ffirst + " flast=" + flast);

		int prev = 0;
		do{
//		Dbg.wr("cur=" + cur);
			int hlast = asmbuf[cur + off + 1]&0xffff;
			if(ffirst > hlast){ // begin of the fragment is after the end of the hole
//			Dbg.wr("ffirst(" + ffirst + ") > (" + hlast + ") hlast");
				continue;
			}

			int hfirst = asmbuf[cur + off + 1]>>>16;
//		Dbg.wr("hfirst=" + hfirst + " hlast=" + hlast);
			if(flast < hfirst){ // end of the fragment is before the begin of the hole
//			Dbg.wr("flast (" + flast + ") < (" + hfirst + ") hfirst");
				continue;
			}

			if(ffirst > hfirst){ // we fill the rear part of the hole
//			Dbg.wr("ffirst > hfirst");
				// if head points at this hole, it will so after setting the new borders too
				setHole(hfirst, ffirst - 1, nextHole(cur));
				bytesFilled += hlast - ffirst;
			}

			if(flast < hlast){ // we fill the front of the hole
//			Dbg.wr("flast < hlast");
				if(mf){
					// if this is the first hole in the list we need to correct the head pointer
					if(cur == getHead()){
//					Dbg.wr("chg head\n");
						setHead(flast + 1);
					} else{ // we need to correct the link pointing here
//					Dbg.wr("chg prev\n");
						asmbuf[prev + off] = (flast + 1)>>2;
					}
					setHole(flast + 1, hlast, nextHole(cur));
				}
				bytesFilled += flast - hfirst + 1;
			}
//		Dbg.wr("end of loop");
			prev = cur;
		} while((cur = nextHole(cur)) != 0);
//	Dbg.wr(Integer.toHexString(getHead()) + " " + Integer.toHexString(prev) + " " + Integer.toHexString(cur));

		int j = ((ffirst + 3)>>2) + off;
		int max = ((flast + 3)>>2) + off;
//	Dbg.wr("von " + j + " bis " + max + " = " + (max - j));

		if(max >= asmbuf.length){
			Dbg.wr("can not reassemble the whole packet. its too long!\n");
			asm.free();
			asm = null;
			return false;
		}

		for(int i = off; j <= max; j++, i++){
			asmbuf[j] = buf[i];
		}
//  DONE what happens if we fill (a part of) a hole multiple times?
//	asm.setLen(asm.len() + flast - ffirst + 1);
		asm.addToLen(bytesFilled);
//	Dbg.wr("that packet filled ");
//	Dbg.intVal(bytesFilled);
//	Dbg.wr("bytes\n");
		return getHead() == prev;
	}

	/**
	 next list item.

	 @param cur index of the current hole in buf
	 @return index of the next hole descriptor or 0, if cur is the last
	 */
	public static int nextHole(int cur){
		return asmbuf[off + cur]&0xffff;
	}

	private static int getHead(){
		return asmbuf[1]&0xffff;
	}

	private static void setHole(int first, int last, int next){
//	Dbg.wr("first=" + Integer.toHexString(first) + "=" + first);
		first >>= 2;
//	Dbg.wr("wrting to " + first);
//	Dbg.wr("hfirst=" + Integer.toHexString((first<<18)&0xffff0000) + " hlast=" + last + " next=" + (next>>2));

		asmbuf[first + off] = next>>2;
		asmbuf[first + off + 1] = (first<<18)|last;
//	Dbg.wr("firstlast=" + Integer.toHexString(asmbuf[first + off + 1]));
	}

	private static void setHead(int newHead){
//	Dbg.wr("changing head pointer to " + (newHead>>2));
		asmbuf[1] = (asmbuf[1]&(0xffff0000))|(newHead>>2);
//	Dbg.wr("new head=" + getHead());
	}

	private static boolean relatedPacket(int[] rcvbuf){
		return rcvbuf[3] == asmbuf[3] // src ip
		       && (rcvbuf[1]&0xffff0000) == (asmbuf[1]&0xffff0000) // id
		       && (rcvbuf[2]&0x00ff0000) == (asmbuf[2]&0x00ff0000) // transport prot
		       && (rcvbuf[4] == asmbuf[4]); // dst ip
	}

	/**
	 Reassembles a packet.

	 @param p      a received, potentially fragmented packet.
	 @param offset Offset where IP payload begins.
	 @return a complete packet (either if called with the last missing fragment or an unfragmented Packet) or null, if the
	 packet could not be reassembled (yet).
	 */
	public static Packet process(Packet p, int offset){
		int[] buf = p.buf;
		if((buf[1]&0x3FFF) == 0) // packet is not a fragment
			return p;

		Packet ret = null;
		synchronized(mutex){
			if(asm == null){ // first fragment and no other packet reassembly ongoing
//		Dbg.wr("first fragment... ");
				asm = PacketPool.getFreshPacket();
				if(asm != null){
					off = offset;
					asmbuf = asm.buf;
					asmbuf[0] = buf[0]; // ipv4, hdrlen, dsf, (wrong) total len
					asmbuf[1] = buf[1]; // id, flags
					asmbuf[2] = buf[2]; // ttl, prot, hdr chksum
					setHead(0);
					asmbuf[3] = buf[3]; // src ip
					asmbuf[4] = buf[4]; // dst ip
					asm.setLen(20); // reassembled packet wont have options in it
					setHole(0, 0xffff, 0);
					reassemble(buf);
					timestamp = (int)System.currentTimeMillis();
				}
			} else{
				if(relatedPacket(buf)){
//			Dbg.wr("fragment related to the currently processed datagram... ");
					if(reassemble(buf)){
						asmbuf[0] = 0x45000000|asm.len();
						asm.setLinkLayer(p.linkLayer());
						ret = asm;
						asm = null;
//				Dbg.wr("packet complete!\n");
//				ret.print(0);
					}

				}
				// atm we support one concurrent reassembly only,
				// so we need to drop unrelated fragments.
			}
		}
		p.free();
		return ret;
	}

	public static void setAsmTimeout(int asmTimeout){
		Reassembler.asmTimeout = asmTimeout;
	}
}

}
