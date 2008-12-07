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

import java.util.*;


/**
 * Extension of JopSim to simulation real-time transactional memory (RTTM)
 * 
 * @author Martin Schoeberl
 *
 */
public class TMSim extends JopSim {
	
	final static int MAGIC = -10000;
	final static boolean LOG = false;

	TMSim(String fn, IOSimMin ioSim, int max) {
		super(fn, ioSim, max);
	}
	
	int trCnt;
	int retryCnt;
	int maxRead;
	int maxWrite;
	
	int nestingCnt;
	int savedPc;
	
	LinkedHashSet<Integer> readSet = new LinkedHashSet<Integer>();
	LinkedHashMap<Integer, Integer> writeSet = new LinkedHashMap<Integer, Integer>();

	// TODO: we should earlier abort the transaction as we
	// can read inconsistent data from another commited transaction
	int readMem(int addr) {

		// Transaction active and not an I/O address
		if (nestingCnt>0 && addr>=0) {
			Integer addrInt = new Integer(addr);
			readSet.add(addrInt);
			if (writeSet.containsKey(addrInt)) {
				return writeSet.get(addrInt).intValue();
			}
		}
		return super.readMem(addr);

	}

	void writeMem(int addr, int data) {

		if (addr==MAGIC) {
			if (data!=0) {
				startTransaction();
			} else {
				endTransaction();
			}
			return;
		}
		// Transaction active and not an I/O address
		if (nestingCnt>0 && addr>=0) {
			writeSet.put(new Integer(addr), new Integer(data));
		} else {
			super.writeMem(addr, data);			
		}
	}
	
	void commit() {
		if (LOG) System.out.print("Commiting TR "+trCnt+" on CPU "+io.cpuId);
		if (LOG) System.out.println(" - write set "+writeSet.size()+" read set "+readSet.size());
		if (writeSet.size()>maxWrite) maxWrite = writeSet.size();
		if (readSet.size()>maxRead) maxRead = readSet.size();
		
		Collection<Integer> keys = writeSet.keySet();
		for (Iterator<Integer> iterator = keys.iterator(); iterator.hasNext();) {
			Integer addr = iterator.next();
			int thisAddr = addr;
			int val = writeSet.get(addr);
			super.writeMem(thisAddr, val);
			
			// test for conflict
			for (int i=0; i<nrCpus; ++i) {
				if (i==io.cpuId) continue;
				TMSim otherSim = (TMSim) js[i];
				if (otherSim.abort) continue;
				for (Iterator<Integer> other = otherSim.readSet.iterator(); other.hasNext();) {
					int otherAddr = other.next();
					if (otherAddr==thisAddr) {
						otherSim.abort=true;
						if (LOG) System.out.println("Transaction on CPU "+i+" aborted");
						break;
					}
				}
			}
		}
		writeSet.clear();
		readSet.clear();
	}
	
	void retry() {
		abort = false;
		pc = savedPc;
		// also restore the stack for the write
		stack[++sp] = 1;
		stack[++sp] = MAGIC;
		writeSet.clear();
		readSet.clear();
		if (LOG) System.out.println("Retry TR "+trCnt+" on CPU "+io.cpuId);
		--trCnt;
		++retryCnt;
	}
	
	/**
	 * The single commit token. 0 means free otherwise it
	 * contains the CPU ID + 1.
	 */
	static int commitingCpu;
	boolean abort;
	
	void startTransaction() {
		++trCnt;
		if (LOG) System.out.println("Start TR "+trCnt+" on CPU "+io.cpuId);
		if (nestingCnt==0) {
			savedPc = pc-1;	
		}
		++nestingCnt;
	}

	void endTransaction() {
		if (LOG) System.out.println("End TR "+trCnt+" on CPU "+io.cpuId);
		--nestingCnt;
		if (nestingCnt==0) {
			// do the commit or retry
			if (abort) {
				retry();
			} else {
				if (commitingCpu!=0) {
					// wait for commit token
					--pc;
					stack[++sp] = 0;
					stack[++sp] = MAGIC;
					System.out.println("wait for token");
				} else {
					commitingCpu=io.cpuId+1;
					commit();
					commitingCpu=0;
				}
			}
		}
	}
	
	void stat() {
		super.stat();
		System.out.println("TM statistics");
		System.out.println("Nr of transactions: "+trCnt);
		System.out.println("Nr of retries: "+retryCnt);
		System.out.println("Max write set "+maxWrite+" max read set "+maxRead);
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
