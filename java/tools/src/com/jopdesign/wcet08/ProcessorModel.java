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
package com.jopdesign.wcet08;

import java.util.List;

import org.apache.bcel.generic.Instruction;

import com.jopdesign.build.ClassInfo;
import com.jopdesign.build.MethodInfo;
import com.jopdesign.wcet08.frontend.BasicBlock;
import com.jopdesign.wcet08.frontend.ControlFlowGraph;
import com.jopdesign.wcet08.frontend.WcetAppInfo;
import com.jopdesign.wcet08.jop.MethodCache;

public interface ProcessorModel{

	/**
	 * Check whether we need to deal with the given statement in a special way,
	 * because it is translated to a processor specific bytecode.
     *
	 * @param instr the instruction to check
	 * @return true, if the instruction is translated to a processor specific bytecode
	 */
	public boolean isSpecialInvoke(ClassInfo ci, Instruction i);
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
	public MethodInfo getJavaImplementation(WcetAppInfo ai, ClassInfo ci, Instruction instr);
	
	public int getNativeOpCode(ClassInfo ci, Instruction instr);

	/**
	 * Get number of bytes needed to encode an instruction
	 * @param context The class the instruction belongs to
	 * @param instruction 
	 * @return
	 */
	public int getNumberOfBytes(ClassInfo context, Instruction instruction);

	/**
	 * Get classes, which contain methods invoked by the JVM.
	 * Used for Java implemented bytecodes, exceptions.
	 * @return
	 */
	public List<String> getJVMClasses();
	
	public int getExecutionTime(ClassInfo context, Instruction i);
	public int getMethodCacheLoadTime(int words, boolean loadOnInvoke);
	/**
	 * return method cache, or NoMethodCache if the processor does not have a method cache
	 * @return
	 */
	public MethodCache getMethodCache();
	public boolean hasMethodCache();
	public long getInvokeReturnMissCost(ControlFlowGraph invokerFlowGraph,ControlFlowGraph receiverFlowGraph);
	public long basicBlockWCET(BasicBlock codeBlock);
}
