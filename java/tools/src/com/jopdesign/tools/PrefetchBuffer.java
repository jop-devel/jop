
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
