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
 * <li/> Local State (pipeline, arbiter)
 * <li/> Java Implemented Bytecodes
 * <li/> Instruction Cache
 * </ul>
 * 
 * We will need to extend this interface in order to support other
 * hardware aspects.
 * 
 * The type parameter LocalState will be used for timings which depend on
 * a local state (CMP, Pipelines), but for which (in contrast to method cache
 * access cycles), a local worst case can be assumed. 
 */
public abstract class TimingTable<LocalState> {
	
	/**
	 * Class for encapsulating a instruction to be analyzed
	 */
	class Instruction {
		public int opcode;
		public long cacheAccessCycles;
		public Instruction(int opcode, long cac) { 
			this.opcode = opcode;
			this.cacheAccessCycles = cac;
		}
	}
	
	/** Get the WCET for an instruction.
	 *  The default implementation delegates to {@code getCycles(opcode,null)}.
	 * */
	public long getCycles(int opcode, Long cacheAccessCycles) {
		return getCycles(opcode,cacheAccessCycles, null);
	}
	
	/** Get the timing info for a 'locally dependent' instruction.
	 *  For example, pipeline or arbiter state are supplied.
	 *  If {@code st} is {@code null}, the worst case scenario shall
	 *  be assumed.  
	 */
	public abstract long getCycles(int opcode, Long cacheAccessCycles, LocalState st);

	/*                      Get WCETs of Basic Block
	 * ----------------------------------------------------------------------
	 */

	/** 
	 * Get the timing info for a basic block.<br/>
	 * The default implementation simply sums the WCETs of the instructions.
	 */
	public long getCycles(List<Instruction> opcodes) {
		long wcet = 0;
		for(Instruction instr : opcodes) {
			wcet += getCycles(instr.opcode, instr.cacheAccessCycles);
		}
		return wcet;
	}
	/** 
	 * Get the timing info for a basic block.<br/>
	 * The default implementation simply sums the WCETs of the instructions.
	 */
	public long getCycles(List<Instruction> opcodes, LocalState st) {
		return getCycles(opcodes);
	}
	
	
	/*             Aspect: Java Implemented Bytecodes
	 * ----------------------------------------------------------------------
	 */
	
	/**
	 * query whether the given opcode is implemented in Java.
	 */
	public boolean isImplementedInJava(int opcode) {
		return false;
	}
	
	/**
	 * return the number of 'dispatch cycles' for a Java implemented bytecode.
	 * Implementations should throw a runtime error if the opcode is not implemented
	 * in Java.
	 */
	public boolean javaImplBcDispatchCycles(int opcode, int cacheAccessDelay) {
		throw new AssertionError("The platform " + this.getClass() 
				               + " does not support Java implemented bytecodes");
	}
}
