package ejip123;

/**
 Implementation of the important parts of RFC 792. Echo requests and replies and different error messages are supported.
 For the constants of this class see <a href=http://www.iana.org/assignments/icmp-parameters>iana.org</a>.
 */
public class Icmp{
public static final int PROTOCOL = 1;
private final static int T_ECHO_REP = 0;
//public final static int T_DEST_UNREACH = 3;
private final static int T_ECHO_REQ = 8;
//public final static int T_TIME_EXC = 11;
private static int pingTimeout = 1300;
private static PingReplyHandler pingHandler = null;
private static int timestamp = 0;

private Icmp(){
}

static void loop(int cur){
	if(timestamp + pingTimeout - cur < 0){
		if(pingHandler != null)
			pingHandler.pingTimeout();
		pingHandler = null;
	}
}

static void receive(Packet p, int off){
//	int chksum = p.buf[5]&0xffff;
	int type = p.buf[off]>>>24;
//	int code = (p.buf[5] >>> 16)&0xff;
	int src = p.buf[3];
	int dst = p.buf[4];
//	Dbg.wr('P');
//	Dbg.hexVal(type);

	if(Ip.chkSum(p.buf, off, p.len() - (off<<2)) != 0){
/*
		Dbg.wr("icmp checksum failed, dropping.");
//		p.print(96);
		Dbg.wr("should have been ");
		Dbg.hexVal(p.buf[off]&0xffff);
		Dbg.wr("was ");
		p.buf[off] &= 0xffff0000;
		Dbg.hexVal(Ip.chkSum(p.buf, off, p.len() - (off<<2)));
*/
		p.free();
	} else{
/*
														 |        | | | |S| |
														 |        | | | |H| |F
														 |        | | | |O|M|o
														 |        | |S| |U|U|o
														 |        | |H| |L|S|t
														 |        |M|O| |D|T|n
														 |        |U|U|M| | |o
														 |        |S|L|A|N|N|t
														 |        |T|D|Y|O|O|t
		FEATURE                                          |SECTION | | | |T|T|e
			  Included octets same as received           |3.2.2   |x| | | | |
TODO?	  Demux ICMP Error to transport protocol         |3.2.2   |x| | | | |
		  Send ICMP error message with TOS=0             |3.2.2   | |x| | | |
		  Send ICMP error message for:                   |        | | | | | |
		   - ICMP error msg                              |3.2.2   | | | | |x|
		   - IP b'cast or IP m'cast                      |3.2.2   | | | | |x|
		   - Link-layer b'cast                           |3.2.2   | | | | |x|
		   - Non-initial fragment                        |3.2.2   | | | | |x|
		   - Datagram with non-unique src address        |3.2.2   | | | | |x|
		  Return ICMP error msgs (when not prohibited)   |3.3.8   |x| | | | |
														 |        | | | | | |
		  Dest Unreachable:                              |        | | | | | |
			Generate Dest Unreachable (code 2/3)         |3.2.2.1 | |x| | | |
TODO?		Pass ICMP Dest Unreachable to higher layer   |3.2.2.1 |x| | | | |
		  Redirect:                                      |        | | | | | |
TODO?		Update route cache when recv Redirect        |3.2.2.2 |x| | | | |
TODO?	  Time Exceeded: pass to higher layer            |3.2.2.4 |x| | | | | (when frag reassembly timeout exceeds e.g. frags missing)
TODO?	  Parameter Problem:                             |        | | | | | | (problems with header parameters preventing processing)
			Send Parameter Problem messages              |3.2.2.5 | |x| | | |
			Pass Parameter Problem to higher layer       |3.2.2.5 |x| | | | |
			Report Parameter Problem to user             |3.2.2.5 | | |x| | |
 */
		switch(type){
			case T_ECHO_REQ:
				// we need to mirror the data back anyway, so we reuse this packet
				// just recalculate the checksum and set ICMP type to reply (0)
				p.buf[off] = T_ECHO_REP;
				p.buf[off] |= Ip.chkSum(p.buf, off, p.len() - (off<<2));
				Ip.send(p, dst, src, PROTOCOL);
				break;
/*
			case T_DEST_UNREACH:
				if(code==2 || code==3){
					//inform upper, sending layer
				}
				break;
*/
			case T_ECHO_REP:
				p.free();
				if(pingHandler != null && p.buf[off + 1] == timestamp){
					pingHandler.pingReply(((int)System.currentTimeMillis()) - timestamp);
					pingHandler = null;
				}
				break;
			default:
				p.free();
				break;
		}
	}
}

/**
 Sends an ICMP ping to the specified host. On reception of a reply the given handler will be called.

 @param dstIp Destination IP address.
 @param ph    The handler whose {@link PingReplyHandler#pingReply(int)} method should be called.
 @return true if a packet was sent to a link layer */
public static boolean ping(int dstIp, PingReplyHandler ph){
	Packet p;
	if(pingHandler == null && (p = PacketPool.getFreshPacket()) != null){
		pingHandler = ph;
		int off = Ip.OFFSET;
		p.buf[off] = T_ECHO_REQ<<24;
		p.buf[off + 2] = 0xDEADBABE;
		p.buf[off + 3] = 0x12345678;
		timestamp = (int)System.currentTimeMillis();
		p.buf[off + 1] = timestamp;
		p.setLen((off + 4)<<2);
		p.buf[off] |= Ip.chkSum(p.buf, off, p.len() - (off<<2));
		return Ip.send(p, 0, dstIp, PROTOCOL);
	}
	return false;

}

private static void sendError(int type, int code, int[] origBuf, int origOff){
	Packet p;
	if((p = PacketPool.getFreshPacket()) != null){
		int off = Ip.OFFSET;
		int[] buf = p.buf;
		buf[off] = (type<<24)|(code<<16);
		int payOff = off + 2;
		for(int i = 0; i < origOff + 2; i++){
			buf[payOff + i] = origBuf[i];
		}
		p.setLen((off + origOff + 4)<<2);
		buf[off] |= Ip.chkSum(buf, off, p.len() - (off<<2));
		Ip.send(p, 0, origBuf[3], PROTOCOL);
	}

}

/**
 Sends an ICMP time exceeded error message to the sending host. We support reassembly timeouts only atm (no ttl
 exceeded).

 @param origBuf The buffer of the original packet.
 @param origOff The offset in the original packet. */
public static void sendTimeExceeded(int[] origBuf, int origOff){
	sendError(11, 1, origBuf, origOff);
}

/**
 Sends an ICMP port unreachable error message to the sending host.

 @param origBuf Buffer of the received packet.
 @param origOff Offset of ip payload in that buffer. */
public static void sendPortUnreachable(int[] origBuf, int origOff){
	sendError(3, 3, origBuf, origOff);
}

/**
 Sends an ICMP protocol unreachable error message to the sending host.

 @param origBuf Buffer of the received packet.
 @param origOff Offset of ip payload in that buffer. */
public static void sendProtocolUnreachable(int[] origBuf, int origOff){
	sendError(3, 2, origBuf, origOff);
}

public static void setPingTimeout(int pingTimeout){
	Icmp.pingTimeout = pingTimeout;
}

}
