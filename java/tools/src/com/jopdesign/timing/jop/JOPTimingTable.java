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

import java.util.HashSet;
import java.util.TreeMap;
import java.util.Vector;

import com.jopdesign.timing.InstructionInfo;
import com.jopdesign.timing.TimingTable;
import com.jopdesign.timing.jop.MicrocodeAnalysis.MicrocodeVerificationException;

/** Microcode Timing Table
 * Before generating the timing table do not forget to run e.g.
 * {@code make gen_mem -e ASM_SRC=jvm JVM_TYPE=USB @}
 */
public abstract class JOPTimingTable extends TimingTable<JOPInstructionInfo> {

	protected MicropathTable micropathTable;
	protected HashSet<Integer> bytecodeAccessInstructions = new HashSet<Integer>();
	protected int readWaitStates;
	protected int writeWaitStates;
	protected TreeMap<Integer, MicrocodeVerificationException> analysisErrors;

	@Override
	public long getCycles(JOPInstructionInfo instrInfo) {
		return getCycles(instrInfo.getOpcode(), instrInfo.hit, instrInfo.wordsLoaded);
	}

	public long getLocalCycles(int opcode) {
		return getCycles(opcode, false, 0);
	}
	public abstract long getCycles(int opcode, boolean isHit, int words);

	protected JOPTimingTable(MicropathTable mpt) {
		this.micropathTable = mpt;
		analysisErrors = new TreeMap<Integer,MicrocodeVerificationException>(mpt.getAnalysisErrors());
		this.readWaitStates = this.writeWaitStates = -1; // not configured

		for(int i = 0; i < 256; i++) {
			if(mpt.hasTiming(i) && mpt.hasBytecodeLoad(i)) {
				this.bytecodeAccessInstructions.add(i);
			}
		}
	}

	public MicrocodeVerificationException getAnalysisError(int opcode) {
		return analysisErrors.get(opcode);
	}

	public boolean isImplemented(int opcode) {
		return micropathTable.hasMicrocodeImpl(opcode);
	}


	@Override
	public boolean hasTimingInfo(int opcode) {
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

	/** FIXME: This is not the optimal way to compute cache penalties.
	 *  It should be sound, though.
	 */
	public long getMethodCacheMissPenalty(int words, boolean loadOnInvoke) {
		long maxDiff = Long.MIN_VALUE;
		for(int opcode : bytecodeAccessInstructions) {
			if(loadOnInvoke && InstructionInfo.isReturnOpcode(opcode)) continue;
			long onHit = getCycles(opcode,true,words);
			long onMiss = getCycles(opcode,false,words);
			long diff = onMiss - onHit;
			if(diff < 0) throw new AssertionError("Miss is cheaper than hit ? - Not on JOP !");
			maxDiff = Math.max(diff, maxDiff);
		}
		return maxDiff;
	}

}
