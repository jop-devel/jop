package com.jopdesign.wcet.jop;

import java.io.IOException;
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
import com.jopdesign.timing.jop.JOPCmpTimingTable;
import com.jopdesign.timing.jop.JOPTimingTable;
import com.jopdesign.timing.jop.MicrocodeAnalysis;
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
	private JOPTimingTable timing;
	private JOPConfig config;

	/* TODO: add configuration stuff */
	public JOPModel(Project p) throws IOException {
		this.config = new JOPConfig(p);
		this.cache = MethodCache.getCacheModel(p);
		if(config.cmp) {
			this.timing = JOPCmpTimingTable.getCmpTimingTable(
					MicrocodeAnalysis.DEFAULT_ASM_FILE,config.rws, config.wws,config.cpus.intValue(), config.timeslot.intValue());
		} else {
			this.timing = JOPTimingTable.getTimingTable(config.asmFile);
			timing.configureWaitStates(config.rws, config.wws);
		}
	}
	
	public boolean isSpecialInvoke(MethodInfo context, Instruction i) {		
		if(! (i instanceof INVOKESTATIC)) return false;
		INVOKESTATIC isi = (INVOKESTATIC) i;
		String classname = isi.getClassName(context.getConstantPoolGen());
		return (classname.equals(JOP_NATIVE));		
	}

	/* FIXME: [NO THROW HACK] */	
	public boolean isImplementedInJava(Instruction ii) {
		return (JopInstr.isInJava(ii.getOpcode()) && ! isUnboundedBytecode(ii));
	}
	/** return true if we are not able to compute a WCET for the given bytecode */
	public boolean isUnboundedBytecode(Instruction ii) {
		return 	(ii instanceof ATHROW || ii instanceof NEW ||
				 ii instanceof NEWARRAY || ii instanceof ANEWARRAY);

	}
	public MethodInfo getJavaImplementation(WcetAppInfo ai, MethodInfo context, Instruction instr) {
		ClassInfo receiver = ai.getClassInfo(JVM_CLASS);
		String methodName = "f_"+instr.getName();
		try {
			return ai.searchMethod(receiver,methodName);
		} catch (MethodNotFoundException e) {
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
	public int getExecutionTime(MethodInfo context, InstructionHandle ih) {
		Instruction i = ih.getInstruction();
		int jopcode = this.getNativeOpCode(context,i);
		long cycles = timing.getLocalCycles(jopcode);
		if(cycles < 0) {
			if(isUnboundedBytecode(i)){
				Project.logger.error("[HACK] Unsupported (unbounded) bytecode: "+i.getName()+
						             " in " + context.getFQMethodName()+
						             ".\nApproximating with 2000 cycles, but result is not safe anymore.");
				return 2000;
			} else {
				throw new AssertionError("Requesting #cycles of non-implemented opcode: "+i+"(opcode "+jopcode+")");
			} 
		} else {
			return (int) cycles;
		}
	}
	public int getMethodCacheLoadTime(int words, boolean loadOnInvoke) {
		long hidden = timing.methodCacheHiddenAccessCycles(loadOnInvoke);
		long loadTime = timing.methodCacheAccessCycles(false, words);
		return (int) Math.max(0,loadTime - hidden);
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
			wcet += getExecutionTime(ctx, ih);
		}
		return wcet;
	}
	
}
