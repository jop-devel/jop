package ejip123;

/** UDP see RFC 768. */
public class Udp{

private static final Object mutex = new Object();
public static final int PROTOCOL = 17;
/**
 @deprecated  */
public static final int DATA = 7;	// offset of data in words
/** Offset of udp payload in ip packets w/o options (in 32b words). */
public static final int OFFSET = Ip.OFFSET + 2;

private static int MAX_HANDLER = 0;
private static UdpHandler[] list = null;
private static int[] ports = null;

private Udp(){
}

public static void init(int handlerCnt){
	MAX_HANDLER = handlerCnt <= 0 ? 4 : handlerCnt;
	list = new UdpHandler[MAX_HANDLER];
	ports = new int[MAX_HANDLER];
}

/**
 Adds a handler for UDP requests.

 @param port The port the handler wants to listens on.
 @param h    The handler.
 @return true, if h gets added, false if the list is full or there is already one handler for that port. */
public static boolean addHandler(int port, UdpHandler h){
	/* DONE should we check if there is already someone listening on the port?
	* According to the code, that calls the handler, we should. */

	synchronized(mutex){
		int n = -1;
		// search for a free place and check that the port is not taken yet
		for(int i = 0; i < MAX_HANDLER; ++i){
			if(list[i] == null){
				n = i;
			} else if(ports[i] == port){
				return false;
			}
		}
		if(n >= 0){
			ports[n] = port;
			list[n] = h;
			return true;
		}
	}
	return false;
}

/** Removes an UDP handler.

 @param port The port the handler is currently registered to.
 @return False, if there was no handler registered for that port. True, otherwise.
 */
public static boolean removeHandler(int port){
	synchronized(mutex){
		for(int i = 0; i < MAX_HANDLER; ++i){
			if(list[i] != null){
				if(ports[i] == port){
					list[i] = null;
					return true;
				}
			}
		}
	}
	return false;
}

/* process packet and generate reply if necessary. */
static void process(Packet p, int off){
	int[] buf = p.buf;
	int port = buf[off];
//	int remPort = port>>>16;
	port &= 0xffff;

	synchronized(mutex){
		for(int i = 0; i < MAX_HANDLER; ++i){
			// searching for the handler registered at this port
			if(list[i] != null && ports[i] == port){
				// a checksum of zero indicates disabled checksumming
				if((buf[off + 1]&0xffff) != 0){
					// inject pseudo header for udp checksum into original packet
					// e.g. for packets w/o options: ttl, prot and ip checksum are overwritten
					buf[off - 1] = buf[4]; // dst ip
					buf[off - 2] = buf[3]; // src ip
					buf[off - 3] = (PROTOCOL<<16) + p.len() - (off<<2);
					if(Ip.chkSum(buf, off - 3, p.len() - ((off - 3)<<2)) != 0){
						p.free();	// mark packet free
						return;
					}
				}
				p.setStatus(Packet.APP);
				list[i].request(p, off + 2);
				p.freeIfApp();
				return;
			}
		}
	}
	Icmp.sendPortUnreachable(buf, off);
	p.free();	// mark packet free
}

/**
 Sends an UDP packet. Source IP and port will be chosen by the implementation.

 @param p          The packet to be sent. The length of the packet needs to be set correctly.
 @param dstIp      Destination IP.
 @param dstPort    Destination port.
 @return true if a packet was sent to a link layer. */
public static boolean send(Packet p, int dstIp, int dstPort){
	return send(p, 0, dstIp, dstPort, dstPort, true);
}

/**
 Sends an UDP packet. Source port will be chosen by the implementation.

 @param p          The packet to be sent. The length of the packet needs to be set correctly.
 @param srcIp      Source IP. If 0, the IP of the used interface will be used.
 @param dstIp      Destination IP.
 @param dstPort    Destination port.
 @return true if a packet was sent to a link layer. */
public static boolean send(Packet p, int srcIp, int dstIp, int dstPort){
	return send(p, srcIp, dstIp, dstPort, dstPort, true);
}

/**
 Sends an UDP packet.

 @param p          The packet to be sent. The length of the packet needs to be set correctly.
 @param srcIp      Source IP. If 0, the IP of the used interface will be used.
 @param dstIp      Destination IP.
 @param srcPort    Source port.
 @param dstPort    Destination port.
 @param calcChksum If true the UDP checksum will be calculated and stored in the packet, else the calculation will be
 skipped and the field be set to 0.
 @return true if a packet was sent to a link layer. */
public static boolean send(Packet p, int srcIp, int dstIp, int srcPort, int dstPort, boolean calcChksum){
	if(p==null)
		return false;
	int off = Ip.OFFSET;
	// 1. UDP header
	int[] buf = p.buf;
	buf[off] = (srcPort<<16) + dstPort;
	buf[off + 1] = (p.len() - 20)<<16;

	if(calcChksum){
		// (optional) 2. UDP pseudo header in front to calc UDP checksum
		if(srcIp == 0){
			LinkLayer ll = Router.getIf(dstIp);
			if(ll == null){
				p.free();
				return false;
			}
			p.setLinkLayer(ll);
			srcIp = ll.getIp();
		}

		buf[off - 1] = dstIp;
		buf[off - 2] = srcIp;
		buf[off - 3] = (PROTOCOL<<16) + p.len() - (off<<2);

		int i = Ip.chkSum(buf, off - 3, p.len() - ((off - 3)<<2));
		buf[off + 1] |= (i == 0) ? 0xffff : i; // special case in UDP: no checksum -> 0, calculated checksum=0 -> 0xffff
	}

	// 3. remains of the IP header
	return Ip.send(p, srcIp, dstIp, PROTOCOL);
}
}
