/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2008, Martin Schoeberl (martin@jopdesign.com)

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
package com.jopdesign.tools;


/**
 * Extension of JopSim to simulation real-time transactional memory (RTTM)
 * 
 * @author Martin Schoeberl
 *
 */
public class TMSim extends JopSim {

	TMSim(String fn, IOSimMin ioSim, int max) {
		super(fn, ioSim, max);
	}
	
	int trCnt;
	int nestingCnt;
	int savedPc;

	int readMem(int addr) {

		return super.readMem(addr);

	}

	void writeMem(int addr, int data) {

		if (addr==-10000) {
			if (data!=0) {
				startTransaction();
			} else {
				endTransaction();
			}
			return;
		}
		super.writeMem(addr, data);

	}
	
	int simAbort = 3;
	
	void startTransaction() {
		System.out.println("start transaction");
		++trCnt;
		if (nestingCnt==0) {
			savedPc = pc-1;
		}
		++nestingCnt;
	}

	void endTransaction() {
		System.out.println("end transaction");
		--nestingCnt;
		if (nestingCnt==0) {
			// do the commit or retry
			if (simAbort>0) {
				--simAbort;
				pc = savedPc;
			}
		}
	}
	
	void stat() {
		super.stat();
		System.out.println("TM statistics");
		System.out.println("Nr of transactions: "+trCnt);
	}

	/**
	 * @param args
	 */
	public static void main(String args[]) {

		IOSimMin io;

		int maxInstr = getArgs(args);

		for (int i = 0; i < nrCpus; ++i) {
			io = new IOSimMin();
			io.setCpuId(i);
			js[i] = new TMSim(args[0], io, maxInstr);
			io.setJopSimRef(js[i]);
		}

		runSimulation();
	}

}
