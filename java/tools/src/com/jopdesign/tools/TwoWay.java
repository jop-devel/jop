
/**
*
*	Simulation of instruction buffer
*
*/

package com.jopdesign.tools;

public class TwoWay extends Cache {

	final static int BLOCK_SIZE = 16;
	final static int BLOCK_CNT = MAX_BC/BLOCK_SIZE;
	int[] tag;
	boolean[] valid;
	boolean[] lru;

	TwoWay(int[] main, JopSim js) {

		mem = main;
		sim = js;

		tag = new int[BLOCK_CNT];
		valid = new boolean[BLOCK_CNT];
		lru = new boolean[BLOCK_CNT];
		for (int i=0; i<BLOCK_CNT; ++i) {
			valid[i] = false;
			lru[i] = false;
		}
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

		++cacheRead;
		int tagAddr = addr/BLOCK_SIZE;
		int block = tagAddr%BLOCK_CNT;
		block &= 0xfffffe;
		if (tag[block]==tagAddr && valid[block]) {
			;
		} else if (tag[block+1]==tagAddr && valid[block+1]) {
			block++;
		} else {

			if (lru[block]) {
				block++;
			}
			tag[block] = tagAddr;
			valid[block] = true;
			for (int i=0; i<BLOCK_SIZE/4; ++i) {
				int val = sim.readInstrMem(tagAddr*BLOCK_SIZE/4+i);
				for (int j=0; j<4; ++j) {
					bc[block*BLOCK_SIZE+i*4+(3-j)] = (byte) val;
					val >>>= 8;
				}
			}
			memRead += BLOCK_SIZE;
			memTrans++;
		}
		lru[block] = true;
		lru[block ^ 0x01] = false;
		return bc[block*BLOCK_SIZE + addr%BLOCK_SIZE];

	}


}
