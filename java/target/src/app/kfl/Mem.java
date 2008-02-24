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
*	Flash programmer (!copy! for kfl).
*		read and write data to address 0x80000.
*
*/

public class Mem {

	public static final int IO_PORT = 0;
	public static final int IO_STATUS = 1;
	public static final int IO_UART = 2;
	public static final int IO_CNT = 10;

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

		int i = JopSys.rdMem(addr+0x80000) & 0x0ff;
		print_hex(i);
	}
	
	static void readMemInc() {

		int i = JopSys.rdMem(addr+0x80000) & 0x0ff;
		++addr;
		print_hex(i);
	}
	
	static void writeBuf() {

		buf[addr & 0x7f] = data;
		++addr;
	}

	static void amdProgram() {

		JopSys.wrMem(0xaa, 0x80555);
		JopSys.wrMem(0x55, 0x802aa);
		JopSys.wrMem(0xa0, 0x80555);
		JopSys.wrMem(data, addr+0x80000);
	}
	static void amdChipErase() {

		JopSys.wrMem(0xaa, 0x80555);
		JopSys.wrMem(0x55, 0x802aa);
		JopSys.wrMem(0x80, 0x80555);
		JopSys.wrMem(0xaa, 0x80555);
		JopSys.wrMem(0x55, 0x802aa);
		JopSys.wrMem(0x10, 0x80555);
	}
	static void amdSectorErase() {

		JopSys.wrMem(0xaa, 0x80555);
		JopSys.wrMem(0x55, 0x802aa);
		JopSys.wrMem(0x80, 0x80555);
		JopSys.wrMem(0xaa, 0x80555);
		JopSys.wrMem(0x55, 0x802aa);
		JopSys.wrMem(0x30, addr+0x80000);
	}

	static void amdFast() {

		int i, j;

		data = read_char();
		amdProgram();

		i = JopSys.rd(JopSys.IO_CNT)+20000;	// one ms timeout at 20MHz
		do {
			j = JopSys.rdMem(addr+0x80000) & 0x0ff;
		} while (j!=data && (i-JopSys.rd(JopSys.IO_CNT)>=0));
		print_char(j);

		if (j==data) addr++;
	}

	static void programBuf() {
		for (int i=0; i<buf.length; ++i) {
			JopSys.wrMem(buf[i], ((addr+0x80000) & 0xfff80) | i);
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
		while ((JopSys.rd(IO_STATUS)&1)==0) ;
		JopSys.wr(i, IO_UART);
	}

	static int read_char() {
		while ((JopSys.rd(IO_STATUS)&2)==0); 
		return JopSys.rd(IO_UART);
	}
}
