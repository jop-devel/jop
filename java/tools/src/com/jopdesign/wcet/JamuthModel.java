/*
 * This file is part of JOP, the Java Optimized Processor
 * see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2010, Benedikt Huber (benedikt.huber@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jopdesign.wcet;

import com.jopdesign.common.AppInfo;
import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.code.BasicBlock;
import com.jopdesign.common.code.ControlFlowGraph;
import com.jopdesign.common.code.ExecutionContext;
import com.jopdesign.timing.jamuth.JamuthInstructionInfo;
import com.jopdesign.timing.jamuth.JamuthTimingTable;
import com.jopdesign.tools.JopInstr;
import com.jopdesign.wcet.jop.MethodCache;
import com.jopdesign.wcet.jop.NoMethodCache;
import org.apache.bcel.generic.BranchHandle;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class JamuthModel implements ProcessorModel {
	private JamuthTimingTable tt;
	private final MethodCache NO_METHOD_CACHE;

	public JamuthModel(Project p) {
		tt = new JamuthTimingTable();
		NO_METHOD_CACHE = new NoMethodCache(p);
	}
	
	public String getName() {
		return "jamuth";
	}

	public long basicBlockWCET(ExecutionContext ctx, BasicBlock codeBlock) {
		ArrayList<JamuthInstructionInfo> instructions =
			new ArrayList<JamuthInstructionInfo>();
		for(InstructionHandle ih : codeBlock.getInstructions()) {
			instructions.add(getInstructionInfo(ih));
		}
		return tt.getCycles(instructions);
	}

	private JamuthInstructionInfo getInstructionInfo(InstructionHandle ih)
	{
		int alignment = getAlignmentOfTarget(ih);
		return new JamuthInstructionInfo(ih.getInstruction().getOpcode(),alignment);
	}

	// <su> It is sufficient to know the instruction address within a method.
	// <su> The start of a method is linked to 64-bit boundaries
	private int getAlignmentOfTarget(InstructionHandle ih)
	{
		if(ih instanceof BranchHandle) {
			BranchHandle ihb = (BranchHandle)ih;
			return ihb.getTarget().getPosition() % 8;
		} else {
			return 0;
		}
	}

	public long getExecutionTime(ExecutionContext ctx, InstructionHandle instr) {
		return (int) tt.getCycles(getInstructionInfo(instr));
	}

	public long getInvokeReturnMissCost(ControlFlowGraph invokerFlowGraph,
			                            ControlFlowGraph receiverFlowGraph) {
		return 0;
	}
	// FIXME: native jamuth classes not yet supported
	public List<String> getJVMClasses() {
		return new Vector<String>();
	}
	// FIXME: Java implemented bytecodes ?
	public MethodInfo getJavaImplementation(AppInfo ai,
			                                MethodInfo ctx,
			                                Instruction instr) {
		throw new AssertionError("jamuth model does not (yet) support java implemented methods");
	}

	public MethodCache getMethodCache() {
		return NO_METHOD_CACHE;
	}

	public int getMethodCacheLoadTime(int words, boolean loadOnInvoke) {
		return 0;
		// throw new AssertionError("jamuth model does not have method cache");
	}

	public int getNativeOpCode(MethodInfo ctx, Instruction instr) {
		// FIXME: jamuth specific instructions ?
		return instr.getOpcode();
	}

	public int getNumberOfBytes(MethodInfo context, Instruction instruction) {
		int opCode = getNativeOpCode(context, instruction);
		// FIXME jamuth specific instructions ?
		if(opCode >= 0) return JopInstr.len(opCode);
		else throw new AssertionError("Invalid opcode: "+context+" : "+instruction);
	}

	public boolean hasMethodCache() {
		return false;
	}

	public boolean isImplementedInJava(Instruction i) {
		return false;
	}

	public boolean isSpecialInvoke(MethodInfo ctx, Instruction i) {
		return false;
	}
	public long getMethodCacheMissPenalty(int numberOfWords, boolean loadOnInvoke) {
		return 0;
	}

}
