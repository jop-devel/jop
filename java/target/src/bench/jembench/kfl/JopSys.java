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

package jembench.kfl;

/**
*	system functions and constants.
*	Version for BB KFL (clock frequ)!
*/

public class JopSys {

	static int byteAvail;
	static int triacVal;
	static int impCnt;
	/**
	 * Simulate BB IO for benchmark.
	 */
	static int rd(int addr) {
		if (addr==IO_CNT) {
			return KflNative.rd(Const.IO_CNT);
		} else if (addr==Msg.IO_STATUS && byteAvail>0) {
			// simulate a byte in rs485 uart
			return 32;
		} else if (addr==Msg.IO_RS485) {
			if (byteAvail>0) {
				--byteAvail;
				return buf[byteAvail];
			}
		} else if (addr==BBSys.IO_TRIAC) {
			// all Us are ok;
			return BBSys.MSK_U;
		} else if (addr==BBSys.IO_IADC) {
			if (triacVal!=0) {
				return Triac.MIN_STROM + 
					(Triac.MIN_STROM<<10) + 
					(Triac.MIN_STROM<<20); 
			} else {
				return 0;
			}
		} else if (addr==BBSys.IO_SENSOR) {
			int sens = 0;
			if (triacVal!=0) {
				if ((cnt&0x03)==1) {
					sens |= BBSys.BIT_SENSI;
					if (Mast.state==BBSys.MS_UP) {
						++impCnt;
					} else if (Mast.state==BBSys.MS_DOWN) {
						--impCnt;
					}
				}
			}
			if (impCnt<4) sens |= BBSys.BIT_SENSU;
			if (impCnt>20) sens |= BBSys.BIT_SENSO;
			return sens;
		}
		return 0;
	}
	static void wr(int val, int addr){
		if (addr==BBSys.IO_WD) {
			KflNative.wr(val, Const.IO_WD);
		} else if (addr==BBSys.IO_TRIAC) {
			triacVal = val;
			KflNative.wr(val, Const.IO_LED);
		}
	}
	public static final int IO_CNT = 0; // is 0 in actual hardware

	public static final int INTERVAL = 73728/2;	// five ms
	public static final int MS = 7373;

	static int min, max;
	static boolean first;
	static int simState;
	static int[] buf;

	/**
	 * Initialization for benchmarking KFL Mast loop.
	 */
	public static void initBench() {
		
		min = 1000000000;
		max = 0;
		first = true;
		simState = 0;
		impCnt = 0;
		buf = new int[4];
		byteAvail = 0;
/*
		util.Dbg.initSerWait();
		util.Dbg.wr("Benchmark started\n");
*/
	}
	
	static int cnt;
	static int timestamp;
	static int startTime;
	/**
	 * Called once per loop instead of waitForNextInterval.
	 */
	public static void benchLoop() {
		
/*
		if (first) {
			first = false;
			timestamp = Native.rd(Const.IO_CNT);
			startTime = timestamp;
		} else {
			int time = Native.rd(Const.IO_CNT);
			int diff = time-timestamp;
			if (diff > max) max = diff;
			if (diff < min) min = diff;
			timestamp = time;
		}
*/
		++cnt;
		if ((cnt&0x07) == 0) {
			masterCmd();
		} else if ((cnt&0x03)==3) {
			masterPoll();
		}

//	Don't wait for the next interval.
//	We want to measure performance of workload in
//	execution time.
//
//		Timer.waitForNextInterval();
/*

		if (cnt==10000) {
			endBench();
		}
*/
	}
	
/*
	static void endBench() {
		startTime = Native.rd(Const.IO_CNT)-startTime;
		util.Dbg.wr("\nBenchmark finished in ");
		util.Dbg.intVal(startTime);
		util.Dbg.wr("cycles\n");
		dbgOut();
//		for (;;);
	}
*/
	
	// invoked every 8 cycles
	
	static void masterPoll() {
		simMsg(BBSys.CMD_STATUS, 0);
	}
	
	static int waitTime;
	
	static void masterCmd() {
		
		if (simState==0) {
			simMsg(BBSys.CMD_SET_STATE, BBSys.MS_RDY);
			simState = 1;
		} else if (simState==1) {
			simMsg(BBSys.CMD_SET_MAXCNT, 1000);
			simState = 2;
			waitTime = 2;
		} else if (simState==2) {
			if (Mast.state==BBSys.MS_RDY) {
				if (waitTime==0) {
					simMsg(BBSys.MS_UP, 0);
// util.Dbg.wr("up ");
					waitTime = 4;	// minimum 20 cycles (4*8)
					simState = 3;		
				} else {
					--waitTime;
				}
			}
		} else if (simState==3) {
			if (Mast.state==BBSys.MS_RDY) {
				if (waitTime==0) {
					simMsg(BBSys.MS_DOWN, 0);
// util.Dbg.wr("down ");
					waitTime = 4;	// minimum 20 cycles (4*8)
					simState = 2;		
				} else {
					--waitTime;
				}
			}
		}
	}

	static void dbgOut() {

/*
		util.Dbg.intVal(Mast.state);
		util.Dbg.intVal(Mast.lastErr);
		util.Dbg.intVal(cnt);
		util.Dbg.intVal(min);
		util.Dbg.intVal(max);
		util.Dbg.wr('\n');
*/
		
	}

	private static final int ADDR_MSK = 0x7c0000;
	private static final int CMD_MSK  = 0x03f000;
	private static final int DATA_MSK = 0x000fff;
	
	static void simMsg(int cmd, int data) {

		int val;
		int i;

		int addr = 1;
		addr <<= 18;
		cmd <<= 12;
		data &= DATA_MSK;
		val = 0x800000 | addr | cmd | data;
		val <<= 8;
		val |= Msg.crc(val);		// append crc

		for (i=0; i<4; ++i) {	// @WCA loop=4
			buf[i] = val & 0x0ff;
			val >>>= 8;
		}
		byteAvail = 4;

	}
	
}
