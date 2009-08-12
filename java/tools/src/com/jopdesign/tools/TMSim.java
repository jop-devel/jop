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
 * @author Peter Hilber
 * 
 */
public class TMSim extends JopSim {

	// write 0 to this address to start and anything else to end transaction
	// TMTODO use M/LSB only
	static final int TM_MAGIC = -10000;
	static final int TM_START_TRANSACTION = 1;
	static final int TM_END_TRANSACTION = 0;
	static final int TM_EARLY_COMMIT = 2;	
	
	
	static final boolean LOG = true;
	static final boolean LOG_ACCESSES = true;

	TMSim(String fn, IOSimMin ioSim, int max) {
		super(fn, ioSim, max);
	}

	// Statistics
	int trCnt;
	int retryCnt;
	int maxRead;
	int maxWrite;
	int maxSum;

	int nestingCnt;
	boolean inEarlyCommit = false;
	int savedPc;

	LinkedHashSet<Integer> readSet = new LinkedHashSet<Integer>();
	LinkedHashMap<Integer, Integer> writeSet = new LinkedHashMap<Integer, Integer>();
	
	LinkedHashSet<Integer> readTags = new LinkedHashSet<Integer>();
	LinkedHashMap<Integer, Integer> writeBuffer = new LinkedHashMap<Integer, Integer>();
	
	static final int readTagsCapacity = 128;
	static final int writeBufferCapacity = 128;

	// TMTODO ? we should earlier abort the transaction as we
	// can read inconsistent data from another committed transaction
	// TMTODO ? we're still having constants and method table loads in
	// the read set, but cache load uses readInstrMem().
	int readMem(int addr, Access type) {
		if (LOG_ACCESSES)
			System.out.println("read: " + addr);

		// TMTODO: use access type
		switch (type) {
		case MTAB:
			break;
		default:
			// Transaction active and not an I/O address
			if (nestingCnt > 0 && addr >= 0) {
				readSet.add(addr);
				
				if (!inEarlyCommit) {
					if (readTags.size() < readTagsCapacity) {
						readTags.add(addr);
					} else {
						earlyCommit();
					}
					
					// TMTODO how is this read after EC?
					if (writeSet.containsKey(addr)) {
						return writeSet.get(addr);
					}
				} else {
					return super.readMem(addr, type);
				}
			}
			break;
		}
		return super.readMem(addr, type);

	}

	void writeMem(int addr, int data, Access type) {
		if (LOG_ACCESSES)
			System.out.println("write: " + addr + " data: " + data);

		if (addr == TM_MAGIC) {
			switch (data) {
			case TM_START_TRANSACTION:
				startTransaction();
				break;
			case TM_END_TRANSACTION:
				endTransaction();
				break;
			case TM_EARLY_COMMIT:
				earlyCommit();
				break;
			}
			return;
		}
		// Transaction active and not an I/O address
		if (nestingCnt > 0 && addr >= 0) {
			writeSet.put(new Integer(addr), new Integer(data));
			
			if (inEarlyCommit) {
				flushWrite(addr, data, type);
			} else {
				writeBuffer.put(addr, data);
			}								
		} else {
			super.writeMem(addr, data, type);
		}
	}
	
	/*
	 * TMTODO access type may be lost
	 */
	void flushWrite(int addr, int data, Access type) {
		super.writeMem(addr, data, Access.INTERN);

		// test for conflict
		for (int i = 0; i < nrCpus; ++i) {
			if (i == io.cpuId)
				continue;
			TMSim otherSim = (TMSim) js[i];
			if (otherSim.abort)
				continue;
			if (otherSim.readSet.contains(addr)) {
				otherSim.abort = true;
				if (LOG)
					System.out.println("Transaction on CPU " + i
							+ " aborted");
			}
		}
	}
	
	void internalCommit() {
		Collection<Integer> keys = writeSet.keySet();
		
		for (Iterator<Integer> iterator = keys.iterator(); iterator.hasNext();) {
			Integer addr = iterator.next();
			int val = writeSet.get(addr);
			// TMTODO ? type lost
			flushWrite(addr, val, Access.INTERN);
		}
		
		// TMTODO
//		writeBuffer.clear();
//		readTags.clear();
	}
	
	void earlyCommit() {
		if (LOG)
			System.out.println("Early Commit TR " + trCnt + " on CPU " + io.cpuId);
		
		// TMTODO early commit is not covered by current simulation
		
		inEarlyCommit = true;
		internalCommit();
	}
	
	void commit() {
		if (LOG)
			System.out.print("Committing TR " + trCnt + " on CPU " + io.cpuId);
		if (LOG)
			System.out.println(" - write set " + writeSet.size() + " read set "
					+ readSet.size());
		
		inEarlyCommit = false;
					
		if (writeSet.size() > maxWrite)
			maxWrite = writeSet.size();
		if (readSet.size() > maxRead)
			maxRead = readSet.size();

		internalCommit();
		
		// find the sum of different addresses
		for (Iterator<Integer> it = readSet.iterator(); it.hasNext();) {
			int addr = it.next();
			writeSet.put(addr, 0);
		}
		if (writeSet.size() > maxSum)
			maxSum = writeSet.size();

		writeSet.clear();
		readSet.clear();
		writeBuffer.clear();
		readTags.clear();
	}

	void retry() {
		abort = false;
		pc = savedPc;
		// also restore the stack for the write
		stack[++sp] = 1; // TMTODO
		stack[++sp] = TM_MAGIC;

		writeSet.clear();
		readSet.clear();
		writeBuffer.clear();
		readTags.clear();
		
		if (LOG)
			System.out.println("Retry TR " + trCnt + " on CPU " + io.cpuId);
		--trCnt;
		++retryCnt;
	}

	/**
	 * The single commit token. 0 means free otherwise it contains the CPU ID +
	 * 1.
	 */
	static int committingCpu;
	boolean abort;

	void startTransaction() {
		++trCnt;
		if (LOG)
			System.out.println("Start TR " + trCnt + " on CPU " + io.cpuId);
		if (nestingCnt == 0) {
			savedPc = pc - 1;
		}
		++nestingCnt;
	}

	void endTransaction() {
		if (LOG)
			System.out.println("End TR " + trCnt + " on CPU " + io.cpuId);
		--nestingCnt;
		if (nestingCnt == 0) {
			// do the commit or retry
			if (abort) {
				retry();
			} else {
				if (committingCpu != 0) {
					// wait for commit token ~by repeating write~
					// TMTODO how would this work in HW?
					--pc;
					stack[++sp] = 0;
					stack[++sp] = TM_MAGIC;
					System.out.println("wait for token");
				} else {
					committingCpu = io.cpuId + 1;
					commit();
					committingCpu = 0;
				}
			}
		}
	}

	void stat() {
		super.stat();
		System.out.println("TM statistics");
		System.out.println("Nr of transactions: " + trCnt);
		System.out.println("Nr of retries: " + retryCnt);
		System.out.println("Max write set " + maxWrite + " max read set "
				+ maxRead + " max sum " + maxSum);
	}

	/**
	 * @param args
	 */
	public static void main(String args[]) {

		IOSimMin io;

		int maxInstr = getArgs(args);

		String ioDevice = System.getProperty("ioclass");
		if(ioDevice != null) {
			System.out.println("Using IO Class: " + ioDevice);
		}
		else {
			System.out.println("Using IO Class: IOSimMin");
		}

		for (int i = 0; i < nrCpus; ++i) {
			// select the IO simulation
			if (ioDevice!=null) {
				try {
					io = (IOSimMin) Class.forName("com.jopdesign.tools."+ioDevice).newInstance();
					
				} catch (Exception e) {
					e.printStackTrace();
					io = new IOSimMin();
				}			
			} else {
				io = new IOSimMin();
			}
			io.setCpuId(i);
			js[i] = new TMSim(args[0], io, maxInstr);
			io.setJopSimRef(js[i]);
		}

		runSimulation();
	}

}
