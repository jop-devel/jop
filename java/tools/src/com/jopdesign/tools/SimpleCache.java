
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

		loadBc(start, len);
		return pc;
	}

	int corrPc(int pc) {

		return pc;
	}

	int invoke(int start, int len) {

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
