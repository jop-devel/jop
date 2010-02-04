/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2008-2009, Benedikt Huber (benedikt.huber@gmail.com)

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
package com.jopdesign.wcet;

import java.util.List;

import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;

import com.jopdesign.build.MethodInfo;
import com.jopdesign.wcet.analysis.ExecutionContext;
import com.jopdesign.wcet.frontend.BasicBlock;
import com.jopdesign.wcet.frontend.ControlFlowGraph;
import com.jopdesign.wcet.frontend.WcetAppInfo;
import com.jopdesign.wcet.jop.MethodCache;

public interface ProcessorModel {
	/** A human readable name of the Processor Model */
	public String getName();

	/**
	 * Check whether we need to deal with the given statement in a special way,
	 * because it is translated to a processor specific bytecode.
     *
	 * @param instr the instruction to check
	 * @return true, if the instruction is translated to a processor specific bytecode
	 */
	public boolean isSpecialInvoke(MethodInfo ctx, Instruction i);
	/**
	 * Check whether the given instruction is implemented in Java.
	 */
	public boolean isImplementedInJava(Instruction i);

	/**
	 * For Java implemented bytecodes, get the method
	 * implementing the bytecode.
	 * @return the reference to the Java implementation of the bytecode,
	 *         or null if the instruction is not implemented in Java.
	 */
	public MethodInfo getJavaImplementation(WcetAppInfo ai, MethodInfo ctx, Instruction instr);

	public int getNativeOpCode(MethodInfo ctx, Instruction instr);

	/**
	 * Get number of bytes needed to encode an instruction
	 * @param context The class the instruction belongs to
	 * @param instruction
	 * @return
	 */
	public int getNumberOfBytes(MethodInfo context, Instruction instruction);

	/**
	 * Get classes, which contain methods invoked by the JVM.
	 * Used for Java implemented bytecodes, exceptions.
	 * @return
	 */
	public List<String> getJVMClasses();

	public int getExecutionTime(ExecutionContext context, InstructionHandle i);

	public long basicBlockWCET(ExecutionContext context, BasicBlock codeBlock);

	public boolean hasMethodCache();
	/**
	 * return method cache, or NoMethodCache if the processor does not have a method cache
	 * @return
	 */
	public MethodCache getMethodCache();

	/**
	 * get the miss penalty (method cache)
	 * FIXME: We have to rewrite this portion of the analyzer - hardcoding miss penalties
	 * is to inflexible
	 * @param numberOfWords ... size of the method
	 * @param loadOnInvoke  ... whether the method is loaded on invoke
	 * @return
	 */
	public long getMethodCacheMissPenalty(int numberOfWords, boolean loadOnInvoke);

	/**
	 * FIXME: We have to rewrite this portion of the analyzer - hardcoding miss penalties
	 * is to inflexible
	 * @param invokerFlowGraph
	 * @param receiverFlowGraph
	 * @return
	 */
	public long getInvokeReturnMissCost(ControlFlowGraph invokerFlowGraph,ControlFlowGraph receiverFlowGraph);

}
