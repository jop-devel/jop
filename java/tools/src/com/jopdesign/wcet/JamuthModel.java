package com.jopdesign.wcet;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.bcel.generic.BranchHandle;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;

import com.jopdesign.build.MethodInfo;
import com.jopdesign.timing.jamuth.JamuthInstructionInfo;
import com.jopdesign.timing.jamuth.JamuthTimingTable;
import com.jopdesign.tools.JopInstr;
import com.jopdesign.wcet.analysis.ExecutionContext;
import com.jopdesign.wcet.frontend.BasicBlock;
import com.jopdesign.wcet.frontend.ControlFlowGraph;
import com.jopdesign.wcet.frontend.WcetAppInfo;
import com.jopdesign.wcet.jop.MethodCache;
import com.jopdesign.wcet.jop.NoMethodCache;

public class JamuthModel implements ProcessorModel {
	private JamuthTimingTable tt;
	private final MethodCache NO_METHOD_CACHE;

	public JamuthModel(Project p) {
		tt = new JamuthTimingTable();
		NO_METHOD_CACHE = new NoMethodCache(p);
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

	public int getExecutionTime(ExecutionContext ctx, InstructionHandle instr) {
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
	public MethodInfo getJavaImplementation(WcetAppInfo ai,
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
