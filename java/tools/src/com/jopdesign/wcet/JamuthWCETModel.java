/*
 * This file is part of JOP, the Java Optimized Processor
 * see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2010, Stefan Hepp (stefan@stefant.org).
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

import com.jopdesign.common.code.BasicBlock;
import com.jopdesign.common.code.ControlFlowGraph;
import com.jopdesign.common.code.ExecutionContext;
import com.jopdesign.timing.jamuth.JamuthInstructionInfo;
import com.jopdesign.timing.jamuth.JamuthTimingTable;
import com.jopdesign.wcet.jop.CacheModel;
import com.jopdesign.wcet.jop.MethodCache;
import com.jopdesign.wcet.jop.NoMethodCache;
import com.jopdesign.wcet.jop.ObjectCache;

import org.apache.bcel.generic.BranchHandle;
import org.apache.bcel.generic.InstructionHandle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JamuthWCETModel implements WCETProcessorModel {
	private JamuthTimingTable tt;
	private final MethodCache NO_METHOD_CACHE;

	public JamuthWCETModel(WCETTool p) {
		tt = new JamuthTimingTable();
		NO_METHOD_CACHE = new NoMethodCache();
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

	@Override
	public MethodCache getMethodCache() {
		return NO_METHOD_CACHE;
	}

	@Override
	public ObjectCache getObjectCache() {
		return null;
	}

	@Override
	public List<CacheModel> getCaches() {
		return Collections.emptyList();
	}


}
