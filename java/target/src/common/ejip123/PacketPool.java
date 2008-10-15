package ejip123;

/** A pool of packets. Encapsulates the access to the static packet buffers. */
public class PacketPool{

private static int CNT = 0;
private static final Object mutex = new Object();
private static Packet[] packets;
/** Size of packet buffers in bytes. */
private static int BUF_SIZE;

private PacketPool(){
}

/**
 Allocates a number of packet buffers.

 @param cnt  Number of packet buffers. Defaults to 8, if <=0.
 @param size Size of a buffer. Must be a multiple of 4. Defaults to 1500, if <=0. */
public static void init(int cnt, int size){
	synchronized(mutex){
		CNT = (cnt <= 0) ? 8 : cnt;
		packets = new Packet[CNT];
		BUF_SIZE = (size <= 0) ? 1500 : size;
		for(int i = 0; i < CNT; ++i){
			packets[i] = new Packet(BUF_SIZE);
		}
	}
}

/**
 Fetches a free packet and sets it to allocated.

 @return An allocated Packet or null if there are no free packets atm. */
public static Packet getFreshPacket(){

	synchronized(mutex){
		for(int i = 0; i < CNT; ++i){
			Packet p = packets[i];
			if(p.testSetStatus(Packet.FREE, Packet.ALLOC)){
				return p;
			}
		}
	}
	//Dbg.wr('!');
	return null;
}

/**
 Fetches a received packet (that was processed by a link layer) and sets it to allocated.

 @return An allocated Packet or null if there are no free packets atm. */
public static Packet getReceivedPacket(){
// TODO: we may wanna start the search not at 0 all the time...
// packets may starve in the higher slots because new packets arrive and get inserted and processed in the low slots.
	synchronized(mutex){
		for(int i = 0; i < CNT; ++i){
			Packet p = packets[i];
			if(p.testSetStatus(Packet.RCV, Packet.ALLOC)){
				return p;
			}
		}
	}
	return null;
}

/**
 Allocates a free packet and sets its link layer.

 @param linkLayer The LinkLayer.
 @return The changed packet or null, if there are no free packets. */
static Packet getFreshRcvPacket(LinkLayer linkLayer){
	Packet p = getFreshPacket();
	if(p != null){
//		Dbg.wr("R ");
		p.setLinkLayer(linkLayer);
	}
	return p;
}

/**
 Gets packet ready to be sent.

 @param ll The LinkLayer through which this packet will be sent.
 @return a Packet */
public static Packet getTxPacket(LinkLayer ll){
	synchronized(mutex){
		for(int i = 0; i < CNT; ++i){
			Packet p = packets[i];
			if(p.linkLayer() == ll && (p.status() == Packet.DGRAM_RDY || p.status() == Packet.CON_RDY)){
				return p;
			}
		}
	}
	return null;
}

public static int PACKET_CNT(){
	return CNT;
}

public static int PACKET_SIZE(){
	return BUF_SIZE;
}
}
