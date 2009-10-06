package com.jopdesign.wcet.allocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.bcel.Constants;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.ReferenceType;

import com.jopdesign.build.MethodInfo;
import com.jopdesign.tools.JopInstr;
import com.jopdesign.wcet.ProcessorModel;
import com.jopdesign.wcet.Project;
import com.jopdesign.wcet.frontend.BasicBlock;
import com.jopdesign.wcet.frontend.ControlFlowGraph;
import com.jopdesign.wcet.frontend.WcetAppInfo;
import com.jopdesign.wcet.jop.MethodCache;
import com.jopdesign.wcet.jop.NoMethodCache;

public class AllocationModel implements ProcessorModel {

	public static final String JOP_NATIVE = "com.jopdesign.sys.Native";
	private final MethodCache NO_METHOD_CACHE;
	
	public AllocationModel(Project p) {
		NO_METHOD_CACHE = new NoMethodCache(p);
	}

	public long basicBlockWCET(BasicBlock bb) {
		int size = 0;
		MethodInfo ctx = bb.getMethodInfo();
		for(InstructionHandle ih : bb.getInstructions()) {
			size += getExecutionTime(ctx, ih);
		}
		return size;
	}

	public int getExecutionTime(MethodInfo context, InstructionHandle i) {
		int opcode = i.getInstruction().getOpcode();
		if (opcode == Constants.NEW) {
			return 1;
		} else if (opcode == Constants.NEWARRAY || opcode == Constants.ANEWARRAY) {
			return 1;
		} else {
			return 0;
		}
	}

	public long getInvokeReturnMissCost(ControlFlowGraph invokerFlowGraph, ControlFlowGraph receiverFlowGraph) {
		return 0;
	}

	public MethodInfo getJavaImplementation(WcetAppInfo ai, MethodInfo ctx, Instruction instr) {
		throw new AssertionError("allocation model model does not (yet) support java implemented methods");
	}

	public List<String> getJVMClasses() {
		return new Vector<String>();
	}

	public long getMethodCacheMissPenalty(int numberOfWords, boolean loadOnInvoke) {
		return 0;
	}

	public MethodCache getMethodCache() {
		return NO_METHOD_CACHE;
	}


	public int getNativeOpCode(MethodInfo context, Instruction instr) {
		if(isSpecialInvoke(context,instr)) {
			INVOKESTATIC isi = (INVOKESTATIC) instr;
			String methodName = isi.getMethodName(context.getConstantPoolGen());
			return JopInstr.getNative(methodName);
		} else {
			return instr.getOpcode();
		}
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

	public boolean isSpecialInvoke(MethodInfo context, Instruction i) {
		if(! (i instanceof INVOKESTATIC)) return false;
		INVOKESTATIC isi = (INVOKESTATIC) i;
		ReferenceType refTy = isi.getReferenceType(context.getConstantPoolGen());
		if(refTy instanceof ObjectType){
			ObjectType objTy = (ObjectType) refTy;
			String className = objTy.getClassName();
			return (className.equals(JOP_NATIVE));
		} else {
			return false;
		}
	}

}
