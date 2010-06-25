/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2010, Martin Schoeberl (martin@jopdesign.com)

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

/**
 * @author martin
 *
 */
public class DE2Eth {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		System.out.println("Test DE-2 Ehternet");
		
		// control lines at bit position 20-16
		//RESET = 1, CMD = 0, Data_dir = 0, IOR = 0, IOW = 0
		int Mask = 0x00100000;

		Native.wr(0x00100000, Const.DM9000);
		// wait for 50 ms (data sheet says 20 ms)
		long t = System.currentTimeMillis()+50;
		while (System.currentTimeMillis()-t < 0) {
			;
		}
		Native.wr(0x00000000, Const.DM9000);
		t = System.currentTimeMillis()+5;
		while (System.currentTimeMillis()-t < 0) {
			;
		}

		for (int i=123; i<133; ++i) {
			// write to index port
			Native.wr(0x00000 + i, Const.DM9000);
			Native.wr(0x50000 + i, Const.DM9000);
			Native.wr(0x50000 + i, Const.DM9000);
			Native.wr(0x50000 + i, Const.DM9000);
			Native.wr(0x50000 + i, Const.DM9000);
			Native.wr(0x50000 + i, Const.DM9000);
			Native.wr(0x40000 + i, Const.DM9000);
			Native.wr(0x40000 + i, Const.DM9000);
			Native.wr(0x40000 + i, Const.DM9000);
			Native.wr(0x40000 + i, Const.DM9000);
			Native.wr(0x00000 + i, Const.DM9000);
			
			System.out.println("Test "+i);
			// read the index port back
			Native.wr(0x20000, Const.DM9000);
			for (int j=0; j<3; ++j) {
				int val = Native.rd(Const.DM9000);
				// mask out the interrupt bit
				// val &= 0xffff;
				System.out.println(val);			
			}
			Native.wr(0x00000, Const.DM9000);
		}

		System.out.println("Test write/read to TX read pointer address");
		write(0x22, 3);
		System.out.println(read(0x22));
	}

	static void write(int index, int val) {
		// write to index port
		Native.wr(0x00000 + index, Const.DM9000);
		Native.wr(0x50000 + index, Const.DM9000);
		Native.wr(0x40000 + index, Const.DM9000);
		Native.wr(0x00000 + index, Const.DM9000);
		// write to data port
		Native.wr(0x00000 + val, Const.DM9000);
		Native.wr(0x0d000 + val, Const.DM9000);
		Native.wr(0x0c000 + val, Const.DM9000);
		Native.wr(0x00000 + val, Const.DM9000);
	}
	
	static int read(int index) {
		// write to index port
		Native.wr(0x00000 + index, Const.DM9000);
		Native.wr(0x50000 + index, Const.DM9000);
		Native.wr(0x40000 + index, Const.DM9000);
		Native.wr(0x00000 + index, Const.DM9000);
		Native.wr(0xa0000, Const.DM9000);
		int val = Native.rd(Const.DM9000);
		// mask out the interrupt bit
		val &= 0xffff;
		Native.wr(0x00000, Const.DM9000);
		return val;
	}
}