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


/**
*
*	Simulation of simple cache (as in JOP)
*
*/

package com.jopdesign.tools;

public class TwoBlockCache extends Cache {

	int[] addr = {0, 0};
	int next = 0;
	int currentBlock = 0;

	TwoBlockCache(int[] main, JopSim js) {

		mem = main;
		sim = js;
	}


	int corrPc(int pc) {

		// save block relative pc on invoke
		return pc & (MAX_BC_MASK>>1);
	}

	// TODO use both blocks for methods larger than 512 bytes
	int invoke(int start, int len) {

		if (len*4 > (MAX_BC>>1)) {
			System.out.println("block too large");
			System.exit(0);
		}
		int off = testCache(start, len);
		return off;
	}

	int ret(int start, int len, int pc) {

		int off = testCache(start, len);
		return off+pc;
	}

	int testCache(int start, int len) {
		this.lastHit = true;
		if(flush) {
			flush=false;
			addr[0]=0;
			addr[1]=0;
		} else if (start==addr[0]) {
			currentBlock = 0;
			return 0;
		} else if (start==addr[1]) {
			currentBlock = 1;
			return (MAX_BC>>1);
		}
		this.lastHit=false;
		if (currentBlock==0) {
			next = 1;
		} else {
			next = 0;
		}
		currentBlock = next;
		int off = 0; 
		if (next!=0) {
			off = MAX_BC>>1;
		}
		addr[next] = start;
		loadBc(off, start, len);
		return off;
	}

	void loadBc(int off, int start, int len) {

// high byte of word is first bc!!!
		for (int i=0; i<len; ++i) {
			int val = sim.readInstrMem(start+i);
			for (int j=0; j<4; ++j) {
				bc[off+i*4+(3-j)] = (byte) val;
				val >>>= 8;
			}
		}

		memRead += len*4;
		memTrans++;
	}

	byte bc(int addr) {
		++cacheRead;
		return bc[addr];
	}


}
