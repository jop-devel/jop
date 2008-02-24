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

package util;

//
//	Flash programmer.
//		read and write data to address 0x80000.
//
//

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

public class Mem {


	private static int[] buf;
	private static int cmd, ch, val, addr, data;

	public static void main( String s[] ) {

		buf = new int[128];
		addr = data = 0;
		cmd = ' ';

		for (;;) {
			if (cmd=='a' || cmd=='d') {
				val = read_val();
				if (cmd=='a') addr = val;
				if (cmd=='d') data = val;
				cmd = ch;							// one character ahaed

			} else {

				if (cmd=='r') {
	
					readMem();

				} else if (cmd=='i') {
	
					readMemInc();
	
				} else if (cmd=='w') {
	
					writeBuf();
	
				} else if (cmd=='p') {				// Atmel 29 128 byte program
	
					programBuf();

				} else if (cmd=='m') {				// AMD program
	
					amdProgram();
	
				} else if (cmd=='x') {				// AMD chip erase
	
					amdChipErase();
	
				} else if (cmd=='s') {				// AMD sector erase
	
					amdSectorErase();
	
				} else if (cmd=='!') {	// 'speed' programming

					amdFast();

				}
				cmd = read_char();
				print_char(cmd);
			}
		}

	}

	static int read_val() {

		int val = 0;
		for (;;) {
			ch = read_char();
			print_char(ch);
			if (ch>='0' && ch <='9') {
				val <<= 4;
				val += ch-'0';
			} else if (ch>='a' && ch <='f') {
				val <<= 4;
				val += ch-'a'+10;
			} else {
				break;
			}
		}
		return val;
	}

	static void readMem() {

		int i = Native.rdMem(addr+0x80000) & 0x0ff;
		print_hex(i);
	}
	
	static void readMemInc() {

		int i = Native.rdMem(addr+0x80000) & 0x0ff;
		++addr;
		print_hex(i);
	}
	
	static void writeBuf() {

		buf[addr & 0x7f] = data;
		++addr;
	}

	static void amdProgram() {

		Native.wrMem(0xaa, 0x80555);
		Native.wrMem(0x55, 0x802aa);
		Native.wrMem(0xa0, 0x80555);
		Native.wrMem(data, addr+0x80000);
	}
	static void amdChipErase() {

		Native.wrMem(0xaa, 0x80555);
		Native.wrMem(0x55, 0x802aa);
		Native.wrMem(0x80, 0x80555);
		Native.wrMem(0xaa, 0x80555);
		Native.wrMem(0x55, 0x802aa);
		Native.wrMem(0x10, 0x80555);
	}
	static void amdSectorErase() {

		Native.wrMem(0xaa, 0x80555);
		Native.wrMem(0x55, 0x802aa);
		Native.wrMem(0x80, 0x80555);
		Native.wrMem(0xaa, 0x80555);
		Native.wrMem(0x55, 0x802aa);
		Native.wrMem(0x30, addr+0x80000);
	}

	static void amdFast() {

		int i, j;

		data = read_char();
		amdProgram();

		i = Native.rd(Const.IO_US_CNT)+1000;	// one ms timeout
		do {
			j = Native.rdMem(addr+0x80000) & 0x0ff;
		} while (j!=data && (i-Native.rd(Const.IO_US_CNT)>=0));
		print_char(j);

		if (j==data) addr++;
	}

	static void programBuf() {
		for (int i=0; i<buf.length; ++i) {
			Native.wrMem(buf[i], ((addr+0x80000) & 0xfff80) | i);
			buf[i] = -1;
		}
	}

	static void print_hex(int i) {

		int j;

		for (j=0;i>=16;++j) i-= 16;
		print_char(j<10?j+'0':j-10+'a');
		print_char(i<10?i+'0':i-10+'a');
	}

	static void print_char(int i) {
		while ((Native.rd(Const.IO_STATUS)&1)==0) ;
		Native.wr(i, Const.IO_UART);
	}

/**
*	TODO: there is a bug with the uart!
*		without usleep it does not work!
*/
	static int read_char() {
		while ((Native.rd(Const.IO_STATUS)&2)==0) Timer.usleep(1);
		return Native.rd(Const.IO_UART);
	}
}
