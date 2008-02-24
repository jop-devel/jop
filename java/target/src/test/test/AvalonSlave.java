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

import com.jopdesign.sys.*;

public class AvalonSlave {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		final int AVALON_TEST_SLAVE_0_BASE = 0x00800000/4;
		
		System.out.println("Avalon slave test");
		System.out.println("cnt="+Native.rd(AVALON_TEST_SLAVE_0_BASE));
		System.out.println("xyz="+Native.rd(AVALON_TEST_SLAVE_0_BASE+1));
		Native.wr(1234, AVALON_TEST_SLAVE_0_BASE+1);
		System.out.println("cnt="+Native.rd(AVALON_TEST_SLAVE_0_BASE));
		System.out.println("xyz="+Native.rd(AVALON_TEST_SLAVE_0_BASE+1));
	}

}
