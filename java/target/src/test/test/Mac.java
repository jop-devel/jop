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

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

public class Mac {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		int i;
		mac(2000, 1000);
		for (i=0; i<4; ++i) ; // wait a little bit
		mac(2000, 2000);
		for (i=0; i<4; ++i) ; // wait a little bit
		mac(3000, 3000);
		for (i=0; i<4; ++i) ; // wait a little bit
		System.out.println("result "+Native.rdMem(Const.IO_MAC_A)+
				" "+Native.rdMem(Const.IO_MAC_B));
		
		mac(-5, 5);
		for (i=0; i<4; ++i) ; // wait a little bit
		mac(5, -5);
		for (i=0; i<4; ++i) ; // wait a little bit
		System.out.println("result "+Native.rdMem(Const.IO_MAC_A)+
				" "+Native.rdMem(Const.IO_MAC_B));
	}

	static void mac(int a, int b) {
		
		Native.wrMem(a, Const.IO_MAC_A);
		Native.wrMem(b, Const.IO_MAC_B);
	}
	
}
