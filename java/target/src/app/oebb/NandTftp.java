/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2009, Martin Schoeberl (martin@jopdesign.com)

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


/**
 * 
 */
package oebb;

import ejip.*;
import util.Amd;
import util.SingleFileFS;
import util.Timer;

/**
 * @author Martin Schoeberl (martin@jopdesign.com)
 *
 */
public class NandTftp extends BgTftp {

	private final int IDLE = -1;
	private final int WRITE_JOP = 4;
	private final int WRITE_JAVA = 0;
	private final int WRITE_STR = 2;
	private final int WRITE_BGID = 3;

	SingleFileFS fs;
	int state;
	int data[];
	
	NandTftp(Ejip ejipRef, SingleFileFS fs) {
		super(ejipRef);
		this.fs = fs;
		state = IDLE;
		data = new int[128];
		System.out.println("Erase NAND if not empty");
		fs.eraseStart();
	}

	/**
	*	Save data for future programming.
	*/
	protected void write(int[] buf, int block) {

		int i, j;
		int base;

		block--;				// data blocks start with 1
		i = fn>>8;
		if (i!='f') {
			return;
		}
		if (state==IDLE) {
			fs.openWrite();
			state = ((fn&0xff)-'0');
		}
		Timer.wd();				// toggle for each block?
System.out.print("Save to NAND "); System.out.println(block);
		// base += ((fn&0xff)-'0')<<16;	// 64 KB sector
		for (j=0; j<128; ++j) {
			data[j] = buf[Udp.DATA+1+j]; 
		}
		fs.writePage(data);
	}

	/**
	*	Program the Flash if all sectors have been received.
	*/
	protected void eof(int cnt) {

System.out.print("Check "); System.out.print(cnt); System.out.println(" blocks");

		if (cnt==MAX_BLOCKS+1) {
			// we have received a full sector, something will follow
			// take care to have a non full sector as end marker
			return;
		}

		int i, j, w;
		int base;

		i = fn>>8;
		if (i!='f') return;		// filename not valid
		
		
		fs.writeLastPage(data, 0);
		System.out.println("NAND read file");
		fs.openRead();
		program: for (base = state<<16;;) {
			System.out.print("Erase sector "); System.out.println(base);
			synchronized (sector) {
				Amd.erase(base);
System.out.print("Program ");
System.out.println(base);
				for (i=0; i<MAX_BLOCKS; ++i) {
					Timer.wd();				// toggle for each block?
System.out.print(" blk "); System.out.print(i);
					int size = fs.readPage(data);
					for (j=0; j<128; ++j) {
						w = data[j];
						Amd.program(base, w>>>24);
						Amd.program(base+1, w>>>16);
						Amd.program(base+2, w>>>8);
						Amd.program(base+3, w);
						base += 4;
					}
					if (size<512) {
						break program;
					}
				}
			}
		}
		System.out.println("Erase file from NAND Flash");
		fs.erase();
		state=IDLE;
	}

}
