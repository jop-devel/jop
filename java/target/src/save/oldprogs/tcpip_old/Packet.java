package tcpip;
/**
*	Packet.java: Packet buffer handling for a
*	minimalistic TCP/IP stack.
*
*	Copyright Martin Schoeberl.
*
*   This software may be used and distributed according to the terms
*   of the GNU General Public License, incorporated herein by reference.
*
*   The author may be reached as martin.schoeberl@chello.at
*
*
*
*   Changelog:
*		2002-11-11	use LinkLayer info to mix Slip and Ethernet packets
*
*/

import util.*;

public class Packet {

	public final static int MAX = 1500;		// maximum Packet length in bytes
	public final static int MAXW = 1500/4;	// maximum Packet length in word
	public final static int MAXLLH = 7;		// 7 16 bit words for ethernet
	private final static int CNT = 8;		// size of packet pool

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
	/** interface source */
	private LinkLayer src;

	//	no direct construction
	private Packet() {
		llh = new int[MAXLLH];
		buf = new int[MAXW];
		len = 0;
		status = FREE;
		src = null;
	}

	private static boolean initOk;
	private static Packet[] packets;

	public static void init() {

		if (initOk) return;
		initOk = true;
		packets = new Packet[CNT];
		for (int i=0; i<CNT; ++i) {
			packets[i] = new Packet();
		}
	}

private static void dbg() {

	Dbg.wr('|');
	for (int i=0; i<CNT; ++i) {
		Dbg.wr('0'+packets[i].status);
	}
	Dbg.wr('|');
}


/**
*	Request a packet of a given type from the pool and set new type.
*/
	public static Packet getPacket(int type, int newType) {

		int i;

		if (!initOk) {
			init();
		}
		for (i=0; i<CNT; ++i) {
			if (packets[i].status==type) {
				break;
			}
		}
		if (i==CNT) {
if (type==FREE) Dbg.wr('!');
			return null;
		}
		packets[i].status = newType;
// dbg();
		return packets[i];
	}

/**
*	Request a packet of a given type from the pool and set new type and source.
*/
	public static Packet getPacket(int type, int newType, LinkLayer s) {

		int i;

		if (!initOk) {
			init();
		}
		for (i=0; i<CNT; ++i) {
			if (packets[i].status==type) {
				break;
			}
		}
		if (i==CNT) {
if (type==FREE) Dbg.wr('!');
			return null;
		}
		packets[i].status = newType;
		packets[i].src = s;
// dbg();
		return packets[i];
	}

/**
*	Request a packet of a given type and source from the pool and set new type.
*/
	public static Packet getPacket(LinkLayer s, int type, int newType) {

		int i;

		if (!initOk) {
			init();
		}
		for (i=0; i<CNT; ++i) {
			if (packets[i].status==type && packets[i].src==s) {
				break;
			}
		}
		if (i==CNT) {
			return null;
		}
		packets[i].status = newType;
// dbg();
		return packets[i];
	}

	public void setStatus(int v) {
		status = v;
// dbg();
	}

}
