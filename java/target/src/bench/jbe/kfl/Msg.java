/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2001-2008, Martin Schoeberl (martin@jopdesign.com)

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package jbe.kfl;


/**
*	message handling.
*	Mast and Zentrale.
*/


public class Msg {

	static final int IO_STATUS = 1;
	static final int IO_UART = 2;
	static final int IO_RS485 = 15;

	//
	//	comm timeout for Zentrale
	//		must include send and receive time (2*1 ms)
	//	minimum 2*INTERVAL + 2ms (=> >12ms)
	//
	//	msg time: 1/38400*10*4 + 100us = 1.14 ms
	//
	//	measured: 12 ms is ok
	//		but max. msg turnaround with bus idle is 18-19 ms
	//		so 15 ms is a good value
	//
	private static final int COMM_TIMEOUT = 15*JopSys.MS;
	private static final int EXT_COMM_TIMEOUT = 100*JopSys.MS;		// for msg FL_PROG!

	private static final int ADDR_MSK = 0x7c0000;
	private static final int CMD_MSK  = 0x03f000;
	private static final int DATA_MSK = 0x000fff;
	private static int address;

//
//	buffer for serial and rs485 input
//
	private static int[] buf, bufSer, buf485;
	private static int cntSer, cnt485;

	private static boolean send;		// send a msg in loop (is in buf)

	private static int cmd;
	private static int val;

/** one message pending. */
	public static boolean available;
	public static boolean msg485;		// msg. from rs485 (or rs232)

/**
*	initailize buffers and set station address.
*/
	public static void init(int addr) {		// it would be time to change jvm for objects!!!

		address = addr<<18;
		bufSer = new int[4];
		buf485 = new int[4];
		buf = new int[4];
		cntSer = cnt485 = 0;
		available = false;
		send = false;
	}

/*
*	set new address
*/
	public static void setAddr(int addr) {

		address = addr<<18;
	}

/*
	better values for polynom on short messages see:

		'Determining optimal cyclic codes for embedded networks'
		(www.cmu.edu)
*/
/**
*	claculate crc value with polynom x^8 + x^2 + x + 1
*	and initial value 0xff
*	on 32 bit data
*/
	static int crc(int val) {

		int reg = -1;

		for (int i=0; i<32; ++i) { // @WCA loop=32
			reg <<= 1;
			if (val<0) reg |= 1;
			val <<=1;
			if ((reg & 0x100) != 0) reg ^= 0x07;
		}
		reg &= 0xff;

		return reg;
	}


/**
*	message loop.
*/
	static void loop() {

		if (send) {		// send the response
			doSend();
		} else {		// OR check if new msg received
			doRcv();
		}
	}


	private static void doRcv() {

		int i;

		int old485 = cnt485;
		while ((JopSys.rd(IO_STATUS)&2)!=0 && cntSer<4) { // @WCA loop=4
			bufSer[cntSer++] = JopSys.rd(IO_UART);
			msg485 = false;
		}
		while ((JopSys.rd(IO_STATUS)&32)!=0 && cnt485<4) { // @WCA loop=4
			buf485[cnt485++] = JopSys.rd(IO_RS485);
			msg485 = true;
		}
		if (cntSer==4) {					// read messages from serial and rs485
			for (i=0; i<4; ++i) { // @WCA loop=4
				buf485[i] = bufSer[i];
			}
			cnt485 = 4;
			cntSer = 0;
		}
		if (cnt485==4) {
			val = 0;
			for (i=0; i<4; ++i) { // @WCA loop=4
				val <<= 8;
				val |= buf485[i];
			}
			if (crc(val)==0) {			// ignore messages with wrong crc
				if (val<0) {			// cmds have MSB set
					val >>>= 8;
					if ((val & ADDR_MSK) == address) {
						cmd = val & CMD_MSK;
						val &= DATA_MSK;
						available = true;
					}
				}
			}
			cnt485 = 0;
		}
//
//	remove bytes if msg not complete after second loop (only RS485)
//
		if (old485!=0 && (old485==cnt485)) {
			cnt485 = 0;
		}
	}

	private static void doSend() {

		int i;

		for (i=3; i>=0; --i) {	// @WCA loop=4
			// buf is filled in 'wrong' way (LSB)
			if (msg485) {
				JopSys.wr(buf[i], IO_RS485);
			} else {
				JopSys.wr(buf[i], IO_UART);
			}
		}
		send = false;
	}

	static int readCmd() {

		available = false;
		return cmd>>>12;
	}

	static int readData() {

		return val;
	}

/**
*	write back the answer.
*/
	static void write(int val) {

		int i;

		val &= DATA_MSK;		// for shure
		val |= address | cmd;
		val <<= 8;
		val |= crc(val);		// append crc

		for (i=0; i<4; ++i) { // @WCA loop=4
			buf[i] = val & 0x0ff;
			val >>>= 8;
		}

		send = true;			// mark buf full, to be sent in next loop
	}

/**
*	write back error code.
*/
	static void err(int val) {

		cmd = 0;
		write(val);
	}
}
