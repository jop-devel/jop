package tcpip;

/**
*	Udp.java: UDP functions.
*
*	Copyright Martin Schoeberl.
*
*   This software may be used and distributed according to the terms
*   of the GNU General Public License, incorporated herein by reference.
*
*   The author may be reached as martin.schoeberl@chello.at
*
*	It's enough to handel a HTTP request (and nothing more)!
*
*
*   Changelog:
*		2002-10-24	creation.
*
*
*/

import util.*;

public class Udp {

	public static final int PROTOCOL = 17;

	public static final int HEAD = 5;	// offset of data in words
	public static final int DATA = 7;	// offset of data in words
	static int count;

	static void process(Packet p) {

		int i, j;
		int[] buf = p.buf;

		int port = buf[HEAD];
		int remport = port >>> 16;
		port &= 0xffff;

/*
if (port!=1625) {
Dbg.wr('\n');
Dbg.wr('U');
Dbg.intVal(port);
}
*/
		if (port == Tftp.PORT) {
			
			Tftp.process(p);

		} else if (port == 1625) {

			// do the Dgb thing!
			i = Dbg.readBuffer(buf, 7);
			p.len = 28+i;

		} else {
			p.len = 0;
			return;
		}

		if (p.len==0) return;

		// 'exchange' port numbers
		buf[HEAD] = (port<<16) + remport;

		// Fill in UDP header
		buf[HEAD+1] = (p.len-20)<<16;
		buf[2] = (PROTOCOL<<16) + p.len - 20; 		// set protocol and udp length in iph checksum for tcp checksum
		i = TcpIp.chkSum(buf, 2, p.len-8);
		if (i==0) i = 0xffff;
		buf[HEAD+1] |= i;
	
	}

}
