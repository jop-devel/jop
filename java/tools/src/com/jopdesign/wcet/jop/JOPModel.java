package com.jopdesign.wcet.jop;

import java.util.List;
import java.util.Vector;

import org.apache.bcel.generic.ANEWARRAY;
import org.apache.bcel.generic.ATHROW;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.NEW;
import org.apache.bcel.generic.NEWARRAY;

import com.jopdesign.build.ClassInfo;
import com.jopdesign.build.MethodInfo;
import com.jopdesign.timing.WCETInstruction;
import com.jopdesign.tools.JopInstr;
import com.jopdesign.wcet.ProcessorModel;
import com.jopdesign.wcet.Project;
import com.jopdesign.wcet.frontend.BasicBlock;
import com.jopdesign.wcet.frontend.ControlFlowGraph;
import com.jopdesign.wcet.frontend.WcetAppInfo;
import com.jopdesign.wcet.frontend.WcetAppInfo.MethodNotFoundException;

public class JOPModel implements ProcessorModel {
	public static final String JVM_CLASS = "com.jopdesign.sys.JVM";
	public static final String JOP_NATIVE = "com.jopdesign.sys.Native";
	private MethodCache cache;
	/* TODO: add configuration stuff */
	public JOPModel(Project p) {
		this.cache = MethodCache.getCacheModel(p);
	}
	public boolean isSpecialInvoke(MethodInfo context, Instruction i) {		
		if(! (i instanceof INVOKESTATIC)) return false;
		INVOKESTATIC isi = (INVOKESTATIC) i;
		String classname = isi.getClassName(context.getConstantPoolGen());
		return (classname.equals(JOP_NATIVE));		
	}

	/* FIXME: [NO THROW HACK] */
	public boolean isImplementedInJava(Instruction ii) {
		boolean isUnboundedBC =
			(ii instanceof ATHROW || ii instanceof NEW || 
			 ii instanceof NEWARRAY || ii instanceof ANEWARRAY);
		return (WCETInstruction.isInJava(ii.getOpcode()) && ! isUnboundedBC);
	}
	
	public MethodInfo getJavaImplementation(WcetAppInfo ai, MethodInfo context, Instruction instr) {
		if(WCETInstruction.isInJava(getNativeOpCode(context,instr))) {
			ClassInfo receiver = ai.getClassInfo(JVM_CLASS);
			String methodName = "f_"+instr.getName();
			try {
				return ai.searchMethod(receiver,methodName);
			} catch (MethodNotFoundException e) {
				throw new AssertionError("Failed to find java implementation for: "+instr);
			}
		} else {
			return null;
		}
	}
	public int getNumberOfBytes(MethodInfo context, Instruction instruction) {
		int opCode = getNativeOpCode(context, instruction);
		if(opCode >= 0) return JopInstr.len(opCode);
		else throw new AssertionError("Invalid opcode: "+context+" : "+instruction);
	}
	/* performance hot spot */
	public int getNativeOpCode(MethodInfo context, Instruction instr) {
		if(isSpecialInvoke(context,instr)) {
			INVOKESTATIC isi = (INVOKESTATIC) instr;
			String methodName = isi.getMethodName(context.getConstantPoolGen());
			return JopInstr.getNative(methodName);
		} else {
			return instr.getOpcode();
		}
	}
	public List<String> getJVMClasses() {
		List<String> jvmClasses = new Vector<String>();
		jvmClasses.add(JVM_CLASS);
		return jvmClasses;
	}
	/* get plain execution time, without global effects */
	public int getExecutionTime(MethodInfo context, Instruction i) {
		int jopcode = this.getNativeOpCode(context,i);
		int cycles = WCETInstruction.getCycles(jopcode,false,0);
		if(cycles < 0) {
			if(isImplementedInJava(i)) {
				return WCETInstruction.getCycles(new INVOKESTATIC(0).getOpcode(), false, 0);
			} else {
				throw new AssertionError("Requesting #cycles of non-implemented opcode: "+i+"(opcode "+jopcode+")");
			} 
		} else {
			return cycles;
		}
	}
	public int getMethodCacheLoadTime(int words, boolean loadOnInvoke) {
		int hidden;
		if(loadOnInvoke) {
			hidden = WCETInstruction.INVOKE_HIDDEN_LOAD_CYCLES;
		} else {
			hidden = WCETInstruction.MIN_HIDDEN_LOAD_CYCLES;
		}
		int loadTime = WCETInstruction.calculateB(false, words);
		return Math.max(0,loadTime - hidden);
	}
	public MethodCache getMethodCache() {
		return cache;
	}
	public boolean hasMethodCache() {
		if(this.cache.cacheSizeWords <= 0) throw new AssertionError("Bad cache");
		return this.cache.cacheSizeWords > 0;
	}
	public long getInvokeReturnMissCost(ControlFlowGraph invoker,ControlFlowGraph invokee) {
		return cache.getInvokeReturnMissCost(this, invoker, invokee);
	}
	public long basicBlockWCET(BasicBlock bb) {
		int wcet = 0;
		MethodInfo ctx = bb.getMethodInfo();
		for(InstructionHandle ih : bb.getInstructions()) {
			wcet += getExecutionTime(ctx, ih.getInstruction());
		}
		return wcet;
	}
	
}
