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
 *		2009-03-22	Object oriented version
 *
 *
 *	TODO: use a source port as TID.
 *		timeout and resend or cancel connection.
 *
 */

import util.Amd;
import util.Timer;

/**
 * Tftp.java: A simple TFTP Server. see rfc1350.
 */

public class Tftp implements UdpHandler {

	public static final int PORT = 69;

	public static final int IDLE = 0;

	public static final int RRQ = 1;
	public static final int WRQ = 2;
	public static final int DAT = 3;
	public static final int ACK = 4;
	public static final int ERR = 5;

	/**
	 * Set to true to simulate some lost packets
	 */
	public static final boolean SIM_ERR = false;
	static final int errorInterval = 71;

	private int state;
	protected int fn;
	private int endBlock;
	private int block;
	private int last_block;
	private int simerr;

	private int block_out;
	private int timeout;
	private int time;

	private int srcIp, dstIp, dstPort;
	private LinkLayer ipLink;

	private Ejip ejip;

	public Tftp(Ejip ejipRef) {
		ejip = ejipRef;
		tftpInit();
	}

	public void tftpInit() {

		state = IDLE;
		last_block = 0;
		block = 0;
		fn = 0;
		block_out = 0;
		timeout = 0;
	}

	/** drop the packet end reset state */
	public void discard(Packet p) {
		p.len = 0;
		tftpInit();
	}

	public void onTheFly(int block) {

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

	private void resend() {

		time <<= 1;
		if (time > 64) {
			if (Logging.LOG)
				Logging.wr("TFTP give up");
			tftpInit();
			return;
		}

		if (Logging.LOG)
			Logging.wr("TFTP resend ");
		if (Logging.LOG)
			Logging.intVal(block_out);
		// retransmit DATA
		timeout = Timer.getTimeoutSec(time);

		Packet p = ejip.getFreePacket(ipLink);
		if (p == null) { // got no free buffer!
			if (Logging.LOG)
				Logging.wr('!');
			if (Logging.LOG)
				Logging.wr('b');
			return;
		}

		p.buf[Udp.DATA] = (DAT << 16) + block_out;
		if (block_out == endBlock) {
			p.len = Udp.DATA * 4 + 4; // last block is zero length
		} else {
			read(p.buf, block_out);
			p.len = Udp.DATA * 4 + 4 + 512;
		}
		Udp.build(p, srcIp, dstIp, dstPort);
	}

	/**
	 * handle the TFTP packets.
	 * 
	 * filename is fixed length (2): 'ix' internal memory (read only) 'f0'..'f8'
	 * flash sector (64 KB)
	 * 
	 */
	public void request(Packet p) {

		int i;
		int[] buf = p.buf;

		if (Logging.LOG)
			Logging.wr('F');
		if (Logging.LOG)
			Logging.hexVal(buf[Udp.DATA]);

		int op = buf[Udp.DATA] >>> 16;

		if (SIM_ERR) {
			++simerr;
			if (simerr % errorInterval == 0) {
				if (Logging.LOG) {
					Logging.wr(" tftp dropped");
					Logging.lf();
				}
				ejip.returnPacket(p); // mark packet free return;
				return;
			}
		}

		if (op == RRQ) {

			state = RRQ;
			fn = buf[Udp.DATA] & 0xffff;
			i = fn >> 8;
			if (i == 'i') {
				endBlock = 2 + 1; // (256*4)/512
			} else if (i == 'f') {
				endBlock = 128 + 1; // 64K/512
			} else {
				endBlock = 1 + 1;
			}

			block = 1;
			buf[Udp.DATA] = (DAT << 16) + block;
			read(buf, block);
			p.len = Udp.DATA * 4 + 4 + 512;
			onTheFly(block);

		} else if (op == ACK) {

			i = (buf[Udp.DATA] & 0xffff); // get block number
			if (i < block) {
				// a ACK for an already sent package
				// drop it
				ejip.returnPacket(p); // mark packet free
				return;
			}

			block = i + 1; // use one higher then last acked block
			if (block > endBlock) { // ACK of last block
				discard(p);
			} else {
				buf[Udp.DATA] = (DAT << 16) + block;
				if (block == endBlock) {
					p.len = Udp.DATA * 4 + 4; // last block is zero length
				} else {
					read(buf, block);
					p.len = Udp.DATA * 4 + 4 + 512;
				}
				onTheFly(block);

				if (SIM_ERR) {
					++simerr;
					if (simerr % errorInterval == 0) {
						if (Logging.LOG) {
							Logging.wr(" simulate wrong data on read ");
							Logging.lf();
						}
						buf[Udp.DATA+13] = 0x12345678;
					}
				}				 
			}

		} else if (op == WRQ) {

			state = WRQ;
			fn = buf[Udp.DATA] & 0xffff;
			block = 1;
			buf[Udp.DATA] = (ACK << 16);
			p.len = Udp.DATA * 4 + 4;

		} else if (op == DAT) {

			i = (buf[Udp.DATA] & 0xffff); // get block number

			if (state == IDLE) {
				if (i == last_block) {
					// ACK of last data block got lost,
					// but we received the data and finished programming
					buf[Udp.DATA] = (ACK << 16) + last_block; // just ack it
					p.len = Udp.DATA * 4 + 4; // we have already received it
					// before
				}
			} else if (state != WRQ) {
				discard(p);
			} else if (block != i) { // not the expected block
				// is it a second write with the old block number?
				if (block - 1 == i) {
					buf[Udp.DATA] = (ACK << 16) + i; // just ack it
					p.len = Udp.DATA * 4 + 4; // we have already received it
					// before
				} else {
					p.len = 0; // else just discard packet
				}
			} else {
				// if (Logging.LOG) Logging.wr('a');
				// if (Logging.LOG) Logging.intVal(block);
				if (p.len > Udp.DATA * 4 + 4) {
					write(buf, block);
				}
				buf[Udp.DATA] = (ACK << 16) + block;
				boolean last = p.len != Udp.DATA * 4 + 4 + 512;
				p.len = Udp.DATA * 4 + 4;
				++block;
				if (last) { // end of write
					eof(block - 1);
					tftpInit();
					// remember very last written block
					last_block = block - 1;
				}
			}
		} else {
			p.len = 0;
			tftpInit();
			if (Logging.LOG)
				Logging.wr("error ");
		}

		if (p.len == 0) {
			ejip.returnPacket(p); // mark packet free
		} else {
			reply(p);
		}
	}

	// TODO: insert reply back to request or split in smaller methods
	public void reply(Packet p) {

		if (SIM_ERR) {
			++simerr;
			if (simerr % errorInterval == 0) {
				if (Logging.LOG) {
					Logging.wr("reply dropped ");
					Logging.lf();
				}
				ejip.returnPacket(p); // mark packet free
				return;
			}
		}

		int[] buf = p.buf;
		if (Logging.LOG)
			Logging.wr("tftp reply: ");
		if (Logging.LOG)
			Logging.hexVal(buf[Udp.DATA] & 0xffff);

		// generate a reply with IP src/dst exchanged
		dstPort = buf[Udp.HEAD] >>> 16;
		srcIp = buf[4];
		dstIp = buf[3];
		ipLink = p.getLinkLayer();
		Udp.build(p, srcIp, dstIp, dstPort);
	}

	/**
	 * Invoked on each received block. Program the Flash in default
	 * implementation.
	 */
	protected void write(int[] buf, int block) {

		int i;
		int base;

		block--;
		i = fn >> 8;
		Timer.wd(); // toggle for each block?
		if (i == 'f') { // program flash
			base = (block << 9);
			base += ((fn & 0xff) - '0') << 16; // 64 KB sector
			if ((base & 0xffff) == 0) {
				Amd.erase(base);
			}
			progloop(base, buf);
		}
	}

	/**
	 * Invoked at end of write
	 * 
	 * @param i
	 *            block number of last block
	 */
	protected void eof(int i) {
		// nothing to do in default implementation
	}

	private void progloop(int base, int[] buf) {

		int i, w;

		for (i = 1; i < 129; ++i) {
			w = buf[Udp.DATA + i];
			Amd.program(base, w >>> 24);
			Amd.program(base + 1, w >>> 16);
			Amd.program(base + 2, w >>> 8);
			Amd.program(base + 3, w);
			base += 4;
		}
	}

	/**
	 * read internal memory or flash.
	 */
	public void read(int[] buf, int block) {

		int i, j, k;
		int base;

		block--;
		i = fn >> 8;
		if (i == 'i') { // read internal memory
			base = block << 7;
			for (i = 0; i < 128; ++i) { // @WCA loop<=128
				buf[Udp.DATA + 1 + i] = com.jopdesign.sys.Native.rdIntMem(base
						+ i);
			}
		} else if (i == 'f') { // read flash
			base = (block << 9) + 0x80000; // add offset because we use
			// Native.rdMem!
			base += ((fn & 0xff) - '0') << 16; // 64 KB sector

			k = 0;
			for (i = 0; i < 128; ++i) { // @WCA loop<=128
				synchronized (this) {
					for (j = 0; j < 4; ++j) { // @WCA loop<=4
						k <<= 8;
						k += com.jopdesign.sys.Native
								.rdMem(base + (i << 2) + j);
					}
				}
				buf[Udp.DATA + 1 + i] = k;
			}
		} else { // read nothing
			for (i = 0; i < 128; ++i) { // @WCA loop<=128
				buf[Udp.DATA + 1 + i] = 0;
			}
		}
	}

}
