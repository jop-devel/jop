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

/*
 * Created on 12.04.2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package simhw;

import com.jopdesign.sys.Const;
import java.io.*;

/**
 * @author martin
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class BaseSim {

	public BaseSim() {
		startTime = System.currentTimeMillis();
		fname = new File("flash");
		if (!fname.exists()) {
			try {
				flash = new RandomAccessFile(fname, "rw");
				for (int i=0; i<8*65536; ++i) {
					flash.write(0xff);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			try {
				flash = new RandomAccessFile(fname, "rw");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
	private static final int FLASH_START = 0x80000;
	/**
	*	JOP ticks per ms
	*/
	private static int TICKS = 20000;		// 20 MHz
	private long startTime;
	private File fname;
	private RandomAccessFile flash;
	private int cmd = 0;

	public int rd(int address) {

		switch (address) {
			case Const.IO_CNT:
				return (int) ((System.currentTimeMillis()-startTime)*TICKS);
			case Const.IO_US_CNT:
				return (int) ((System.currentTimeMillis()-startTime)*1000);
			default:
				System.out.println("Native: read address "+address+" not implemented");
		}

		return 0x80;	// INIT_DONE for CS8900
	}

	public void wr(int val, int address) {
		switch (address) {
			case Const.IO_INT_ENA:
				// ignore here
				break;
			case Const.IO_WD:
				System.out.print(val==0 ? "o" : "*");
				break;
			case Const.IO_LED:
				// ignore here
				break;
			default :
				System.out.println("Native: write address "+address+" not implemented");
				break;
		}
	}

	/**
	 * @param addr
	 * @return
	 */
	public int rdMem(int addr) {
		
		if (addr<0) {
			return rd(addr);
		}
		long pos = addr-FLASH_START;
		try {
			flash.seek(pos);
			int val = flash.read();
// System.out.println("read "+pos+ " val "+val);
			return val;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0xff;
	}

	/**
	 * @param val
	 * @param addr
	 */
	public void wrMem(int val, int addr) {

		if (addr<0) {
			wr(val, addr);
		}

/* erase
		Native.wrMem(0xaa, 0x80555);
		Native.wrMem(0x55, 0x802aa);
		Native.wrMem(0x80, 0x80555);
		Native.wrMem(0xaa, 0x80555);
		Native.wrMem(0x55, 0x802aa);
		Native.wrMem(0x30, addr);
*/
/* program
		Native.wrMem(0xaa, 0x80555);
		Native.wrMem(0x55, 0x802aa);
		Native.wrMem(0xa0, 0x80555);
		Native.wrMem(data, addr);
*/

		// TODO write enable and erase sequence
		long pos = addr-FLASH_START;
		if (cmd==0) {
			if (val==0x55 && pos==0x02aa) {
				cmd = 1;
				return;
			}
		} else if (cmd==1) {
			if (val==0x30) {
				// erase
				try {
					flash.seek(pos);
// System.out.println("erase "+pos);
					for (int i=0; i<65536; ++i) {
						flash.write(255);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				cmd = 0;
				return;
			} else if (val==0xa0) {
				cmd = 2;	// allow programming
				return;
			} else {
				cmd = 0;
				return;
			}
		} else if (cmd==2) {
			try {
				flash.seek(pos);
				/*
				int orig = flash.read();
				flash.seek(pos);
				val = ~(~orig & ~val);
				*/
				flash.write(val);
// System.out.println("program "+pos+ " val "+val);
			} catch (IOException e) {
				e.printStackTrace();
			}
			cmd = 0;
		}
		
	}
}
