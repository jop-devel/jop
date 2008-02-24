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

package test;

import com.jopdesign.sys.Native;

/**
*	Test.java ... the name implys it
*/

public class Flash {

	public static void main(String[] args) {

		int i, j, k;


		/* Read ID from Flash */
		Native.wrMem(0xaa, 0x80555);
		Native.wrMem(0x55, 0x802aa);
		Native.wrMem(0x90, 0x80555);
		i = Native.rdMem(0x80000);
		Native.wrMem(0xaa, 0x80555);
		Native.wrMem(0x55, 0x802aa);
		Native.wrMem(0x90, 0x80555);
		j = Native.rdMem(0x80001);
		System.out.print("Flash ");
		System.out.print(i);
		System.out.print(" ");
		System.out.print(j);
		System.out.print(" ");
		if (i==0x01 & j==0x4f) {
			System.out.println(" AMD Am29LV040B");
		} else {
			System.out.println(" error reading Flash");
		}

/* read ID  and status from NAND */
//		   Native.wrMem(0xff, 0x100001);
		Native.wrMem(0x90, 0x100001);
		Native.wrMem(0x00, 0x100002);
//
//			should read 0x98 and 0x73
//
		i = Native.rdMem(0x100000);	// Manufacturer
		j = Native.rdMem(0x100000);	// Size
		System.out.print("NAND ");
		System.out.print(i);
		System.out.print(" ");
		System.out.print(j);
		System.out.print(" ");
		if (i==0x198) {
			System.out.print("Toshiba ");
		} else if (i==0x120) {
			System.out.print("ST ");
		} else {
			System.out.println("Unknown manufacturer");
		}
			
		if (j==0x173) {
			System.out.println("16 MB");
		} else if (j==0x175) {
			System.out.println("32 MB");
		} else if (j==0x176) {
			System.out.println("64 MB");
		} else if (j==0x179) {
			System.out.println("128 MB");
		} else {
			System.out.println("error reading NAND");
		}

//
//			read status, should be 0xc0
//
		Native.wrMem(0x70, 0x100001);
		i = Native.rdMem(0x100000)&0x1c1;
		j = Native.rdMem(0x100000)&0x1c1;
		System.out.print(i);
		System.out.print(" ");
		System.out.print(j);
		System.out.print(" ");
		if (i==0x1c0 && j==0x1c0) {
			System.out.println("status OK");
		} else {
			System.out.println("error reading NAND status");
		}

	}

}
