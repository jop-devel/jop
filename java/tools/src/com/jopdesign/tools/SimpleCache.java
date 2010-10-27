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

public class SimpleCache extends Cache {


	SimpleCache(int[] main, JopSim js) {

		mem = main;
		sim = js;
	}


	int ret(int start, int len, int pc) {
		this.lastHit = false;
		loadBc(start, len);
		return pc;
	}

	int corrPc(int pc) {

		return pc;
	}

	int invoke(int start, int len) {
		this.lastHit = false;
		loadBc(start, len);
		return 0;
	}

	void loadBc(int start, int len) {

// high byte of word is first bc!!!
		for (int i=0; i<len; ++i) {
			int val = sim.readInstrMem(start+i);
			for (int j=0; j<4; ++j) {
				bc[i*4+(3-j)] = (byte) val;
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
