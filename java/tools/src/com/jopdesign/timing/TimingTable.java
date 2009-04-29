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

package com.jopdesign.timing;

import java.util.List;

/** 
 * Timing information for a Java processor
 * 
 * Currently we support the following aspects:
 * <ul>
 * <li/> Cycles needed to execute an instruction (InstrParam as type parameter))
 * <li/> WCET of basic blocks
 * <li/> Method Caches
 * <li/> Java Implemented Bytecodes
 * </ul>
 * 
 * We currently only support architectures where there is a worst case for the
 * ProcessorState.
 */
public abstract class TimingTable<InstrParam> {
	
	/**
	 * Class for encapsulating a instruction to be analyzed
	 */
	public static class InstructionInfo<InstrParam> {
		int opcode;
		InstrParam procState = null;
		public InstructionInfo(int opcode) { 
			this.opcode = opcode;
		}
		public InstructionInfo(int opcode, InstrParam ps) {
			this(opcode);
			this.procState = ps;
		}
		public boolean hasStateInfo() {
			return procState != null;
		}
	}
	
	/**
	 * return true if timing info is available for the given instruction
	 */
	public abstract boolean hasTimingInfo(int opcode);
	
	/** 
	 * Get the WCET for an instruction.
	 * The default implementation delegates to {@code getCycles(opcode,null)}.
	 * */
	public abstract long getCycles(int opcode);

	/** 
	 * Get the WCET for an instruction.
	 */
	public abstract long getCycles(int opcode, InstrParam ps);
	

	/*                      Get WCETs of Basic Block
	 * ----------------------------------------------------------------------
	 */

	/** 
	 * Get the timing info for a basic block.<br/>
	 * The default implementation simply sums the WCETs of the instructions.
	 */
	public long getCycles(List<InstructionInfo<InstrParam>> opcodes) {
		long wcet = 0;
		for(InstructionInfo<InstrParam> instr : opcodes) {
			wcet += getCycles(instr.opcode, instr.procState);
		}
		return wcet;
	}

	 /*             Aspect: Method Cache
	  ---------------------------------------------------------------------
	 */
	public long methodCacheAccessCycles(boolean hit, int words) {
		throw new AssertionError("getMethodCacheAccessCycles: architecture has not method cache");
	}
	public long methodCacheHiddenAccessCycles(boolean onInvoke) {
		throw new AssertionError("methodCacheHiddenAccessCycles: architecture has not method cache");		
	}
	public long methodCacheHiddenAccessCycles(int opcode) {
		throw new AssertionError("methodCacheHiddenAccessCycles: architecture has not method cache");		
	}
	
	/*             Aspect: Java Implemented Bytecodes
	 * ----------------------------------------------------------------------
	 */
		
	/**
	 * return the number of 'dispatch cycles' for a Java implemented bytecode.
	 * Implementations should throw a runtime error if the opcode is not implemented
	 * in Java.
	 */
	public long javaImplBcDispatchCycles(int opcode, InstrParam st) {
		throw new AssertionError("The platform " + this.getClass() 
				               + " does not support Java implemented bytecodes");
	}
}
