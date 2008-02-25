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
*	Simulation of instruction buffer
*
*/

package com.jopdesign.tools;

public class PrefetchBuffer extends Cache {

	int buf1, buf2;
	int bufAddr1 = -1;
	int bufAddr2 = -1;

	PrefetchBuffer(int[] main, JopSim js) {

		mem = main;
		sim = js;
	}


	int ret(int start, int len, int pc) {

		return pc;
	}

	int corrPc(int pc) {

		return pc;
	}

	int invoke(int start, int len) {

		return start*4;
	}

	byte bc(int addr) {

		int word = addr>>2;
		++cacheRead;
		if (bufAddr1 != word) {
			// perhaps in second buffer?
			if (bufAddr2==word) {
				buf1 = buf2;
				bufAddr1 = bufAddr2;
			} else {
				buf1 = sim.readInstrMem(word);
				memRead += 4;
				memTrans++;
				bufAddr1 = word;
			}
		}
		if ((addr&0x03)>1 && bufAddr2!=word+1) {
			// read another word for the operands
			buf2 = sim.readInstrMem(word+1);
			memRead += 4;
			memTrans++;
			bufAddr2 = word+1;
		}
		return (byte) (buf1>>>(8*(3-(addr&0x03))));
	}


}
