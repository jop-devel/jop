/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2009, Benedikt Huber (benedikt.huber@gmail.com)

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

package com.jopdesign.timing.jop;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import com.jopdesign.timing.InstructionInfo;
import com.jopdesign.timing.MethodCacheTiming;
import com.jopdesign.timing.TimingTable;
import com.jopdesign.timing.jop.MicrocodeAnalysis.MicrocodeVerificationException;

/** Microcode Timing Table
 * Before generating the timing table do not forget to run e.g.
 * {@code make gen_mem -e ASM_SRC=jvm JVM_TYPE=USB @}
 */
public abstract class JOPTimingTable extends TimingTable<JOPInstructionInfo> implements MethodCacheTiming {

	protected MicropathTable micropathTable;
	protected HashSet<Short> bytecodeAccessInstructions = new HashSet<Short>();
	protected int readWaitStates;
	protected int writeWaitStates;
	protected TreeMap<Integer, MicrocodeVerificationException> analysisErrors;

	// custom timings for experiments
	public Map<Integer,Long> customTiming = new HashMap<Integer,Long>();


	protected JOPTimingTable(MicropathTable mpt) {
		this.micropathTable = mpt;
		analysisErrors = new TreeMap<Integer,MicrocodeVerificationException>(mpt.getAnalysisErrors());
		this.readWaitStates = this.writeWaitStates = -1; // not configured

		for(short i = 0; i < 256; i++) {
			if(mpt.hasTiming(i) && mpt.hasBytecodeLoad(i)) {
				this.bytecodeAccessInstructions.add(i);
			}
		}
	}

	/** Override timing for certain instructions (for experiments) */
	public void setCustomTiming(int opcode, long l) {
		this.customTiming.put(opcode, l);
	}

	@Override
	public long getCycles(JOPInstructionInfo instrInfo) {
		
		// see whether a custom timing is configured for this opcode
		if(customTiming.containsKey(instrInfo.getOpcode())) {
			return customTiming.get(instrInfo.getOpcode());
		}
		return getCycles(instrInfo.getOpcode(), instrInfo.hit, instrInfo.wordsLoaded);
	}

	public long getLocalCycles(int opcode) {

		// see whether a custom timing is configured for this opcode
		if(customTiming.containsKey(opcode)) {
			return customTiming.get(opcode);
		}
		return getCycles(opcode, false, 0);
	}
	
	protected abstract long getCycles(int opcode, boolean isHit, int words);

	public MicrocodeVerificationException getAnalysisError(int opcode) {
		return analysisErrors.get(opcode);
	}

	public boolean isImplemented(int opcode) {
		return micropathTable.hasMicrocodeImpl(opcode);
	}

	@Override
	public boolean hasTimingInfo(int opcode) {
		if(customTiming.containsKey(opcode)) return true;
		return micropathTable.hasTiming(opcode);
	}

	public void configureWaitStates(int r, int w) {
		this.readWaitStates = r;
		this.writeWaitStates = w;
	}

	public boolean hasBytecodeLoad(short opcode) {
		for(MicrocodePath path:  micropathTable.getMicroPaths(opcode)) {
			if(path.hasBytecodeLoad()) return true;
		}
		return false;
	}

	protected static boolean hasBytecodeLoad(Vector<MicropathTiming> timings) {
		for(MicropathTiming ptime : timings) {
			if(ptime.hasBytecodeLoad()) return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see com.jopdesign.timing.MethodCacheTiming#getMethodCacheMissPenalty(int, boolean)
	 */
	@Override
	public long getMethodCacheMissPenalty(int words, boolean loadOnInvoke) {
		long maxDiff = Long.MIN_VALUE;
		for(short opcode : bytecodeAccessInstructions) {
			if(loadOnInvoke && InstructionInfo.isReturnOpcode(opcode)) continue;
			long diff = getMethodCacheMissPenalty(words, opcode); 
			maxDiff = Math.max(diff, maxDiff);
		}
		return maxDiff;
	}

	/* (non-Javadoc)
	 * @see com.jopdesign.timing.MethodCacheTiming#getMethodCacheMissPenalty(int, short)
	 */
	@Override
	public long getMethodCacheMissPenalty(int words, short opcode) {

		long onHit = getCycles(opcode,true,words);
		long onMiss = getCycles(opcode,false,words);
		long diff = onMiss - onHit;
		if(diff < 0) throw new AssertionError("Miss is cheaper than hit ? - Not on JOP !");
		return diff;
	}

}
