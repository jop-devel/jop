
/**
*
*	Simulation of instruction buffer
*
*/

package com.jopdesign.tools;

public class DirectMapped extends Cache {

	int blockSize;
	int blockCnt;
	int[] tag;
	boolean[] valid;

	DirectMapped(int[] main, JopSim js, int size, int blSize) {

		mem = main;
		sim = js;

		blockSize = blSize;
		blockCnt = size*MAX_BC/blockSize;
		bc = new byte[size*MAX_BC];
		tag = new int[blockCnt];
		valid = new boolean[blockCnt];
		for (int i=0; i<blockCnt; ++i) {
			valid[i] = false;
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
		int tagAddr = addr/blockSize;
		int block = tagAddr%blockCnt;
		if (tag[block]!=tagAddr || !valid[block]) {

			tag[block] = tagAddr;
			valid[block] = true;
			for (int i=0; i<blockSize/4; ++i) {
				int val = sim.readInstrMem(tagAddr*blockSize/4+i);
				for (int j=0; j<4; ++j) {
					bc[block*blockSize+i*4+(3-j)] = (byte) val;
					val >>>= 8;
				}
			}
			memRead += blockSize;
			memTrans++;
		}
		return bc[block*blockSize + addr%blockSize];

	}

	public String toString() {

/*
		return super.toString()+" ("+(bc.length/1024)+" KB "+
			(blockSize<10 ? " " : "")+blockSize+ " bytes)";
		return "Direct mapped "+blockSize+ " bytes & "+
			(bc.length/1024)+" KB";
*/
		return "Direct mapped "+(bc.length/1024)+" KB & "+blockSize;
	}

}
