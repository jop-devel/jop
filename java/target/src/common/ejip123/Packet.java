package ejip123;

import ejip123.util.Dbg;

/** A packet buffer. Contains a fixed length array for data and some control information. */
public class Packet{
/** The packet is free to use. */
public final static int FREE = 0;
/** Allocated and either under interpretation or under construction. */
public final static int ALLOC = 1;

// the following states are used while the packets float from one layer to the other
/** A packet of a connectionless protocol ready to be sent by a link layer. */
public final static int DGRAM_RDY = 2;
/** Received packet ready to be processed by the network stack. */
public final static int RCV = 3;
/** Used to identify a connection-orientated protocol packet in the IP layer. This is changed to CON_RDY in the IP layer. */
public final static int CON_PREP = 4;
/** A packet of a connection-orientated protocol ready to be sent. This will change to CON_ONFLY after sending. */
public final static int CON_RDY = 5;
/** A sent connection-oriented packet. Needs to be freed by the corresponding protocol, not by a link layer. */
public final static int CON_ONFLY = 6;
/** A packet that is used outside the scope of the core network stack (e.g. {@link UdpHandler}). */
public final static int APP = 7;

//private final Object this = new Object();
/** Current status of the packet. */
private int status;
/** Packet length in bytes. */
private int len;
/** source/destination interface. */
private LinkLayer linkLayer;
/** network layer protocol. e.g. ARP, IP. As defined by ethernet/802.3 */
private int prot;
/** Buffer for the ip datagram. */
public final int[] buf;

public Packet(int max){
	buf = new int[max>>2];
	linkLayer = null;
}

public void setStatus(int v){
	synchronized(this){
		status = v;
	}
}

/** Frees a packet. */
public void free(){
	synchronized(this){
		len = 0;
		status = FREE;
		linkLayer = null;
	}
}

/** Frees a packet, if it is currently set to {@link Packet#APP}. */
public boolean freeIfApp(){
	synchronized(this){
		if(status == APP){
//			Dbg.wr("freed packerl\n");
			len = 0;
			status = FREE;
			linkLayer = null;
			return true;
		} else
			return false;
	}
}

/**
 Changes the status of the packet. The status is changed iff it equals <code>test</code> only.

 @param test The status the packet has to be before calling.
 @param set  The status the packet should become.
 @return True, if the status was changed, true otherwise. */
public boolean testSetStatus(int test, int set){
	synchronized(this){
		if(status == test){
			status = set;
			return true;
		}
	}
	return false;
}

public boolean isConPrep(){
	return status == CON_PREP;
}

/**
 Writes a CharSequence into the packet at a given offset.

 @param off offset in the packet in bytes
 @param s   source of characters.
 @return returns the number of bytes written into the packet buffer. */
public int setData(int off, CharSequence s){
	int cnt;
	synchronized(this){
		cnt = Math.min(s.length(), PacketPool.PACKET_SIZE() - off);

		if(cnt <= 0)
			return 0;

		int first = (4 - off)&0x3;
		int last = (off + cnt)&0x3;
		int mid = cnt - first - last;
		int b = 0;
		int w = off>>2;
		if(first != 0){
			int t = buf[w]>>>(first<<3);
			for(int j = (off&0x3); j < 4; ++j){
				t <<= 8;
				t += s.charAt(b);
				b++;
			}
			buf[w] = t;
			w++;
		}

		int k = 0;
		for(int i = 0; i < mid; i += 4){
			for(int j = 0; j < 4; ++j){
				k <<= 8;
				if(b < cnt){
					k += s.charAt(b);
					b++;
				}
			}
			buf[w + (i>>>2)] = k;
		}


		if(last != 0){
			int msk; // = 0xffffffff>>>(last<<3)
			if(last == 1)
				msk = 0xffffff;
			else if(last == 2)
				msk = 0xffff;
			else if(last == 3)
				msk = 0xff;
			else
				msk = 0;

			w = (off + cnt)>>2;
			int t = 0;
			for(int j = 0; j < 4; ++j){
				t <<= 8;
				if(j < last){
					t += s.charAt(b);
					b++;
				}
			}
			t += (buf[w]&msk);//>>>((4-last)<<3);
			buf[w] = t;
		}

		int overshoot = off + cnt - len; // number of bytes written over the current length of the packet
		if(overshoot > 0)
			len += overshoot;

	}
	return cnt;
}

/**
 Appends a CharSequence at the end of the packet.

 @param s The source of characters.
 @return The count of bytes written. */
public int appendData(CharSequence s){
	return setData(len, s);
}

/**
 Copy packet data into a StringBuffer. Ensures that only data inside the current length boundary gets copied.

 @param off offset in the packet in 32-bit words.
 @param s   StringBuffer destination.
 @return the number of bytes copied. */
public int getData(int off, StringBuffer s){
	// DONE: capacity check to ensure allocation free appending
	int slen = Math.min(len - (off<<2), s.capacity());
	s.setLength(0);
	for(int i = (off<<2); i < slen + (off<<2); i++){
		s.append((char)((buf[i>>2]>>>(24 - ((i&3)<<3)))&0xff));
	}
	return slen;
}

/**
 Make a deep copy from Packet p. Used just for ARP requests with a TCP packet as the TCP packet is kept in the
 connection.
 */
public void copy(Packet p){

	synchronized(this){
		len = p.len();
		linkLayer = p.linkLayer();
		prot = p.prot();
		for(int i = 0; i < PacketPool.PACKET_SIZE(); ++i)
			buf[i] = p.buf[i];
	}
}

public void print(int length){

	if(length <= 0){
		length = len;
	}

	Dbg.wr('\n');
	if(length == 0){
		Dbg.wr("empty packet.\n");
		return;
	}
	Dbg.wr("dumping packet content inside the first ");
	Dbg.intVal(length);
	Dbg.wr('/');
	Dbg.intVal(len);
	Dbg.wr("bytes\n");

	int max = length + 14;
	for(int i = 0, iip = 8; i < max; i++, iip = (iip <= 0) ? 24 : iip - 8){
		int rem = i&0xf; // rem = i % 16
		if(rem == 0){
			Dbg.wr('\n');
			if(i < 0x100)
				Dbg.wr('0');
			if(i < 0x1000)
				Dbg.wr('0');
			Dbg.hexVal(i);
			Dbg.wr(' ');
		}
		if(rem == 8)
			Dbg.wr(' ');

		if(i < 14){
			Dbg.hexVal(0);
		} else{
			Dbg.hexVal((buf[(i - 14)>>>2]>>>iip)&0xff);
		}
	}
	Dbg.lf();
}

public int status(){
	return status;
}

public int len(){
	return len;
}

public void setLen(int len){
	this.len = len;
}

public void addToLen(int amount){
	len += amount;
}

public LinkLayer linkLayer(){
	return linkLayer;
}

public void setLinkLayer(LinkLayer linkLayer){
	this.linkLayer = linkLayer;
}

public int prot(){
	return prot;
}

public void setProt(int prot){
	this.prot = prot;
}

public void setRdy(){
	status = (status == CON_PREP) ? Packet.CON_RDY : Packet.DGRAM_RDY;
}

/**
 Sets one octet of the packet.

 @param c   The octet to be written into the packet.
 @param pos The offset in bytes of octet. */
public void setByte(int c, int pos){
	if(pos > PacketPool.PACKET_SIZE())
		return;

	int posw = pos>>2;
	int shift = 24 - ((pos&3)<<3);
	int msk = ~(0xff<<shift);
	synchronized(this){
		int tmp = buf[posw]&msk;
		tmp |= (c<<shift);
		buf[posw] = tmp;
	}

}

/** Pads the last word with zeros. */
public void padWithZeros(){
	int filled = len&3;
	synchronized(this){
		if(filled == 1)
			buf[len>>2] &= 0xff000000;
		else if(filled == 2)
			buf[len>>2] &= 0xffff0000;
		else if(filled == 3)
			buf[len>>2] &= 0xffffff00;
	}
}
}
