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

/*
*   Changelog:
*		2002-10-24	creation.
*
*
*	TODO: use a source port as TID.
*		timeout and resend or cancel connection.
*
*/

import util.*;

/**
*	Tftp.java: A simple TFTP Server. see rfc1350.
*/

public class Tftp {

	public static final int PORT = 69;

	private static final int IDLE = 0;

	private static final int RRQ = 1;
	private static final int WRQ = 2;
	private static final int DAT = 3;
	private static final int ACK = 4;
	private static final int ERR = 5;

	private static boolean initOk;

	private static int state;
	private static int fn;
	private static int endBlock;
	private static int block;
private static int simerr;

	private static void tftpInit() {

		initOk = true;
		state = IDLE;
		block = 0;
		fn = 0;
	}

	private static void error(Packet p) {

		p.buf[Udp.DATA] = (ERR<<16) + 4711;
		p.buf[Udp.DATA+1] = 'x'<<24;
		p.len = Udp.DATA+6;
		tftpInit();
	}

	/** drop the packet end reset state */
	private static void discard(Packet p) {
		p.len = 0;			
		tftpInit();
	}

	static void timer() {
		// TODO: retransmit DATA
	}

	/**
	*	handle the TFTP packets.
	*
	*	filename is fixed length (2):
	*		'ix'		internal memory (read only)
	*		'f0'..'f8'	flash sector (64 KB)
	*
	*/
	static void process(Packet p) {

		if (!initOk) tftpInit();

		int i, j;
		int[] buf = p.buf;

Dbg.wr('F');

		int op = buf[Udp.DATA]>>>16;

Dbg.intVal(op);
Dbg.intVal(buf[Udp.DATA]&0xffff);

/*
++simerr;
if (simerr%3==1) { 
Dbg.wr('x');
p.len=0; return;
}
*/

		if (op==RRQ) {

//Amd.sectorErase(0x20000);
/* just change state and cancel old communication
			if (state!=IDLE) {
				error(p);
				return;
			}
*/
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

		} else if (op==ACK) {

			block = (buf[Udp.DATA] & 0xffff)+1;		// use one higher then last acked block
			if (block>endBlock) {
				discard(p);
			} else {
				buf[Udp.DATA] = (DAT<<16)+block;
				if (block==endBlock) {
					p.len = Udp.DATA*4+4;			// last block is zero length
				} else {
					read(buf, block);
					p.len = Udp.DATA*4+4+512;
				}
			}

		} else if (op==WRQ) {

			state = WRQ;
			fn = buf[Udp.DATA]&0xffff;
			block = 1;
			buf[Udp.DATA] = (ACK<<16);
			p.len = Udp.DATA*4+4;

		} else if (op==DAT) {

			i = (buf[Udp.DATA] & 0xffff);	// get block number

			if (state!=WRQ) {
				discard(p);
			} else if (block != i) {		// not the expected block
				// is it a second write with the old block number?
				if (block-1 == i) {
					buf[Udp.DATA] = (ACK<<16)+i;	// just ack it
					p.len = Udp.DATA*4+4;			// we have allready received it before
				} else {
					discard(p);
				}
			} else {
// Dbg.wr('a');
// Dbg.intVal(block);
				if (p.len > Udp.DATA*4+4) {
					program(buf, block);
				}
				buf[Udp.DATA] = (ACK<<16)+block;
				boolean last = p.len != Udp.DATA*4+4+512;
				p.len = Udp.DATA*4+4;
				++block;
				if (last) {				// end of write
					tftpInit();
				}
			}
		} else {
			error(p);
		}

	}

	/**
	*	program flash.
	*/
	private static void program(int[] buf, int block) {

		int i, j;
		int base;

		block--;
		i = fn>>8;
		Timer.wd();				// toggle for each block?
		if (i=='f') {			// program flash
			base = (block<<9);
			base += ((fn&0xff)-'0')<<16;	// 64 KB sector
			if ((base & 0xffff) ==0) {
				Amd.erase(base);
			}
			progloop(base, buf);
		}
	}

	private static void progloop(int base, int[] buf) {

		int i, w;

		for (i=1; i<129; ++i) {
			w = buf[Udp.DATA+i];
			Amd.program(base, w>>>24);
			Amd.program(base+1, w>>>16);
			Amd.program(base+2, w>>>8);
			Amd.program(base+3, w);
			base += 4;
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
				for (j=0; j<4; ++j) {
					k <<= 8;
					k += com.jopdesign.sys.Native.rdMem(base+(i<<2)+j);
				}
				buf[Udp.DATA+1+i] = k;
			}
		} else {						// read nothing
			for (i=0; i<128; ++i) {
				buf[Udp.DATA+1+i] = 0;
			}
		}
	}

}

