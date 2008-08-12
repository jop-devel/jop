/*
 * Copyright (c) Martin Schoeberl, martin@jopdesign.com
 * All rights reserved.
 *
 *
 */

package oebb;

/*
*   Changelog:
*		2002-10-24	creation.
*		2004-12-04	A 'special' version for the OEBB project.
*
*
*	TODO: use a source port as TID.
*		timeout and resend or cancel connection.
*
*/

import com.jopdesign.sys.Native;

import util.Amd;
import util.Dbg;
import util.Timer;
import ejip.LinkLayer;
import ejip.Packet;
import ejip.Udp;
import ejip.UdpHandler;

/**
*	BgTftp.java: A simple TFTP Server. see rfc1350.
*/

public class BgTftp extends UdpHandler {

	public static final int PORT = 69;

	private static final int IDLE = 0;

	private static final int RRQ = 1;
	private static final int WRQ = 2;
	private static final int DAT = 3;
	private static final int ACK = 4;
	private static final int ERR = 5;

	private static int state;
	private static int fn;
	private static int endBlock;
	private static int block;
	private static int last_block;
private static int simerr;

	private static int srcIp, dstIp, dstPort;
	private static LinkLayer ipLink;
	
	final static int MAX_BLOCKS = 128;
	static int[] sector;

	BgTftp() {
		// +1 is for the final block on a 64KB write
		// that contains no data
		sector = new int[(MAX_BLOCKS+1)*512/4];
		tftpInit();
	}

	private static void tftpInit() {

		state = IDLE;
		last_block = 0;
		block = 0;
		fn = 0;
		block_out = 0;
		timeout = 0;
	}

/* not used
	private static void error(Packet p) {

		p.buf[Udp.DATA] = (ERR<<16) + 4711;
		p.buf[Udp.DATA+1] = 'x'<<24;
		p.len = Udp.DATA+6;
		tftpInit();
	}
*/

	/** drop the packet end reset state */
	private static void discard(Packet p) {
		p.len = 0;			
		tftpInit();
	}

	private static int block_out;
	private static int timeout;
	private static int time;

	private static void onTheFly(int block) {

		block_out = block;
		time = 4;
		timeout = Timer.getTimeoutSec(time);
	}

	public void loop() {

		if (block_out != 0) {
			if (Timer.timeout(timeout)) {
				resend();
			}
		}
	}

	private static void resend() {


		time <<= 1;
		if (time > 64) {
Dbg.wr("TFTP give up");
			tftpInit();
			return;
		}

Dbg.wr("TFTP resend ");
Dbg.intVal(block_out);
		// retransmit DATA
		timeout = Timer.getTimeoutSec(time);

		Packet p = Packet.getPacket(Packet.FREE, Packet.ALLOC, ipLink);
		if (p == null) {								// got no free buffer!
			Dbg.wr('!');
			Dbg.wr('b');
			return;
		}
		p.buf[Udp.DATA] = (DAT<<16)+block_out;
		if (block_out==endBlock) {
			p.len = Udp.DATA*4+4;			// last block is zero length
		} else {
			read(p.buf, block_out);
			p.len = Udp.DATA*4+4+512;
		}
		Udp.build(p, srcIp, dstIp, dstPort);
	}

	/**
	*	handle the TFTP packets.
	*
	*	filename is fixed length (2):
	*		'ix'		internal memory (read only)
	*		'f0'..'f8'	flash sector (64 KB)
	*
	*/
	public void request(Packet p) {

		int i, j;
		int[] buf = p.buf;

Dbg.wr('F');
Dbg.hexVal(buf[Udp.DATA]);

		int op = buf[Udp.DATA]>>>16;


/*
++simerr;
if (simerr%23==0) { 
Dbg.wr(" tftp dropped");
Dbg.lf();
p.setStatus(Packet.FREE);	// mark packet free
return;
}
*/

		if (op==RRQ) {

			state = RRQ;
			fn = buf[Udp.DATA]&0xffff;
			i = fn>>8;
			if (i=='i') {
				endBlock = 2+1;			// (256*4)/512
			} else if (i=='f') {
				endBlock = 128+1;			// 64K/512
			} else {
				endBlock = 1+1;
			}

			block = 1;
			buf[Udp.DATA] = (DAT<<16)+block;
			read(buf, block);
			p.len = Udp.DATA*4+4+512;
			onTheFly(block);

		} else if (op==ACK) {

			i = (buf[Udp.DATA] & 0xffff);	// get block number
			if (i < block) {
				// a ACK for an allready sent package
				// drop it
				p.setStatus(Packet.FREE);	// mark packet free
				return;
			}

			block = i+1;					// use one higher then last acked block
			if (block>endBlock) {			// ACK of last block
				discard(p);
			} else {
				buf[Udp.DATA] = (DAT<<16)+block;
				if (block==endBlock) {
					p.len = Udp.DATA*4+4;			// last block is zero length
				} else {
					read(buf, block);
					p.len = Udp.DATA*4+4+512;
				}
				onTheFly(block);
/*
++simerr;
if (simerr%23==0) { 
Dbg.wr(" simulate wrong data on read ");
Dbg.lf();
buf[Udp.DATA+13] = 0x12345678;
}
*/
			}

		} else if (op==WRQ) {

			state = WRQ;
			fn = buf[Udp.DATA]&0xffff;
			block = 1;
			buf[Udp.DATA] = (ACK<<16);
			p.len = Udp.DATA*4+4;

		} else if (op==DAT) {

			i = (buf[Udp.DATA] & 0xffff);	// get block number

			if (state==IDLE) {
				if (i==last_block) {
					// ACK of last data block got lost,
					// but we received the data and finished programming
					buf[Udp.DATA] = (ACK<<16)+last_block;	// just ack it
					p.len = Udp.DATA*4+4;			// we have allready received it before
				}
			} else if (state!=WRQ) {
				discard(p);
			} else if (block != i) {		// not the expected block
				// is it a second write with the old block number?
				if (block-1 == i) {
					buf[Udp.DATA] = (ACK<<16)+i;	// just ack it
					p.len = Udp.DATA*4+4;			// we have allready received it before
				} else {
					p.len = 0;				// else just discarde paket
				}
			} else {
// Dbg.wr('a');
// Dbg.intVal(block);
				if (p.len > Udp.DATA*4+4) {
					save(buf, block);
				}
				buf[Udp.DATA] = (ACK<<16)+block;
				boolean last = p.len != Udp.DATA*4+4+512;
				p.len = Udp.DATA*4+4;
				++block;
				if (last) {				// end of write
					program(block-1);
					tftpInit();
					// remember very last written block
					last_block = block-1;
				}
			}
		} else {
			p.len = 0;
			tftpInit();
Dbg.wr("error ");
		}

		if (p.len==0) {
			p.setStatus(Packet.FREE);	// mark packet free
		} else {
			reply(p);
		}
	}

// TODO: insert reply back to request or split in smaller methods
	private static void reply(Packet p) {

/*
++simerr;
if (simerr%23==0) { 
Dbg.wr("reply dropped ");
Dbg.lf();
p.setStatus(Packet.FREE);	// mark packet free
return;
}
*/
int[] buf = p.buf;
Dbg.wr("tftp reply: ");
Dbg.intVal(buf[Udp.DATA] & 0xffff);

			// generate a reply with IP src/dst exchanged
			dstPort = buf[Udp.HEAD]>>>16;
			srcIp = buf[4];
			dstIp = buf[3];
			ipLink = p.getLinkLayer();
			Udp.build(p, srcIp, dstIp, dstPort);
	}

	/**
	*	Save data for future programming.
	*/
	private static void save(int[] buf, int block) {

		int i, j;
		int base;

		block--;				// data blocks start with 1
		i = fn>>8;
		Timer.wd();				// toggle for each block?
System.out.print("Save "); System.out.println(block);
		if (i=='f') {			// program flash
			// here we count in 128 word blocks = 512 byte blocks
			base = (block<<7);
			if (block>MAX_BLOCKS) {
				System.out.println("Too many blocks!");
				return;
			}
			// base += ((fn&0xff)-'0')<<16;	// 64 KB sector
			for (j=0; j<128; ++j) {
				sector[base+j] = buf[Udp.DATA+1+j]; 
			}
		}
	}

	/**
	*	Program one sector of the Flash.
	*/
	private static void program(int cnt) {

System.out.print("Program "); System.out.print(cnt); System.out.println(" blocks");

		int i, j, w;
		int base;

		i = fn>>8;
		if (i!='f') return;			// filename not valid
		base = ((fn&0xff)-'0')<<16;	// 64 KB sector
// System.out.print("Erase sector "); System.out.println(base);
		synchronized (sector) {
			Amd.erase(base);
System.out.println("Program ");

			for (i=0; i<cnt && i<MAX_BLOCKS; ++i) {
				Timer.wd();				// toggle for each block?
 System.out.print(" blk "); System.out.print(i);
				for (j=0; j<128; ++j) {
					w = sector[i*128+j];
					Amd.program(base, w>>>24);
					Amd.program(base+1, w>>>16);
					Amd.program(base+2, w>>>8);
					Amd.program(base+3, w);
					base += 4;
				}
			}	
		}
	}

	/**
	*	read internal memory or flash.
	*/
	private static void read(int[] buf, int block) {

		int i, j, k;
		int base;

		block--;
		i = fn>>8;
		if (i=='i') {					// read internal memory
			base = block<<7;
			for (i=0; i<128; ++i) {
				buf[Udp.DATA+1+i] = com.jopdesign.sys.Native.rdIntMem(base+i);
			}
		} else if (i=='f') {				// read flash
			base = (block<<9) + 0x80000;	// add offset because we use Native.rdMem!
			base += ((fn&0xff)-'0')<<16;	// 64 KB sector

			k = 0;
			for (i=0; i<128; ++i) {
				synchronized (sector) {
					for (j=0; j<4; ++j) {
						k <<= 8;
						k += com.jopdesign.sys.Native.rdMem(base+(i<<2)+j);
					}					
				}
				buf[Udp.DATA+1+i] = k;
			}
		} else {						// read nothing
			for (i=0; i<128; ++i) {
				buf[Udp.DATA+1+i] = 0;
			}
		}
	}
	
	/**
	 * move log data
	 * @author admin
	 *
	 */
	static void moveLog() {
		
		int i, j;
//		synchronized (sector) {
			Timer.wd();
			j = sector.length;
			for (i=0; i<j; ++i) {
				sector[i] = -1;
			}
			for (i=0; i<Flash.CONFIG_LEN/4; ++i) {
				sector[i] = intVal(Flash.BGID_START+(i<<2));
			}
			j = Flash.CONFIG_LEN/4;
			for (i=8000; i<16384; ++i) {
				sector[j] = intVal(Flash.BGID_START+(i<<2));
				++j;
			}
			fn = (((int)'f')<<8) + '3';
			program(MAX_BLOCKS);
//		}
	}
	
	/**
	 * Set bgid if it got lost
	 * @param bgid
	 */
	static void programBgid(int bgid) {
		
		int i,j;
		
		Timer.wd();
		j = sector.length;
		for (i=0; i<j; ++i) {
			sector[i] = -1;
		}
		sector[0] = bgid;
		fn = (((int)'f')<<8) + '3';
		program(1);

	}
	
	static int intVal(int addr) {

		int val = 0;
		synchronized (sector) {
			for (int i=0; i<4; ++i) {
				val <<= 8;
				val += Native.rdMem(addr+i);
			}			
		}

		return val;
	}

}

