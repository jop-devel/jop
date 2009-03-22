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

import util.NandTest;

import com.jopdesign.sys.Native;

/**
*	Test.java ... the name implies it
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

		NandTest.test();

	}

}
