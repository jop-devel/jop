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
*	Logbook
*/

public class Log {

	public static final int UP_STARTED = 1;
	public static final int DOWN_STARTED = 2;
	public static final int IS_UP = 3;
	public static final int IS_DOWN = 4;
	public static final int ERROR = 5;
	public static final int STOP = 6;				// currently not used
	public static final int NOTSTOP = 7;

	private static boolean auto;

	private static int[] buf;

	public static void init() {

		buf = new int[128];
		auto = false;
	}

	public static void write(int action, int errnr) { write(action, errnr, 0); }
	public static void write(int action) { write(action, 0, 0); }

/**
*	write log entry.
*/
	public static void write(int action, int errnr, int msnr) {

		int addr = findLastLog();
		int i, nr;

		if (addr<0) {						// first entry
			addr = 0;
			nr = 1;
		} else {
			nr = read32(Flash.LOG_START+addr)+1;	// increment log counter
			addr += Flash.LOG_SIZE;				// increment address
			addr &= Flash.LOG_LEN-1;				// ring buffer
		}
		addr += Flash.LOG_START-Flash.FLASH_START;		// addr now a relativ flash address

		Flash.setPage(addr>>7);
		for (i=0; i<128; ++i) {
			buf[i] = Flash.read();
		}
		Timer.wd();
		
		logSetValues(addr & 0x7f, nr, action, errnr, msnr);	// local address in buffer

		Timer.wd();
		Flash.setPage(addr>>7);
		for (i=0; i<128; ++i) {
			Flash.setData(buf[i]);
		}
		Flash.program();
	}

/**
*
*/
	public static void setAuto(boolean f) {
		auto = f;
	}

/**
*	set log entrys in buffer.
*/
	private static void logSetValues(int i, int nr, int action, int errnr, int msnr) {

		buf[i] = nr>>>24;		// entry number
		buf[i+1] = nr>>>16;
		buf[i+2] = nr>>>8;
		buf[i+3] = nr;
		buf[i+4] = Clock.getYear()>>>8;
		buf[i+5] = Clock.getYear();
		buf[i+6] = Clock.getMonth();
		buf[i+7] = Clock.getDay();
		buf[i+8] = Clock.getSec()>>>8;
		buf[i+9] = Clock.getSec();
		if (auto) action |= 0x80;
		buf[i+10] = action;
									// TODO: diese Zeile fuehrt zum Absturz!!!
									// if (auto) buf[i+10] |= 0x80;
		buf[i+11] = errnr;
		buf[i+12] = msnr;			// MS for errnr
/*	Bauteile fehlen!!!
buf[i+13] = 0;
*/
		buf[i+13] = Temp.calc(46000-JopSys.rd(BBSys.IO_ADC));
Timer.wd();
		buf[i+14] = 0;
		buf[i+15] = 0;
		for (int j=0; j<16 && j<Flash.LOG_SIZE-16; ++j) {
			buf[i+16+j] = Station.temp[j];			// reserved for MS temp.
		}
	}

/**
*	get log values.
*/
	public static int getSec(int addr) {
		return read16(Flash.LOG_START+addr+8);
	}
	public static int getAction(int addr) {
		return JopSys.rdMem(Flash.LOG_START+addr+10)&0x7f;	// mask out automatic flag
	}
	public static int getErrnr(int addr) {
		return JopSys.rdMem(Flash.LOG_START+addr+11);
	}
	public static int getMsnr(int addr) {
		return JopSys.rdMem(Flash.LOG_START+addr+12);
	}
/**
*	return relativ address of log with highest nr.
*/
	private static int findLastLog() {

		int i, j;
		int max = 0;
		int addr = 0;

		for (i=0; i<Flash.LOG_LEN; i+=Flash.LOG_SIZE) {
			j = read32(Flash.LOG_START+i);
			if (j>max) {
				max = j;
				addr = i;
			}
			Timer.wd();
		}

		if (max==0) return -1;		// 'empty'

		return addr;
	}

/**
*	return log nr for last log entry.
*	-1 if no log entry exists
*/
	public static int findLastNr() {

		int addr = findLastLog();
		int nr;

		if (addr<0) {						// first entry
			return -1;
		} else {
			return read32(Flash.LOG_START+addr);
		}
	}

/**
*	return relativ address for log number
*	-1 if nr does not exist
*/
	public static int getAddr(int nr) {

		int i, j;
		int max = 0;
		int addr = 0;

		for (i=0; i<Flash.LOG_LEN; i+=Flash.LOG_SIZE) {
			j = read32(Flash.LOG_START+i);
			if (j==nr) return i;
			Timer.wd();
		}
		return -1;
	}





	private static int read16(int addr) {

		return (JopSys.rdMem(addr)<<8) | JopSys.rdMem(addr+1);
	}
	private static int read32(int addr) {

		return (JopSys.rdMem(addr)<<24) |
			(JopSys.rdMem(addr+1)<<16) |
			(JopSys.rdMem(addr+2)<<8) |
			JopSys.rdMem(addr+3);
	}
}
