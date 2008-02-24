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

package kfl;

/**
*	Flash programming.
*		read and write data to address 0x80000.
*
*	address mapping:
*
*		0x00000 - 0x17fff	96kB Acex config
*		0x18000 - 0x1bfff	16kB java program
*		0x1c000 - 0x1c3ff	1kB MS data
*		0x1c400 - 0x1dfff	7kB text data
*		0x1e000 - 0x1ffff	8kB log book
*		0x1ffff				address of Mast
*
*/

public class Flash {

	public static final int LAST_ADDR = 0x9ffff;
	public static final int FLASH_START = 0x80000;

	public static final int TEXT_START = FLASH_START+0x1c400;


	public static final int LOG_LEN = 8192;
	public static final int LOG_SIZE = 32;
	public static final int LOG_START = 0xa0000-LOG_LEN;

//
//	use relativ offset for MS data
//
	public static final int MS_DATA = 0x1c000;
	public static final int MS_DATA_LEN = 17*32;

	private static int[] buf;
	private static int addr, cnt;

	public static void init() {

		buf = new int[128];
		for (int i=0; i<128; ++i) {
			buf[i] = 0xff;
		}
		addr = 0;
		cnt = 0;
	}

//
//	only for Mast
//
	public static void setStationAddress(int val) {

		if (val<0 || val>31) return;

		JopSys.wrMem(val, LAST_ADDR);
		while(JopSys.rdMem(LAST_ADDR) != val)		// WD on failure
			;
	}
	public static int getStationAddress() {

		int i = JopSys.rdMem(LAST_ADDR);
		if (i<0 || i>31) {
			return 0;
		} else {
			return i;
		}
	}

/*
*	Set address in 128 byte pages.
*/
	public static void setPage(int p) {

		addr = p<<7;
		cnt = 0;
	}


	public static void setData(int data) {

		buf[cnt & 0x7f] = data;
		++cnt;
	}


/*
*	read one page in intern buffer.
*/
	private static void readPage() {

		for (int i=0; i<128; ++i) {
			buf[i] = JopSys.rdMem(((FLASH_START+addr) & 0xfff80) | i);
		}
	}

/**
*	program one byte.
*/
	public static void write(int a, int data) {

		addr = a;
		readPage();
		buf[addr & 0x7f] = data;
		program();
	}

/**
*	program one 16 bit value.
*/
	public static void write16(int a, int data) {

		addr = a;
		readPage();
		buf[addr & 0x7f] = data>>>8;
		buf[(addr+1) & 0x7f] = data;
		program();
	}

	public static void program() {

		int i, val;

		val = buf[127] & 0xff;
		for (i=0; i<128; ++i) {
			JopSys.wrMem(buf[i], ((FLASH_START+addr) & 0xfff80) | i);
			buf[i] = 0xff;
		}
		// wait 1 ms to start programming (min 150 us)
		Timer.sleep(1);
		while(JopSys.rdMem(FLASH_START+addr+127) != val)		// WD on failure
			;
	}

	public static int read() {

		return JopSys.rdMem(FLASH_START+addr+(cnt++));
	}

	public static int read(int addr) {

		return JopSys.rdMem(FLASH_START+addr);
	}
	public static int read16(int addr) {

		return (JopSys.rdMem(FLASH_START+addr)<<8) | JopSys.rdMem(FLASH_START+addr+1);
	}
}
